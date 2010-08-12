package de.codewave.mytunesrss.datastore.statement;

public enum TrackSource {
    ITunes(), FileSystem();

    public String getJspName() {
        return name();
    }
}