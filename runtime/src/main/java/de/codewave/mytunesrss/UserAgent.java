package de.codewave.mytunesrss;

public enum UserAgent {
    Iphone("iphone"), Psp("psp"), Unknown("default"), NintendoWii("wii");

    public static UserAgent fromConfigKey(String configKey) {
        for (UserAgent userAgent : UserAgent.values()) {
            if (userAgent.toConfigKey().equals(configKey)) {
                return userAgent;
            }
        }
        throw new IllegalArgumentException("No user agent enum value for config key \"" + configKey + " \".");
    }

    private final String myConfigKey;

    UserAgent(String configKey) {
        myConfigKey = configKey;
    }

    public String toConfigKey() {
        return myConfigKey;
    }
}
