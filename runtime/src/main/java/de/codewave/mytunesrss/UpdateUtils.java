/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.utils.network.*;
import de.codewave.utils.swing.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.prefs.*;

/**
 * de.codewave.mytunesrss.UpdateUtils
 */
public class UpdateUtils {
    public static void checkForUpdate(boolean autoCheck) {
        CheckUpdateTask checkUpdateTask = new CheckUpdateTask();
        MyTunesRssUtils.executeTask(MyTunesRssUtils.getBundleString("pleaseWait.updateCheckTitle"),
                                    MyTunesRssUtils.getBundleString("pleaseWait.updateCheck"), MyTunesRssUtils.getBundleString("cancel"), true,
                                    checkUpdateTask);
        if (!checkUpdateTask.isCancelled()) {
            UpdateInfo updateInfo = checkUpdateTask.getUpdateInfo();
            if (updateInfo != null) {
                String noNagVersion = Preferences.userRoot().node(MyTunesRssConfig.PREF_ROOT).get("updateIgnoreVersion", MyTunesRss.VERSION);
                if (!updateInfo.getVersion().equals(MyTunesRss.VERSION) && (!autoCheck || !noNagVersion.equals(updateInfo.getVersion()))) {
                    if (askForUpdate(updateInfo, autoCheck)) {
                        File targetFile = new File(updateInfo.getFileName());
                        final FileDialog fileDialog = new FileDialog(MyTunesRss.ROOT_FRAME,
                                                                     MyTunesRssUtils.getBundleString("dialog.saveUpdate"),
                                                                     FileDialog.SAVE);
                        fileDialog.setDirectory(targetFile.getParent());
                        fileDialog.setFile(targetFile.getName());
                        fileDialog.setVisible(true);
                        if (fileDialog.getFile() != null) {
                            targetFile = new File(fileDialog.getDirectory(), fileDialog.getFile());
                            downloadUpdate(updateInfo.getUrl(), targetFile, updateInfo.getVersion());
                        }
                    }
                } else if (!autoCheck) {
                    MyTunesRssUtils.showInfoMessage(MyTunesRss.ROOT_FRAME, MyTunesRssUtils.getBundleString("info.noUpdate"));
                }
            } else if (!autoCheck) {
                MyTunesRssUtils.showErrorMessage(MyTunesRss.ROOT_FRAME, MyTunesRssUtils.getBundleString("error.noUpdateInfo"));
            }
        }
    }

    private static void downloadUpdate(final URL url, final File file, String version) {
        MyTunesRssUtils.executeTask(MyTunesRssUtils.getBundleString("pleaseWait.dowloadTitle"),
                                    MyTunesRssUtils.getBundleString("pleaseWait.downloadMessage", version), MyTunesRssUtils.getBundleString("cancel"), true,
                                    new DownloadUpdateTask(url, file));
    }

    private static boolean askForUpdate(UpdateInfo updateInfo, boolean autoCheck) {
        JOptionPane pane = SwingUtils.createMaxLengthOptionPane(MyTunesRss.OPTION_PANE_MAX_MESSAGE_LENGTH);
        pane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
        pane.setMessage(MyTunesRssUtils.getBundleString("info.newVersionMessage", MyTunesRss.VERSION, updateInfo.getVersion()));
        String stopNagging = MyTunesRssUtils.getBundleString("info.newVersionStopNagging");
        String later = MyTunesRssUtils.getBundleString("info.newVersionLater");
        String download = MyTunesRssUtils.getBundleString("info.newVersionDownload");
        String cancel = MyTunesRssUtils.getBundleString("cancel");
        String moreInfo = MyTunesRssUtils.getBundleString("info.newVersionMoreInfo");
        if (autoCheck) {
            pane.setOptions(new String[]{stopNagging, later, download, moreInfo});
        } else {
            pane.setOptions(new String[]{cancel, download, moreInfo});
        }
        pane.setInitialValue(moreInfo);
        JDialog dialog = pane.createDialog(MyTunesRss.ROOT_FRAME, MyTunesRssUtils.getBundleString("info.newVersionTitle", updateInfo.getVersion()));
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        do {
            dialog.setVisible(true);
            if (pane.getValue() == moreInfo) {
                MyTunesRssUtils.showInfoMessage(MyTunesRss.ROOT_FRAME, updateInfo.getInfo().trim());
            }
        } while (pane.getValue() == moreInfo);
        if (pane.getValue() == stopNagging) {
            Preferences.userRoot().node(MyTunesRssConfig.PREF_ROOT).put("updateIgnoreVersion", updateInfo.getVersion());
        }
        return pane.getValue() == download;
    }

    public static class DownloadUpdateTask extends MyTunesRssTask {
        Downloader myDownloader;
        File myFile;

        public DownloadUpdateTask(URL url, File file) {
            myDownloader = NetworkUtils.createDownloader(url, file, new DownloadProgressListener() {
                public void reportProgress(int progress) {
                    setPercentage(progress);
                }
            });
            myFile = file;
        }

        public void execute() {
            switch (myDownloader.download()) {
                case Finished:
                    MyTunesRssUtils.showInfoMessage(MyTunesRss.ROOT_FRAME, MyTunesRssUtils.getBundleString("info.newVersionDownloadDone"));
                    break;
                case Cancelled:
                    if (myFile.isFile()) {
                        myFile.delete();
                    }
                    MyTunesRssUtils.showErrorMessage(MyTunesRss.ROOT_FRAME, MyTunesRssUtils.getBundleString("info.newVersionDownloadCancelled"));
                    break;
                case Failed:
                    if (myFile.isFile()) {
                        myFile.delete();
                    }
                    MyTunesRssUtils.showErrorMessage(MyTunesRss.ROOT_FRAME, MyTunesRssUtils.getBundleString("info.newVersionDownloadFailed"));
                    break;
            }
        }

        protected void cancel() {
            myDownloader.cancel();
        }
    }

    public static class CheckUpdateTask extends MyTunesRssTask {
        private UpdateInfo myUpdateInfo;
        private boolean myCancelled;
        private boolean myDone;
        private static final int READ_TIMEOUT = 30000;

        public UpdateInfo getUpdateInfo() {
            return myUpdateInfo;
        }

        public boolean isCancelled() {
            return myCancelled;
        }

        public void execute() throws Exception {
            long startTime = System.currentTimeMillis();
            new Thread(new Runnable() {
                public void run() {
                  Proxy proxy = null;
                  if (MyTunesRss.CONFIG.isProxyServer()) {
                    proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(MyTunesRss.CONFIG.getProxyHost(), MyTunesRss.CONFIG.getProxyPort()));
                  }
                  myUpdateInfo = NetworkUtils.getCurrentUpdateInfo(MyTunesRss.UPDATE_URL, MyTunesRss.BUNDLE.getLocale(), READ_TIMEOUT, proxy);
                    myDone = true;
                }
            }, MyTunesRss.THREAD_PREFIX + "UpdateInfoGetter").start();
            while (!myDone && !myCancelled) {
                setPercentage((int)Math.min(((System.currentTimeMillis() - startTime) * 100) / READ_TIMEOUT, 100));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // intentionally left blank
                }
            }
        }

        protected void cancel() {
            myCancelled = true;
        }
    }
}