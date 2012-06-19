/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.bonjour.BonjourDevice;
import de.codewave.mytunesrss.command.WebAppScope;
import de.codewave.mytunesrss.config.TranscoderConfig;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.rest.MyTunesRssRestException;
import de.codewave.mytunesrss.rest.representation.BonjourDeviceRepresentation;
import de.codewave.mytunesrss.rest.representation.SettingsRepresentation;
import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.validation.ValidateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@ValidateRequest
@Path("session")
public class SessionResource extends RestResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionResource.class);

    /**
     * Get the settings for the current session.
     *
     * @return The settings of the current session.
     */
    @GET
    @Produces({"application/json"})
    @GZIP
    public SettingsRepresentation getSettings(
            @Context HttpServletRequest request
    ) {
        SettingsRepresentation settings = new SettingsRepresentation();
        User user = MyTunesRssWebUtils.getAuthUser(request);
        settings.setTranscoders(getTranscoders(user));
        settings.setPermissions(getPermissions(user));
        settings.setAirtunesTargets(getAirtunesTargets());
        return settings;
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
        if (user.isTranscoder()) {
            for (TranscoderConfig config : MyTunesRss.CONFIG.getTranscoderConfigs()) {
                transcoderNames.add(config.getName());
            }
            Collections.sort(transcoderNames);
        }
        return transcoderNames;
    }

    public List<String> getPermissions(User user) {
        List<String> permissions = new ArrayList<String>();
        for (String permission : new String[]{"audio", "changeEmail", "changePassword", "createPlaylists", "createPublicPlaylists", "download"}) { // TODO
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
    public Response loginOrPing(
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
                return Response.ok().build();
            }
        }
        if (MyTunesRssWebUtils.getAuthUser(request) != null) {
            // login with existing session
            throw new MyTunesRssRestException(HttpServletResponse.SC_BAD_REQUEST, "EXISTING_USER_SESSION");
        }
        byte[] passwordHash = MyTunesRss.SHA1_DIGEST.digest(password.getBytes("UTF-8"));
        if (MyTunesRssWebUtils.isAuthorized(username, password, passwordHash) && !MyTunesRss.CONFIG.getUser(username).isEmptyPassword()) {
            // login successful
            MyTunesRssWebUtils.authorize(WebAppScope.Session, request, username);
            return Response.created(uriInfo.getBaseUriBuilder().path(LibraryResource.class).build()).build();
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
