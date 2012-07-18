package de.codewave.mytunesrss.rest.representation;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.List;

/**
 * Settings of the current session.
 */
@XmlRootElement
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class SessionRepresentation implements RestRepresentation {

    private URI myLibraryUri;

    private List<String> myTranscoders;

    private List<BonjourDeviceRepresentation> myAirtunesTargets;

    private List<String> myPermissions;

    private Integer mySessionTimeoutSeconds;

    private Integer mySearchFuzziness;

    /**
     * Main library URI.
     */
    public URI getLibraryUri() {
        return myLibraryUri;
    }

    public void setLibraryUri(URI libraryUri) {
        myLibraryUri = libraryUri;
    }

    /**
     * List of available transcoders.
     */
    public List<String> getTranscoders() {
        return myTranscoders;
    }

    public void setTranscoders(List<String> transcoders) {
        myTranscoders = transcoders;
    }

    /**
     * List of available airtunes targets that can be used for the server local media player.
     */
    public List<BonjourDeviceRepresentation> getAirtunesTargets() {
        return myAirtunesTargets;
    }

    public void setAirtunesTargets(List<BonjourDeviceRepresentation> airtunesTargets) {
        myAirtunesTargets = airtunesTargets;
    }

    /**
     * List of permissions of the current user.
     */
    public List<String> getPermissions() {
        return myPermissions;
    }

    public void setPermissions(List<String> permissions) {
        myPermissions = permissions;
    }

    /**
     * Session timeout in seconds, use this interval minus a few seconds for pinging the server to keep the session alive if necessary.
     */
    public Integer getSessionTimeoutSeconds() {
        return mySessionTimeoutSeconds;
    }

    public void setSessionTimeoutSeconds(Integer sessionTimeoutSeconds) {
        mySessionTimeoutSeconds = sessionTimeoutSeconds;
    }

    /**
     * The configured search fuzziness for the user which is either a value from 0 to 100 or -1 for no default value. In case a value from 0 to 100 is returned,
     * any parameter for the track search is ignored and the returned value is used.
     */
    public Integer getSearchFuzziness() {
        return mySearchFuzziness;
    }

    public void setSearchFuzziness(Integer searchFuzziness) {
        mySearchFuzziness = searchFuzziness;
    }
}
