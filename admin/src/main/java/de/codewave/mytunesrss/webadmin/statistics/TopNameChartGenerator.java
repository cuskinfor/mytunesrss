/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.statistics;

import de.codewave.mytunesrss.datastore.statement.Track;
import org.apache.commons.lang3.StringUtils;

public class TopNameChartGenerator extends TopTrackDownloadChartGenerator {

    @Override
    public String toString() {
        return "statisticsConfigPanel.reportType.topName";
    }

    @Override
    protected String getItem(Track track) {
        switch (track.getMediaType()) {
            case Audio:
                return StringUtils.isNotBlank(track.getArtist()) ? track.getArtist() + " - " + track.getName() : track.getName();
            case Video:
                switch (track.getVideoType()) {
                    case Movie:
                        return track.getName();
                    case TvShow:
                        return track.getSeries() + " - " + track.getName();
                    default:
                        return track.getName();
                }
            default:
                return track.getName();
        }
    }
}
