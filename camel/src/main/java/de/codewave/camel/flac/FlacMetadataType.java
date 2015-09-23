package de.codewave.camel.flac;

public enum FlacMetadataType {
    STREAMINFO(), PADDING(), APPLICATION(), SEEKTABLE(), VORBIS_COMMENT(), CUESHEET(), PICTURE(), UNKNOWN();

    public static FlacMetadataType getTypeForValue(int value) {
        switch (value) {
            case 0:
                return STREAMINFO;
            case 1:
                return PADDING;
            case 2:
                return APPLICATION;
            case 3:
                return SEEKTABLE;
            case 4:
                return VORBIS_COMMENT;
            case 5:
                return CUESHEET;
            case 6:
                return PICTURE;
            case 127:
                throw new IllegalArgumentException("Illegal metadata type \"" + value + "\".");
            default:
                return UNKNOWN;
        }
    }
}
