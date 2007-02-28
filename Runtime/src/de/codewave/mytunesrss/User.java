package de.codewave.mytunesrss;

import java.util.*;

/**
 * de.codewave.mytunesrss.User
 */
public class User {
    public enum QuotaType {
        None, Day, Week, Month;

        @Override
        public String toString() {
            return MyTunesRss.BUNDLE.getString("editUser.quotaType." + name());
        }
    }

    private boolean myActive = true;
    private String myName;
    private byte[] myPasswordHash;
    private boolean myDownload;
    private boolean myRss;
    private boolean myPlaylist;
    private boolean myUpload;
    private boolean myChangePassword;
    private QuotaType myQuotaType;
    private long myDownBytes;
    private long myQuotaDownBytes;
    private long myBytesQuota;
    private long myResetTime;
    private long myQuotaResetTime;
    private int myMaximumZipEntries;
    private String myFileTypes;

    public User(String name) {
        myName = name;
        myResetTime = System.currentTimeMillis();
        myQuotaResetTime = myResetTime;
        myQuotaType = QuotaType.None;
    }

    public boolean isActive() {
        return myActive;
    }

    public void setActive(boolean active) {
        myActive = active;
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

    public boolean isDownload() {
        return myDownload;
    }

    public void setDownload(boolean download) {
        myDownload = download;
    }

    public boolean isPlaylist() {
        return myPlaylist;
    }

    public void setPlaylist(boolean playlist) {
        myPlaylist = playlist;
    }

    public boolean isRss() {
        return myRss;
    }

    public void setRss(boolean rss) {
        myRss = rss;
    }

    public boolean isUpload() {
        return myUpload;
    }

    public void setUpload(boolean upload) {
        myUpload = upload;
    }

    public boolean isChangePassword() {
        return myChangePassword;
    }

    public void setChangePassword(boolean changePassword) {
        myChangePassword = changePassword;
    }

    public long getBytesQuota() {
        return myBytesQuota;
    }

    public void setBytesQuota(long bytesQuota) {
        myBytesQuota = bytesQuota;
    }

    public long getDownBytes() {
        return myDownBytes;
    }

    public void setDownBytes(long downBytes) {
        myDownBytes = downBytes;
    }

    public long getQuotaDownBytes() {
        return myQuotaDownBytes;
    }

    public void setQuotaDownBytes(long quotaDownBytes) {
        myQuotaDownBytes = quotaDownBytes;
    }

    public long getQuotaResetTime() {
        return myQuotaResetTime;
    }

    public void setQuotaResetTime(long quotaResetTime) {
        myQuotaResetTime = quotaResetTime;
    }

    public QuotaType getQuotaType() {
        return myQuotaType;
    }

    public void setQuotaType(QuotaType quotaType) {
        myQuotaType = quotaType;
    }

    public boolean isQuota() {
        return myQuotaType != QuotaType.None;
    }

    public long getResetTime() {
        return myResetTime;
    }

    public void setResetTime(long resetTime) {
        myResetTime = resetTime;
    }

    public int getMaximumZipEntries() {
        return myMaximumZipEntries;
    }

    public void setMaximumZipEntries(int maximumZipEntries) {
        myMaximumZipEntries = maximumZipEntries;
    }

    public String getFileTypes() {
        return myFileTypes;
    }

    public void setFileTypes(String fileTypes) {
        myFileTypes = fileTypes;
    }

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof User && getName().equals(((User)object).getName());
    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }

    public boolean isQuotaExceeded() {
        if (myQuotaType != QuotaType.None && myBytesQuota > 0) {
            checkQuotaReset();
            return myBytesQuota > 0 && myQuotaDownBytes >= myBytesQuota;
        }
        return false;
    }

    public long getQuotaRemaining() {
        if (myQuotaType != QuotaType.None && myBytesQuota > 0) {
            checkQuotaReset();
            return Math.max(myBytesQuota - myQuotaDownBytes, 0);
        }
        return 0;
    }

    private void checkQuotaReset() {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date(myQuotaResetTime));
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        switch (myQuotaType) {
            case Month:
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.add(Calendar.MONTH, 1);
                break;
            case Week:
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                calendar.add(Calendar.DAY_OF_MONTH, 7);
                break;
            case Day:
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
        }
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            myQuotaDownBytes = 0;
            myQuotaResetTime = System.currentTimeMillis();
        }
    }
}
