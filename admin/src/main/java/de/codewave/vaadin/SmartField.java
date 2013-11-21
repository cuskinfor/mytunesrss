package de.codewave.vaadin;

import com.vaadin.data.Property;

import java.security.MessageDigest;
import java.util.Map;

public interface SmartField {
    String getStringValue(String defaultValue);

    int getIntegerValue(int defaultValue);

    long getLongValue(long defaultValue);

    void setValue(Object newValue) throws Property.ReadOnlyException, Property.ConversionException;

    void setValue(String value, String defaultValue);

    void setValue(Number value, long minValue, long maxValue, Object defaultValue);

    byte[] getStringHashValue(MessageDigest digest);

    void changeVariables(Object source, Map<String, Object> variables);
}
