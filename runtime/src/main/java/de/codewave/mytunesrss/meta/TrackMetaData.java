package de.codewave.mytunesrss.meta;

/**
 * de.codewave.mytunesrss.meta.TrackMetaData
 */
public class TrackMetaData {
    Image myImage;
    String myMp4Codec;

    public Image getImage() {
        return myImage;
    }

    public void setImage(Image image) {
        myImage = image;
    }

    public String getMp4Codec() {
        return myMp4Codec;
    }

    public void setMp4Codec(String mp4Codec) {
        myMp4Codec = mp4Codec;
    }
}