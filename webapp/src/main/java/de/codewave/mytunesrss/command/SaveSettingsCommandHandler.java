/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssWebUtils;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.mytunesrss.servlet.WebConfig;
import de.codewave.utils.MiscUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * de.codewave.mytunesrss.command.SaveSettingsCommandHandler
 */
public class SaveSettingsCommandHandler extends MyTunesRssCommandHandler {
    @Override
    public void executeAuthorized() throws Exception {
        if (isSessionAuthorized() && getAuthUser().isEditWebSettings()) {
            WebConfig webConfig = getWebConfig();
            if (transferAndValidate(webConfig)) {
                MyTunesRssWebUtils.saveWebConfig(getRequest(), getResponse(), getAuthUser(), webConfig);
                if (getSession().getAttribute(WebConfig.MYTUNESRSS_COM_USER) != null) {
                    restartMyTunesRssCom();
                } else {
                    redirect(MyTunesRssWebUtils.getCommandCall(getRequest(), MyTunesRssCommand.ShowPortal));
                }
            } else {
                forward(MyTunesRssResource.Settings);
            }
        } else {
            redirect(MyTunesRssWebUtils.getResourceCommandCall(getRequest(), MyTunesRssResource.Login));
        }
    }

    private boolean transferAndValidate(WebConfig webConfig) {
        webConfig.setShowDownload(getBooleanRequestParameter("showDownload", false));
        webConfig.setShowRss(isParameterValue("feedType", "rss"));
        webConfig.setShowPlaylist(isParameterValue("feedType", "playlist"));
        webConfig.setPlaylistType(getRequestParameter("playlistType", "m3u"));
        webConfig.setShowPlayer(getBooleanRequestParameter("showPlayer", false));
        webConfig.setTheme(getRequest().getParameter("theme"));
        webConfig.setFlashplayer(StringUtils.trimToNull(getRequestParameter("flashplayer", null)));
        webConfig.setYahooMediaPlayer(getBooleanRequestParameter("showYahooMediaPlayer", false));
        webConfig.setBrowserStartIndex(getRequest().getParameter("browserStartIndex"));
        if (getAuthUser().isChangeEmail() && !StringUtils.equals(getAuthUser().getEmail(), getRequest().getParameter("email"))) {
            String oldEmail = getAuthUser().getEmail();
            getAuthUser().setEmail(getRequest().getParameter("email"));
            MyTunesRss.ADMIN_NOTIFY.notifyEmailChange(getAuthUser(), oldEmail);
        }
        webConfig.setMyTunesRssComAddress(getBooleanRequestParameter("myTunesRssComAddress", false));
        webConfig.setAlbumImageSize(getIntegerRequestParameter("albImgSize", 128));
        webConfig.setRemoteControl(getBooleanRequestParameter("remoteControl", false));
        webConfig.setAddRemoteControl(getBooleanRequestParameter("addRemoteControl", false));
        webConfig.setKeepAlive(getBooleanRequestParameter("keepAlive", false));
        webConfig.setShowExternalSites(getBooleanRequestParameter("showExternalSites", false));
        webConfig.setShowAddToPlaylist(getBooleanRequestParameter("showAddToPlaylist", false));
        webConfig.setPhotoSize(getIntegerRequestParameter("photoSize", 0));
        transferTranscoders(webConfig);
        boolean error = false;
        error |= transferAndValidatePageSize(webConfig);
        error |= transferAndValidateMaxSearchResults(webConfig);
        error |= transferAndValidatePhotoPageSize(webConfig);
        error |= transferAndValidatePhotoJpegQuality(webConfig);
        error |= transferAndValidatePassword();
        error |= transferAndValidateLastFmAccount();
        return !error;
    }

    private void transferTranscoders(WebConfig webConfig) {
        StringBuilder transcoders = new StringBuilder();
        if (getRequest().getParameterValues("transcoder") != null) {
            for (String tcName : getRequest().getParameterValues("transcoder")) {
                transcoders.append(",").append(tcName);
            }
        }
        webConfig.setActiveTranscoders(transcoders.length() > 0 ? transcoders.substring(1) : null);
    }

