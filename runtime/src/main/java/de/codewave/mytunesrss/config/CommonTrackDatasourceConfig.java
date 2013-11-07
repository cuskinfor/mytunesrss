/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.config;

import de.codewave.mytunesrss.ImageImportType;

import java.util.List;

public interface CommonTrackDatasourceConfig {
    String getArtistDropWords();

    void setArtistDropWords(String artistDropWords);

    String getDisabledMp4Codecs();

    void setDisabledMp4Codecs(String disabledMp4Codecs);

    List<String> getTrackImagePatterns();

    void setTrackImagePatterns(List<String> trackImageMappings);

    ImageImportType getTrackImageImportType();

    void setTrackImageImportType(ImageImportType trackImageImportType);
}
