/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import org.apache.commons.codec.binary.*;

import java.io.*;

/**
 * de.codewave.mytunesrss.MyTunesRssBase64Utils
 */
public class MyTunesRssBase64Utils {
    public static String encode(byte[] bytes) {
        if (bytes != null) {
            byte[] encoded = Base64.encodeBase64(bytes);
            try {
                return new String(encoded, "UTF-8").replace('+', '-').replace('/', '_');
            } catch (UnsupportedEncodingException e) {
                return new String(encoded).replace('+', '-').replace('/', '_');
            }
        }
        return null;
    }

    public static String encode(String text) {
        if (text != null) {
            try {
                return encode(text.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                return encode(text.getBytes());

            }
        }
        return null;
    }

    public static byte[] decode(String base64) {
        if (base64 != null) {
            String encoded = base64.replace('_', '/').replace('-', '+');
            try {
                return Base64.decodeBase64(encoded.getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                return Base64.decodeBase64(encoded.getBytes());
            }
        }
        return null;
    }

    public static String decodeToString(String base64) {
        if (base64 != null) {
            try {
                return new String(decode(base64), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return new String(decode(base64));
            }
        }
        return null;
    }
}