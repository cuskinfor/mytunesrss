/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.statistics;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.FindTrackQuery;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.statistics.DownloadEvent;
import de.codewave.mytunesrss.statistics.StatEventType;
import de.codewave.mytunesrss.statistics.StatisticsEvent;
import de.codewave.utils.sql.DataStoreSession;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableLong;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Day;

import java.sql.SQLException;
import java.util.*;

public class TopNameChartGenerator extends TopChartGenerator {

    @Override
    public String toString() {
        return "statisticsConfigPanel.reportType.topName";
    }

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
