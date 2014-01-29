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
import de.codewave.mytunesrss.rest.IncludeExcludeInterceptor;
import de.codewave.mytunesrss.rest.MyTunesRssRestException;
import de.codewave.mytunesrss.rest.UserPermission;
import de.codewave.mytunesrss.rest.representation.BonjourDeviceRepresentation;
import de.codewave.mytunesrss.rest.representation.SessionRepresentation;
import de.codewave.utils.servlet.ServletUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.validation.ValidateRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.util.*;

@ValidateRequest
@Path("session")
public class SessionResource extends RestResource {

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
        if (IncludeExcludeInterceptor.isAttr("libraryUri")) {
            session.setLibraryUri(uriInfo.getBaseUriBuilder().path(LibraryResource.class).build());
        }
        if (IncludeExcludeInterceptor.isAttr("transcoders")) {
            session.setTranscoders(getTranscoders(user));
        }
        if (IncludeExcludeInterceptor.isAttr("permissions")) {
            session.setPermissions(getPermissions(user));
        }
        if (IncludeExcludeInterceptor.isAttr("airtunesTargets")) {
            session.setAirtunesTargets(getAirtunesTargets());
        }
        if (IncludeExcludeInterceptor.isAttr("sessionTimeoutMinutes")) {
            session.setSessionTimeoutMinutes(user.getSessionTimeout());
        }
        if (IncludeExcludeInterceptor.isAttr("searchFuzziness")) {
            session.setSearchFuzziness(user.getSearchFuzziness());
        }
        return session;
    }

    private List<BonjourDeviceRepresentation> getAirtunesTargets() {
        Collection<BonjourDevice> devices = MyTunesRss.VLC_PLAYER.getRaopDevices();
        List<BonjourDeviceRepresentation> airtunesTargets = new ArrayList<>(devices.size());
        for (BonjourDevice device : devices) {
            airtunesTargets.add(new BonjourDeviceRepresentation(device));
        }
        return airtunesTargets;
    }

    public List<String> getTranscoders(User user) {
        List<String> transcoderNames = new ArrayList<>();
        if (user.isTranscoder() && MyTunesRss.CONFIG.isValidVlcConfig()) {
            for (TranscoderConfig config : MyTunesRss.CONFIG.getTranscoderConfigs()) {
                transcoderNames.add(config.getName());
            }
            Collections.sort(transcoderNames);
        }
        return transcoderNames;
    }

    public List<String> getPermissions(User user) {
        List<String> permissions = new ArrayList<>();
        for (UserPermission permission : UserPermission.values()) {
            if (permission.isGranted(user)) {
                permissions.add(StringUtils.uncapitalize(permission.name()));
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
            if (MyTunesRss.CONFIG.getUser(username) != null && !MyTunesRss.CONFIG.getUser(username).isActive()) {
                MyTunesRss.ADMIN_NOTIFY.notifyLoginExpired(username, ServletUtils.getBestRemoteAddress(request));
            } else {
                MyTunesRss.ADMIN_NOTIFY.notifyLoginFailure(username, ServletUtils.getBestRemoteAddress(request));
            }
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
