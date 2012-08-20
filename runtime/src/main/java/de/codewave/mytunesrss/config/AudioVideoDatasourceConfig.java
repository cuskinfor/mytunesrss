/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.config;

import java.util.List;

public interface AudioVideoDatasourceConfig {
    String getArtistDropWords();

    void setArtistDropWords(String artistDropWords);

    String getDisabledMp4Codecs();

    void setDisabledMp4Codecs(String disabledMp4Codecs);

    List<ReplacementRule> getTrackImageMappings();

    void setTrackImageMappings(List<ReplacementRule> trackImageMappings);
}
