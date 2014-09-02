package de.codewave.mytunesrss.datastore.statement;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Collection;

/**
 * de.codewave.mytunesrss.datastore.statement.SmartInfo
 */
public class SmartInfo {

    public static boolean isLuceneCriteria(Collection<SmartInfo> smartInfos) {
        for (SmartInfo smartInfo : smartInfos) {
            if (smartInfo.isLuceneCriteria()) {
                return true;
            }
        }
        return false;
    }

    private SmartFieldType myFieldType;
    private String myPattern;
    private boolean myInvert;

    public SmartInfo(SmartFieldType fieldType, String pattern, boolean invert) {
        myFieldType = fieldType;
        myPattern = pattern;
        myInvert = invert;
    }

    public SmartFieldType getFieldType() {
        return myFieldType;
    }

    public void setFieldType(SmartFieldType fieldType) {
        myFieldType = fieldType;
    }

    public String getPattern() {
        return myPattern;
    }

    public void setPattern(String pattern) {
        myPattern = pattern;
    }

    public boolean isInvert() {
        return myInvert;
    }

    public void setInvert(boolean invert) {
        myInvert = invert;
    }

    public boolean isLuceneCriteria() {
        return myFieldType.isLucene();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("type", myFieldType).append("pattern", myPattern).append("invert", myInvert).build();
    }
}
