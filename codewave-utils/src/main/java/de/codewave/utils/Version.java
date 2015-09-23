package de.codewave.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * de.codewave.utils.Version
 */
public class Version implements Comparable {
    private int myMajor;
    private int myMinor;
    private int myBugfix;
    private String myAppendix;

    public Version(String version) {
        version = StringUtils.replace(version, "-SNAPSHOT", ""); // remove snapshot
        String[] versionAndAppendix = version.split("-", 2);
        String[] parts = (versionAndAppendix[0] + ".0.0").split("\\.");
        if (versionAndAppendix.length > 1 && !versionAndAppendix[1].equalsIgnoreCase("null")) {
            myAppendix = versionAndAppendix[1];
        }
        myMajor = Integer.parseInt(parts[0]);
        myMinor = Integer.parseInt(parts[1]);
        myBugfix = Integer.parseInt(parts[2]);
    }

    public Version(int major, int minor, int bugfix) {
        myMajor = major;
        myMinor = minor;
        myBugfix = bugfix;
    }

    public int getMajor() {
        return myMajor;
    }

    public void setMajor(int major) {
        myMajor = major;
    }

    public int getMinor() {
        return myMinor;
    }

    public void setMinor(int minor) {
        myMinor = minor;
    }

    public int getBugfix() {
        return myBugfix;
    }

    public void setBugfix(int bugfix) {
        myBugfix = bugfix;
    }

    public String getAppendix() {
        return myAppendix;
    }

    public void setAppendix(String appendix) {
        myAppendix = appendix;
    }

    @Override
    public boolean equals(Object obj) {
        return compareTo(obj) == 0;
    }

    @Override
    public int hashCode() {
        return new Integer(myMajor).hashCode() + new Integer(myMinor).hashCode() + new Integer(myBugfix).hashCode();
    }

    public int compareTo(Object o) {
        if (o == null) {
            throw new NullPointerException("Cannot compare version object to null.");
        }
        if (o instanceof Version) {
            Version other = (Version)o;
            if (other.myMajor != myMajor) {
                return myMajor - other.myMajor;
            }
            if (other.myMinor != myMinor) {
                return myMinor - other.myMinor;
            }
            if (myBugfix != other.myBugfix) {
                return myBugfix- other.myBugfix;
            }
            if (StringUtils.isNotBlank(myAppendix) && StringUtils.isNotBlank(other.myAppendix)) {
                return compareAppendix(myAppendix, other.myAppendix);
            } else if (StringUtils.isBlank(myAppendix) && StringUtils.isBlank(other.myAppendix)) {
                return 0;
            } else if (StringUtils.isBlank(myAppendix)) {
                return 1;
            }
            return -1;
        }
        throw new IllegalArgumentException("Cannot compare version object to type \"" + o.getClass().getName() + "\".");
    }

    private int compareAppendix(String appendix1, String appendix2) {
        String[] appendixNames = {"ALPHA", "BETA", "EAP", "RC"};
        String[] appendix1parts = StringUtils.split(appendix1, "-");
        String[] appendix2parts = StringUtils.split(appendix2, "-");
        int index1 = ArrayUtils.indexOf(appendixNames, appendix1parts[0]);
        int index2 = ArrayUtils.indexOf(appendixNames, appendix2parts[0]);
        if (index1 != index2) {
            return index1 - index2;
        }
        return Integer.parseInt(appendix1parts[1]) - Integer.parseInt(appendix2parts[1]);
    }

    @Override
    public String toString() {
        return myMajor + "." + myMinor + "." + myBugfix + (StringUtils.isNotBlank(myAppendix) ? "-" + myAppendix : "");
    }
}
