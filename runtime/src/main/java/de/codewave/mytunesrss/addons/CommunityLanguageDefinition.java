package de.codewave.mytunesrss.addons;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CommunityLanguageDefinition {
    private String myNick;
    private String myCode;
    private String myVersion;

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
}
