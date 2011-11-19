/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.vaadin;

import com.vaadin.data.Property;
import com.vaadin.ui.TextField;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.MiscUtils;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Map;

public class SmartTextField extends TextField {

    public SmartTextField() {
    }

    public SmartTextField(String caption) {
        super(caption);
    }

    public SmartTextField(Property dataSource) {
        super(dataSource);
    }

    public SmartTextField(String caption, Property dataSource) {
        super(caption, dataSource);
    }

    public SmartTextField(String caption, String value) {
        super(caption, value);
    }

    public String getStringValue(String defaultValue) {
        Object o = getValue();
        if (o != null) {
            return StringUtils.defaultIfEmpty(StringUtils.trimToEmpty(o.toString()), defaultValue);
        }
        return defaultValue;
    }

    public int getIntegerValue(int defaultValue) {
        String s = getStringValue(null);
        if (StringUtils.isNotBlank(s)) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public long getLongValue(long defaultValue) {
        String s = getStringValue(null);
        if (StringUtils.isNotBlank(s)) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    @Override
    public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
        if (newValue instanceof String || newValue == null) {
            super.setValue(StringUtils.trimToEmpty((String)newValue));
        } else {
            super.setValue(newValue);
        }
    }

    public void setValue(String value, String defaultValue) {
        setValue(StringUtils.defaultIfEmpty(value, defaultValue));
    }

    public void setValue(Number value, long minValue, long maxValue, Object defaultValue) {
        if (value == null) {
            setValue(defaultValue);
        } else if (minValue <= value.longValue() && maxValue >= value.longValue()) {
            setValue(value);
        } else {
            setValue(defaultValue);
        }
    }

    public byte[] getStringHashValue(MessageDigest digest) {
        Object o = getValue();
        if (o == null) {
            return new byte[0];
        } else if (o instanceof byte[]) {
            return (byte[])o;
        } else {
            return digest.digest(MiscUtils.getUtf8Bytes(StringUtils.trimToEmpty(o.toString())));
        }
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
