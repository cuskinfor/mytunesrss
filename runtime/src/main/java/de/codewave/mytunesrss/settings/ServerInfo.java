/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.server.MyTunesRssSessionInfo;
import de.codewave.utils.network.NetworkUtils;
import de.codewave.utils.swing.SwingUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ServerInfo {
    private static final Log LOG = LogFactory.getLog(ServerInfo.class);

    private JPanel myRootPanel;
    private JTable myConnections;
    private JTextField myExternalAddress;
    private JTextArea myInternalAddresses;
    private String myServerPort;
    private Timer myTimer = new Timer("ServerInfoRefreshTimer");
    private ActiveConnectionsTableModel myConnectionsTableModel = new ActiveConnectionsTableModel();

    public void display(final JFrame parent, String serverPort) {
        myServerPort = serverPort;
        fetchServerStatusLater();
        init();
        JDialog dialog = new JDialog(parent, MyTunesRssUtils.getBundleString("serverStatus.title"), true);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                myTimer.cancel();
            }
        });
        dialog.add(myRootPanel);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        myConnections.setModel(myConnectionsTableModel);
        myTimer.schedule(new RefreshTask(), 1000);
        SwingUtils.packAndShowRelativeTo(dialog, parent);
    }

    private void fetchServerStatusLater() {
        new Thread(new Runnable() {
            public void run() {
                fetchLocalAddresses();
            }
        }, MyTunesRss.THREAD_PREFIX + "FetchLocalAddresses").start();
        new Thread(new Runnable() {
            public void run() {
                fetchExternalAddress();
            }
        }, MyTunesRss.THREAD_PREFIX + "FetchExternalAddress").start();
    }

    private void init() {
        myExternalAddress.setText(MyTunesRssUtils.getBundleString("serverStatus.fetching"));
        myInternalAddresses.setText(MyTunesRssUtils.getBundleString("serverStatus.fetching"));
    }

    private void fetchLocalAddresses() {
        final String[] localAddresses = getLocalAddresses(myServerPort);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                StringBuffer info = new StringBuffer();
                if (localAddresses != null && localAddresses.length > 0) {
                    for (int i = 0; i < localAddresses.length; i++) {
                        info.append(localAddresses[i]);
                        if (i + 1 < localAddresses.length) {
                            info.append("\n");
                        }
                    }
                    myInternalAddresses.setText(info.toString());
                } else {
                    myInternalAddresses.setText(MyTunesRssUtils.getBundleString("serverStatus.unavailable"));
                }
            }
        });
    }

    public static String[] getLocalAddresses(String serverPort) {
        String[] addresses = NetworkUtils.getLocalNetworkAddresses();
        for (int i = 0; i < addresses.length; i++) {
            addresses[i] = "http://" + addresses[i] + ":" + serverPort;
        }
        return addresses;
    }

    private void fetchExternalAddress() {
        final String externalAddress = getExternalAddress(myServerPort);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (StringUtils.isNotEmpty(externalAddress) && !"unreachable".equals(externalAddress)) {
                    myExternalAddress.setText(externalAddress);
                } else {
                    myExternalAddress.setText(MyTunesRssUtils.getBundleString("serverStatus.unavailable"));
                }
            }
        });
    }

    public static String getExternalAddress(String serverPort) {
        BufferedReader reader = null;
        try {
            URLConnection connection = new URL("http://www.codewave.de/tools/getip.php").openConnection();
            if (connection != null) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                if (reader != null) {
                    return "http://" + reader.readLine() + ":" + serverPort;
                }
            }
        } catch (IOException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not read my external address from \"www.codewave.de/tools/getip.php\".", e);
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not close reader.", e);
                    }
                }
            }
        }
        return null;
    }

    public class RefreshTask extends TimerTask {
        public void run() {
            myConnectionsTableModel.refresh();
            myConnectionsTableModel.fireTableDataChanged();
            try {
                myTimer.schedule(new RefreshTask(), 1000);
            } catch (IllegalStateException e) {
                // timer was cancelled, so we just don't schedule any further tasks
            }
        }
    }

    public class ActiveConnectionsTableModel extends AbstractTableModel {
        private List<MyTunesRssSessionInfo> sessions = MyTunesRss.WEBSERVER.getSessionInfos();

        public void refresh() {
            sessions = MyTunesRss.WEBSERVER.getSessionInfos();
        }

        public int getRowCount() {
            return sessions.size();
        }

        public int getColumnCount() {
            return 5;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return String.class;
        }

        public Object getValueAt(int row, int column) {
            MyTunesRssSessionInfo info = sessions.get(row);
            switch (column) {
                case 0:
                    return info.getRemoteAddress();
                case 1:
                    return info.getUser() != null ? info.getUser().getName() : "";
                case 2:
                    return formatDate(info.getConnectTime());
                case 3:
                    return formatDate(info.getLastAccessTime());
                case 4:
                    double bytes = info.getBytesStreamed();
                    return MyTunesRssUtils.getMemorySizeForDisplay((long)bytes);
                default:
                    throw new IllegalArgumentException("no such column: " + column);
            }
        }

        private String formatDate(long millis) {
            DateFormat format;
            if (System.currentTimeMillis() < millis || System.currentTimeMillis() - millis > 3600 * 24 * 1000) {
                format = new SimpleDateFormat(MyTunesRssUtils.getBundleString("serverStatus.connectionTimeFormat"));// older than 24 hours
            } else {
                format = new SimpleDateFormat(MyTunesRssUtils.getBundleString("serverStatus.connectionTimeFormatSameDay"));
            }
            return format.format(new Date(millis));
        }

        @Override
        public String getColumnName(int column) {
            return MyTunesRssUtils.getBundleString("serverStatus.connectionsHeader" + column);
        }
    }
}
