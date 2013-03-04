/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.bonjour.BonjourDevice;
import de.codewave.mytunesrss.command.WebAppScope;
import de.codewave.mytunesrss.config.transcoder.TranscoderConfig;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.rest.MyTunesRssRestException;
import de.codewave.mytunesrss.rest.representation.BonjourDeviceRepresentation;
import de.codewave.mytunesrss.rest.representation.SessionRepresentation;
import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.validation.ValidateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@ValidateRequest
@Path("session")
public class SessionResource extends RestResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionResource.class);
    private static final String[] PERMISSION_NAMES = new String[] {
            "audio", "movies", "tvShows", "rss", "playlist", "download", "yahooPlayer", "specialPlaylists", "player", "remoteControl", "externalSites", "editTags",
            "transcoder", "changePassword", "changeEmail", "editLastFmAccount", "editWebSettings", "createPlaylists", "createPublicPlaylists", "photos",
            "downloadPhotoAlbum", "share"
    };

    /**
     * Get the settings for the current session.
     *
     * @return The settings of the current session.
     */
    @GET
    @Produces({"application/json"})
    @GZIP
    public SessionRepresentation getSession(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request
    ) {
        SessionRepresentation session = new SessionRepresentation();
        User user = MyTunesRssWebUtils.getAuthUser(request);
        session.setLibraryUri(uriInfo.getBaseUriBuilder().path(LibraryResource.class).build());
        session.setTranscoders(getTranscoders(user));
        session.setPermissions(getPermissions(user));
        session.setAirtunesTargets(getAirtunesTargets());
        session.setSessionTimeoutMinutes(user.getSessionTimeout());
        session.setSearchFuzziness(user.getSearchFuzziness());
        return session;
    }

    private List<BonjourDeviceRepresentation> getAirtunesTargets() {
        Collection<BonjourDevice> devices = MyTunesRss.VLC_PLAYER.getRaopDevices();
        List<BonjourDeviceRepresentation> airtunesTargets = new ArrayList<BonjourDeviceRepresentation>(devices.size());
        for (BonjourDevice device : devices) {
            airtunesTargets.add(new BonjourDeviceRepresentation(device));
        }
        return airtunesTargets;
    }

    public List<String> getTranscoders(User user) {
        List<String> transcoderNames = new ArrayList<String>();
        if (user.isTranscoder() && MyTunesRss.CONFIG.isValidVlcConfig()) {
            for (TranscoderConfig config : MyTunesRss.CONFIG.getTranscoderConfigs()) {
                transcoderNames.add(config.getName());
            }
            Collections.sort(transcoderNames);
        }
        return transcoderNames;
    }

    public List<String> getPermissions(User user) {
        List<String> permissions = new ArrayList<String>();
        for (String permission : PERMISSION_NAMES) {
            try {
                if ((Boolean) User.class.getMethod("is" + StringUtils.capitalize(permission)).invoke(user)) {
                    permissions.add(permission);
                }
            } catch (IllegalAccessException e) {
                LOGGER.warn("Could not get permission \"" + permission + "\".", e);
            } catch (InvocationTargetException e) {
                LOGGER.warn("Could not get permission \"" + permission + "\".", e);
            } catch (NoSuchMethodException e) {
                LOGGER.warn("Could not get permission \"" + permission + "\".", e);
            }
        }
        return permissions;
    }

    /**
     * Login a user.
     *
     * @param uriInfo
     * @param username A user name.
     * @param password A password.
     * @return
     * @throws UnsupportedEncodingException
     */
    @POST
    @Produces({"application/json"})
    public SessionRepresentation loginOrPing(
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request,
            @FormParam("username") String username,
            @FormParam("password") String password
    ) throws UnsupportedEncodingException {
        if (StringUtils.isBlank(username) && StringUtils.isBlank(password)) {
            // ping session
            if (MyTunesRssWebUtils.getAuthUser(request) == null) {
                throw new MyTunesRssRestException(HttpServletResponse.SC_UNAUTHORIZED, "NO_VALID_USER_SESSION");
            } else {
                return getSession(uriInfo, request);
            }
        }
        if (MyTunesRssWebUtils.getAuthUser(request) != null) {
            // login with existing session
            throw new MyTunesRssRestException(HttpServletResponse.SC_BAD_REQUEST, "EXISTING_USER_SESSION");
        }
        byte[] passwordHash = MyTunesRss.SHA1_DIGEST.get().digest(password.getBytes("UTF-8"));
        if (MyTunesRssWebUtils.isAuthorized(username, password, passwordHash) && !MyTunesRss.CONFIG.getUser(username).isEmptyPassword()) {
            // login successful
            MyTunesRssWebUtils.authorize(WebAppScope.Session, request, username);
            return getSession(uriInfo, request);
        } else {
            // invalid login
            throw new MyTunesRssRestException(HttpServletResponse.SC_UNAUTHORIZED, "INVALID_LOGIN");
        }
    }

    /**
     * Logout a user.
     */
    @DELETE
    public void logout(
            @Context HttpServletRequest request
    ) {
        request.getSession().invalidate();
    }
}
