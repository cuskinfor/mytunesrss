/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.jsp;

import de.codewave.utils.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

import org.apache.commons.codec.binary.*;

/**
 * de.codewave.utils.jsp.CodewaveFunctions
 */
public class CodewaveFunctions {
    public static boolean contains(Collection collection, Object element) {
        return collection.contains(element);
    }

    public static Object choose(boolean condition, Object trueObject, Object falseObject) {
        return condition ? trueObject : falseObject;
    }

    public static String replace(String string, String target, String replacement) {
        return string.replace(target, replacement);
    }

    public static String getSystemProperty(String name) {
        return System.getProperty(name);
    }

    public static String message(String message, Object[] parameters) {
        return MessageFormat.format(message, parameters);
    }

    public static String encodeBase64(String data) throws UnsupportedEncodingException {
        return new String(Base64.encodeBase64(data.getBytes("UTF-8")), "UTF-8");
    }

    public static String decodeBase64(String data) throws UnsupportedEncodingException {
        return new String(Base64.decodeBase64(data.getBytes("UTF-8")), "UTF-8");
    }
}