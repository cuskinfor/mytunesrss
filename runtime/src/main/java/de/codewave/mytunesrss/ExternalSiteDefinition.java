package de.codewave.mytunesrss;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * de.codewave.mytunesrss.ExternalSiteDefinition
 */
public class ExternalSiteDefinition {
    private String myType;
    private String myName;
    private String myUrl;

    public ExternalSiteDefinition(String type, String name, String url) {
        myType = type;
        myName = name;
        myUrl = url;
    }

    public String getType() {
        return myType;
    }

    public void setType(String type) {
        myType = type;
    }

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public String getUrl() {
        return myUrl;
    }

    public void setUrl(String url) {
        myUrl = url;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, new String[]{"myUrl"});
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, new String[]{"myUrl"});
    }
}