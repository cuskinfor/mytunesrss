/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest.resource;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.bonjour.BonjourDevice;
import de.codewave.mytunesrss.config.TranscoderConfig;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.rest.representation.BonjourDeviceRepresentation;
import de.codewave.mytunesrss.rest.representation.SettingsRepresentation;
import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.spi.validation.ValidateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
    public SettingsRepresentation getSettings() {
        SettingsRepresentation settings = new SettingsRepresentation();
        settings.setTranscoders(getTranscoders());
        settings.setPermissions(getPermissions());
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

    public List<String> getTranscoders() {
        List<String> transcoderNames = new ArrayList<String>();
        if (getAuthUser().isTranscoder()) {
            for (TranscoderConfig config : MyTunesRss.CONFIG.getTranscoderConfigs()) {
                transcoderNames.add(config.getName());
            }
            Collections.sort(transcoderNames);
        }
        return transcoderNames;
    }

    public List<String> getPermissions() {
        List<String> permissions = new ArrayList<String>();
        User user = getAuthUser();
        for (String permission : new String[] {"audio", "changeEmail", "changePassword", "createPlaylists", "createPublicPlaylists", "download"}) { // TODO
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
     * Pings the current session to keep it alive.
     */
    @POST
    public void ping() {
        // nothing to do, just keep the session alive
    }
}