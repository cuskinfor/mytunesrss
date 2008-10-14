package de.codewave.mytunesrss.remote;

import org.jabsorb.JSONSerializer;
import org.jabsorb.serializer.impl.*;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.MarshallException;
import org.json.JSONObject;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * de.codewave.mytunesrss.remote.MyTunesRssJsonSerializer
 */
public class MyTunesRssJsonSerializer extends JSONSerializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssJsonSerializer.class);

    public MyTunesRssJsonSerializer() throws Exception {
        super();
        setMarshallClassHints(false);
        registerDefaultSerializers();
    }

    @Override
    public void registerDefaultSerializers() throws Exception {
        registerSerializer(new RawJSONArraySerializer());
        registerSerializer(new RawJSONObjectSerializer());
        registerSerializer(new BeanSerializer());
        registerSerializer(new ArraySerializer());
        registerSerializer(new DictionarySerializer());
        registerSerializer(new MapSerializer() {
            @Override
            public Object marshall(SerializerState state, Object p, Object o) throws MarshallException {
                Object result = super.marshall(state, p, o);
                try {
                    if (result != null && result instanceof JSONObject && ((JSONObject)result).get("map") != null) {
                        return ((JSONObject)result).get("map");
                    }
                } catch (JSONException e) {
                    LOGGER.error("Could not extract real map object.", e);
                }
                return result;
            }
        });
        registerSerializer(new SetSerializer());
        registerSerializer(new ListSerializer() {
            @Override
            public Object marshall(SerializerState state, Object p, Object o) throws MarshallException {
                Object result = super.marshall(state, p, o);
                try {
                    if (result != null && result instanceof JSONObject && ((JSONObject)result).get("list") != null) {
                        return ((JSONObject)result).get("list");
                    }
                } catch (JSONException e) {
                    LOGGER.error("Could not extract real list object.", e);
                }
                return result;
            }
        });
        registerSerializer(new DateSerializer());
        registerSerializer(new StringSerializer());
        registerSerializer(new NumberSerializer());
        registerSerializer(new BooleanSerializer());
        registerSerializer(new PrimitiveSerializer());
    }
}