package de.codewave.mytunesrss.addons;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CommunityLanguageDefinition {
    private int myId;
    private String myUserHash;
    private String myNick;
    private String myCode;
    private String myVersion;
    private long lastUpdate;

    public int getId() {
        return myId;
    }

    public void setId(int id) {
        myId = id;
    }

    public String getUserHash() {
        return myUserHash;
    }

    public void setUserHash(String userHash) {
        myUserHash = userHash;
    }

    public String getNick() {
        return myNick;
    }

    public void setNick(String nick) {
        myNick = nick;
    }

    public String getCode() {
        return myCode;
    }

    public void setCode(String code) {
        myCode = code;
    }

    public String getVersion() {
        return myVersion;
    }

    public void setVersion(String version) {
        myVersion = version;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
