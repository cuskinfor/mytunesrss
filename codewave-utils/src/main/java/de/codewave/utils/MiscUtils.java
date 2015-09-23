/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils;

import com.ibm.icu.text.Normalizer;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * de.codewave.utils.MiscUtils
 */
public class MiscUtils {
    public static List<String> splitString(String s, int maxSize) {
        List<String> linesOfMaxSize = new ArrayList<String>();
        int i = 0;
        while (i < s.length()) {
            linesOfMaxSize.add(s.substring(i, Math.min(i + maxSize, s.length())));
            i += maxSize;
        }
        return linesOfMaxSize;
    }

    public static byte[] getUtf8Bytes(String s) {
        try {
            return s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not found.", e);
        }
    }

    public static String getUtf8String(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not found.", e);
        }
    }

    public static String getUtf8UrlEncoded(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not found.", e);
        }
    }

    public static String getUtf8UrlDecoded(String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not found.", e);
        }
    }

    public static String compose(String text) {
        return StringUtils.isBlank(text) ? text : Normalizer.compose(text, false);
    }

    public static String decompose(String text) {
        return StringUtils.isBlank(text) ? text : Normalizer.decompose(text, false);
    }

    public static String toNaturalSortString(String in, int len, int maxExpanded) {
        List<String> tokens = tokenizeForNaturalSortString(in);
        StringBuilder result = new StringBuilder();
        int nonNumericTokenLength = 0;
        int expanded = 0;
        for (String token : tokens) {
            if (expanded == maxExpanded) {
                result.append(token);
            } else {
                if (result.length() == 0 || !Character.isDigit(token.charAt(0))) {
                    result.append(token);
                    nonNumericTokenLength = token.length();
                } else {
                    int padding = len - nonNumericTokenLength - token.length();
                    if (padding > 0) {
                        result.append(StringUtils.repeat(' ', padding));
                    }
                    result.append(token);
                    expanded++;
                }
            }
        }
        return result.toString();
    }

    static List<String> tokenizeForNaturalSortString(String in) {
        List<String> tokens = new ArrayList<>();
        StringBuilder tokenBuilder = new StringBuilder();
        boolean tokenNumeric = true;
        boolean firstToken = true;
        for (char c : in.toCharArray()) {
            boolean currentNumeric = Character.isDigit(c);
            if (tokenNumeric != currentNumeric && tokenBuilder.length() > 0) {
                if (firstToken) {
                    tokens.add(tokenBuilder.toString());
                    firstToken = false;
                } else {
                    tokens.add(cleanupToken(tokenBuilder.toString()));
                }
                tokenBuilder.delete(0, tokenBuilder.length());
            }
            tokenBuilder.append(c);
            tokenNumeric = currentNumeric;
        }
        if (tokenBuilder.length() > 0) {
            if (firstToken) {
                tokens.add(tokenBuilder.toString());
            } else {
                tokens.add(cleanupToken(tokenBuilder.toString()));
            }
        }
        if (tokens.size() > 1 && Character.isDigit(tokens.get(0).charAt(0))) {
            tokens.set(0, tokens.get(0) + tokens.remove(1));
        }
        return tokens;
    }

    static String cleanupToken(String token) {
        return StringUtils.defaultIfEmpty(StringUtils.stripStart(token, "0"), "0");
    }

}
