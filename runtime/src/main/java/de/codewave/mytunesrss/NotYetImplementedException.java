/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

public class NotYetImplementedException extends UnsupportedOperationException {

    public NotYetImplementedException() {
        super("Not yet implemented: \"" + Thread.currentThread().getStackTrace()[2].getClassName() + "#" + Thread.currentThread().getStackTrace()[2].getMethodName() + "\"!");
    }

}
