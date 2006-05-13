/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jsp;

import java.io.*;

/**
 * de.codewave.mytunesrss.jsp.MyTunesFunctions
 */
public class MyTunesFunctions {
    public static String virtualName(File file) {
        String name = file.getName();
        name = name.replace(' ', '_');
        return name;
    }
}