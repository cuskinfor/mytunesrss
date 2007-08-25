/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.server;

/**
 * de.codewave.mytunesrss.server.CheckHealthResult
 */
public interface CheckHealthResult {
    byte OK = 0;
    byte NULL_DATA_STORE = 1;
    byte INVALID_HTTP_RESPONSE = 2;
    byte SERVER_COMMUNICATION_FAILURE = 3;
    byte EOF = 4;
}