/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.servlet;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * de.codewave.utils.servlet.RangeHeader
 */
public class RangeHeader {
    private static final Logger LOGGER = LoggerFactory.getLogger(RangeHeader.class);

    private long myRangeFrom = -1;
    private long myRangeTo = -1;
    private boolean myRangeRequest;

    public RangeHeader(HttpServletRequest request) {
        if (ServletUtils.isRangeRequest(request)) {
            myRangeRequest = true;
            String rangeHeader = StringUtils.trimToEmpty(request.getHeader("Range"));
            rangeHeader = rangeHeader.substring(6);
            int i = rangeHeader.indexOf('-');
            if (i == 0) {
                myRangeTo = Long.parseLong(rangeHeader.substring(1));
            } else {
                myRangeFrom = Long.parseLong(rangeHeader.substring(0, i));
                if (i + 1 < rangeHeader.length()) {
                    myRangeTo = Long.parseLong(rangeHeader.substring(i + 1));
                }
            }
        }
    }

    public boolean isRangeRequest() {
        return myRangeRequest;
    }

    public long getRangeFrom() {
        return myRangeFrom;
    }

    public long getRangeTo() {
        return myRangeTo;
    }

    public long getFirstByte(long contentLength) {
        if (contentLength > 0 && isRangeRequest()) {
            return myRangeFrom > -1 ? Math.min(myRangeFrom, contentLength - 1L) : Math.max(0L, contentLength - myRangeTo);
        }
        return 0;
    }

    public long getLastByte(long contentLength) {
        if (contentLength > 0) {
            if (isRangeRequest()) {
                return myRangeTo > -1 ? (myRangeFrom > -1 ? Math.min(myRangeTo, contentLength - 1L) : contentLength - 1) : contentLength - 1L;
            }
            return contentLength - 1;
        } else {
            return 0;
        }
    }

    public long getSize(long contentLength) {
        return contentLength > 0 ? getLastByte(contentLength) - getFirstByte(contentLength) + 1L : 0;
    }

    public void setResponseHeaders(HttpServletResponse response, long contentLength) {
        if (isRangeRequest()) {
            String contentRangeHeaderValue = "bytes " + getFirstByte(contentLength) + "-" + getLastByte(contentLength) + "/" + contentLength;
            LOGGER.debug("Sending 'Content-Range' header \"" + contentRangeHeaderValue + "\".");
            response.setHeader("Content-Range", contentRangeHeaderValue);
            response.setHeader("Accept-Ranges", "bytes");
        }
    }
}
