package de.codewave.mytunesrss;

import de.codewave.utils.*;

import java.io.*;
import java.security.*;

/**
 * de.codewave.mytunesrss.Crypt
 */
public class Crypt {
    public static void main(String[] args) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        System.out.println(Base64Utils.encode(MessageDigest.getInstance("SHA").digest(args[0].getBytes("UTF-8"))));
    }
}