    private boolean transferAndValidateLastFmAccount() {
        if (getAuthUser().isEditLastFmAccount()) {
            String username = getRequestParameter("lastfmusername", null);
            String password1 = getRequestParameter("lastfmpassword1", null);
            String password2 = getRequestParameter("lastfmpassword2", null);
            getAuthUser().setLastFmUsername(username);
            if (StringUtils.isNotEmpty(password1) || StringUtils.isNotEmpty(password2)) {
                if (StringUtils.equals(password1, password2)) {
                    getAuthUser().setLastFmPasswordHash(MyTunesRss.MD5_DIGEST.get().digest(MiscUtils.getUtf8Bytes(password1)));
                } else {
                    addError(new BundleError("error.settingsLastFmPasswordMismatch"));
                    return true;
                }
            }
        }
        return false;
    }

    private boolean transferAndValidatePassword() {
        if (getAuthUser().isChangePassword()) {
            String password1 = getRequestParameter("password1", null);
            String password2 = getRequestParameter("password2", null);
            if (StringUtils.isNotEmpty(password1) || StringUtils.isNotEmpty(password2)) {
                if (StringUtils.equals(password1, password2)) {
                    byte[] passwordHash = MyTunesRss.SHA1_DIGEST.get().digest(MiscUtils.getUtf8Bytes(password1));
                    if (!Arrays.equals(passwordHash, getAuthUser().getPasswordHash())) {
                        getAuthUser().setPasswordHash(passwordHash);
                        MyTunesRss.ADMIN_NOTIFY.notifyPasswordChange(getAuthUser());
                    }
                } else {
                    addError(new BundleError("error.settingsPasswordMismatch"));
                    return true;
                }
            }
        }
        return false;
    }

    private boolean transferAndValidatePageSize(WebConfig webConfig) {
        try {
            webConfig.setPageSize(getIntegerRequestParameter("pageSize", 0));
            if (webConfig.getPageSize() < 0 || webConfig.getPageSize() > 999) {
                addError(new BundleError("error.settingsPageSizeRange"));
                return true;
            }
        } catch (NumberFormatException ignored) {
            addError(new BundleError("error.settingsPageSizeRange"));
            return true;
        }
        return false;
    }

    private boolean transferAndValidateMaxSearchResults(WebConfig webConfig) {
        try {
            webConfig.setMaxSearchResults(getIntegerRequestParameter("maxSearchResults", 250));
            if (webConfig.getMaxSearchResults() < 1 || webConfig.getMaxSearchResults() > 10000) {
                addError(new BundleError("error.settingsMaxSearchResultsRange"));
                return true;
            }
        } catch (NumberFormatException ignored) {
            addError(new BundleError("error.settingsMaxSearchResultsRange"));
            return true;
        }
        return false;
    }

    private boolean transferAndValidatePhotoPageSize(WebConfig webConfig) {
        try {
            webConfig.setPhotoPageSize(getIntegerRequestParameter("photoPageSize", 0));
            if (webConfig.getPhotoPageSize() < 0 || webConfig.getPhotoPageSize() > 999) {
                addError(new BundleError("error.settingsPhotoPageSizeRange"));
                return true;
            }
        } catch (NumberFormatException ignored) {
            addError(new BundleError("error.settingsPhotoPageSizeRange"));
            return true;
        }
        return false;
    }

    private boolean transferAndValidatePhotoJpegQuality(WebConfig webConfig) {
        try {
            webConfig.setPhotoJpegQuality(getIntegerRequestParameter("photoJpegQuality", 0));
            if (webConfig.getPhotoJpegQuality() < 1 || webConfig.getPhotoJpegQuality() > 100) {
                addError(new BundleError("error.settingsPhotoJpegQualityRange"));
                return true;
            }
        } catch (NumberFormatException ignored) {
            addError(new BundleError("error.settingsPhotoJpegQualityRange"));
            return true;
        }
        return false;
    }

}
