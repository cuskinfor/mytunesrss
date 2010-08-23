/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.transcoder;

import de.codewave.utils.servlet.StreamSender;

import java.io.InputStream;

public class TranscoderStreamSender extends StreamSender {

    private Transcoder myTranscoder;

    public TranscoderStreamSender(InputStream stream, String contentType, long contentLength, Transcoder transcoder) {
        super(stream, contentType, contentLength);
        myTranscoder = transcoder;
    }

    public Transcoder getTranscoder() {
        return myTranscoder;
    }
}
