/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.rest;

import org.hibernate.validator.method.MethodConstraintViolationException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class MethodConstraintViolationExceptionMapper implements ExceptionMapper<MethodConstraintViolationException> {
    public Response toResponse(MethodConstraintViolationException exception) {
        Response.ResponseBuilder responseBuilder = Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(exception.getConstraintViolations().iterator().next().getMessage());
        return responseBuilder.build();
    }
}
