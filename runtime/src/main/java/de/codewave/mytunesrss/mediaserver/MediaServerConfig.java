/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.MyTunesRss;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class MediaServerConfig {

    public static MediaServerConfig load() {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
        mapper.getDeserializationConfig().withAnnotationIntrospector(introspector);
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return mapper.readValue(getConfigFile(), MediaServerConfig.class);
        } catch (IOException ignored) {
            return new MediaServerConfig();
        }
    }

    public static void save(MediaServerConfig mediaServerConfig) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
        mapper.getSerializationConfig().withAnnotationIntrospector(introspector);
        mapper.configure(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES, false);
        mapper.writeValue(getConfigFile(), mediaServerConfig);
    }

    private static File getConfigFile() {
        return new File(MyTunesRss.PREFERENCES_DATA_PATH, "media_server.json");
    }

    private MediaServerClientProfile myDefaultClientProfile = new MediaServerClientProfile();
    private List<MediaServerClientProfile> myClientProfiles = new ArrayList<>();

    @XmlElement
    public MediaServerClientProfile getDefaultClientProfile() {
        return myDefaultClientProfile;
    }

    public void setDefaultClientProfile(MediaServerClientProfile defaultClientProfile) {
        myDefaultClientProfile = defaultClientProfile;
    }

    @XmlElement
    public List<MediaServerClientProfile> getClientProfiles() {
        return new ArrayList<>(myClientProfiles);
    }

    public void addClientProfile(MediaServerClientProfile mediaServerClientProfile) {
        for (MediaServerClientProfile existingProfile : myClientProfiles) {
            if (existingProfile.getName().equals(mediaServerClientProfile.getName())) {
                throw new IllegalArgumentException("Client profile with name \"" + mediaServerClientProfile.getName() + "\" already exists.");
            }
        }
        myClientProfiles.add(mediaServerClientProfile);
    }

    public void setClientProfiles(List<MediaServerClientProfile> clientProfiles) {
        myClientProfiles = new ArrayList<>(clientProfiles);
    }

    public MediaServerClientProfile getClientProfile(String userAgent, String clientIp) {
        for (MediaServerClientProfile clientProfile : getClientProfiles()) {
            if (clientProfile.matches(userAgent, clientIp)) {
                return clientProfile;
            }
        }
        MediaServerClientProfile defaultClientProfile = getDefaultClientProfile();
        if (defaultClientProfile.matches(userAgent, clientIp)) {
            return defaultClientProfile;
        }
        return null;
    }

}
