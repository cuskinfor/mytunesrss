/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

/**
 * de.codewave.mytunesrss.CheckHealthResult
 */
public interface CheckHealthResult {
    byte OK = 0;
    byte EMPTY_LIBRARY = 1;
    byte INVALID_HTTP_RESPONSE = 2;
    byte SERVER_COMMUNICATION_FAILURE = 3;
    byte EOF = 4;
}