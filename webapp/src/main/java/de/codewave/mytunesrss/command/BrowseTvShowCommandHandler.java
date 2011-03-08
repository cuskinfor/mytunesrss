/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

/**
 * de.codewave.mytunesrss.command.BrowseTrackCommandHandler
 */
public class BrowseTvShowCommandHandler extends BrowseVideoCommandHandler {
    @Override
    protected MyTunesRssResource getResource() {
        return MyTunesRssResource.BrowseTvShow;
    }

    @Override
    protected VideoType getVideoType() {
        return VideoType.TvShow;
    }
}