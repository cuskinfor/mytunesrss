package de.codewave.mytunesrss;

import de.codewave.camel.mp4.Mp4Utils;
import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.command.StatusCodeSender;
import de.codewave.mytunesrss.command.WebAppScope;
import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.config.transcoder.TranscoderConfig;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.Error;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.mytunesrss.server.MyTunesRssSessionInfo;
import de.codewave.mytunesrss.servlet.WebConfig;
import de.codewave.mytunesrss.transcoder.Transcoder;
import de.codewave.utils.Base64Utils;
import de.codewave.utils.servlet.ServletUtils;
import de.codewave.utils.servlet.SessionManager;
import de.codewave.utils.servlet.StreamSender;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * <b>Description:</b>   <br> <b>Copyright:</b>     Copyright (c) 2006<br> <b>Company:</b>       daGama Business Travel GmbH<br> <b>Creation Date:</b>
 * 08.11.2006
 *
 * @author Michael Descher
 * @version $Id:$
 */
public class MyTunesRssWebUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MyTunesRssWebUtils.class);

    public static String getApplicationUrl(HttpServletRequest request) {
        return ServletUtils.getApplicationUrl(request);
    }

    public static String getServletUrl(HttpServletRequest request) {
        return getApplicationUrl(request) + "/mytunesrss";
    }

    public static User getAuthUser(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("authUser");
        if (user == null) {
            user = (User) request.getAttribute("authUser");
        }
        return user;
    }

    public static WebConfig getWebConfig(HttpServletRequest httpServletRequest) {
        WebConfig webConfig = (WebConfig) httpServletRequest.getAttribute("config");
        if (webConfig == null) {
            webConfig = (WebConfig) httpServletRequest.getSession().getAttribute("config");
            if (webConfig == null) {
                webConfig = new WebConfig();
                webConfig.clearWithDefaults(httpServletRequest);
                webConfig.load(httpServletRequest, getAuthUser(httpServletRequest));
                httpServletRequest.getSession().setAttribute("config", webConfig);
                LOGGER.debug("Created session configuration.");
            }
            httpServletRequest.setAttribute("config", webConfig);
            LOGGER.debug("Created request configuration: " + new HashMap<>(webConfig.getMap()).toString());
        }
        String activeTranscodersFromRequest = MyTunesRssWebUtils.getActiveTranscodingFromRequest(httpServletRequest);
        if (activeTranscodersFromRequest != null) {
            webConfig.setActiveTranscoders(activeTranscodersFromRequest);
        }
        return webConfig;
    }

    public static void addError(HttpServletRequest request, Error error, String holderName) {
        Set<Error> errors = (Set<Error>) request.getSession().getAttribute(holderName);
        if (errors == null) {
            synchronized (request.getSession()) {
                errors = (Set<Error>) request.getSession().getAttribute(holderName);
                if (errors == null) {
                    errors = new LinkedHashSet<>();
                    request.getSession().setAttribute(holderName, errors);
                }
            }
        }
        errors.add(error);
    }

    public static boolean isError(HttpServletRequest request, String holderName) {
        Set<Error> errors = (Set<Error>) request.getSession().getAttribute(holderName);
        return errors != null && !errors.isEmpty();
    }

    private static boolean isUserAgentIphone(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return StringUtils.isNotEmpty(userAgent) && userAgent.contains("iPhone");
    }

    private static boolean isUserAgentNintendoWii(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return StringUtils.isNotEmpty(userAgent) && userAgent.contains("Nintendo Wii");
    }

    public static UserAgent getUserAgent(HttpServletRequest request) {
        if (isUserAgentIphone(request)) {
            return UserAgent.Iphone;
        } else if (isUserAgentNintendoWii(request)) {
            return UserAgent.NintendoWii;
        }
        return UserAgent.Unknown;
    }

    public static String getCommandCall(HttpServletRequest request, MyTunesRssCommand command) {
        String servletUrl = getServletUrl(request);
        return MyTunesRssWebUtils.getApplicationUrl(request) + servletUrl.substring(servletUrl.lastIndexOf("/")) + "/" + command.getName();
    }

    public static String getResourceCommandCall(HttpServletRequest request, MyTunesRssResource resource) {
        String servletUrl = getServletUrl(request);
        return MyTunesRssWebUtils.getApplicationUrl(request) + servletUrl.substring(servletUrl.lastIndexOf("/")) + "/" + MyTunesRssCommand.ShowResource.getName() + "/resource=" + resource.name();
    }

    public static String createTranscodingPathInfo(WebConfig config) {
        return createTranscodingParamValue(StringUtils.split(StringUtils.trimToEmpty(config.getActiveTranscoders()), ','));
    }

    public static String createTranscodingParamValue(String[] transcoderNames) {
        StringBuilder tc = new StringBuilder();
        for (String tcName : transcoderNames) {
            tc.append(tcName).append(",");
        }
        return tc.length() > 0 ? tc.substring(0, tc.length() - 1) : "";
    }

    public static String getActiveTranscodingFromRequest(HttpServletRequest request) {
        return request.getParameter("tc");
    }

    /**
     * Move tracks in the playlist to another position.
     *
     * @param playlistTracks List of tracks.
     * @param first          Index of first track to move (0-based).
     * @param count          Number of tracks to move.
     * @param offset         Offset to move, can be positive to move downwards or negative to move upwards.
     */
    public static void movePlaylistTracks(List<Track> playlistTracks, int first, int count, int offset) {
        for (int i = 0; i < Math.abs(offset); i++) {
            for (int k = 0; k < count; k++) {
                int swapLeft;
                if (offset < 0) {
                    swapLeft = first + k - 1;
                } else {
                    swapLeft = first + count - k - 1;
                }
                if (swapLeft >= 0 && swapLeft + 1 < playlistTracks.size()) {
                    Track tempTrack = playlistTracks.get(swapLeft);
                    playlistTracks.set(swapLeft, playlistTracks.get(swapLeft + 1));
                    playlistTracks.set(swapLeft + 1, tempTrack);
                } else {
                    break;
                }
            }
            first += Math.signum(offset);
        }
    }

    public static void createParameterModel(HttpServletRequest request, String... parameterNames) {
        for (String parameterName : parameterNames) {
            String[] parts = parameterName.split("\\.");
            Map map = null;
            for (int i = 0; i < parts.length; i++) {
                if (i < parts.length - 1) {
                    if (map == null) {
                        map = (Map) request.getAttribute(parts[i]);
                        if (map == null) {
                            map = new HashMap();
                            request.setAttribute(parts[i], map);
                        }
                    } else {
                        if (!map.containsKey(parts[i])) {
                            map.put(parts[i], new HashMap());
                        }
                        map = (Map) map.get(parts[i]);
                    }
                } else {
                    if (map == null) {
                        request.setAttribute(parts[i], request.getParameter(parameterName));
                    } else {
                        map.put(parts[i], request.getParameter(parameterName));
                    }
                }
            }
        }
    }

    public static String getBundleString(HttpServletRequest request, String key) {
        LocalizationContext context = (LocalizationContext) request.getSession().getAttribute(Config.FMT_LOCALIZATION_CONTEXT + ".session");
        ResourceBundle bundle = context != null ? context.getResourceBundle() : ResourceBundle.getBundle("de/codewave/mytunesrss/MyTunesRssWeb",
                request.getLocale());
        return bundle.getString(key);
    }

    /**
     * Get language from cookie.
     *
     * @param request Servlet request.
     * @return Language from cookie or NULL if no cookie was found.
     */
    public static String getCookieLanguage(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                if ((MyTunesRss.APPLICATION_IDENTIFIER + "Language").equals(cookie.getName())) {
                    return StringUtils.trimToNull(Base64Utils.decodeToString(cookie.getValue()));
                }
            }
        }
        return null;
    }

    /**
     * Set the language cookie.
     *
     * @param request      Servlet request.
     * @param response     Servlet response.
     * @param languageCode Language code.
     */
    public static void setCookieLanguage(HttpServletRequest request, HttpServletResponse response, String languageCode) {
        Cookie cookie;
        if (StringUtils.isNotBlank(languageCode)) {
            cookie = new Cookie(MyTunesRss.APPLICATION_IDENTIFIER + "Language", Base64Utils.encode(StringUtils.trim(languageCode)));
            cookie.setVersion(1);
            cookie.setMaxAge(3600 * 24 * 365); // one year
        } else {
            cookie = new Cookie(MyTunesRss.APPLICATION_IDENTIFIER + "Language", "");
            cookie.setVersion(1);
            cookie.setMaxAge(0); // expire now
        }
        cookie.setComment("MyTunesRSS language cookie");
        String servletUrl = MyTunesRssWebUtils.getServletUrl(request);
        cookie.setPath(servletUrl.substring(servletUrl.lastIndexOf("/")));
        response.addCookie(cookie);
    }

    public static void saveWebConfig(HttpServletRequest request, HttpServletResponse response, User user, WebConfig webConfig) {
        if (user != null && !user.isSharedUser()) {
            // save in user profile on server
            user.setWebConfig(MyTunesRssWebUtils.getUserAgent(request), webConfig.createCookieValue());
            MyTunesRss.CONFIG.save(); // save new user settings
            webConfig.removeCookie(request, response); // remove cookie if it exists
        } else {
            // save in cookie
            webConfig.save(request, response);
        }
    }

    public static Transcoder getTranscoder(HttpServletRequest request, Track track) {
        if (MyTunesRss.CONFIG.isValidVlcConfig()) {
            boolean notranscode = "true".equals(request.getParameter("notranscode"));
            User authUser = getAuthUser(request);
            return (authUser != null && authUser.getForceTranscoder(track) != null) || !notranscode ? Transcoder.createTranscoder(track, authUser, MyTunesRssWebUtils.getActiveTranscodingFromRequest(request)) : null;
        } else {
            return null;
        }
    }

    public static StreamSender getMediaStreamSender(HttpServletRequest request, Track track, File file) throws IOException {
        long ifModifiedSince = request.getDateHeader("If-Modified-Since");
        Transcoder transcoder = getTranscoder(request, track);
        if (transcoder == null) {
            // no transcoding
            if (ifModifiedSince != -1 && (file.lastModified() / 1000) <= (ifModifiedSince / 1000)) {
                // file has not been modified after if-modified-since timestamp
                return new StatusCodeSender(HttpServletResponse.SC_NOT_MODIFIED);
            } else {
                // no if-modified-since request or file has been modified
                if (Mp4Utils.isMp4File(file)) {
                    // use qt-faststart for MP4 files
                    LOGGER.info("Using QT-FASTSTART utility.");
                    return new StreamSender(Mp4Utils.getFastStartInputStream(file), track.getContentType(), track.getContentLength());
                } else {
                    // standard file
                    return new StreamSender(new FileInputStream(file), track.getContentType(), track.getContentLength());
                }
            }
        } else {
            // transcoder requested
            return transcoder.getStreamSender(file, ifModifiedSince);
        }
    }

    public static boolean isHttpLiveStreaming(HttpServletRequest request, Track track, boolean ignoreContentType, boolean ignoreUserAgent) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Checking for HTTP Live Streaming.");
        }
        if (MyTunesRss.CONFIG.isVlcEnabled() && MyTunesRssUtils.canExecute(MyTunesRss.CONFIG.getVlcExecutable()) && (ignoreUserAgent || getUserAgent(request) == UserAgent.Iphone) && track.getMediaType() == MediaType.Video) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("HTTP Live Streaming available, user agent is iPhone and media type is video.");
            }
            if (ignoreContentType) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Ignoring content type of track or transcoder.");
                }
                return true;
            }
            Transcoder transcoder = getTranscoder(request, track);
            if (transcoder != null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Transcoder content type is \"" + transcoder.getTargetContentType() + "\".");
                }
                return "video/MP2T".equalsIgnoreCase(transcoder.getTargetContentType());
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Track content type is \"" + track.getContentType() + "\".");
                }
                return "video/MP2T".equalsIgnoreCase(track.getContentType());
            }
        }
        return false;
    }

    public static void rememberLogin(HttpServletResponse response, String username, byte[] passwordHash) {
        try {
            StringBuilder cookieValue = new StringBuilder(Base64.encodeBase64String(username.getBytes("UTF-8")).trim());
            cookieValue.append(";").append(new String(Base64.encodeBase64(passwordHash), "UTF-8").trim());
            response.addCookie(createLoginCookie(cookieValue.toString()));
        } catch (UnsupportedEncodingException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Character set UTF-8 not found.");
            }

        }
    }

    private static Cookie createLoginCookie(String cookieValue) {
        Cookie cookie = new Cookie(MyTunesRss.APPLICATION_IDENTIFIER + "UserV2", Base64Utils.encode(cookieValue));
        cookie.setVersion(1);
        cookie.setComment("MyTunesRSS user cookie");
        cookie.setMaxAge(3600 * 24 * 60); // 60 days
        cookie.setPath("/");
        return cookie;
    }

    public static void forgetLogin(HttpServletResponse response) {
        Cookie cookie = createLoginCookie("");
        cookie.setMaxAge(0); // delete cookie
        response.addCookie(cookie);
    }

    public static String getRememberedUsername(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (StringUtils.equals(cookie.getName(), MyTunesRss.APPLICATION_IDENTIFIER + "UserV2")) {
                    try {
                        return new String(Base64.decodeBase64(Base64Utils.decodeToString(cookie.getValue()).split(";")[0]), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        if (LOGGER.isErrorEnabled()) {
                            LOGGER.error("Character set UTF-8 not found.");
                        }
                    }
                }
            }
        }
        return null;
    }

    public static byte[] getRememberedPasswordHash(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (StringUtils.equals(cookie.getName(), MyTunesRss.APPLICATION_IDENTIFIER + "UserV2")) {
                    try {
                        return Base64.decodeBase64(Base64Utils.decodeToString(cookie.getValue()).split(";")[1]);
                    } catch (Exception e) {
                        if (LOGGER.isWarnEnabled()) {
                            LOGGER.warn("Could not get password from user cookie value.", e);
                        }
                        return null;
                    }
                }
            }
        }
        return null;
    }

    public static TranscoderConfig getTranscoder(String activeTranscoders, Track track) {
        if (MyTunesRss.CONFIG.isValidVlcConfig()) {
            for (TranscoderConfig config : MyTunesRss.CONFIG.getTranscoderConfigs()) {
                if (isActiveTranscoder(activeTranscoders, config.getName()) && config.isValidFor(track)) {
                    return config;
                }
            }
        }
        return null;
    }

    public static boolean isActiveTranscoder(String activeTranscoders, String transcoder) {
        return ArrayUtils.contains(StringUtils.split(activeTranscoders, ','), transcoder);
    }

    public static boolean isAuthorized(String userName, String password, byte[] passwordHash) {
        return isAuthorized(userName, passwordHash) || isAuthorizedLdapUser(userName, password);
    }

    private static boolean isAuthorizedLdapUser(String userName, String password) {
        return MyTunesRssUtils.loginLDAP(userName, password);
    }

    public static boolean isAuthorized(String userName, byte[] passwordHash) {
        return StringUtils.isNotBlank(userName) && passwordHash != null && passwordHash.length > 0 && isAuthorizedLocalUsers(userName, passwordHash);
    }

    private static boolean isAuthorizedLocalUsers(String userName, byte[] passwordHash) {
        LOGGER.debug("Checking authorization with local users.");
        User user = MyTunesRss.CONFIG.getUser(userName);
        return user != null && !user.isGroup() && Arrays.equals(user.getPasswordHash(), passwordHash) && user.isActive();
    }

    public static void authorize(WebAppScope scope, HttpServletRequest request, String userName) {
        User user = MyTunesRss.CONFIG.getUser(userName);
        if (scope == WebAppScope.Request) {
            LOGGER.debug("Authorizing request for user \"" + userName + "\".");
            request.setAttribute("authUser", user);
            request.setAttribute("auth", MyTunesRssUtils.createAuthToken(user));
        } else if (scope == WebAppScope.Session) {
            LOGGER.debug("Authorizing session for user \"" + userName + "\".");
            request.getSession().setAttribute("authUser", user);
            request.getSession().setAttribute("auth", MyTunesRssUtils.createAuthToken(user));
        }
        if (getAuthUser(request) != null && !getAuthUser(request).isSharedUser()) {
            getWebConfig(request).clearWithDefaults(request);
            getWebConfig(request).load(request, getAuthUser(request));
        }
        ((MyTunesRssSessionInfo) SessionManager.getSessionInfo(request)).setUser(user);
        request.getSession().setMaxInactiveInterval(user.getSessionTimeout() * 60);
    }

    public static String createCacheControlValue(long maxAgeSeconds) {
        return "max-age=" + maxAgeSeconds + ", must-revalidate, private";
    }

}
