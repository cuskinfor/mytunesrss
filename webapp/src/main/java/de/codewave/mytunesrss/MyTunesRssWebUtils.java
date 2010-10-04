package de.codewave.mytunesrss;

import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.Error;
import de.codewave.mytunesrss.remote.MyTunesRssRemoteEnv;
import de.codewave.mytunesrss.remote.Session;
import de.codewave.mytunesrss.servlet.WebConfig;
import de.codewave.mytunesrss.transcoder.Transcoder;
import de.codewave.utils.servlet.ServletUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
//        String uri = request.getRequestURI();
//        int levelCount = StringUtils.countMatches(uri, "/");
//        String appUrl = StringUtils.repeat("../", levelCount - StringUtils.countMatches(request.getContextPath(), "/") - 1);
//        return appUrl.endsWith("/") ? appUrl.substring(0, appUrl.length() - 1) : appUrl;
    }

    public static String getServletUrl(HttpServletRequest request) {
        return getApplicationUrl(request) + "/mytunesrss";
    }

    public static User getAuthUser(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("authUser");
        if (user == null) {
            user = (User) request.getAttribute("authUser");
            if (user == null) {
                Session session = MyTunesRssRemoteEnv.getSession();
                user = session != null ? session.getUser() : null;
            }
        }
        return user;
    }

    /**
     * Encrypt the path info. The parts of the path info are expected to be url encoded already.
     * Any %2F and %5C will be url encoded once again since tomcat does not like those characters in the path info.
     * So the path info decoder will have to decode the parts once since tomcat only decodes once of course.
     *
     * @param request
     * @param pathInfo
     * @return
     */
    public static String encryptPathInfo(HttpServletRequest request, String pathInfo) {
        String result = pathInfo;
        try {
            if (MyTunesRss.CONFIG.getPathInfoKey() != null && (getAuthUser(request) == null || getAuthUser(request).isUrlEncryption())) {
                Cipher cipher = Cipher.getInstance(MyTunesRss.CONFIG.getPathInfoKey().getAlgorithm());
                cipher.init(Cipher.ENCRYPT_MODE, MyTunesRss.CONFIG.getPathInfoKey());
                result = "%7B" + MyTunesRssBase64Utils.encode(cipher.doFinal(pathInfo.getBytes("UTF-8"))) + "%7D";
            }
        } catch (Exception e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Could not encrypt path info.", e);
            }
        }
        // re-encode %2F and %5C for the reason specified in the java doc
        return result.replace("%2F", "%252F").replace("%2f", "%252F").replace("%5C", "%255C").replace("%5c", "%255C");
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
            LOGGER.debug("Created request configuration: " + new HashMap<String, String>(webConfig.getMap()).toString());
        }
        MyTunesRssWebUtils.setTranscodingFromRequest(webConfig, httpServletRequest);
        return webConfig;
    }

    public static void addError(HttpServletRequest request, Error error, String holderName) {
        Set<Error> errors = (Set<Error>) request.getSession().getAttribute(holderName);
        if (errors == null) {
            synchronized (request.getSession()) {
                errors = (Set<Error>) request.getSession().getAttribute(holderName);
                if (errors == null) {
                    errors = new LinkedHashSet<Error>();
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

    private static boolean isUserAgentPsp(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return StringUtils.isNotEmpty(userAgent) && userAgent.contains("PSP");
    }

    private static boolean isUserAgentIphone(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return StringUtils.isNotEmpty(userAgent) && userAgent.contains("iPhone");
    }

    private static boolean isUserAgentSafari(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return StringUtils.isNotEmpty(userAgent) && userAgent.contains("Safari");
    }

    private static boolean isUserAgentNintendoWii(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return StringUtils.isNotEmpty(userAgent) && userAgent.contains("Nintendo Wii");
    }

    public static UserAgent getUserAgent(HttpServletRequest request) {
        if (isUserAgentPsp(request)) {
            return UserAgent.Psp;
        } else if (isUserAgentIphone(request)) {
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

    public static String createTranscodingPathInfo(WebConfig config) {
        return createTranscodingParamValue(StringUtils.split(StringUtils.trimToEmpty(config.getActiveTranscoders()), ','));
    }

    public static String createTranscodingParamValue(String[] transcoderNames) {
        StringBuilder tc = new StringBuilder();
        for (String tcName : transcoderNames) {
            tc.append("N").append(tcName).append("_");
        }
        return tc.length() > 0 ? tc.substring(0, tc.length() - 1) : "";
    }

    public static void setTranscodingFromRequest(WebConfig config, HttpServletRequest request) {
        String tcValue = request.getParameter("tc");
        StringBuilder names = new StringBuilder();
        if (StringUtils.isNotBlank(tcValue)) {
            for (String tc : tcValue.trim().split("_")) {
                char key = tc.charAt(0);
                String value = tc.substring(1);
                switch (key) {
                    case 'N':
                        names.append(",").append(tc.substring(1));
                        break;
                    default:
                        LOGGER.warn("Illegal transcodig parameter \"" + tc + "\" ignored.");
                }
            }
            if (names.length() > 1) {
                config.setActiveTranscoders(names.substring(1));
            }
        }
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
                if ("MyTunesRSS_Language".equals(cookie.getName())) {
                    return StringUtils.trimToNull(cookie.getValue());
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
        if (StringUtils.isNotBlank(languageCode)) {
            Cookie cookie = new Cookie("MyTunesRSS_Language", StringUtils.trim(languageCode));
            cookie.setComment("MyTunesRSS language cookie");
            cookie.setMaxAge(3600 * 24 * 365); // one year
            String servletUrl = MyTunesRssWebUtils.getServletUrl(request);
            cookie.setPath(servletUrl.substring(servletUrl.lastIndexOf("/")));
            response.addCookie(cookie);
        }
    }

    public static void saveWebConfig(HttpServletRequest request, HttpServletResponse response, User user, WebConfig webConfig) {
        if (user != null && !user.isSharedUser()) {
            // save in user profile on server
            user.setWebConfig(MyTunesRssWebUtils.getUserAgent(request), webConfig.createCookieValue());
            MyTunesRss.CONFIG.save(); // save new user settings
        } else {
            // save in cookie
            webConfig.save(request, response);
        }
    }

    public static Transcoder getTranscoder(HttpServletRequest request, Track track) {
        boolean notranscode = "true".equals(request.getParameter("notranscode"));
        boolean tempFile = ServletUtils.isRangeRequest(request) || ServletUtils.isHeadRequest(request);
        User authUser = getAuthUser(request);
        return (authUser != null && authUser.isForceTranscoders()) || !notranscode ? Transcoder.createTranscoder(track, authUser, getWebConfig(request), tempFile) : null;
    }

    public static InputStream getMediaStream(HttpServletRequest request, Track track) throws IOException {
        Transcoder transcoder = getTranscoder(request, track);
        if (transcoder != null) {
            return transcoder.getStream();
        } else {
            return new FileInputStream(track.getFile());
        }
    }

    public static boolean isHttpLiveStreaming(HttpServletRequest request, Track track, boolean ignoreContentType) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Checking for HTTP Live Streaming.");
        }
        if (MyTunesRss.HTTP_LIVE_STREAMING_AVAILABLE && getUserAgent(request) == UserAgent.Iphone && track.getMediaType() == MediaType.Video) {
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

    public static void rememberLogin(HttpServletRequest request, HttpServletResponse response, String username, byte[] passwordHash) {
        try {
            StringBuilder cookieValue = new StringBuilder(Base64.encodeBase64String(username.getBytes("UTF-8")).trim());
            cookieValue.append(";").append(new String(Base64.encodeBase64(passwordHash), "UTF-8").trim());
            response.addCookie(createLoginCookie(request, cookieValue.toString()));
        } catch (UnsupportedEncodingException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Character set UTF-8 not found.");
            }

        }
    }

    private static Cookie createLoginCookie(HttpServletRequest request, String cookieValue) {
        Cookie cookie = new Cookie(MyTunesRss.APPLICATION_IDENTIFIER + "User", cookieValue);
        cookie.setComment("MyTunesRSS user cookie");
        cookie.setMaxAge(3600 * 24 * 60);// 60 days
        String servletUrl = MyTunesRssWebUtils.getServletUrl(request);
        cookie.setPath(servletUrl.substring(servletUrl.lastIndexOf("/")));
        return cookie;
    }

    public static void forgetLogin(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = createLoginCookie(request, "");
        cookie.setMaxAge(0); // delete cookie
        response.addCookie(cookie);
    }

    public static String getRememberedUsername(HttpServletRequest request) {
        for (Cookie cookie : request.getCookies()) {
            if (StringUtils.equals(cookie.getName(), MyTunesRss.APPLICATION_IDENTIFIER + "User")) {
                try {
                    return new String(Base64.decodeBase64(cookie.getValue().split(";")[0]), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error("Character set UTF-8 not found.");
                    }
                }
            }
        }
        return null;
    }

    public static byte[] getRememberedPasswordHash(HttpServletRequest request) {
        for (Cookie cookie : request.getCookies()) {
            if (StringUtils.equals(cookie.getName(), MyTunesRss.APPLICATION_IDENTIFIER + "User")) {
                return Base64.decodeBase64(cookie.getValue().split(";")[1]);
            }
        }
        return null;
    }

}
