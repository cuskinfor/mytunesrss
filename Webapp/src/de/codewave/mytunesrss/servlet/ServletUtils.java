/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import javax.servlet.http.*;

/**
 * de.codewave.mytunesrss.servlet.ServletUtils
 */
public class ServletUtils {
    public static String getApplicationUrl(HttpServletRequest request) {
        String requestUrl = request.getRequestURL().toString();
        String contextPath = request.getContextPath();
        return requestUrl.substring(0, requestUrl.indexOf(contextPath)) + contextPath;
    }
}