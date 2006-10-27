/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import de.codewave.utils.*;

/**
 * de.codewave.mytunesrss.MyTunesRssBase64Utils
 */
public class MyTunesRssBase64Utils {
    private static final Base64 BASE64_CODER = new Base64();

    static {
        BASE64_CODER.setDictionary(new DefaultBase64Dictionary() {
            @Override
            protected char getCharFor62() {
                return '-';
            }

            @Override
            protected char getCharFor63() {
                return '_';
            }
        });
    }

    public static String encode(byte[] bytes) {
        return BASE64_CODER.encode(bytes);
    }

    public static String encode(String text) {
        return BASE64_CODER.encode(text);
    }

    public static byte[] decode(String base64) {
        return BASE64_CODER.decode(base64);
    }

    public static String decodeToString(String base64) {
        return BASE64_CODER.decodeToString(base64);
    }
}