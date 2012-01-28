package de.codewave.mytunesrss.addons;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@XmlRootElement
public class LanguageDefinition {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
        MAPPER.getDeserializationConfig().setAnnotationIntrospector(introspector);
        MAPPER.getSerializationConfig().setAnnotationIntrospector(introspector);
    }


    public static LanguageDefinition deserialize(InputStream is) throws IOException {
        return MAPPER.readValue(is, LanguageDefinition.class);
    }

    public static List<LanguageDefinition> deserializeList(InputStream is) throws IOException {
        return MAPPER.readValue(is, new TypeReference<List<LanguageDefinition>>() {
        });
    }

    public static void serialize(LanguageDefinition definition, OutputStream os) throws IOException {
        MAPPER.writeValue(os, definition);
    }

    private Integer myId;
    private String myUserHash;
    private String myNick;
    private String myCode;
    private String myVersion;
    private long lastUpdate;

    public Integer getId() {
        return myId;
    }

    public LanguageDefinition setId(Integer id) {
        myId = id;
        return this;
    }

    public String getUserHash() {
        return myUserHash;
    }

    public LanguageDefinition setUserHash(String userHash) {
        myUserHash = userHash;
        return this;
    }

    public String getNick() {
        return myNick;
    }

    public LanguageDefinition setNick(String nick) {
        myNick = nick;
        return this;
    }

    public String getCode() {
        return myCode;
    }

    public LanguageDefinition setCode(String code) {
        myCode = code;
        return this;
    }

    public String getVersion() {
        return myVersion;
    }

    public LanguageDefinition setVersion(String version) {
        myVersion = version;
        return this;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public LanguageDefinition setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
        return this;
    }
}
