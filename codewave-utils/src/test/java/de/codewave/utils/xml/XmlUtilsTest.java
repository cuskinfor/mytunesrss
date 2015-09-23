/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.xml;

import junit.framework.*;
import org.xml.sax.*;

import javax.xml.parsers.*;
import javax.print.attribute.standard.*;
import java.io.*;
import java.util.*;

/**
 * de.codewave.utils.xml.XmlUtilsTest
 */
public class XmlUtilsTest extends TestCase {
    public static Test suite() {
        return new TestSuite(XmlUtilsTest.class);
    }

    public XmlUtilsTest(String name) {
        super(name);
    }

    public void testGetDefaultNamespaceUri() throws IOException, ParserConfigurationException, SAXException {
        assertNull(XmlUtils.getDefaultNamespaceUri(getClass().getResource("no-namespace.xml"), "test"));
        assertEquals("http://www.codewave.de", XmlUtils.getDefaultNamespaceUri(getClass().getResource("namespace.xml"), "test"));
    }

    public void testGetFileEncoding() {
        assertEquals("ISO-8859-1", XmlUtils.getEncoding(new StringReader("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>")));
        assertEquals("UTF-8", XmlUtils.getEncoding(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")));
        assertNull(XmlUtils.getEncoding(new StringReader("\n<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>")));
    }
}