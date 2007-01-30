package de.codewave.mytunesrss;

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
    private boolean myM3u;
    private boolean myUpload;
    private QuotaType myQuotaType;
    private int myDownFiles;
    private long myDownBytes;
    private int myQuotaDownFiles;
    private long myQuotaDownBytes;
    private int myFileQuota;
    private long myBytesQuota;
    private long myResetTime;
    private long myQuotaResetTime;
    private int myMaximumZipEntries;

    public User(String name) {
        myName = name;
        myResetTime = System.currentTimeMillis();
        myQuotaResetTime = myResetTime;
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

    public boolean isUpload() {
        return myUpload;
    }

    public void setUpload(boolean upload) {
        myUpload = upload;
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

    public int getDownFiles() {
        return myDownFiles;
    }

    public void setDownFiles(int downFiles) {
        myDownFiles = downFiles;
    }

    public int getFileQuota() {
        return myFileQuota;
    }

    public void setFileQuota(int fileQuota) {
        myFileQuota = fileQuota;
    }

    public long getQuotaDownBytes() {
        return myQuotaDownBytes;
    }

    public void setQuotaDownBytes(long quotaDownBytes) {
        myQuotaDownBytes = quotaDownBytes;
    }

    public int getQuotaDownFiles() {
        return myQuotaDownFiles;
    }

    public void setQuotaDownFiles(int quotaDownFiles) {
        myQuotaDownFiles = quotaDownFiles;
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

    @Override
    public boolean equals(Object object) {
        return object != null && object instanceof User && getName().equals(((User)object).getName());
    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }
}
