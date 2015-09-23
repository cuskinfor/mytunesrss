/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * de.codewave.utils.servlet.FileSender
 */
public class FileSender extends StreamSender {
    public FileSender(File file, String contentType, long contentLength) throws FileNotFoundException {
        super(new FileInputStream(file), contentType, contentLength);
    }
}