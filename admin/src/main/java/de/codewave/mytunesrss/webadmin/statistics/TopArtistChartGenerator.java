/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.statistics;

import de.codewave.mytunesrss.MediaType;
import de.codewave.mytunesrss.datastore.statement.Track;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableLong;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TopArtistChartGenerator extends TopChartGenerator {

    @Override
    public String toString() {
        return "statisticsConfigPanel.reportType.topArtist";
    }

    protected String getItem(Track track) {
        return track.getMediaType() == MediaType.Audio ? track.getArtist() : null;
    }
}
