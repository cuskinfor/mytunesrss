/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.TranscoderConfig;
import de.codewave.mytunesrss.config.User;
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

    @GET
    @Produces({"application/json"})
    @GZIP
    public Map getSettings() {
        Map settings = new HashMap();
        settings.put("transcoders", getTranscoders());
        settings.put("permissions", getPermissions());
        return settings;
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

    public Map<String, Boolean> getPermissions() {
        Map<String, Boolean> permissions = new HashMap<String, Boolean>();
        User user = getAuthUser();
        for (String permission : new String[] {"audio", "changeEmail", "changePassword", "createPlaylists", "createPublicPlaylists", "download"}) { // TODO
            try {
                permissions.put(permission, (Boolean) User.class.getMethod("is" + StringUtils.capitalize(permission)).invoke(user));
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

    @POST
    public void ping() {
        // nothing to do, just keep the session alive
    }
}
