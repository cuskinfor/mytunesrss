package de.codewave.mytunesrss.datastore.statement;

public enum TrackSource {
    ITunes(), FileSystem(), YouTube();

    public boolean isExternal() {
        switch (this) {
            case YouTube:
                return true;
            default:
                return false;
        }
    }

    public String getJspName() {
        return name();
    }
}