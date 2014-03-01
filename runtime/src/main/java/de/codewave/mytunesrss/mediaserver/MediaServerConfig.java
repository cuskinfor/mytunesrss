/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.mediaserver;

import de.codewave.mytunesrss.MyTunesRss;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
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
        mapper.getSerializationConfig().withAnnotationIntrospector(introspector);
        try {
            return mapper.readValue(new File(MyTunesRss.PREFERENCES_DATA_PATH, "media_server.json"), MediaServerConfig.class);
        } catch (IOException e) {
            return new MediaServerConfig();
        }
    }

    public static void save(MediaServerConfig mediaServerConfig) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
        mapper.getDeserializationConfig().withAnnotationIntrospector(introspector);
        mapper.getSerializationConfig().withAnnotationIntrospector(introspector);
        mapper.writeValue(new File(MyTunesRss.PREFERENCES_DATA_PATH, "media_server.json"), mediaServerConfig);
    }

    @XmlElement
    private List<MediaServerClientProfile> myClientProfiles = new ArrayList<>();

    public List<MediaServerClientProfile> getClientProfiles() {
        return myClientProfiles;
    }

    public void setClientProfiles(List<MediaServerClientProfile> clientProfiles) {
        myClientProfiles = clientProfiles;
    }

    public MediaServerClientProfile getClientProfile(String userAgent) {
        for (MediaServerClientProfile clientProfile : getClientProfiles()) {
            if (clientProfile.matches(userAgent)) {
                return clientProfile;
            }
        }
        return new MediaServerClientProfile();
    }
}
