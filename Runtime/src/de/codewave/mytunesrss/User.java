package de.codewave.mytunesrss;

import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import java.io.*;

/**
 * de.codewave.mytunesrss.User
 */
public class User {
    private static final Log LOG = LogFactory.getLog(User.class);

    private String myName;
    private byte[] myPasswordHash;
    private boolean myDownload;
    private boolean myRss;
    private boolean myM3u;

    public User(String name) {
        myName = name;
    }

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public byte[] getPasswordHash() {
        return myPasswordHash;
    }

    public void setPasswordHash(byte[] passwordHash) {
        myPasswordHash = passwordHash;
    }

    public void setPassword(String password) {
        if (StringUtils.isNotEmpty(StringUtils.trim(password))) {
            try {
                myPasswordHash = MyTunesRss.MESSAGE_DIGEST.digest(StringUtils.trim(password).getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not create password hash.", e);
                }
            }
        } else {
            myPasswordHash = null;
        }
    }

    public boolean isDownload() {
        return myDownload;
    }

    public void setDownload(boolean download) {
        myDownload = download;
    }

    public boolean isM3u() {
        return myM3u;
    }

    public void setM3u(boolean m3u) {
        myM3u = m3u;
    }

    public boolean isRss() {
        return myRss;
    }

    public void setRss(boolean rss) {
        myRss = rss;
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof User && getName().equals(((User)object).getName());
    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }
}
