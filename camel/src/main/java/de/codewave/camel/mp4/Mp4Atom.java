/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.camel.mp4;

import de.codewave.camel.CamelUtils;
import de.codewave.camel.Endianness;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * de.codewave.camel.mp4.Mp4Atom
 */
public class Mp4Atom {
    private static final Logger LOGGER = LoggerFactory.getLogger(Mp4Atom.class);


    private String myId;
    private String myPath;
    private List<Mp4Atom> myChildren = new ArrayList<Mp4Atom>();
    private byte[] myData;
    private long myAtomSize;
    private long myOffset;

    public Mp4Atom(String path, long offset, List<Mp4Atom> children, byte[] data, long atomSize) {
        myId = path.substring(path.lastIndexOf('.') + 1);
        myPath = path;
        if (children != null) {
            myChildren.addAll(children);
        }
        myData = data;
        myAtomSize = atomSize;
        myOffset = offset;
    }

    public byte[] getHeader() {
        byte[] header = new byte[(int)(getAtomSize() - getData().length)];
        if (header.length == 8) {
            CamelUtils.setLongValue(header, 0, 4, Endianness.Big, getAtomSize());
            try {
                CamelUtils.setString(header, 4, 4, "US-ASCII", getId());
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Mandatory platform encoding US-ASCII not found.");
            }
        } else if (header.length == 16) {
            CamelUtils.setLongValue(header, 0, 4, Endianness.Big, 1);
            try {
                CamelUtils.setString(header, 4, 4, "US-ASCII", getId());
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Mandatory platform encoding US-ASCII not found.");
            }
            CamelUtils.setLongValue(header, 8, 8, Endianness.Big, getAtomSize());
        } else {
            return null;
        }
        return header;
    }

    public byte[] getData() {
        return myData;
    }

    public String getId() {
        return myId;
    }

    public String getPath() {
        return myPath;
    }

    public long getOffset() {
        return myOffset;
    }

    public int getBodySize() {
        return myData != null ? myData.length : 0;
    }

    public long getAtomSize() {
        return myAtomSize;
    }

    public List<Mp4Atom> getChildren() {
        return myChildren;
    }

    public Mp4Atom getFirstChild(String name) {
        String[] nameParts = StringUtils.split(name, ".", 2);
        for (Mp4Atom atom : getChildren()) {
            if (nameParts[0].equals(atom.getId())) {
                return nameParts.length == 1 ? atom : atom.getFirstChild(nameParts[1]);
            }
        }
        return null;
    }

    public List<Mp4Atom> getChildren(String name) {
        List<Mp4Atom> children = new ArrayList<Mp4Atom>();
        String[] nameParts = StringUtils.split(name, ".", 2);
        for (Mp4Atom atom : getChildren()) {
            if (nameParts[0].equals(atom.getId())) {
                if (nameParts.length == 1) {
                    children.add(atom);
                } else {
                    children.addAll(atom.getChildren(nameParts[1]));
                }
            }
        }
        return children;
    }

    public Mp4Atom findFirstChildWithType(Class<? extends Mp4Atom> type) {
        List<Mp4Atom> atoms = findChildrenWithType(type);
        return atoms.isEmpty() ? null : atoms.get(0);
    }

    public List<Mp4Atom> findChildrenWithType(Class<? extends Mp4Atom> type) {
        List<Mp4Atom> atoms = new ArrayList<Mp4Atom>();
        for (Mp4Atom atom : getChildren()) {
            if (type.isAssignableFrom(atom.getClass())) {
                atoms.add(atom);
            } else {
                atoms.addAll(atom.findChildrenWithType(type));
            }
        }
        return atoms;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getId() + ", offset=" + getOffset() + ", atomSize=" + getAtomSize() + ", bodySize=" + getBodySize();
    }

    public String getDataAsString(int offset, String encoding) {
        if (getData() != null) {
            try {
                return new String(getData(), offset, getData().length - offset, encoding);
            } catch (UnsupportedEncodingException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Specified encoding \"" + encoding + "\" not supported on this platform." , e);
                }
            }
        }
        return null;
    }

    public String getDataAsString(int offset, int length, String encoding) {
        if (getData() != null) {
            try {
                return new String(getData(), offset, length, encoding);
            } catch (UnsupportedEncodingException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Specified encoding \"" + encoding + "\" not supported on this platform." , e);
                }
            }
        }
        return null;
    }
}
