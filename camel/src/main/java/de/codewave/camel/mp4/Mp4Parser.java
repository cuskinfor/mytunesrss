package de.codewave.camel.mp4;

import de.codewave.camel.CamelUtils;
import de.codewave.camel.Endianness;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class Mp4Parser {

    private static final Collection<String> ATOMS_WITH_CHILDREN = new HashSet<String>();
    static {
        ATOMS_WITH_CHILDREN.add("moov");
        ATOMS_WITH_CHILDREN.add("cmov");
        ATOMS_WITH_CHILDREN.add("trak");
        ATOMS_WITH_CHILDREN.add("edts");
        ATOMS_WITH_CHILDREN.add("mdia");
        ATOMS_WITH_CHILDREN.add("minf");
        ATOMS_WITH_CHILDREN.add("stbl");
        ATOMS_WITH_CHILDREN.add("stsd");
        ATOMS_WITH_CHILDREN.add("udta");
        ATOMS_WITH_CHILDREN.add("meta");
        ATOMS_WITH_CHILDREN.add("ilst");
        ATOMS_WITH_CHILDREN.add("rmda");
        ATOMS_WITH_CHILDREN.add("rmra");
        ATOMS_WITH_CHILDREN.add("drms");
        ATOMS_WITH_CHILDREN.add("sinf");
        ATOMS_WITH_CHILDREN.add("----");
    }

    private final int myMaxAtomSize;
    private final Mp4AtomFactory myAtomFactory;
    private final boolean myParseChildren;

    public Mp4Parser() {
        this(1024 * 1024 * 10, true); // default to 10 MB
    }

    public Mp4Parser(int maxAtomSizeToRead, boolean parseChildren) {
        myMaxAtomSize = maxAtomSizeToRead;
        myParseChildren = parseChildren;
        myAtomFactory = new Mp4AtomFactory(this);
    }

    public Mp4Atom parseAndGet(File file, String atomName) throws IOException {
        return parse(file, new String[] {atomName}).getFirst(atomName);
    }

    public Mp4AtomList parse(File file, String... atomNames) throws IOException {
        InputStream stream = null;
        try {
            stream = new BufferedInputStream(new FileInputStream(file));
            return parse(stream, atomNames);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    public Mp4Atom parseAndGet(URL url, String atomName) throws IOException {
        return parse(url, new String[] {atomName}).getFirst(atomName);
    }

    public Mp4AtomList parse(URL url, String... atomNames) throws IOException {
        InputStream stream = null;
        try {
            stream = new BufferedInputStream(url.openStream());
            return parse(stream, atomNames);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    public Mp4Atom parseAndGet(InputStream stream, String atomName) throws IOException {
        return parse(stream, new String[] {atomName}).getFirst(atomName);
    }

    public Mp4AtomList parse(InputStream stream, String... atomNames) throws IOException {
        return new Mp4AtomList(myAtomFactory, parse(stream, null, 0, atomNames));
    }

    private List<Mp4Atom> parse(InputStream stream, String basePath, long offset, String... atomsNames) throws IOException {
        List<Mp4Atom> atoms = new ArrayList<Mp4Atom>();
        byte[] header = new byte[8];
        for (int bytesRead = stream.read(header); bytesRead == 8; bytesRead = stream.read(header)) {
            long atomSize = CamelUtils.getLongValue(header, 0, 4, false, Endianness.Big);
            if (atomSize == 0 && StringUtils.endsWithIgnoreCase(basePath, "meta")) {
                header[0] = header[4];
                header[1] = header[5];
                header[2] = header[6];
                header[3] = header[7];
                stream.read(header, 4, 4);
                offset += 4;
                atomSize = CamelUtils.getLongValue(header, 0, 4, false, Endianness.Big);
            }
            String atomId = CamelUtils.getString(header, 4, 4, CamelUtils.DEFAULT_CHARSET);
            String atomPath = StringUtils.isBlank(basePath) ? atomId : basePath + "." + atomId;
            long skipSize = 8;
            if (atomSize == 1) {
                byte[] length64 = new byte[8];
                stream.read(length64);
                atomSize = CamelUtils.getLongValue(length64, 0, 8, false, Endianness.Big);
                skipSize = 16;
            }
            if (atomSize == 0 && isValidAtomId(atomId)) {
                if (hasChildren(atomPath)) {
                    long additionalSkipSize = skip(stream, myAtomFactory.getAdditionalSkipSize(atomPath));
                    atoms.add(myAtomFactory.create(atomPath, offset, parse(stream, atomPath, offset + skipSize + additionalSkipSize, atomsNames), null, atomSize));
                } else if (Mp4Utils.matches(atomPath, atomsNames)) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        byte[] buffer = new byte[1024 * 1024 * 10]; // 10 MB buffer
                        for (int i = stream.read(buffer); i > -1 && atomSize + i < myMaxAtomSize; i = stream.read(buffer)) {
                            baos.write(buffer, 0, i);
                            atomSize += i;
                        }
                    } finally {
                        baos.close();
                    }
                    atoms.add(myAtomFactory.create(atomPath, offset, null, baos.toByteArray(), atomSize));
                } else {
                    atoms.add(myAtomFactory.create(atomPath, offset, null, null, atomSize));
                }
                // this was the last one, stop parsing
                return atoms;
            } else if (atomSize <= myMaxAtomSize && isValidAtomId(atomId)) {
                if (hasChildren(atomPath)) {
                    long additionalSkipSize = skip(stream, myAtomFactory.getAdditionalSkipSize(atomPath));
                    atoms.add(myAtomFactory.create(atomPath, offset, parse(new LimitedInputStream(stream, atomSize - skipSize - additionalSkipSize), atomPath, offset + skipSize + additionalSkipSize, atomsNames), null, atomSize));
                } else {
                    int bodyLength = (int)(atomSize - skipSize);
                    if (Mp4Utils.matches(atomPath, atomsNames)) {
                        byte[] data = new byte[bodyLength];
                        if (stream.read(data) == data.length) {
                            atoms.add(myAtomFactory.create(atomPath, offset, null, data, atomSize));
                        } else {
                            // atom could not be fully read, stop parsing
                            return atoms;
                        }
                    } else {
                        long skippedTotal = 0;
                        while (skippedTotal < bodyLength) {
                            long skipped = stream.skip(bodyLength - skippedTotal);
                            if (skipped < 1) {
                                break;
                            }
                            skippedTotal += skipped;
                        }
                        atoms.add(myAtomFactory.create(atomPath, offset, null, null, atomSize));
                    }
                }
            } else if (atomSize >= 8 && isValidAtomId(atomId)) {
                if (hasChildren(atomPath)) {
                    long additionalSkipSize = skip(stream, myAtomFactory.getAdditionalSkipSize(atomPath));
                    atoms.add(myAtomFactory.create(atomPath, offset, parse(new LimitedInputStream(stream, atomSize - skipSize - additionalSkipSize), atomPath, offset + skipSize + additionalSkipSize, atomsNames), null, atomSize));
                } else {
                    long bodyLength = atomSize - skipSize;
                    long skippedTotal = 0;
                    while (skippedTotal < bodyLength) {
                        long skipped = stream.skip(bodyLength - skippedTotal);
                        if (skipped < 1) {
                            break;
                        }
                        skippedTotal += skipped;
                    }
                    atoms.add(myAtomFactory.create(atomPath, offset, null, null, atomSize));
                }
            } else {
                // illegal data detected, skip to end of stream
                while (stream.read() > -1) {
                    stream.skip(Long.MAX_VALUE);
                }
                return atoms;
            }
            offset += atomSize;
        }
        return atoms;
    }

    private long skip(InputStream stream, long skipSize) throws IOException {
        for (long skipped = 0; skipped < skipSize; ) {
            skipped += stream.skip(skipSize - skipped);
        }
        return skipSize;
    }

    private boolean isValidAtomId(String atomId) {
        atomId = atomId.trim();
        if (atomId.length() > 0) {
            for (int i = 0; i < atomId.length(); i++) {
                char c = atomId.charAt(i);
                if (c < 0x20 || (c > 0x7e && c < 0xa0)) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    private boolean hasChildren(String atomPath) {
        if (!myParseChildren) {
            return false;
        }
        String atomId = atomPath.substring(atomPath.lastIndexOf('.') + 1);
        return ATOMS_WITH_CHILDREN.contains(atomId.toLowerCase()) || (StringUtils.endsWithIgnoreCase(atomPath, "meta.ilst." + atomId));
    }
}
