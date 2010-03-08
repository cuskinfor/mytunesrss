/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.vaadin;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validatable;
import com.vaadin.ui.Table;

public class VaadinUtils {

    public static void validate(Validatable... validatables) {
        for (Validatable validatable : validatables) {
            if (validatable instanceof Table) {
                Table table = (Table) validatable;
                for (Object itemId : table.getItemIds()) {
                    Item item = table.getItem(itemId);
                    for (Object itemPropertyId : item.getItemPropertyIds()) {
                        Property itemProperty = item.getItemProperty(itemPropertyId);
                        if (itemProperty.getValue() instanceof Validatable) {
                            ((Validatable) itemProperty.getValue()).validate();
                        }
                    }
                }
            } else {
                validatable.validate();
            }
        }
    }

    public static boolean isValid(Validatable... validatables) {
        for (Validatable validatable : validatables) {
            if (validatable instanceof Table) {
                Table table = (Table) validatable;
                for (Object itemId : table.getItemIds()) {
                    Item item = table.getItem(itemId);
                    for (Object itemPropertyId : item.getItemPropertyIds()) {
                        Property itemProperty = item.getItemProperty(itemPropertyId);
                        if (itemProperty.getValue() instanceof Validatable && !(((Validatable) itemProperty.getValue()).isValid())) {
                            return false;
                        }
                    }
                }
            } else if (!validatable.isValid()) {
                return false;
            }
        }
        return true;
    }
}
