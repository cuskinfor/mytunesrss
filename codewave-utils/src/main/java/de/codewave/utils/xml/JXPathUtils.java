/*
 * Copyright (c) 2005 Codewave Software. All Rights Reserved.
 */
package de.codewave.utils.xml;

import org.apache.commons.jxpath.*;
import org.apache.commons.jxpath.xml.*;
import org.apache.commons.lang3.*;

import org.apache.commons.codec.binary.*;
import org.xml.sax.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

/**
 * Xml utilities for the jxpath api.
 */
public class JXPathUtils {
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JXPathUtils.class);

    /**
     * Public id entities.
     */
    private static Map<String, URL> PUBLIC_ENTITIES = new HashMap<String, URL>();

    /**
     * System id entities.
     */
    private static Map<String, URL> SYSTEM_ENTITIES = new HashMap<String, URL>();

    /**
     * Regular expression patterns for finding the encoding in the xml header
     */
    private static Pattern XML_HEAD_PATTERN = Pattern.compile("<\\?xml.*?encoding=[\"']?([^'\"\\s?]+).*?\\?>");

    /**
     * Add an entity.
     *
     * @param publicId The public id.
     * @param systemId The system id.
     * @param url      The url with the document.
     */
    public static void registerEntity(String publicId, String systemId, URL url) {
        if (!StringUtils.isEmpty(publicId)) {
            PUBLIC_ENTITIES.put(publicId, url);
        }
        if (!StringUtils.isEmpty(systemId)) {
            SYSTEM_ENTITIES.put(systemId, url);
        }
    }

    /**
     * Get the jxpath context for the xml at the specified url. The method uses a {@link DocumentContainer} for reading the xml. The context returned
     * is lenient.
     *
     * @param url The {@link java.net.URL} to read the XML from.
     *
     * @return A lenient jxpath context.
     */
    public static JXPathContext getContext(URL url) {
        return getContext(url, null);
    }

    /**
     * Get the jxpath context for the xml at the specified url. The method uses a {@link DocumentContainer} for reading the xml. The context returned
     * is lenient.
     *
     * @param url      The url to read the xml from.
     * @param systemId The prefix to use for relative system ids in the XML document
     *
     * @return A lenient jxpath context.
     */
    public static JXPathContext getContext(URL url, String systemId) {
        JXPathContext context = null;
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            documentBuilder.setEntityResolver(new EntityResolver() {
                public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                    URL url = PUBLIC_ENTITIES.get(publicId);
                    if (url == null) {
                        url = SYSTEM_ENTITIES.get(systemId);
                    }
                    return url != null ? new InputSource(url.openStream()) : null;
                }
            });
            if (systemId != null) {
                context = JXPathContext.newContext(documentBuilder.parse(url.openStream(), systemId));
            } else {
                context = JXPathContext.newContext(documentBuilder.parse(url.openStream()));
            }
            context.setLenient(true);
        } catch (SAXException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not parse the XML document from URL [" + url + "].", e);
            }

        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not read XML document from URL [" + url + "].", e);
            }

        } catch (ParserConfigurationException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not parse XML document from URL [" + url + "].", e);
            }
        }
        return context;
    }

    /**
     * Get the jxpath context for the specified XML text. The method uses a DOM parser for parsing. Since the parser expects an input stream, the XML
     * string is converted to a byte array using the {@link String#getBytes} method and the platform's default character set and then a byte array
     * input stream is created from this byte array. The context returned is lenient.
     *
     * @param xml The XML string.
     *
     * @return A lenient jxpath context.
     */
    public static JXPathContext getContext(String xml) {
        String encoding = getXmlEncoding(xml);
        byte[] bytes = new byte[0];
        try {
            bytes = xml.getBytes(encoding);
        } catch (UnsupportedEncodingException e) {
            bytes = xml.getBytes();
        }
        JXPathContext context = JXPathContext.newContext(new DOMParser().parseXML(new ByteArrayInputStream(bytes)));
        context.setLenient(true);
        return context;
    }

    /**
     * Try to find the encoding of the specified xml string.
     * @param xml An xml string.
     * @return The encoding of the xml or the platform default from the system property "file.encoding" if none is found. If neither an
     * encoding nor the default encoding are found, "UTF-8" is returned.
     */
    static String getXmlEncoding(String xml) {
        String encoding = System.getProperty("file.encoding");
        if (StringUtils.isEmpty(encoding)) {
            encoding = "UTF-8";
        }
        Matcher matcher = XML_HEAD_PATTERN.matcher(xml);
        if (matcher.find()) {
            encoding = matcher.group(1);
        }
        return encoding;
    }

    /**
     * Get a lenient jxpath context object for an xpath expression.
     *
     * @param context
     * @param xPathExpression
     *
     * @return A lenient jxpath context.
     */
    public static JXPathContext getContext(JXPathContext context, String xPathExpression) {
        if (context != null) {
            Pointer pointer = context.getPointer(xPathExpression);
            JXPathContext subContext = JXPathContext.newContext(pointer.getNode());
            subContext.setLenient(true);
            return subContext;
        }
        return null;
    }

    /**
     * Get an iterator of lenient jxpath context objects for an xpath expression.
     *
     * @param context         The context.
     * @param xPathExpression The xpath expression.
     *
     * @return An iterator of lenient context objects.
     */
    public static Iterator<JXPathContext> getContextIterator(JXPathContext context, String xPathExpression) {
        if (context != null) {
            final Iterator<Pointer> pointerIterator = context.iteratePointers(xPathExpression);
            return new Iterator<JXPathContext>() {
                public boolean hasNext() {
                    return pointerIterator.hasNext();
                }

                public JXPathContext next() {
                    JXPathContext context = JXPathContext.newContext(pointerIterator.next().getNode());
                    context.setLenient(true);
                    return context;
                }

                public void remove() {
                    throw new UnsupportedOperationException("Remove operation not supported on this iterator!");
                }
            };
        }
        Set<JXPathContext> emptySet = new HashSet<JXPathContext>(0);
        return emptySet.iterator();
    }

    /**
     * Get a value from an xpath expression as a string.
     *
     * @param context         The base context.
     * @param xPathExpression The xpath expression.
     * @param defaultValue    The default value to return in case the expression value is <code>null</code>.
     *
     * @return The value.
     */
    public static String getStringValue(JXPathContext context, String xPathExpression, String defaultValue) {
        Object value = context != null ? context.getValue(xPathExpression) : null;
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * Get a value from an xpath expression as a boolean value. Only the string <code>true</code> evaluates to <code>true</code>. Everything else
     * evaluates to <code>false</code>.
     *
     * @param context         The base context.
     * @param xPathExpression The xpath expression.
     * @param defaultValue    The default value to return in case the expression value is <code>null</code>.
     *
     * @return The value.
     */
    public static boolean getBooleanValue(JXPathContext context, String xPathExpression, boolean defaultValue) {
        Object value = context != null ? context.getValue(xPathExpression) : null;
        return value != null ? Boolean.valueOf(value.toString()) : defaultValue;
    }

    /**
     * Get a value from an xpath expression as an int value.
     *
     * @param context         The base context.
     * @param xPathExpression The xpath expression.
     * @param defaultValue    The default value to return in case the expression value is <code>null</code>.
     *
     * @return The value.
     */
    public static int getIntValue(JXPathContext context, String xPathExpression, int defaultValue) {
        Object value = context != null ? context.getValue(xPathExpression) : null;
        return value != null ? Integer.parseInt(value.toString()) : defaultValue;
    }

    /**
     * Get a value from an xpath expression as a long value.
     *
     * @param context         The base context.
     * @param xPathExpression The xpath expression.
     * @param defaultValue    The default value to return in case the expression value is <code>null</code>.
     *
     * @return The value.
     */
    public static long getLongValue(JXPathContext context, String xPathExpression, long defaultValue) {
        Object value = context != null ? context.getValue(xPathExpression) : null;
        return value != null ? Long.parseLong(value.toString()) : defaultValue;
    }

    /**
     * Get a value from an xpath expression as a double value.
     *
     * @param context         The base context.
     * @param xPathExpression The xpath expression.
     * @param defaultValue    The default value to return in case the expression value is <code>null</code>.
     *
     * @return The value.
     */
    public static double getDoubleValue(JXPathContext context, String xPathExpression, double defaultValue) {
        Object value = context != null ? context.getValue(xPathExpression) : null;
        return value != null ? Double.parseDouble(value.toString()) : defaultValue;
    }

    /**
     * Get a value from an xpath expression as a class. The string value is taken as a class name which is then looked up and returned. A {@link
     * ClassNotFoundException} is thrown in case the class with the specified name does not exist. The default value is not used in this case.
     *
     * @param context         The base context.
     * @param xPathExpression The xpath expression.
     * @param defaultValue    The default value to return in case the expression value is <code>null</code>.
     *
     * @return The value.
     *
     * @throws ClassNotFoundException The class specified in the value does not exist.
     */
    public static Class getClassValue(JXPathContext context, String xPathExpression, Class defaultValue) throws ClassNotFoundException {
        Object value = context != null ? context.getValue(xPathExpression) : null;
        return value != null ? Class.forName(value.toString()) : defaultValue;
    }

    /**
     * Get a value from an xpath expression as a byte array. The string value is taken as a base64 encoded byte array.
     *
     * @param context         The base context.
     * @param xPathExpression The xpath expression.
     * @param defaultValue    The default value to return in case the expression value is <code>null</code>.
     *
     * @return The value.
     */
    public static byte[] getByteArray(JXPathContext context, String xPathExpression, byte[] defaultValue) {
        Object value = context != null ? context.getValue(xPathExpression) : null;
        if (value != null) {
            try {
                return Base64.decodeBase64(value.toString().getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UTF-8 not found!");
            }
        }
        return defaultValue;
    }
}
