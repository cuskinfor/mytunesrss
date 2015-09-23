/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3;

import de.codewave.camel.mp3.exception.IllegalHeaderException;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * de.codewave.camel.mp3.ScanMp3Test
 */
public class ScanMp3Test {

    @Test
    @Ignore
    public void testScanAllFiles() throws IOException, IllegalHeaderException {
        scan(new File("/Users/mdescher/Music/iTunes/iTunes Music"));
    }

    private void scan(File file) throws IOException, IllegalHeaderException {
        if (file.exists()) {
            if (file.isDirectory()) {
                File[] subFiles = file.listFiles(new FileFilter() {
                    public boolean accept(File file) {
                        return file.isDirectory() || file.getName().toLowerCase().endsWith(".mp3");
                    }
                });
                for (File subFile : subFiles) {
                    scan(subFile);
                }
            } else {
                System.out.print(file.getName() + " -> ");
                Id3Tag tag = Mp3Utils.readId3Tag(file);
                if (tag != null) {
                    System.out.println(tag.getLongVersionIdentifier() + " - title: " + tag.getTitle() + " - artist: " + tag.getArtist() + " - album:" + tag.getAlbum());
                } else {
                    System.out.println();
                }
            }
        }
    }
}