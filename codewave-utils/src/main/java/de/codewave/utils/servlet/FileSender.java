/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.servlet;

import javax.servlet.http.*;
import java.io.*;

/**
 * de.codewave.utils.servlet.FileSender
 */
public class FileSender extends StreamSender {
    public FileSender(File file, String contentType, long contentLength) throws FileNotFoundException {
        super(new FileInputStream(file), contentType, contentLength);
    }
}