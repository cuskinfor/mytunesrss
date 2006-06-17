/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.utils.network.*;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.prefs.*;
import java.awt.*;

/**
 * de.codewave.mytunesrss.Updater
 */
public class Updater {
    JFrame myParent;

    public Updater(JFrame parent) {
        myParent = parent;
    }

    public void checkForUpdate(boolean autoCheck) {
        CheckUpdateTask checkUpdateTask = new CheckUpdateTask();
        PleaseWait.start(myParent,
                         MyTunesRss.BUNDLE.getString("pleaseWait.updateCheckTitle"),
                         MyTunesRss.BUNDLE.getString("pleaseWait.updateCheck"),
                         true,
                         true,
                         checkUpdateTask);
        if (!checkUpdateTask.isCancelled()) {
            UpdateInfo updateInfo = checkUpdateTask.getUpdateInfo();
            if (updateInfo != null) {
                String noNagVersion = Preferences.userRoot().node("/de/codewave/mytunesrss").get("updateIgnoreVersion", MyTunesRss.VERSION);
                if (!updateInfo.getVersion().equals(MyTunesRss.VERSION) && (!autoCheck || !noNagVersion.equals(updateInfo.getVersion()))) {
                    if (askForUpdate(updateInfo, autoCheck)) {
                        File targetFile = new File(updateInfo.getFileName());
                        final FileDialog fileDialog = new FileDialog(myParent, MyTunesRss.BUNDLE.getString("dialog.saveUpdate"),
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
                    SwingUtils.showInfoMessage(myParent, MyTunesRss.BUNDLE.getString("info.noUpdate"));
                }
            } else if (!autoCheck) {
                SwingUtils.showErrorMessage(myParent, MyTunesRss.BUNDLE.getString("error.noUpdateInfo"));
            }
        }
    }

    private void downloadUpdate(final URL url, final File file, String version) {
        PleaseWait.start(myParent, MyTunesRss.BUNDLE.getString("pleaseWait.dowloadTitle"), MessageFormat.format(MyTunesRss.BUNDLE.getString(
                "pleaseWait.downloadMessage"), version), true, true, new DownloadUpdateTask(url, file));
    }

    private boolean askForUpdate(UpdateInfo updateInfo, boolean autoCheck) {
        JOptionPane pane = new JOptionPane() {
            @Override
            public int getMaxCharactersPerLineCount() {
                return 100;
            }
        };
        pane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
        pane.setMessage(MessageFormat.format(MyTunesRss.BUNDLE.getString("info.newVersionMessage"), MyTunesRss.VERSION, updateInfo.getVersion()));
        String stopNagging = MyTunesRss.BUNDLE.getString("info.newVersionStopNagging");
        String later = MyTunesRss.BUNDLE.getString("info.newVersionLater");
        String download = MyTunesRss.BUNDLE.getString("info.newVersionDownload");
        String cancel = MyTunesRss.BUNDLE.getString("cancel");
        if (autoCheck) {
            pane.setOptions(new String[] {download, later, stopNagging});
        } else {
            pane.setOptions(new String[] {download, cancel});
        }
        pane.setInitialValue(download);
        JDialog dialog = pane.createDialog(myParent, MessageFormat.format(MyTunesRss.BUNDLE.getString("info.newVersionTitle"),
                                                                          updateInfo.getVersion()));
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setVisible(true);
        if (pane.getValue() == stopNagging) {
            Preferences.userRoot().node("/de/codewave/mytunesrss").put("updateIgnoreVersion", updateInfo.getVersion());
        }
        return pane.getValue() == download;
    }

    public class DownloadUpdateTask extends PleaseWait.Task {
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
                    SwingUtils.showInfoMessage(myParent, MyTunesRss.BUNDLE.getString("info.newVersionDownloadDone"));
                    break;
                case Cancelled:
                    if (myFile.exists() && myFile.isFile()) {
                        myFile.delete();
                    }
                    SwingUtils.showErrorMessage(myParent, MyTunesRss.BUNDLE.getString("info.newVersionDownloadCancelled"));
                    break;
                case Failed:
                    if (myFile.exists() && myFile.isFile()) {
                        myFile.delete();
                    }
                    SwingUtils.showErrorMessage(myParent, MyTunesRss.BUNDLE.getString("info.newVersionDownloadFailed"));
                    break;
            }
        }

        protected void cancel() {
            myDownloader.cancel();
        }
    }

    public class CheckUpdateTask extends PleaseWait.Task {
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
                    myUpdateInfo = NetworkUtils.getCurrentUpdateInfo(MyTunesRss.UPDATE_URLS, READ_TIMEOUT);
                    myDone = true;
                }
            }).start();
            while (!myDone && !myCancelled) {
                setPercentage((int)(((System.currentTimeMillis() - startTime) * 100) / READ_TIMEOUT));
                Thread.yield();
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