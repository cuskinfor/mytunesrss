package de.codewave.mytunesrss;

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
    private static final Logger LOG = LoggerFactory.getLogger(MyTunesRssWebUtils.class);

    public static String getServletUrl(HttpServletRequest request) {
        return ServletUtils.getApplicationUrl(request) + "/mytunesrss";
    }

    public static User getAuthUser(HttpServletRequest request) {
        User user = (User)request.getSession().getAttribute("authUser");
        if (user == null) {
            user = (User)request.getAttribute("authUser");
        }
        return user;
    }

    public static String encryptPathInfo(HttpServletRequest request, String pathInfo) {
        try {
            if (MyTunesRss.CONFIG.getPathInfoKey() != null && (getAuthUser(request) == null || getAuthUser(request).isUrlEncryption())) {
                Cipher cipher = Cipher.getInstance(MyTunesRss.CONFIG.getPathInfoKey().getAlgorithm());
                cipher.init(Cipher.ENCRYPT_MODE, MyTunesRss.CONFIG.getPathInfoKey());
                return "{" + MyTunesRssBase64Utils.encode(cipher.doFinal(pathInfo.getBytes("UTF-8"))) + "}";
            } else {
                return pathInfo;
            }
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Could not encrypt path info.", e);
            }
        }
        return pathInfo;
    }

    public static WebConfig getWebConfig(HttpServletRequest httpServletRequest) {
        WebConfig webConfig = (WebConfig)httpServletRequest.getAttribute("config");
        if (webConfig == null) {
            webConfig = (WebConfig)httpServletRequest.getSession().getAttribute("config");
            if (webConfig == null) {
                webConfig = new WebConfig();
                webConfig.clearWithDefaults(httpServletRequest);
                webConfig.load(httpServletRequest);
                httpServletRequest.getSession().setAttribute("config", webConfig);
                LOG.debug("Created session configuration: " + webConfig.getMap().toString());
            }
            MyTunesRssWebUtils.setTranscodingFromRequest(webConfig, httpServletRequest);
            httpServletRequest.setAttribute("config", webConfig);
            LOG.debug("Created request configuration: " + webConfig.getMap().toString());
        }
        return webConfig;
    }

    public static void addError(HttpServletRequest request, Error error, String holderName) {
        Set<Error> errors = (Set<Error>)request.getSession().getAttribute(holderName);
        if (errors == null) {
            synchronized (request.getSession()) {
                errors = (Set<Error>)request.getSession().getAttribute(holderName);
                if (errors == null) {
                    errors = new HashSet<Error>();
                    request.getSession().setAttribute(holderName, errors);
                }
            }
        }
        errors.add(error);
    }

    public static boolean isError(HttpServletRequest request, String holderName) {
        Set<Error> errors = (Set<Error>)request.getSession().getAttribute(holderName);
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

    public static UserAgent getUserAgent(HttpServletRequest request) {
        if (isUserAgentPsp(request)) {
            return UserAgent.Psp;
        } else if (isUserAgentIphone(request)) {
            return UserAgent.Iphone;
        } else if (isUserAgentSafari(request)) {
            return UserAgent.Safari;
        }
        return UserAgent.Unknown;
    }

    public static String createTranscodingPathInfo(WebConfig config) {
        return createTranscodingParamValue(config.isAlac(),
                                           config.isFaad(),
                                           config.isLame(),
                                           config.getLameTargetBitrate(),
                                           config.getLameTargetSampleRate(),
                                           config.isTranscodeOnTheFlyIfPossible());
    }

    public static String createTranscodingParamValue(boolean alacTranscoding, boolean faadTranscoding, boolean lameTranscoding,
            int transcodingBitrate, int transcodingSamplerate, boolean transcodeOnTheFlyIfPossible) {
        StringBuilder tc = new StringBuilder();
        if (alacTranscoding || faadTranscoding || lameTranscoding) {
            tc.append("A").append(alacTranscoding ? "1" : "0").append("_");
            tc.append("F").append(faadTranscoding ? "1" : "0").append("_");
            tc.append("L").append(lameTranscoding ? "1" : "0").append("_");
            tc.append("B").append(transcodingBitrate).append("_S").append(transcodingSamplerate).append("_O").append(
                    transcodeOnTheFlyIfPossible ? "1" : "0");
        }
        return tc.toString();
    }

    public static void setTranscodingFromRequest(WebConfig config, HttpServletRequest request) {
        String tcValue = request.getParameter("tc");
        if (StringUtils.isNotBlank(tcValue)) {
            for (String tc : tcValue.trim().split("_")) {
                char key = tc.charAt(0);
                String value = tc.substring(1);
                switch (key) {
                    case 'A':
                        config.setAlac("1".equals(value));
                        break;
                    case 'F':
                        config.setFaad("1".equals(value));
                        break;
                    case 'L':
                        config.setLame("1".equals(value));
                        break;
                    case 'B':
                        config.setLameTargetBitrate(Integer.valueOf(value));
                        break;
                    case 'S':
                        config.setLameTargetSampleRate(Integer.valueOf(value));
                        break;
                    case 'O':
                        config.setTranscodeOnTheFlyIfPossible("1".equals(value));
                        break;
                    default:
                        LOG.warn("Illegal transcodig parameter \"" + tc + "\" ignored.");
                }
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

    public static String makeHttp(String url) {
        String schemePrefix = "https://";
        if (url != null && url.toLowerCase().startsWith(schemePrefix)) {
            int httpPort = MyTunesRss.CONFIG.isTomcatProxy() ? MyTunesRss.CONFIG.getTomcatProxyPort() : MyTunesRss.CONFIG.getPort();
            int serverSeparator = url.indexOf("/", schemePrefix.length());
            if (serverSeparator == -1) {
                serverSeparator = url.length();
            }
            int portSeparator = url.indexOf(':', schemePrefix.length());
            String oldHost = portSeparator != -1 ? url.substring(schemePrefix.length(), portSeparator) : url.substring(schemePrefix.length(),
                                                                                                                       serverSeparator);
            String httpHost = MyTunesRss.CONFIG.isTomcatProxy() ? MyTunesRss.CONFIG.getTomcatProxyHost() : oldHost;
            String httpScheme = MyTunesRss.CONFIG.isTomcatProxy() ? MyTunesRss.CONFIG.getTomcatProxyScheme() : "http";
            return httpScheme + "://" + httpHost + ":" + httpPort + (serverSeparator < url.length() ? url.substring(serverSeparator) : "");
        }
        return url;
    }

    public static void createParameterModel(HttpServletRequest request, String... parameterNames) {
        for (String parameterName : parameterNames) {
            String[] parts = parameterName.split(".");
            Map map = null;
            for (int i = 0; i < parts.length; i++) {
                if (i < parts.length - 1) {
                    if (map == null) {
                        map = (Map)request.getAttribute(parts[i]);
                        if (map == null) {
                            map = new HashMap();
                            request.setAttribute(parts[i], map);
                        }
                    } else {
                        if (!map.containsKey(parts[i])) {
                            map.put(parts[i], new HashMap());
                        }
                        map = (Map)map.get(parts[i]);
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



