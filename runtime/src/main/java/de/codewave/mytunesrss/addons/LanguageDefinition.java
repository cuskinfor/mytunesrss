package de.codewave.mytunesrss.addons;

import org.apache.commons.lang.StringUtils;

public class LanguageDefinition implements Comparable<LanguageDefinition> {
    private String myCode;
    private String myInfo;

    public LanguageDefinition(String code, String info) {
        myCode = code;
        myInfo = info;
    }

    public String getCode() {
        return myCode;
    }

    public void setCode(String code) {
        myCode = code;
    }

    public String getInfo() {
        return myInfo;
    }

    public void setInfo(String info) {
        myInfo = info;
    }

    @Override
    public String toString() {
        return myCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof LanguageDefinition)) {
            return false;
        }
        return StringUtils.equals(myCode, ((LanguageDefinition) obj).myCode);
    }

    @Override
    public int hashCode() {
        return myCode != null ? myCode.hashCode() : 0;
    }

    public int compareTo(LanguageDefinition o) {
        return myCode.compareTo(o.getCode());
    }
}
