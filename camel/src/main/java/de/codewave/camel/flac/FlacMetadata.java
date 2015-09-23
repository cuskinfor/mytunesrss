package de.codewave.camel.flac;

/**
 * de.codewave.camel.flac.FlacMetadata
 */
public class FlacMetadata {
    private FlacMetadataType myType;
    private FlacMetadataData myData;

    public FlacMetadata(FlacMetadataType type, FlacMetadataData data) {
        myType = type;
        myData = data;
    }

    public FlacMetadataType getType() {
        return myType;
    }

    public void setType(FlacMetadataType type) {
        myType = type;
    }

    public FlacMetadataData getData() {
        return myData;
    }

    public void setData(FlacMetadataData data) {
        myData = data;
    }
}