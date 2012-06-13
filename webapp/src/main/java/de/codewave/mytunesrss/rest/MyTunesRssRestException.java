package de.codewave.mytunesrss.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class MyTunesRssRestException extends WebApplicationException {

    public MyTunesRssRestException(int statusCode, String statusText) {
        super(Response.status(statusCode).entity(statusText).build());
    }

}
