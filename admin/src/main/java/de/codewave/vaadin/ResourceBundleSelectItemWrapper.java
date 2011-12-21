/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.vaadin;

import java.util.ResourceBundle;

public class ResourceBundleSelectItemWrapper<T> {

    private T myItem;
    private ResourceBundle myBundle;

    public ResourceBundleSelectItemWrapper(T item, ResourceBundle bundle) {
        myItem = item;
        myBundle = bundle;
    }

    public T getItem() {
        return myItem;
    }

    @Override
    public String toString() {
        return myBundle.getString(myItem.toString());
    }
}
