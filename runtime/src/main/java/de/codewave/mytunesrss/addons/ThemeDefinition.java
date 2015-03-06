package de.codewave.mytunesrss.addons;

import org.apache.commons.lang3.StringUtils;

public class ThemeDefinition implements Comparable<ThemeDefinition> {
    private String myName;
    private String myInfo;

    public ThemeDefinition(String name, String info) {
        myName = name;
        myInfo = info;
    }

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public String getInfo() {
        return myInfo;
    }

    public void setInfo(String info) {
        myInfo = info;
    }

    @Override
    public String toString() {
        return myName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ThemeDefinition)) {
            return false;
        }
        return StringUtils.equals(myName, ((ThemeDefinition) obj).myName);
    }

    @Override
    public int hashCode() {
        return myName != null ? myName.hashCode() : 0;
    }

    @Override
    public int compareTo(ThemeDefinition o) {
        return myName.compareTo(o.getName());
    }
}
