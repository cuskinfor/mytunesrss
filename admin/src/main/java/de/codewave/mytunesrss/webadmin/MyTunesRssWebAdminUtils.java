/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.terminal.UserError;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.Window;
import de.codewave.vaadin.ComponentFactory;

import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class MyTunesRssWebAdminUtils {

    public static ResourceBundle BUNDLE = PropertyResourceBundle.getBundle("de.codewave.mytunesrss.webadmin.MyTunesRssAdmin");

    public static ComponentFactory COMPONENT_FACTORY = new ComponentFactory(BUNDLE);

    public static String getBundleString(String key, Object... parameters) {
        if (parameters == null || parameters.length == 0) {
            return BUNDLE.getString(key);
        }
        return MessageFormat.format(BUNDLE.getString(key), parameters);
    }

    public static void setError(AbstractComponent component, String messageKey, Object... parameters) {
        if (messageKey == null) {
            component.setComponentError(null);
        } else {
            component.setComponentError(new UserError(getBundleString(messageKey, parameters)));
        }
    }

    public static void setRequired(Field field) {
        COMPONENT_FACTORY.setRequired(field, "error.requiredField");
    }

    public static void setOptional(Field field) {
        COMPONENT_FACTORY.setOptional(field);
    }
}
