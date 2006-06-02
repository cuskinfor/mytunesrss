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
        PleaseWait.start(myParent, MyTunesRss.BUNDLE.getString("pleaseWait.updateCheckTitle"), MyTunesRss.BUNDLE.getString(
                "pleaseWait.updateCheck"), false, false, checkUpdateTask);
        UpdateInfo updateInfo = checkUpdateTask.getUpdateInfo();
        if (updateInfo != null) {
            String noNagVersion = Preferences.userRoot().node("/de/codewave/mytunesrss").get("updateIgnoreVersion", MyTunesRss.VERSION);
            if (!updateInfo.getVersion().equals(MyTunesRss.VERSION) && (!autoCheck || !noNagVersion.equals(updateInfo.getVersion()))) {
                if (askForUpdate(updateInfo, autoCheck)) {
                    final JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    fileChooser.setSelectedFile(new File(updateInfo.getFileName()));
                    if (fileChooser.showSaveDialog(myParent) == JFileChooser.APPROVE_OPTION) {
                        downloadUpdate(updateInfo.getUrl(), fileChooser.getSelectedFile(), updateInfo.getVersion());
                    }
                }
            } else if (!autoCheck) {
                SwingUtils.showInfoMessage(myParent, MyTunesRss.BUNDLE.getString("info.noUpdate"));
            }
        } else if (!autoCheck) {
            SwingUtils.showErrorMessage(myParent, MyTunesRss.BUNDLE.getString("error.noUpdateInfo"));
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
        pane.setMessage(MessageFormat.format(MyTunesRss.BUNDLE.getString("info.newVersionMessage"),
                                             MyTunesRss.VERSION,
                                             updateInfo.getVersion()));
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

    public class CheckUpdateTask extends PleaseWait.NoCancelTask {
        private UpdateInfo myUpdateInfo;

        public UpdateInfo getUpdateInfo() {
            return myUpdateInfo;
        }

        public void execute() throws Exception {
            myUpdateInfo = NetworkUtils.getCurrentUpdateInfo(MyTunesRss.UPDATE_URLS);
        }
    }
}