/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.addons.AddonsUtils;
import de.codewave.mytunesrss.addons.LanguageDefinition;
import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.datastore.MyTunesRssDataStore;
import de.codewave.mytunesrss.datastore.statement.FindAlbumQuery;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.Error;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.mytunesrss.servlet.TransactionFilter;
import de.codewave.mytunesrss.servlet.WebConfig;
import de.codewave.mytunesrss.task.DatabaseBuilderCallable;
import de.codewave.utils.MiscUtils;
import de.codewave.utils.servlet.CommandHandler;
import de.codewave.utils.sql.DataStoreSession;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

/**
 * de.codewave.mytunesrss.command.MyTunesRssCommandHandler
 */
public abstract class MyTunesRssCommandHandler extends CommandHandler {
    private static final Logger LOG = LoggerFactory.getLogger(MyTunesRssCommandHandler.class);

    protected boolean isAuthorized(String userName, String password, byte[] passwordHash) {
        return MyTunesRssWebUtils.isAuthorized(userName, password, passwordHash);
    }

    protected boolean isAuthorized(String userName, byte[] passwordHash) {
        return MyTunesRssWebUtils.isAuthorized(userName, passwordHash);
    }

    protected void authorize(WebAppScope scope, String userName) {
        MyTunesRssWebUtils.authorize(scope, getRequest(), userName);
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
            } catch (NumberFormatException ignored) {// intentionally left blank
            }
        }
        return false;
    }

    protected boolean isSessionAuthorized() {
        if (getSession().getAttribute("auth") != null) {
            User user = (User) getSession().getAttribute("authUser");
            if (user.isActive() && MyTunesRss.CONFIG.getUser(user.getName()) != null) {
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

    protected DataStoreSession getTransaction() {
        return TransactionFilter.getTransaction();
    }

    protected void forward(MyTunesRssResource resource) throws IOException, ServletException {
        prepareRequestForResource();
        resource.beforeForward(getRequest(), getResponse());
        getRequest().setAttribute("userAgent", MyTunesRssWebUtils.getUserAgent(getRequest()).name());
        forward(resource.getValue());
    }

    private void prepareRequestForResource() {
        String servletUrl = MyTunesRssWebUtils.getServletUrl(getRequest());
        getRequest().setAttribute("servletUrl", servletUrl);
        getRequest().setAttribute("permServletUrl", servletUrl);
        getRequest().setAttribute("downloadPlaybackServletUrl", servletUrl);
        getRequest().setAttribute("permFeedServletUrl", getRequest().getAttribute("permServletUrl"));
        getRequest().setAttribute("appUrl", MyTunesRssWebUtils.getApplicationUrl(getRequest()));
        String theme = StringUtils.defaultIfEmpty(MyTunesRssWebUtils.getWebConfig(getRequest()).getTheme(), MyTunesRss.CONFIG.getDefaultUserInterfaceTheme());
        getRequest().setAttribute("themeUrl", MyTunesRssWebUtils.getApplicationUrl(getRequest()) + "/theme/" + MiscUtils.getUtf8UrlEncoded("_" + theme));
        getRequest().setAttribute("mytunesrssVersion", MyTunesRss.VERSION);
        getRequest().setAttribute("sessionCreationTime", getSession().getCreationTime());
        if (getAuthUser() != null && getAuthUser().isQuotaExceeded()) {
            addError(new BundleError("error.quotaExceeded." + getAuthUser().getQuotaType().name()));
        }
        getRequest().setAttribute("globalConfig", MyTunesRss.CONFIG);
        setResourceBundle();
        if (MyTunesRss.EXECUTOR_SERVICE.isDatabaseUpdateRunning()) {
            if (DatabaseBuilderCallable.getState() == DatabaseBuilderCallable.State.UpdatingTracksFromFolder ||
                    DatabaseBuilderCallable.getState() == DatabaseBuilderCallable.State.UpdatingTracksFromItunes) {
                addMessage(new BundleError("info.databaseUpdating"));
            } else {
                addMessage(new BundleError("info.databaseUpdatingImages"));
            }
        }
        getRequest().setAttribute("msgUnknownAlbum", getBundleString("unknownAlbum"));
        getRequest().setAttribute("msgUnknownArtist", getBundleString("unknownArtist"));
        getRequest().setAttribute("msgUnknownTrack", getBundleString("unknownTrack"));
        getRequest().setAttribute("msgUnknownSeries", getBundleString("unknownSeries"));
    }

    private void setResourceBundle() {
        String cookieLanguage = MyTunesRssWebUtils.getCookieLanguage(getRequest());
        Locale locale = StringUtils.isNotBlank(cookieLanguage) ? LanguageDefinition.getLocaleFromCode(cookieLanguage) : getRequest().getLocale();
        LocalizationContext context = (LocalizationContext) getSession().getAttribute(Config.FMT_LOCALIZATION_CONTEXT + ".session");
        if (context == null || !ObjectUtils.equals(context.getLocale(), locale)) {
            ResourceBundle bundle = retrieveBundle(AddonsUtils.getBestLanguageFile(locale), locale);
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
            try {
                bundle = new PropertyResourceBundle(new FileInputStream(language));
            } catch (IOException ignored) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not read language file \"" + language.getAbsolutePath() + "\".");
                }
            }
            getSession().getServletContext().setAttribute("LanguageBundle." + locale.toString(), bundle);
        }
        return bundle;
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
            LOG.debug("Request headers:");
            for (Enumeration<String> enumName = getRequest().getHeaderNames(); enumName.hasMoreElements(); ) {
                String name = enumName.nextElement();
                StringBuilder msg = new StringBuilder("\"").append(name).append("\"=");
                for (Enumeration<String> enumValue = getRequest().getHeaders(name); enumValue.hasMoreElements(); ) {
                    msg.append("\"").append(enumValue.nextElement()).append("\",");
                }
                LOG.debug(msg.substring(0, msg.length() - 1));
            }
        }
        setResourceBundle();
        if (StringUtils.isNotBlank(getRequest().getParameter("_cda"))) {
            getResponse().setHeader("Content-Disposition", "attachment; filename=\"" + MyTunesRssUtils.getLegalFileName(getRequest().getParameter("_cda")) + "\"");
        } else if (StringUtils.isNotBlank(getRequest().getParameter("_cdi"))) {
            getResponse().setHeader("Content-Disposition", "inline; filename=\"" + MyTunesRssUtils.getLegalFileName(getRequest().getParameter("_cdi")) + "\"");
        }
        try {
            if (!isSessionAuthorized() && StringUtils.isNotBlank(MyTunesRss.CONFIG.getAutoLogin())) {
                authorize(WebAppScope.Session, MyTunesRss.CONFIG.getAutoLogin());
            } else if (!MyTunesRss.CONFIG.isDisableWebLogin() && !isSessionAuthorized() && isAuthorized(MyTunesRssWebUtils.getRememberedUsername(getRequest()), MyTunesRssWebUtils.getRememberedPasswordHash(getRequest()))) {
                authorize(WebAppScope.Session, MyTunesRssWebUtils.getRememberedUsername(getRequest()));
            }
            if (!isRequestAuthorized()) {
                executeUnauthorized();
            } else {
                handleDisplayFilter();
                throttleBandwidth();
                executeAuthorized();
            }
        } catch (UnauthorizedException e) {
            addError(new BundleError("error.illegalAccess"));
            if (e.getResource() == null) {
                forward(MyTunesRssResource.RestartTopWindow);
            } else {
                forward(e.getResource());
            }
        } catch (BadRequestException e) {
            addError(new BundleError("error.badRequest", e.getMessage()));
            if (e.getResource() == null) {
                forward(MyTunesRssResource.RestartTopWindow);
            } else {
                forward(e.getResource());
            }
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Unhandled exception: ", e);
            }
            getSession().removeAttribute("errors");
            redirect(MyTunesRssWebUtils.getCommandCall(getRequest(), MyTunesRssCommand.ShowFatalError));
        }
    }

    protected void executeUnauthorized() throws IOException, ServletException {
        forward(MyTunesRssResource.Login);
    }

    protected void throttleBandwidth() {
        // do nothing in base implementation
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
        if (getRequest().getParameter("filterAlbumType") != null) {
            filter.setAlbumType(FindAlbumQuery.AlbumType.valueOf(getRequestParameter("filterAlbumType", FindAlbumQuery.AlbumType.ALL.name())));
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
        return createPager(itemCount, getWebConfig().getEffectivePageSize(), current);
    }

    protected Pager createPager(int itemCount, int pageSize, int current) {
        if (pageSize > 0) {
            List<Pager.Page> pages = new ArrayList<>();
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
        return MyTunesRssWebUtils.getBundleString(getRequest(), key);
    }

    protected String getBundleString(String key, Object... params) {
        return MessageFormat.format(getBundleString(key), params);
    }

    /**
     * Get the filename from the content disposition request parameter "_cdi" or "_cda" and default to "MyTunesRSS" if neither of the two parameters
     * is available.
     *
     * @return The filename.
     */
    protected String getContentDispositionFilename() {
        return StringUtils.trimToEmpty(StringUtils.substringBeforeLast(getRequestParameter("_cdi", getRequestParameter("_cda", "MyTunesRSS")), "."));
    }
}
