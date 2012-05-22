/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest;

import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.config.User;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

public class RestResource {

    @Context
    private HttpServletRequest myRequest;

    protected User getAuthUser() {
        return MyTunesRssWebUtils.getAuthUser(myRequest);
    }
}
