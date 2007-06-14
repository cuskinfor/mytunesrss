/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.*;
import de.codewave.mytunesrss.jsp.*;
import de.codewave.mytunesrss.jsp.Error;
import de.codewave.mytunesrss.server.*;
import de.codewave.mytunesrss.servlet.*;
import de.codewave.mytunesrss.task.*;
import de.codewave.utils.servlet.*;
import de.codewave.utils.swing.*;
import org.apache.commons.lang.*;
import org.apache.commons.logging.*;

import javax.servlet.*;
import java.io.*;
import java.util.*;
import java.net.*;

/**
 * de.codewave.mytunesrss.command.MyTunesRssCommandHandler
 */
public abstract class MyTunesRssCommandHandler extends CommandHandler {
    private static final Log LOG = LogFactory.getLog(MyTunesRssCommandHandler.class);
    private static boolean SCHEDULE_DATABASE_UPDATE;

    protected MyTunesRssConfig getMyTunesRssConfig() {
        return (MyTunesRssConfig)getSession().getServletContext().getAttribute(MyTunesRssConfig.class.getName());
    }

    protected void runDatabaseUpdate() {
        SCHEDULE_DATABASE_UPDATE = false;
        TaskExecutor.execute(MyTunesRss.createDatabaseBuilderTask(), new TaskFinishedListener() {
            public void taskFinished(Task task) {
                if (!((DatabaseBuilderTask)task).isExecuted()) {
                    SCHEDULE_DATABASE_UPDATE = true;
                }
            }
        });
    }

    protected boolean isAuthorized(String userName, byte[] passwordHash) {
        MyTunesRssConfig config = getMyTunesRssConfig();
        User user = config.getUser(userName);
        return user != null && Arrays.equals(user.getPasswordHash(), passwordHash) && user.isActive();
    }

    protected void authorize(WebAppScope scope, String userName) {
        User user = getMyTunesRssConfig().getUser(userName);
        if (scope == WebAppScope.Request) {
            getRequest().setAttribute("auth", MyTunesRssWebUtils.encryptPathInfo(
                    "auth=" + MyTunesRssBase64Utils.encode(user.getName()) + " " + MyTunesRssBase64Utils.encode(user.getPasswordHash())));
            getRequest().setAttribute("authUser", user);
        } else if (scope == WebAppScope.Session) {
            getSession().setAttribute("auth", MyTunesRssWebUtils.encryptPathInfo(
                    "auth=" + MyTunesRssBase64Utils.encode(user.getName()) + " " + MyTunesRssBase64Utils.encode(user.getPasswordHash())));
            getSession().setAttribute("authUser", user);
        } else {
        }
        ((MyTunesRssSessionInfo)SessionManager.getSessionInfo(getRequest())).setUser(user);
        getSession().setMaxInactiveInterval(user.getSessionTimeout() * 60);
    }

    protected User getAuthUser() {
        User user = (User)getSession().getAttribute("authUser");
        if (user == null) {
            user = (User)getRequest().getAttribute("authUser");
        }
        return user;
    }

    protected boolean isRequestAuthorized() {
        if (isSessionAuthorized()) {
            return true;
        }
        if (StringUtils.isNotEmpty(getRequest().getParameter("auth"))) {
            try {
                String auth = getRequestParameter("auth", "");
                int i = auth.indexOf(" ");
                if (i >= 1 && i < auth.length() - 1) {
                    byte[] requestAuthHash = MyTunesRssBase64Utils.decode(auth.substring(i + 1));
                    String userName = MyTunesRssBase64Utils.decodeToString(auth.substring(0, i));
                    if (isAuthorized(userName, requestAuthHash)) {
                        authorize(WebAppScope.Request, userName);
                        return true;
                    }
                }
            } catch (NumberFormatException e) {
                // intentionally left blank
            }
        }
        return false;
    }

    protected boolean isSessionAuthorized() {
        if (getSession().getAttribute("auth") != null) {
            User user = (User)getSession().getAttribute("authUser");
            if (user.isActive() && getMyTunesRssConfig().getUser(user.getName()) != null) {
                return true;
            }
        }
        return false;
    }

    protected void addError(Error error) {
        List<Error> errors = (List<Error>)getSession().getAttribute("errors");
        if (errors == null) {
            synchronized (getSession()) {
                errors = (List<Error>)getSession().getAttribute("errors");
                if (errors == null) {
                    errors = new ArrayList<Error>();
                    getSession().setAttribute("errors", errors);
                }
            }
        }
        errors.add(error);
    }

    protected MyTunesRssDataStore getDataStore() {
        return (MyTunesRssDataStore)getContext().getAttribute(MyTunesRssDataStore.class.getName());
    }

    protected void forward(MyTunesRssResource resource) throws IOException, ServletException {
        prepareRequestForResource();
        resource.beforeForward(getRequest(), getResponse());
        if (isUserAgentPSP()) {
            getResponse().setHeader("Cache-Control", "no-cache");
            getResponse().setHeader("Pragma", "no-cache");
            getResponse().setDateHeader("Expires", 0);
        }
        forward(resource.getValue());
    }

