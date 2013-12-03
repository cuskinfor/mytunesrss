package de.codewave.mytunesrss.vlc;

public enum VlcVersion {
    V20(), V21();

    public boolean isHttpPassword() {
        return this == V21;
    }

    public VlcVersion next() {
        switch (this) {
            case V20:
                return V21;
            case V21:
                return V20;
            default:
                throw new IllegalArgumentException("Unsupported VLC version \"" + this.name() + "\".");
        }
    }
}
