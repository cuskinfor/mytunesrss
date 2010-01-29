/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.datastore.MyTunesRssDataStore;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.Error;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.mytunesrss.server.MyTunesRssSessionInfo;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.mytunesrss.servlet.WebConfig;
import de.codewave.mytunesrss.task.DatabaseBuilderTask;
import de.codewave.utils.servlet.CommandHandler;
import de.codewave.utils.servlet.SessionManager;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.swing.Task;
import de.codewave.utils.swing.TaskExecutor;
import de.codewave.utils.swing.TaskFinishedListener;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.AuthenticationException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.*;

/**
 * de.codewave.mytunesrss.command.MyTunesRssCommandHandler
 */
public abstract class MyTunesRssCommandHandler extends CommandHandler {
    private static final Logger LOG = LoggerFactory.getLogger(MyTunesRssCommandHandler.class);
    private static boolean SCHEDULE_DATABASE_UPDATE;

    protected MyTunesRssConfig getMyTunesRssConfig() {
        return (MyTunesRssConfig) getSession().getServletContext().getAttribute(MyTunesRssConfig.class.getName());
    }

    protected void runDatabaseUpdate() {
        SCHEDULE_DATABASE_UPDATE = false;
        TaskExecutor.execute(new DatabaseBuilderTask(), new TaskFinishedListener() {
            public void taskFinished(Task task) {
                if (!((DatabaseBuilderTask) task).isExecuted()) {
                    SCHEDULE_DATABASE_UPDATE = true;
                }
            }
        });
    }

    protected boolean isAuthorized(String userName, String password, byte[] passwordHash) {
        return isAuthorized(userName, passwordHash) || isAuthorizedLdapUser(userName, password);
    }

    private boolean isAuthorizedLdapUser(String userName, String password) {
        return MyTunesRssUtils.loginLDAP(userName, password);
    }

    protected boolean isAuthorized(String userName, byte[] passwordHash) {
        return isAuthorizedLocalUsers(userName, passwordHash);
    }

    private boolean isAuthorizedLocalUsers(String userName, byte[] passwordHash) {
        LOG.debug("Checking authorization with local users.");
        MyTunesRssConfig config = getMyTunesRssConfig();
        User user = config.getUser(userName);
        return user != null && Arrays.equals(user.getPasswordHash(), passwordHash) && user.isActive();
    }

    protected void authorize(WebAppScope scope, String userName) {
        User user = getMyTunesRssConfig().getUser(userName);
        if (scope == WebAppScope.Request) {
            LOG.debug("Authorizing request for user \"" + userName + "\".");
            getRequest().setAttribute("authUser", user);
            getRequest().setAttribute("auth", createAuthToken(user));
        } else if (scope == WebAppScope.Session) {
            LOG.debug("Authorizing session for user \"" + userName + "\".");
            getSession().setAttribute("authUser", user);
            getSession().setAttribute("auth", createAuthToken(user));
        }
        if (getAuthUser() != null && StringUtils.isNotEmpty(getAuthUser().getWebSettings())) {
            getWebConfig().clearWithDefaults(getRequest());
            getWebConfig().load(getAuthUser());
            getWebConfig().load(getRequest());
        }
        ((MyTunesRssSessionInfo) SessionManager.getSessionInfo(getRequest())).setUser(user);
        getSession().setMaxInactiveInterval(user.getSessionTimeout() * 60);
    }

    protected String createAuthToken(User user) {
        return MyTunesRssWebUtils.encryptPathInfo(getRequest(),
                    "auth=" + MyTunesRssBase64Utils.encode(user.getName()) + "%20" +
                        MyTunesRssBase64Utils.encode(user.getPasswordHash()));
    }

