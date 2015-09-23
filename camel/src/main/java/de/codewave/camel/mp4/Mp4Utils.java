/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp4;

import de.codewave.camel.CamelUtils;
import de.codewave.camel.Endianness;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Vector;

/**
 * Utility functions for MP4 atom handling.
 */
public class Mp4Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mp4Utils.class);

    public static boolean matches(String atomPath, String... atomNames) {
        if (atomNames == null || atomNames.length == 0) {
            return true;
        }
        for (String atomName : atomNames) {
            if (StringUtils.startsWithIgnoreCase(atomPath, atomName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFastStart(File file) throws IOException {
        Mp4AtomList mp4AtomList = new Mp4Parser(0, false).parse(file);
        return mp4AtomList.getFirst("moov").getOffset() < mp4AtomList.getFirst("ftyp").getAtomSize();
    }

    public static void writeFastStart(File file, OutputStream outputStream) throws IOException {
        Mp4AtomList topLevelAtomList = new Mp4Parser(Integer.MAX_VALUE, false).parse(file, "moov");
        SequenceInputStream moovAtomInputStream = new SequenceInputStream(new ByteArrayInputStream(topLevelAtomList.getFirst("moov").getHeader()), new ByteArrayInputStream(topLevelAtomList.getFirst("moov").getData()));
        Mp4AtomList moovAtomList;
        try {
            moovAtomList = new Mp4Parser(Integer.MAX_VALUE, true).parse(moovAtomInputStream, "moov.trak.mdia.minf.stbl.stco", "moov.trak.mdia.minf.stbl.co64");
        } finally {
            moovAtomInputStream.close();
        }
        if (topLevelAtomList.getFirst("moov").getOffset() > topLevelAtomList.getFirst("ftyp").getAtomSize()) {
            LOGGER.debug("MOOV atom is not directly after FTYP atom, so reordering ATOMs.");
            for (Mp4Atom stcoAtom : moovAtomList.get("moov.trak.mdia.minf.stbl.stco")) {
                for (int i = 0; i < CamelUtils.getLongValue(stcoAtom.getData(), 4, 4, false, Endianness.Big); i++) {
                    long oldValue = CamelUtils.getLongValue(stcoAtom.getData(), 8 + (i * 4), 4, false, Endianness.Big);
                    long newValue = oldValue + moovAtomList.getFirst("moov").getAtomSize();
                    CamelUtils.setLongValue(topLevelAtomList.getFirst("moov").getData(), (int)(stcoAtom.getOffset() - topLevelAtomList.getFirst("moov").getHeader().length + stcoAtom.getHeader().length + 8 + (i * 4)), 4, Endianness.Big, newValue);
                }
            }
            try (FileInputStream in = new FileInputStream(file)) {
                for (Mp4Atom atom : topLevelAtomList.toList()) {
                    if (!"moov".equals(atom.getId())) {
                        IOUtils.copy(new LimitedInputStream(in, atom.getAtomSize()), outputStream);
                        if ("ftyp".equals(atom.getId())) {
                            IOUtils.write(topLevelAtomList.getFirst("moov").getHeader(), outputStream);
                            IOUtils.write(topLevelAtomList.getFirst("moov").getData(), outputStream);
                        }
                    } else {
                        long skipped = 0;
                        while (skipped < atom.getAtomSize()) {
                            skipped += in.skip(atom.getAtomSize() - skipped);
                        }
                    }
                }
            }
        } else {
            LOGGER.debug("MOOV atom is already directly after FTYP atom, no action necessary.");
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                IOUtils.copy(fileInputStream, outputStream);
            }
        }
    }

    public static InputStream getFastStartInputStream(File file) throws IOException {
        final Mp4AtomList topLevelAtomList = new Mp4Parser(Integer.MAX_VALUE, false).parse(file, "moov");
        Mp4AtomList moovAtomList;
        try (SequenceInputStream moovAtomInputStream = new SequenceInputStream(new ByteArrayInputStream(topLevelAtomList.getFirst("moov").getHeader()), new ByteArrayInputStream(topLevelAtomList.getFirst("moov").getData()))) {
            moovAtomList = new Mp4Parser(Integer.MAX_VALUE, true).parse(moovAtomInputStream, "moov.trak.mdia.minf.stbl.stco", "moov.trak.mdia.minf.stbl.co64");
        }
        if (topLevelAtomList.getFirst("moov").getOffset() > topLevelAtomList.getFirst("ftyp").getAtomSize()) {
            LOGGER.debug("MOOV atom is not directly after FTYP atom, so reordering ATOMs.");
            for (Mp4Atom stcoAtom : moovAtomList.get("moov.trak.mdia.minf.stbl.stco")) {
                for (int i = 0; i < CamelUtils.getLongValue(stcoAtom.getData(), 4, 4, false, Endianness.Big); i++) {
                    long oldValue = CamelUtils.getLongValue(stcoAtom.getData(), 8 + (i * 4), 4, false, Endianness.Big);
                    long newValue = oldValue + moovAtomList.getFirst("moov").getAtomSize();
                    CamelUtils.setLongValue(topLevelAtomList.getFirst("moov").getData(), (int)(stcoAtom.getOffset() - topLevelAtomList.getFirst("moov").getHeader().length + stcoAtom.getHeader().length + 8 + (i * 4)), 4, Endianness.Big, newValue);
                }
            }
            final FileInputStream in = new FileInputStream(file);
            Vector<InputStream> streams = new Vector<>();
            LimitedInputStream lastLimitedInputStream = null;
            for (final Mp4Atom atom : topLevelAtomList.toList()) {
                if (!"moov".equals(atom.getId())) {
                    lastLimitedInputStream = new LimitedInputStream(in, atom.getAtomSize());
                    streams.add(lastLimitedInputStream);
                    if ("ftyp".equals(atom.getId())) {
                        streams.add(new ByteArrayInputStream(topLevelAtomList.getFirst("moov").getHeader()));
                        streams.add(new ByteArrayInputStream(topLevelAtomList.getFirst("moov").getData()));
                    }
                } else {
                    lastLimitedInputStream = new LimitedInputStream(in, 0) {
                        @Override
                        public int read() throws IOException {
                            long skipped = 0;
                            while (skipped < atom.getAtomSize()) {
                                skipped += in.skip(atom.getAtomSize() - skipped);
                            }
                            return -1;
                        }
                    };
                    streams.add(lastLimitedInputStream);
                }
            }
            lastLimitedInputStream.setCloseDelegate(true);
            return new SequenceInputStream(streams.elements());
        } else {
            LOGGER.debug("MOOV atom is already directly after FTYP atom, no action necessary.");
            return new FileInputStream(file);
        }
    }

    public static boolean isMp4File(File file) {
        byte[] header = new byte[8];
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            try {
                if (fileInputStream.read(header) == 8) {
                    return header[4] == 'f' && header[5] == 't' && header[6] == 'y' && header[7] == 'p';
                }
            } finally {
                fileInputStream.close();
            }
        } catch (IOException ignored) {
            LOGGER.warn("Could not read first 8 byte of file \"" + file.getAbsolutePath() + "\".");
        }
        return false;
    }

    public static class CheckStreamResult {
        private boolean myMp4Stream;
        private InputStream myStream;

        public CheckStreamResult(boolean mp4Stream, InputStream stream) {
            myMp4Stream = mp4Stream;
            myStream = stream;
        }

        public boolean isMp4Stream() {
            return myMp4Stream;
        }

        public InputStream getStream() {
            return myStream;
        }
    }

    public static CheckStreamResult isMp4Stream(InputStream inputStream) {
        if (!inputStream.markSupported()) {
            inputStream = new BufferedInputStream(inputStream);
        }
        inputStream.mark(8);
        byte[] header = new byte[8];
        try {
            try {
                if (inputStream.read(header) == 8) {
                    return header[4] == 'f' && header[5] == 't' && header[6] == 'y' && header[7] == 'p' ? new CheckStreamResult(true, inputStream) : new CheckStreamResult(false, inputStream);
                }
            } finally {
                inputStream.reset();
            }
        } catch (IOException ignored) {
            LOGGER.warn("Could not read first 8 byte of stream.");
        }
        return new CheckStreamResult(false, inputStream);
    }

}
