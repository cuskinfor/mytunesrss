/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.xml;


import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * de.codewave.mytunesrss.itunes.PListHandler
 */
public class PListHandler extends DefaultHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PListHandler.class);

    private Stack myContainers = new Stack();
    private StringBuffer myCurrentCharacters = new StringBuffer();
    private Stack<String> myKeys = new Stack<String>();
    private Object myPList;
    private DateFormat myDateFormat;
    private StringBuffer myPath = new StringBuffer();
    Map<String, PListHandlerListener> myListeners = new HashMap<String, PListHandlerListener>();

    public PListHandler() {
        myDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        myDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public void addListener(String path, PListHandlerListener listener) {
        myListeners.put(path, listener);
    }

    public void removeListener(String path) {
        myListeners.remove(path);
    }

    public Object getPList() {
        return myPList;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (myCurrentCharacters.length() > 0) {
            myCurrentCharacters.delete(0, myCurrentCharacters.length());
        }
        if ("dict".equalsIgnoreCase(qName)) {
            myContainers.push(new HashMap());
            myPath.append("/").append(qName);
        } else if ("array".equalsIgnoreCase(qName)) {
            myContainers.push(new ArrayList());
            myPath.append("/").append(qName);
        } else if ("plist".equalsIgnoreCase(qName)) {
            myContainers.push(new HashSet());
            myPath.append("/").append(qName);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        Object value;
        boolean useValue = false;
        if ("dict".equalsIgnoreCase(qName) || "array".equalsIgnoreCase(qName)) {
            value = myContainers.pop();
            myPath.delete(myPath.lastIndexOf("/"), myPath.length());
            useValue = true;
        } else if ("key".equals(qName)) {
            myKeys.push(myCurrentCharacters.toString());
            value = null;
            myPath.append("[").append(myCurrentCharacters.toString()).append("]");
        } else if ("plist".equalsIgnoreCase(qName)) {
            myPList = ((Iterable)myContainers.pop()).iterator().next();
            value = null;
            myPath.delete(myPath.lastIndexOf("/"), myPath.length());
        } else if ("true".equalsIgnoreCase(qName) || "false".equalsIgnoreCase(qName)) {
            value = new Boolean(qName);
            useValue = true;
        } else if ("date".equalsIgnoreCase(qName)) {
            try {
                value = myDateFormat.parse(myCurrentCharacters.toString().trim());
            } catch (ParseException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not parse date value " + myCurrentCharacters.toString().trim() + " from plist file.", e);
                }
                value = null;
            }
            useValue = true;
        } else if ("integer".equalsIgnoreCase(qName)) {
            try {
                value = Long.parseLong(myCurrentCharacters.toString().trim());
            } catch (NumberFormatException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not parse integer value \"" + myCurrentCharacters.toString().trim() + "\" from plist file.", e);
                }
                value = null;
            }
            useValue = true;
        } else if ("real".equalsIgnoreCase(qName)) {
            try {
                value = Double.parseDouble(myCurrentCharacters.toString().trim());
            } catch (NumberFormatException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Could not parse real value \"" + myCurrentCharacters.toString().trim() + "\" from plist file, using a string instead.",
                              e);
                }
                value = myCurrentCharacters.toString();
            }
            useValue = true;
        } else {
            value = myCurrentCharacters.toString();
            useValue = true;
        }
        if (useValue) {
            Object container = myContainers.peek();
            if (container instanceof Map) {
                String key = myKeys.pop();
                myPath.delete(myPath.lastIndexOf("["), myPath.length());
                PListHandlerListener listener = myListeners.get(myPath.toString());
                if (listener == null || listener.beforeDictPut((Map)container, key, value)) {
                    ((Map)container).put(key, value);
                }
            } else if (container instanceof List) {
                PListHandlerListener listener = myListeners.get(myPath.toString());
                if (listener == null || listener.beforeArrayAdd((List)container, value)) {
                    ((List)container).add(value);
                }
            } else {
                ((Set)container).add(value);
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        myCurrentCharacters.append(new String(ch, start, length));
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
        if ("-//Apple Computer//DTD PLIST 1.0//EN".equals(publicId) || "http://www.apple.com/DTDs/PropertyList-1.0.dtd".equals(systemId)) {
            return new InputSource(getClass().getResourceAsStream("plist.dtd"));
        }
        return super.resolveEntity(publicId, systemId);
    }
}