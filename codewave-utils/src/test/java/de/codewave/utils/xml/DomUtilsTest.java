/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.xml;

import junit.framework.*;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;

/**
 * de.codewave.utils.xml.XmlUtilsTest
 */
public class DomUtilsTest extends TestCase {
    public static Test suite() {
        return new TestSuite(DomUtilsTest.class);
    }

    public DomUtilsTest(String name) {
        super(name);
    }

    public void testCreateDocument() throws TransformerException, ParserConfigurationException {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = document.createElement("settings");
        root.appendChild(DOMUtils.createTextElement(document, "string", "hello world"));
        root.appendChild(DOMUtils.createBooleanElement(document, "boolean", true));
        root.appendChild(DOMUtils.createIntElement(document, "integer", 123));
        root.appendChild(DOMUtils.createLongElement(document, "long", Long.MAX_VALUE));
        root.appendChild(DOMUtils.createByteArrayElement(document, "bytes", "hello world".getBytes()));
        root.appendChild(DOMUtils.createClassElement(document, "bytes", this.getClass()));
        document.appendChild(root);
        DOMUtils.prettyPrint(document, System.out);
    }

}