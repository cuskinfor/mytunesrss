package de.codewave.mytunesrss.vlc;

public enum VlcVersion {
    V20(), V21();
    
    public boolean isHttpPassword() {
        return this == V21;
    }
}
