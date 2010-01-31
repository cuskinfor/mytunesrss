/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

public class LuceneQueryParserException extends Exception {
    public LuceneQueryParserException() {
    }

    public LuceneQueryParserException(String message) {
        super(message);
    }

    public LuceneQueryParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public LuceneQueryParserException(Throwable cause) {
        super(cause);
    }
}
