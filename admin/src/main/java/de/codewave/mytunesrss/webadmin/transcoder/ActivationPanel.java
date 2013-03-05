package de.codewave.mytunesrss.webadmin.transcoder;

import de.codewave.mytunesrss.config.transcoder.TranscoderActivation;

public interface ActivationPanel<T extends TranscoderActivation> {

    T getConfig();
    void initFromConfig(T config);

}
