/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp3;

import de.codewave.camel.mp3.exception.IllegalHeaderException;
import de.codewave.camel.mp3.framebody.v2.PICFrameBody;
import de.codewave.camel.mp3.framebody.v3.APICFrameBody;
import de.codewave.camel.mp3.structure.Frame;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * de.codewave.camel.mp3.ScanMp3Test
 */
public class ListArtworksTest {

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
                Id3Tag tag = Mp3Utils.readId3Tag(file);
                if (tag != null && tag.isId3v2()) {
                    Frame frame = ((Id3v2Tag)tag).getFrame("APIC");
                    if (frame == null) {
                        frame = ((Id3v2Tag)tag).getFrame("PIC");
                        if (frame != null) {
                            System.out.println(file.getName() + " -> " + new PICFrameBody(frame).getMimeType());
                        }
                    } else {
                        System.out.println(file.getName() + " -> " + new APICFrameBody(frame).getMimeType());
                    }
                }
            }
        }
    }
}