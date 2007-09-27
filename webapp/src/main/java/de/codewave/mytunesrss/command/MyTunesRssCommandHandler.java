/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.*;
import de.codewave.mytunesrss.datastore.statement.*;
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
import javax.servlet.jsp.jstl.core.*;
import javax.servlet.jsp.jstl.fmt.*;
import java.io.*;
import java.net.*;
import java.util.*;

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
        MyTunesRssWebUtils.addError(getRequest(), error, "errors");
    }

    protected void addMessage(Error message) {
        MyTunesRssWebUtils.addError(getRequest(), message, "messages");
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
            getRequest().setAttribute("permServletUrl",
                                      MyTunesRss.MYTUNESRSSCOM_URL + "/" + myTunesRssComUsername + getRequest().getContextPath() +
                                              servletUrl.substring(appUrl.length()));
        }
        getRequest().setAttribute("appUrl", ServletUtils.getApplicationUrl(getRequest()));
        getRequest().setAttribute("mytunesrssVersion", MyTunesRss.VERSION);
        getRequest().setAttribute("sessionCreationTime", getSession().getCreationTime());
        getRequest().setAttribute("registered", MyTunesRss.REGISTRATION.isRegistered());
        WebConfig webConfig = getWebConfig();
        if (getAuthUser() != null && getAuthUser().isQuotaExceeded()) {
            addError(new BundleError("error.quotaExceeded." + getAuthUser().getQuotaType().name()));
        }
        getRequest().setAttribute("encryptionKey", MyTunesRss.CONFIG.getPathInfoKey());
        getRequest().setAttribute("globalConfig", MyTunesRss.CONFIG);
        setResourceBundle();
        if (MyTunesRss.REGISTRATION.isRegistered() && webConfig.isValidTranscoder()) {
            getRequest().setAttribute("tc",
                                      webConfig.getLameTargetBitrate() + "," + webConfig.getLameTargetSampleRate() + "," +
                                              webConfig.isTranscodeOnTheFlyIfPossible());
            if (webConfig.isFaad2()) {
                addSuffixReplacement("m4a", "mp3");
            }
        }
        if (DatabaseBuilderTask.isRunning()) {
            if (DatabaseBuilderTask.getState() == DatabaseBuilderTask.State.UpdatingTracksFromFolder ||
                    DatabaseBuilderTask.getState() == DatabaseBuilderTask.State.UpdatingTracksFromItunes) {
                addMessage(new BundleError("info.databaseUpdating"));
            } else {
                addMessage(new BundleError("info.databaseUpdatingImages"));
            }
        }
    }

    private void addSuffixReplacement(String suffix, String replacement) {
        Map<String, String> replacements = (Map<String, String>)getRequest().getAttribute("suffixReplacements");
        if (replacements == null) {
            replacements = new HashMap<String, String>();
            getRequest().setAttribute("suffixReplacements", replacements);
        }
        replacements.put(suffix, replacement);
    }

    private void setResourceBundle() {
        Locale locale = getRequest().getLocale();
        LocalizationContext context = (LocalizationContext)getSession().getAttribute(Config.FMT_LOCALIZATION_CONTEXT + ".session");
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
        ResourceBundle bundle = (ResourceBundle)getSession().getServletContext().getAttribute("LanguageBundle." + locale.toString());
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

    protected void forward(MyTunesRssCommand command) throws IOException, ServletException {
        //prepareRequestForResource();
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
        try {
            if (!isSessionAuthorized() && getWebConfig().isLoginStored() && isAuthorized(getWebConfig().getUserName(),
                                                                                         getWebConfig().getPasswordHash())) {
                authorize(WebAppScope.Session, getWebConfig().getUserName());
            }
            if (!isRequestAuthorized()) {
                forward(MyTunesRssResource.Login);
            } else {
                handleDisplayFilter();
                executeAuthorized();
            }
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Unhandled exception: ", e);
            }
            getSession().removeAttribute("errors");
            redirect(ServletUtils.getApplicationUrl(getRequest()) + "/mytunesrss" + "/" + MyTunesRssCommand.ShowFatalError.getName());
        }
    }

    private void handleDisplayFilter() {
        Map<String, String> filter = (Map<String, String>)getSession().getAttribute("displayFilter");
        if (filter == null) {
            filter = new HashMap<String, String>();
            getSession().setAttribute("displayFilter", filter);
        }
        if (getRequest().getParameter("filterText") != null) {
            filter.put("text", getRequest().getParameter("filterText"));
        }
        if (getRequest().getParameter("filterType") != null) {
            filter.put("type", getRequest().getParameter("filterType"));
        }
        if (getRequest().getParameter("filterProtected") != null) {
            filter.put("protected", getRequest().getParameter("filterProtected"));
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
        redirect(MyTunesRss.MYTUNESRSSCOM_TOOLS_URL + "/redirect.php?username=" + getSession().getAttribute(WebConfig.MYTUNESRSS_COM_USER) +
                "&cookie=" + URLEncoder.encode(getWebConfig().getCookieValue(), "UTF-8"));
    }

    protected List<Track> filterTracks(Collection<Track> tracks) {
        List<Track> filtered = new ArrayList<Track>();
        for (Track track : tracks) {
            if (matchesFilter(track)) {
                filtered.add(track);
            }
        }
        return filtered;
    }

    private boolean matchesFilter(Track track) {
        Map<String, String> filter = (Map<String, String>)getSession().getAttribute("displayFilter");
        if (filter != null) {
            String filterText = filter.get("text");
            String filterType = filter.get("type");
            String filterProtected = filter.get("protected");
            if (StringUtils.isNotEmpty(filterText)) {
                String lowerCaseFilterText = filterText.toLowerCase();
                if (!track.getName().toLowerCase().contains(lowerCaseFilterText) && !track.getAlbum().toLowerCase().contains(lowerCaseFilterText) &&
                        !track.getAlbum().toLowerCase().contains(lowerCaseFilterText)) {
                    return false;
                }
            }
            if ("audio".equals(filterType) && track.isVideo()) {
                return false;
            }
            if ("video".equals(filterType) && !track.isVideo()) {
                return false;
            }
            if ("protected".equals(filterProtected) && !track.isProtected()) {
                return false;
            }
            if ("unprotected".equals(filterProtected) && track.isProtected()) {
                return false;
            }
        }
        return true;
    }
}