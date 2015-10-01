/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.servlet;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

/**
 * de.codewave.mytunesrss.servlet.ProgressRequestWrapper
 */
public class ProgressRequestWrapper extends HttpServletRequestWrapper {
    private int myByteCount;
    private int myPercentage;

    public ProgressRequestWrapper(HttpServletRequest httpServletRequest) {
        super(httpServletRequest);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ServletInputStream originalInputStream = super.getInputStream();
        return new ServletInputStream() {

            private boolean myFinished;

            @Override
            public boolean isFinished() {
                return myFinished;
            }

            @Override
            public boolean isReady() {
                return !myFinished;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                // not implemented
            }

            @Override
            public int read() throws IOException {
                countByte();
                int read = originalInputStream.read();
                if (read == -1) {
                    myFinished = true;
                }
                return read;
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
