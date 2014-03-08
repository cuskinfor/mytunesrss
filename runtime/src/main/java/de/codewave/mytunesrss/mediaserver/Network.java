/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import org.apache.commons.lang3.StringUtils;

public class Network {

    private int[] myIp;
    private int myBits;

    public Network(String s) {
        if (StringUtils.isBlank(s)) {
            myIp = new int[] {0, 0, 0, 0};
            myBits = 0;
        } else {
            int i = s.indexOf('/');
            if (i == 0 || i == s.length() - 1) {
                throw new IllegalArgumentException("Illegal network specification \"" + s + "\".");
            }
            String ip = i != -1 ? s.substring(0, i) : s;
            String bits = i != -1 ? s.substring(i + 1) : "";
            try {
                myBits = Integer.parseInt(StringUtils.defaultIfBlank(bits, "32"));
                if (myBits < 0 || myBits > 32) {
                    throw new IllegalArgumentException("Illegal network specification \"" + s + "\".");
                }
            } catch (NumberFormatException ignored) {
                throw new IllegalArgumentException("Illegal network specification \"" + s + "\".");
            }
            myIp = convertIpString(ip);
        }
    }

    private int[] convertIpString(String ipString) {
        String[] ipParts = StringUtils.split(ipString, ".");
        if (ipParts.length != 4) {
            throw new IllegalArgumentException("Illegal network IP specification \"" + ipString + "\".");
        }
        int[] ip = new int[4];
        for (int i = 0; i < 4; i++) {
            try {
                ip[i] = Integer.parseInt(ipParts[i]);
                if (ip[i] < 0 || ip[i] > 255) {
                    throw new IllegalArgumentException("Illegal network IP specification \"" + ipString + "\".");
                }
            } catch (NumberFormatException ignored) {
                throw new IllegalArgumentException("Illegal network IP specification \"" + ipString + "\".");
            }
        }
        return ip;
    }

    private String getBits(int[] ip, int count) {
        StringBuilder builder = new StringBuilder();
        for (int ipPart : ip) {
            String bits = Integer.toString(ipPart, 2);
            builder.append(StringUtils.leftPad(bits, 8, '0'));
        }
        return builder.substring(0, count);
    }

    public boolean matches(String ipString) {
        try {
            int[] ip = convertIpString(ipString);
            return getBits(myIp, myBits).equals(getBits(ip, myBits));
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

}
