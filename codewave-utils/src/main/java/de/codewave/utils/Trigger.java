/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.utils;

/**
 * de.codewave.utils.Trigger
 */
public class Trigger {
    private boolean myTriggered;

    public boolean isTriggered() {
        return myTriggered;
    }

    public void trigger() {
        myTriggered = true;
    }
}