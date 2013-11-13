/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * de.codewave.mytunesrss.servlet.ProgressRequestWrapper
 */
public class ProgressRequestWrapper extends HttpServletRequestWrapper {
    private int myByteCount;
    private int myPercentage;

    public ProgressRequestWrapper(HttpServletRequest httpServletRequest) throws IOException {
        super(httpServletRequest);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ServletInputStream originalInputStream = super.getInputStream();
        return new ServletInputStream() {
            public int read() throws IOException {
                countByte();
                return originalInputStream.read();
            }
        };
    }

    private void countByte() {
        myByteCount++;
        int percentage = (int)(((float)myByteCount / (float)getContentLength()) * 100f);
        if (percentage > myPercentage) {
            myPercentage = percentage;
            getSession().setAttribute("uploadPercentage", percentage);
        }
    }
}
