package de.codewave.utils;

/**
 * Unsupported operation exception which automatically fetches the name of the class and method where the
 * exception is thrown for the message.
 */
public class NotYetImplementedException extends UnsupportedOperationException {

    /**
     * Create a new exception.
     */
    public NotYetImplementedException() {
        super("Not yet implemented: \"" + Thread.currentThread().getStackTrace()[2].getClassName() + "#" + Thread.currentThread().getStackTrace()[2].getMethodName() + "\"!");
    }

}