    protected User getAuthUser() {
        return MyTunesRssWebUtils.getAuthUser(getRequest());
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
            } catch (NumberFormatException e) {// intentionally left blank
            }
        }
        return false;
    }

    protected boolean isSessionAuthorized() {
        if (getSession().getAttribute("auth") != null) {
            User user = (User) getSession().getAttribute("authUser");
            if (user.isActive() && getMyTunesRssConfig().getUser(user.getName()) != null) {
                return true;
            }
        }
        return false;
    }

    protected void addError(Error error) {
        MyTunesRssWebUtils.addError(getRequest(), error, "errors");
    }

    protected boolean isError() {
        return MyTunesRssWebUtils.isError(getRequest(), "errors");
    }

    protected void addMessage(Error message) {
        MyTunesRssWebUtils.addError(getRequest(), message, "messages");
    }

    protected void createParameterModel(String... parameterNames) {
        MyTunesRssWebUtils.createParameterModel(getRequest(), parameterNames);
    }

    protected DataStoreSession getTransaction() {
        return TransactionFilter.getTransaction();
    }

    protected void forward(MyTunesRssResource resource) throws IOException, ServletException {
        prepareRequestForResource();
        resource.beforeForward(getRequest(), getResponse());
        if (MyTunesRssWebUtils.getUserAgent(getRequest()) == UserAgent.Psp) {
            getResponse().setHeader("Cache-Control", "no-cache");
            getResponse().setHeader("Pragma", "no-cache");
            getResponse().setDateHeader("Expires", 0);
        }
        getRequest().setAttribute("userAgent", MyTunesRssWebUtils.getUserAgent(getRequest()).name());
        forward(resource.getValue());
    }

    private void prepareRequestForResource() {
        String myTunesRssComUsername = (String) getSession().getAttribute(WebConfig.MYTUNESRSS_COM_USER);
        String servletUrl = MyTunesRssWebUtils.getServletUrl(getRequest());
        getRequest().setAttribute("servletUrl", servletUrl);
        WebConfig webConfig = getWebConfig();
        if (StringUtils.isEmpty(myTunesRssComUsername) || !webConfig.isMyTunesRssComAddress()) {
            getRequest().setAttribute("permServletUrl", servletUrl);
            getRequest().setAttribute("downloadPlaybackServletUrl", servletUrl);
        } else {
            String appUrl = MyTunesRssWebUtils.getApplicationUrl(getRequest());
            String url =
                    MyTunesRss.MYTUNESRSSCOM_URL + "/" + myTunesRssComUsername + getRequest().getContextPath() + servletUrl.substring(appUrl.length());
            getRequest().setAttribute("permServletUrl", url);
            getRequest().setAttribute("downloadPlaybackServletUrl", url);
        }
        getRequest().setAttribute("permFeedServletUrl", getRequest().getAttribute("permServletUrl"));
        getRequest().setAttribute("appUrl", MyTunesRssWebUtils.getApplicationUrl(getRequest()));
        //getRequest().setAttribute("absoluteAppUrl", ServletUtils.getApplicationUrl(getRequest()));
        getRequest().setAttribute("mytunesrssVersion", MyTunesRss.VERSION);
        getRequest().setAttribute("sessionCreationTime", getSession().getCreationTime());
        if (getAuthUser() != null && getAuthUser().isQuotaExceeded()) {
            addError(new BundleError("error.quotaExceeded." + getAuthUser().getQuotaType().name()));
        }
        getRequest().setAttribute("encryptionKey", MyTunesRss.CONFIG.getPathInfoKey());
        getRequest().setAttribute("globalConfig", MyTunesRss.CONFIG);
        setResourceBundle();
        if (DatabaseBuilderTask.isRunning()) {
            if (DatabaseBuilderTask.getState() == DatabaseBuilderTask.State.UpdatingTracksFromFolder ||
                    DatabaseBuilderTask.getState() == DatabaseBuilderTask.State.UpdatingTracksFromItunes) {
                addMessage(new BundleError("info.databaseUpdating"));
            } else {
                addMessage(new BundleError("info.databaseUpdatingImages"));
            }
        }
        getRequest().setAttribute("msgUnknown", getBundleString("unknown"));
    }

    private void setResourceBundle() {
        Locale locale = StringUtils.isNotBlank(getWebConfig().getLanguage()) ? new Locale(getWebConfig().getLanguage()) : getRequest().getLocale();
        LocalizationContext context = (LocalizationContext) getSession().getAttribute(Config.FMT_LOCALIZATION_CONTEXT + ".session");
        if (context == null || !ObjectUtils.equals(context.getLocale(), locale)) {
            File language = AddonsUtils.getBestLanguageFile(locale);
            ResourceBundle bundle = retrieveBundle(language, locale);
            if (bundle != null) {
                getSession().setAttribute(Config.FMT_LOCALIZATION_CONTEXT + ".session", new LocalizationContext(bundle, bundle.getLocale()));
            } else {
                getSession().removeAttribute(Config.FMT_LOCALIZATION_CONTEXT + ".session");
            }
        }
    }

    private ResourceBundle retrieveBundle(File language, Locale locale) {
        ResourceBundle bundle = (ResourceBundle) getSession().getServletContext().getAttribute("LanguageBundle." + locale.toString());
        if (bundle == null) {
            if (language != null) {
                try {
                    bundle = new PropertyResourceBundle(new FileInputStream(language));
                } catch (IOException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not read language file \"" + language.getAbsolutePath() + "\".");
                    }
                }
            }
            if (bundle == null) {
                try {
                    bundle = new PropertyResourceBundle(getBestLanguageResource(locale).openStream());
                } catch (IOException e1) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not get one of the default resource bundles.", e1);
                    }
                }
            }
            if (bundle != null) {
                getSession().getServletContext().setAttribute("LanguageBundle." + locale.toString(), bundle);
            }
        }
        return bundle;
    }

    private URL getBestLanguageResource(Locale locale) {
        String[] codes = locale.toString().split("_");
        List<URL> resources = new ArrayList<URL>();
        if (codes.length == 3) {
            resources.add(getClass().getResource("../MyTunesRssWeb_" + codes[0] + "_" + codes[1] + "_" + codes[2] + ".properties"));
        }
        if (codes.length >= 2) {
            resources.add(getClass().getResource("../MyTunesRssWeb_" + codes[0] + "_" + codes[1] + ".properties"));
        }
        resources.add(getClass().getResource("../MyTunesRssWeb_" + codes[0] + ".properties"));
        resources.add(getClass().getResource("../MyTunesRssWeb.properties"));
        for (URL resource : resources) {
            if (resource != null) {
                return resource;
            }
        }
        return null;
    }

    protected WebConfig getWebConfig() {
        return MyTunesRssWebUtils.getWebConfig(getRequest());
    }

    protected void forward(MyTunesRssCommand command) throws IOException, ServletException {//prepareRequestForResource();
        forward("/mytunesrss/" + command.getName());
    }

    protected void redirect(String url) throws IOException {
        getResponse().sendRedirect(url.replace("&amp;", "&"));
    }

    protected Map<String, Boolean> getStates() {
        Map<String, Boolean> states = (Map<String, Boolean>) getSession().getAttribute("states");
        if (states == null) {
            synchronized (getSession()) {
                states = (Map<String, Boolean>) getSession().getAttribute("states");
                if (states == null) {
                    states = new HashMap<String, Boolean>();
                    getSession().setAttribute("states", states);
                }
            }
        }
        return states;
    }

    public void execute() throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Command handler \"" + this.getClass().getName() + "\" called (\"" + getRequest().getScheme() + "://" +
                    getRequest().getServerName() + ":" + getRequest().getServerPort() + getRequest().getRequestURI() + "\").");
            LOG.debug("Request parameters:");
            for (Map.Entry entry : (Iterable<? extends Map.Entry>) getRequest().getParameterMap().entrySet()) {
                if (StringUtils.equals(entry.getKey().toString(), "password")) {
                    LOG.debug("\"password\"=\"********\"");
                } else {
                    StringBuilder msg = new StringBuilder("\"").append(entry.getKey()).append("\"=");
                    for (String value : (String[]) entry.getValue()) {
                        msg.append("\"").append(value).append("\",");
                    }
                    LOG.debug(msg.substring(0, msg.length() - 1));
                }
            }
        }
        if (SCHEDULE_DATABASE_UPDATE) {
            runDatabaseUpdate();
        }
        try {
            if (!isSessionAuthorized() && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAutoLogin())) {
                authorize(WebAppScope.Session, MyTunesRss.CONFIG.getAutoLogin());
            } else if (!MyTunesRss.CONFIG.isDisableWebLogin() && !isSessionAuthorized() && getWebConfig().isLoginStored() && isAuthorized(getWebConfig().getUserName(),
                    getWebConfig().getPasswordHash())) {
                authorize(WebAppScope.Session, getWebConfig().getUserName());
            }
            if (!isRequestAuthorized()) {
                forward(MyTunesRssResource.Login);
            } else {
                handleDisplayFilter();
                if (isSessionAuthorized()) {
                    MyTunesRssRemoteEnv.addOrTouchSessionForRegularSession(getRequest(), getAuthUser());
                }
                executeAuthorized();
            }
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Unhandled exception: ", e);
            }
            getSession().removeAttribute("errors");
            redirect(MyTunesRssWebUtils.getCommandCall(getRequest(), MyTunesRssCommand.ShowFatalError));
        }
    }

    protected MyTunesRssDataStore getDataStore() {
        return ((MyTunesRssDataStore) getContext().getAttribute(MyTunesRssDataStore.class.getName()));
    }

    private void handleDisplayFilter() {
        DisplayFilter filter = getDisplayFilter();
        if (getRequest().getParameter("filterText") != null) {
            filter.setTextFilter(getRequest().getParameter("filterText"));
        }
        if (getRequest().getParameter("filterType") != null) {
            if (StringUtils.isBlank(getRequest().getParameter("filterMediaType"))) {
                filter.setMediaType(null);
            } else {
                filter.setMediaType(MediaType.valueOf(getRequest().getParameter("filterMediaType")));
            }
        }
        if (getRequest().getParameter("filterProtected") != null) {
            if (StringUtils.isBlank(getRequest().getParameter("filterProtected"))) {
                filter.setProtection(null);
            } else {
                filter.setProtection(DisplayFilter.Protection.valueOf(getRequest().getParameter("filterProtected")));
            }
        }
        if (getRequest().getParameter("filterMinYear") != null) {
            filter.setMinYear(getIntegerRequestParameter("filterMinYear", -1));
        }
        if (getRequest().getParameter("filterMaxYear") != null) {
            filter.setMaxYear(getIntegerRequestParameter("filterMaxYear", -1));
        }
    }

    protected DisplayFilter getDisplayFilter() {
        DisplayFilter filter = (DisplayFilter) getSession().getAttribute("displayFilter");
        if (filter == null) {
            filter = new DisplayFilter();
            getSession().setAttribute("displayFilter", filter);
        }
        return filter;
    }

    public void executeAuthorized() throws Exception {// intentionally left blank
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
        LocalizationContext context = (LocalizationContext) getRequest().getSession().getAttribute(Config.FMT_LOCALIZATION_CONTEXT + ".session");
        ResourceBundle bundle = context != null ? context.getResourceBundle() : ResourceBundle.getBundle("de/codewave/mytunesrss/MyTunesRssWeb",
                getRequest().getLocale());
        return bundle.getString(key);
    }

    protected String getBundleString(String key, Object... params) {
        return MessageFormat.format(getBundleString(key), params);
    }

    protected void restartMyTunesRssCom() throws IOException {
        String url = MyTunesRss.MYTUNESRSSCOM_TOOLS_URL + "/redirect.php?username=" + getSession().getAttribute(WebConfig.MYTUNESRSS_COM_USER) +
                "&cookie=" + URLEncoder.encode(getWebConfig().createCookieValue(), "UTF-8");
        LOG.debug("Restarting mytunesrss.com after saving web settings: \"" + url + "\".");
        redirect(url);
    }

    protected int getValidIndex(int index, int pageSize, int listSize) {
        if (index * pageSize > listSize) {
            return (listSize - 1 / pageSize);
        }
        return index;
    }
}