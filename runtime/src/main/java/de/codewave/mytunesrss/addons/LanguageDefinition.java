package de.codewave.mytunesrss.addons;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

@XmlRootElement
public class LanguageDefinition {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
        MAPPER.getDeserializationConfig().withAnnotationIntrospector(introspector);
        MAPPER.getSerializationConfig().withAnnotationIntrospector(introspector);
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

    public static Locale getLocaleFromCode(String code) {
        String[] split = StringUtils.split(code + " _ _ ", "_", 3);
        return new Locale(split[0].trim(), split[1].trim(), split[2].trim());
    }

    private Integer myId;
    private Integer myAccountId;
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

    public Integer getAccountId() {
        return myAccountId;
    }

    public LanguageDefinition setAccountId(Integer accountId) {
        myAccountId = accountId;
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

    @XmlTransient
    public Locale getLocale() {
        return LanguageDefinition.getLocaleFromCode(myCode);
    }
}
