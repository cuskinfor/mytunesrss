/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.vaadin;

import com.vaadin.ui.Form;

public class VaadinUtils {

    public static boolean isModified(Form... forms) {
        for (Form form : forms) {
            if (form.isModified()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValid(Form... forms) {
        for (Form form : forms) {
            if (!form.isValid()) {
                return false;
            }
        }
        return true;
    }

    public static void discard(Form... forms) {
        for (Form form : forms) {
            form.discard();
        }
    }

    public static void commit(Form... forms) {
        for (Form form : forms) {
            form.commit();
        }
    }
}
