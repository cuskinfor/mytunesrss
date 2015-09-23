package de.codewave.utils;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;

public class Base64Utils {
    public static String encode(byte[] bytes) {
        if (bytes != null) {
            byte[] encoded = Base64.encodeBase64(bytes);
            return getUtf8String(encoded);
        }
        return null;
    }

    public static String encode(String text) {
        if (text != null) {
            return encode(getUtf8Bytes(text));
        }
        return null;
    }

    public static byte[] decode(String base64) {
        if (base64 != null) {
            return Base64.decodeBase64(getUtf8Bytes(base64));
        }
        return null;
    }

    public static String decodeToString(String base64) {
        if (base64 != null) {
            return getUtf8String(decode(base64));
        }
        return null;
    }

    private static byte[] getUtf8Bytes(String s) {
        try {
            return s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not found.", e);
        }
    }

    private static String getUtf8String(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not found.", e);
        }
    }
}
