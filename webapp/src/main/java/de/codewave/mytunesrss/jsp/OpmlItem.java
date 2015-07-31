package de.codewave.mytunesrss.jsp;

public class OpmlItem {

    private final String name;
    private final String xmlUrl;

    public OpmlItem(String name, String xmlUrl) {
        this.name = name;
        this.xmlUrl = xmlUrl;
    }

    public String getName() {
        return name;
    }

    public String getXmlUrl() {
        return xmlUrl;
    }
}
