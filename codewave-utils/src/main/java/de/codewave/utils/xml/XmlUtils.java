/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * de.codewave.utils.xml.XmlUtils
 */
public class XmlUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(XmlUtils.class);

    private static final Pattern XML_HEADER_PATTERN = Pattern.compile("\\s*<\\?xml.+encoding=\"([^\"]+)\"\\s*\\?>\\s*");

    public static String getDefaultNamespaceUri(URL xmlResource, String rootElementName)
            throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xmlResource.openStream());
        NodeList nodes = document.getElementsByTagName(rootElementName);
        if (nodes == null || nodes.getLength() == 0) {
            throw new IllegalArgumentException("Root element \"" + rootElementName + "\" not found in xml \"" + xmlResource.toExternalForm() + "\".");
        }
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            Node xmlns = node.getAttributes().getNamedItem("xmlns");
            if (xmlns != null) {
                String value = xmlns.getNodeValue();
                return value.substring(value.indexOf("=") + 1);
            }
        }
        return null;
    }

    public static void parseApplePList(URL pListUrl, PListHandler handler) throws ParserConfigurationException, SAXException, IOException {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        parser.parse(new InputSource(pListUrl.openStream()), handler);
    }

    public static String getEncoding(File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "ISO-8859-1"));
            return getEncoding(reader);
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not read xml file encoding.", e);
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return null;
    }

    public static String getEncoding(Reader reader) {
        BufferedReader bufferedReader = new BufferedReader(reader);
        try {
            String header = bufferedReader.readLine();
            if (header != null) {
                Matcher matcher = XML_HEADER_PATTERN.matcher(header);
                if (matcher.matches()) {
                    return matcher.group(1);
                }
            }
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not read xml file encoding.", e);
            }
        }
        return null;
    }
}