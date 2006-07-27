/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.common.*;
import de.codewave.utils.network.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.Timer;

public class ServerInfo {
    private static final Log LOG = LogFactory.getLog(ServerInfo.class);
    public static final double KBYTE = 1024;
    public static final double MBYTE = 1024 * KBYTE;
    public static final double GBYTE = 1024 * MBYTE;
    public static final NumberFormat BYTE_STREAMED_FORMAT = new DecimalFormat("0");
    public static final NumberFormat KBYTE_STREAMED_FORMAT = new DecimalFormat("0");
    public static final NumberFormat MBYTE_STREAMED_FORMAT = new DecimalFormat("0.000");
    public static final NumberFormat GBYTE_STREAMED_FORMAT = new DecimalFormat("0.000");

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
        JDialog dialog = new JDialog(parent, MyTunesRss.BUNDLE.getString("serverStatus.title"), true);
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                myTimer.cancel();
            }
        });
        dialog.add(myRootPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        myConnections.setModel(myConnectionsTableModel);
        myTimer.schedule(new RefreshTask(), 1000);
        dialog.setVisible(true);
    }

    private void fetchServerStatusLater() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Thread(new Runnable() {
                    public void run() {
                        fetchLocalAddresses();
                    }
                }).start();
                new Thread(new Runnable() {
                    public void run() {
                        fetchExternalAddress();
                    }
                }).start();
            }
        });
    }

    private void init() {
        myExternalAddress.setText(MyTunesRss.BUNDLE.getString("serverStatus.fetching"));
        myInternalAddresses.setText(MyTunesRss.BUNDLE.getString("serverStatus.fetching"));
        myRootPanel.validate();
    }

    private void fetchLocalAddresses() {
        String[] localAddresses = NetworkUtils.getLocalNetworkAddresses();
        final StringBuffer info = new StringBuffer();
        if (localAddresses != null && localAddresses.length > 0) {
            for (int i = 0; i < localAddresses.length; i++) {
                info.append("http://").append(localAddresses[i]).append(":").append(myServerPort);
                if (i + 1 < localAddresses.length) {
                    info.append("\n");
                }
            }
            myInternalAddresses.setText(info.toString());
        } else {
            myInternalAddresses.setText(MyTunesRss.BUNDLE.getString("serverStatus.noLocalAddress"));
        }
        myRootPanel.validate();
    }

    private void fetchExternalAddress() {
        String externalAddress = getExternalAddress();
        if (StringUtils.isNotEmpty(externalAddress) && !externalAddress.equals("unreachable")) {
            myExternalAddress.setText("http://" + externalAddress + ":" + myServerPort);
        } else {
            myExternalAddress.setText(MyTunesRss.BUNDLE.getString("serverStatus.noExternalAddress"));
        }
        myRootPanel.validate();
    }

    private String getExternalAddress() {
        BufferedReader reader = null;
        try {
            URLConnection connection = new URL("http://www.codewave.de/getip.php").openConnection();
            if (connection != null) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                if (reader != null) {
                    return reader.readLine();
                }
            }
        } catch (IOException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Could not read my external address from \"www.codewave.de/getip.php\".", e);
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
            return 4;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return String.class;
        }

        public Object getValueAt(int row, int column) {
            switch (column) {
                case 0:
                    return sessions.get(row).getRemoteAddress();
                case 1:
                    return formatDate(sessions.get(row).getConnectTime());
                case 2:
                    return formatDate(sessions.get(row).getLastAccessTime());
                case 3:
                    double bytes = sessions.get(row).getBytesStreamed();
                    if (bytes > GBYTE) {
                        return GBYTE_STREAMED_FORMAT.format(bytes / GBYTE) + " GB";
                    } else if (bytes > MBYTE) {
                        return MBYTE_STREAMED_FORMAT.format(bytes / MBYTE) + " MB";
                    } else if (bytes > KBYTE) {
                        return KBYTE_STREAMED_FORMAT.format(bytes / KBYTE) + " KB";
                    }
                    return BYTE_STREAMED_FORMAT.format(bytes) + " Byte";
                default:
                    throw new IllegalArgumentException("no such column: " + column);
            }
        }

        private String formatDate(long millis) {
            DateFormat format;
            if (System.currentTimeMillis() < millis || System.currentTimeMillis() - millis > 3600 * 24 * 1000) {
                format = new SimpleDateFormat(MyTunesRss.BUNDLE.getString("serverStatus.connectionTimeFormat"));// older than 24 hours
            } else {
                format = new SimpleDateFormat(MyTunesRss.BUNDLE.getString("serverStatus.connectionTimeFormatSameDay"));
            }
            return format.format(new Date(millis));
        }

        @Override
        public String getColumnName(int column) {
            return MyTunesRss.BUNDLE.getString("serverStatus.connectionsHeader" + column);
        }
    }
}