    protected boolean isUserAgentPSP() {
        String userAgent = getRequest().getHeader("User-Agent");
        return StringUtils.isNotEmpty(userAgent) && userAgent.contains("PSP");
    }

    private void prepareRequestForResource() {
        String myTunesRssComUsername = (String)getSession().getAttribute(WebConfig.MYTUNESRSS_COM_USER);
        String servletUrl = MyTunesRssWebUtils.getServletUrl(getRequest());
        getRequest().setAttribute("servletUrl", servletUrl);
        if (StringUtils.isEmpty(myTunesRssComUsername)) {
            getRequest().setAttribute("permServletUrl", servletUrl);
        } else {
            String appUrl = ServletUtils.getApplicationUrl(getRequest());
            getRequest().setAttribute("permServletUrl", MyTunesRss.MYTUNESRSSCOM_URL + "/" + myTunesRssComUsername + getRequest().getContextPath() + servletUrl.substring(appUrl.length()));
        }
        getRequest().setAttribute("appUrl", ServletUtils.getApplicationUrl(getRequest()));
        getRequest().setAttribute("mytunesrssVersion", MyTunesRss.VERSION);
        getRequest().setAttribute("sessionCreationTime", getSession().getCreationTime());
        getRequest().setAttribute("registered", MyTunesRss.REGISTRATION.isRegistered());
        getWebConfig();// result not needed, method also fills the session attribute "config"
        if (getAuthUser() != null && getAuthUser().isQuotaExceeded()) {
            addError(new BundleError("error.quotaExceeded." + getAuthUser().getQuotaType().name()));
        }
        getRequest().setAttribute("encryptionKey", MyTunesRss.CONFIG.getPathInfoKey());
    }

    protected WebConfig getWebConfig() {
        return MyTunesRssWebUtils.getWebConfig(getRequest());
    }

    protected void forward(MyTunesRssCommand command) throws IOException, ServletException {
        prepareRequestForResource();
        forward("/mytunesrss/" + command.getName());
    }

    protected void redirect(String url) throws IOException {
        getResponse().sendRedirect(url.replace("&amp;", "&"));
    }

    protected Map<String, Boolean> getStates() {
        Map<String, Boolean> states = (Map<String, Boolean>)getSession().getAttribute("states");
        if (states == null) {
            synchronized (getSession()) {
                states = (Map<String, Boolean>)getSession().getAttribute("states");
                if (states == null) {
                    states = new HashMap<String, Boolean>();
                    getSession().setAttribute("states", states);
                }
            }
        }
        return states;
    }

    public void execute() throws Exception {
        if (SCHEDULE_DATABASE_UPDATE) {
            runDatabaseUpdate();
        }
        if (!MyTunesRss.createDatabaseBuilderTask().isRunning()) {
            try {
                if (!isSessionAuthorized() && getWebConfig().isLoginStored() && isAuthorized(getWebConfig().getUserName(),
                                                                                             getWebConfig().getPasswordHash())) {
                    authorize(WebAppScope.Session, getWebConfig().getUserName());
                }
                if (!isRequestAuthorized()) {
                    forward(MyTunesRssResource.Login);
                } else {
                    executeAuthorized();
                }
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Unhandled exception: ", e);
                }
                getSession().removeAttribute("errors");
                redirect(ServletUtils.getApplicationUrl(getRequest()) + "/mytunesrss" + "/" + MyTunesRssCommand.ShowFatalError.getName());
            }
        } else {
            forward(MyTunesRssResource.DatabaseUpdating);
        }
    }

    public void executeAuthorized() throws Exception {
        // intentionally left blank
    }

    protected Pager createPager(int itemCount, int current) {
        int pageSize = getWebConfig().getEffectivePageSize();
        if (pageSize > 0) {
            List<Pager.Page> pages = new ArrayList<Pager.Page>();
            int page = 0;
            for (int index = 0; index < itemCount; index += pageSize) {
                pages.add(new Pager.Page(Integer.toString(page), Integer.toString(page + 1)));
                page++;
            }
            Pager pager = new Pager(pages, 10);
            pager.moveToPage(current);
            return pager;
        }
        return null;
    }

    protected String getBundleString(String key) {
        ResourceBundle bundle = ResourceBundle.getBundle("de/codewave/mytunesrss/MyTunesRssWeb", getRequest().getLocale());
        return bundle.getString(key);
    }

    protected void restartMyTunesRssCom() throws IOException {
        redirect(MyTunesRss.MYTUNESRSSCOM_TOOLS_URL + "/redirect.php?username=" + getSession().getAttribute(WebConfig.MYTUNESRSS_COM_USER) + "&cookie=" + URLEncoder.encode(getWebConfig().getCookieValue(), "UTF-8"));
    }
}