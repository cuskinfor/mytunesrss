/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.servlet.BandwidthLimitFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BandwidthThrottlingCommandHandler extends MyTunesRssCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BandwidthThrottlingCommandHandler.class);

    @Override
    protected void throttleBandwidth() {
        if (getAuthUser() != null && getAuthUser().getBandwidthLimit() > 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Throttling bandwidth to " + getAuthUser().getBandwidthLimit() + " bytes per second.");
            }
            BandwidthLimitFilter.setLimit(getAuthUser().getBandwidthLimit());
        }
    }
}
