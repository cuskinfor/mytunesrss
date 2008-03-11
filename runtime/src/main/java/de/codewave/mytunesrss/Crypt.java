package de.codewave.mytunesrss;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * de.codewave.mytunesrss.Crypt
 */
public class Crypt {
    public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        System.out.println(MyTunesRssBase64Utils.encode(MessageDigest.getInstance("SHA").digest(args[0].getBytes("UTF-8"))));
    }
}