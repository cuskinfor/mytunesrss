/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

public class RegistrationFeedback {
    private String myMessage;
    private boolean myValid;

    public RegistrationFeedback(String message, boolean valid) {
        myMessage = message;
        myValid = valid;
    }

    public String getMessage() {
        return myMessage;
    }

    public boolean isValid() {
        return myValid;
    }
}
