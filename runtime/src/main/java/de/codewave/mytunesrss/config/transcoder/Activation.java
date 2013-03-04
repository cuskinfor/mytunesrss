package de.codewave.mytunesrss.config.transcoder;

public enum Activation {
    FILENAME(FilenameTranscoderActivation.class),
    MP3_SAMPLE_RATE(Mp3SampleRateTranscoderActivation.class),
    MP4_CODEC(Mp4CodecTranscoderActivation.class);

    static Activation forActivation(TranscoderActivation transcoderActivation) {
        for (Activation activation : values()) {
            if (activation.myClazz.isAssignableFrom(transcoderActivation.getClass())) {
                return activation;
            }
        }
        throw new IllegalArgumentException("No activation type for class \"" + transcoderActivation.getClass() + "\".");
    }

    private Class<? extends TranscoderActivation> myClazz;

    private Activation(Class<? extends TranscoderActivation> clazz) {
        myClazz = clazz;
    }

    TranscoderActivation newActivationInstance() throws IllegalAccessException, InstantiationException {
        return myClazz.newInstance();
    }

}
