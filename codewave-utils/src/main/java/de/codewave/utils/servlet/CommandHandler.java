package de.codewave.utils.servlet;

import org.apache.commons.lang3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

/**
 * Handler for servlet commands. Derived classes must implement the {@link #execute()} method and put the command functionality there. The class has
 * some convenience methods.
 */
public abstract class CommandHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);

    /**
     * The thread local servlet request.
     */
    private ThreadLocal<HttpServletRequest> myServletRequest = new ThreadLocal<HttpServletRequest>();

    /**
     * The thread local servlet response.
     */
    private ThreadLocal<HttpServletResponse> myServletResponse = new ThreadLocal<HttpServletResponse>();

    /**
     * Execute a command with the specified servlet request and response. This method sets the thread local request and response and calls the {@link
     * #execute()} method. When the {@link #execute()} method finishes, the thread local request and response are removed.
     *
     * @param servletRequest  A servlet request.
     * @param servletResponse A servlet response.
     *
     * @throws IOException
     * @throws ServletException
     */
    void execute(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException, ServletException {
        myServletRequest.set(servletRequest);
        myServletResponse.set(servletResponse);
        try {
            execute();
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Exception in command handler.", e);
            }
            throw new ServletException("Exception in command handler.", e);
        } finally {
            myServletRequest.remove();
            myServletResponse.remove();
        }
    }

    /**
     * Get the current servlet request,
     *
     * @return The current servlet request.
     */
    protected HttpServletRequest getRequest() {
        return myServletRequest.get();
    }


    /**
     * Get the current servlet response.
     *
     * @return The current servlet response.
     */
    protected HttpServletResponse getResponse() {
        return myServletResponse.get();
    }

    /**
     * Get the current session.
     *
     * @return The current session.
     */
    protected HttpSession getSession() {
        return getRequest().getSession();
    }

    /**
     * Get the servlet context.
     *
     * @return The servlet context.
     */
    protected ServletContext getContext() {
        return getSession().getServletContext();
    }

    /**
     * Forward the request to the specified resource.
     *
     * @param resource A resource.
     *
     * @throws IOException
     * @throws ServletException
     */
    protected void forward(String resource) throws IOException, ServletException {
        getRequest().getRequestDispatcher(resource).forward(getRequest(), getResponse());
    }

    /**
     * Include the specified resource in the request.
     *
     * @param resource A resource.
     *
     * @throws IOException
     * @throws ServletException
     */
    protected void include(String resource) throws IOException, ServletException {
        getRequest().getRequestDispatcher(resource).include(getRequest(), getResponse());
    }

    /**
     * Get a request parameter's value or a default value if the parameter is not specified in the request or its value is an empty string.
     *
     * @param name         The name of the request parameter.
     * @param defaultValue The default value.
     *
     * @return The request parameter value or the default value.
     */
    protected String getRequestParameter(String name, String defaultValue) {
        String value = getRequest().getParameter(name);
        return StringUtils.isNotEmpty(value) ? value : defaultValue;
    }

    /**
     * Get a request parameter's integer value or a default value if the parameter is not specified in the request or its value cannot be converted to
     * an integer.
     *
     * @param name         The name of the request parameter.
     * @param defaultValue The default value.
     *
     * @return The request parameter value or the default value.
     */
    protected int getSafeIntegerRequestParameter(String name, int defaultValue) {
        String value = getRequest().getParameter(name);
        try {
            return StringUtils.isNotEmpty(value) ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Get a request parameter's integer value or a default value if the parameter is not specified in the request.
     *
     * @param name         The name of the request parameter.
     * @param defaultValue The default value.
     *
     * @return The request parameter value or the default value.
     *
     * @throws NumberFormatException if the request parameter value is not empty but cannot be converted to an integer value.
     */
    protected int getIntegerRequestParameter(String name, int defaultValue) {
        String value = getRequest().getParameter(name);
        return StringUtils.isNotEmpty(value) ? Integer.parseInt(value) : defaultValue;
    }

    /**
     * Get a request parameter's boolean value or a default value if the parameter is not specified in the request.
     *
     * @param name         The name of the request parameter.
     * @param defaultValue The default value.
     *
     * @return The request parameter value or the default value.
     */
    protected boolean getBooleanRequestParameter(String name, boolean defaultValue) {
        String value = getRequest().getParameter(name);
        return StringUtils.isNotEmpty(value) ? Boolean.valueOf(value) : defaultValue;
    }

    /**
     * Get all non-empty request parameters with the specified name.
     *
     * @param name A name.
     *
     * @return An array of non-empty request parameters with the specified name.
     */
    protected String[] getNonEmptyParameterValues(String name) {
        String[] values = getRequest().getParameterValues(name);
        if (values != null && values.length > 0) {
            List<String> nonEmptyValues = new ArrayList<String>();
            for (String value : values) {
                if (StringUtils.isNotEmpty(value)) {
                    nonEmptyValues.add(value);
                }
            }
            return nonEmptyValues.toArray(new String[nonEmptyValues.size()]);
        }
        return null;
    }

    /**
     * Check if there is a request parameter with the specified value.
     *
     * @param key   The name of the request parameter.
     * @param value A value.
     *
     * @return <code>true</code> if the request parameter has the specified value or <code>false</code> otherwise.
     */
    protected boolean isParameterValue(String key, String value) {
        String[] values = getNonEmptyParameterValues(key);
        if (values != null) {
            for (String parameterValue : values) {
                if (parameterValue.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if there is a request parameter with the specified value. Ignore case when checking the value.
     *
     * @param key   The name of the request parameter.
     * @param value A value.
     *
     * @return <code>true</code> if the request parameter has the specified value or <code>false</code> otherwise.
     */
    protected boolean isParameterValueIgnoreCase(String key, String value) {
        for (String parameterValue : getNonEmptyParameterValues(key)) {
            if (parameterValue.equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Action method of the command handler. Implement this method in your subclass.
     *
     * @throws Exception
     */
    public abstract void execute() throws Exception;
}
