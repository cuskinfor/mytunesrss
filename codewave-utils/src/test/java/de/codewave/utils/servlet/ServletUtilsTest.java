/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.servlet;

import junit.framework.*;

import java.net.*;

/**
 * de.codewave.utils.servlet.ServletUtilsTest
 */
public class ServletUtilsTest extends TestCase {
    public static Test suite() {
        return new TestSuite(ServletUtilsTest.class);
    }

    public ServletUtilsTest(String name) {
        super(name);
    }

    public void testGetServletMapping() {
        URL webXml = getClass().getResource("test-web.xml");
        assertEquals("/alpha", ServletUtils.getServletMapping(webXml, "AlphaServlet"));
        assertEquals("/beta/*", ServletUtils.getServletMapping(webXml, "BetaServlet"));
        assertEquals("/alpha", ServletUtils.getServletMapping(webXml, AlphaTestServlet.class));
        assertEquals("/beta/*", ServletUtils.getServletMapping(webXml, BetaTestServlet.class));
    }

    public void testGetServletMappingNoNamespace() {
        URL webXml = getClass().getResource("test-nonamespace-web.xml");
        assertEquals("/alpha", ServletUtils.getServletMapping(webXml, "AlphaServlet"));
        assertEquals("/beta/*", ServletUtils.getServletMapping(webXml, "BetaServlet"));
        assertEquals("/alpha", ServletUtils.getServletMapping(webXml, AlphaTestServlet.class));
        assertEquals("/beta/*", ServletUtils.getServletMapping(webXml, BetaTestServlet.class));
    }
}