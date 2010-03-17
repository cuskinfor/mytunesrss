/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.vaadin;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validatable;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Table;
import org.h2.command.CommandContainer;

import java.util.Iterator;

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

    public static boolean isChild(Component ancestor, Component child) {
        Component component = child;
        while (component.getParent() != null && component != ancestor) {
            component = component.getParent();
        }
        return component == ancestor;
    }

    public static <T> T getAncestor(Component component, Class<T> ancestorType) {
        while (component != null && !ancestorType.isAssignableFrom(component.getClass())) {
            component = component.getParent();
        }
        return (T) component;
    }

    public static int getComponentCount(ComponentContainer container, Class componentType) {
        int count = 0;
        Iterator<Component> componentIterator = container.getComponentIterator();
        while (componentIterator.hasNext()) {
            Component component = componentIterator.next();
            if (componentType == null || componentType.isAssignableFrom(component.getClass())) {
                count++;
            }
        }
        return count;
    }
}
