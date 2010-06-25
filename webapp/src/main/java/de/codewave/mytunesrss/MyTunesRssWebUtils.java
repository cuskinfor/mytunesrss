package de.codewave.mytunesrss;

import de.codewave.mytunesrss.command.MyTunesRssCommand;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.Error;
import de.codewave.mytunesrss.servlet.WebConfig;
import de.codewave.utils.servlet.ServletUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;
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
                webConfig.load(httpServletRequest);
                httpServletRequest.getSession().setAttribute("config", webConfig);
                LOGGER.debug("Created session configuration.");
            }
            httpServletRequest.setAttribute("config", webConfig);
            LOGGER.debug("Created request configuration: " + webConfig.getMap().toString());
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
                    errors = new HashSet<Error>();
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
        } else if (isUserAgentSafari(request)) {
            return UserAgent.Safari;
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
        return tc.toString();
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
}
