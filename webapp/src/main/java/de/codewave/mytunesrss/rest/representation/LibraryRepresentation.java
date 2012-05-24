package de.codewave.mytunesrss.rest.representation;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
public class LibraryRepresentation {
    private String myVersion;
    private Map<String, URI> myUri = new HashMap<String, URI>();

    public String getVersion() {
        return myVersion;
    }

    public void setVersion(String version) {
        myVersion = version;
    }

    public Map<String, URI> getUri() {
        return myUri;
    }
}
