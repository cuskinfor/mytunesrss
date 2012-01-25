/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.statistics;

import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.datastore.statement.Track;

public class TopArtistChartGenerator extends TopTrackDownloadChartGenerator {

    @Override
    public String toString() {
        return "statisticsConfigPanel.reportType.topArtist";
    }

    protected String getItem(Track track) {
        return track.getMediaType() == MediaType.Audio ? track.getArtist() : null;
    }
}
