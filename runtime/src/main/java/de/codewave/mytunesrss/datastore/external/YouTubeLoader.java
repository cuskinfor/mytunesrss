package de.codewave.mytunesrss.datastore.external;

import de.codewave.mytunesrss.MediaType;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.statement.InsertOrUpdateTrackStatement;
import de.codewave.mytunesrss.datastore.statement.InsertTrackStatement;
import de.codewave.mytunesrss.datastore.statement.TrackSource;
import de.codewave.mytunesrss.datastore.statement.UpdateTrackStatement;
import de.codewave.mytunesrss.task.DatabaseBuilderTask;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.xml.JXPathUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * de.codewave.mytunesrss.datastore.url.UrlLoader
 */
public class YouTubeLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(YouTubeLoader.class);
    private static final SimpleDateFormat PUBLISH_DATE_FORMAT = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z", Locale.US);
    private static final Pattern EMBED_URL_PATTERN = Pattern.compile(".*embedUrl[ =]*'(.*?)';.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private long myLastUpdateTime;
    private Collection<String> myTrackIds;
    private Set<String> myExistingIds = new HashSet<String>();
    private DataStoreSession myStoreSession;
    private int myUpdatedCount;

    public YouTubeLoader(DataStoreSession storeSession, long lastUpdateTime, Collection<String> trackIds) {
        myStoreSession = storeSession;
        myLastUpdateTime = lastUpdateTime;
        myTrackIds = trackIds;
    }

    public void process(String url) {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(url);
        try {
            if (client.executeMethod(method) == 200) {
                String contentType = MyTunesRssUtils.getBaseType(method.getResponseHeader("Content-Type").getValue());
                if (StringUtils.equalsIgnoreCase(contentType, "application/rss+xml")) {
                    JXPathContext rssFeed = JXPathUtils.getContext(method.getResponseBodyAsString());
                    String feedTitle = JXPathUtils.getStringValue(rssFeed, "/rss/channel/title", null);
                    for (Iterator<JXPathContext> itemIterator = JXPathUtils.getContextIterator(rssFeed, "/rss/channel/item"); itemIterator.hasNext();) {
                        JXPathContext item = itemIterator.next();
                        String title = JXPathUtils.getStringValue(item, "title", null);
                        Map<String, String> redirectUrlParams = getRedirectUrlParams(JXPathUtils.getStringValue(item, "link", null));
                        if (redirectUrlParams != null) {
                            String link = redirectUrlParams.get("LOCATION");
                            String author = JXPathUtils.getStringValue(item, "author", null);
                            String pubDate = JXPathUtils.getStringValue(item, "pubDate", null);
                            String lengthSeconds = redirectUrlParams.get("length_seconds");
                            long modifyTime = System.currentTimeMillis();
                            try {
                                modifyTime = StringUtils.isNotBlank(pubDate) ? PUBLISH_DATE_FORMAT.parse(pubDate).getTime() : System.currentTimeMillis();
                            } catch (ParseException e) {
                                LOGGER.warn("Could not parse YouTube item publish date \"" + pubDate + "\".", e);
                            }
                            String itemId = createItemId(link);
                            boolean existing = myTrackIds.contains(itemId);
                            if (existing) {
                                myExistingIds.add(itemId);
                            }
                            if ((modifyTime >= myLastUpdateTime || !existing)) {
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("Processing item from URL \"" + link + "\".");
                                }
                                InsertOrUpdateTrackStatement statement;
                                if (!MyTunesRss.CONFIG.isIgnoreArtwork()) {
                                    // TODO
                                    // statement = existing ? new UpdateTrackAndImageStatement() : new InsertTrackAndImageStatement(TrackSource.YouTube);
                                    statement = existing ? new UpdateTrackStatement(TrackSource.YouTube) : new InsertTrackStatement(TrackSource.YouTube);
                                } else {
                                    statement = existing ? new UpdateTrackStatement(TrackSource.YouTube) : new InsertTrackStatement(TrackSource.YouTube);
                                }
                                statement.setId(itemId);
                                statement.setFileName(link);
                                statement.setName(title);
                                statement.setArtist(author);
                                statement.setAlbum(feedTitle);
                                if (StringUtils.isNotBlank(lengthSeconds) && StringUtils.isNumeric(lengthSeconds)) {
                                    statement.setTime(Integer.parseInt(lengthSeconds));
                                }
                                statement.setMediaType(MediaType.Video);
                                try {
                                    myStoreSession.executeStatement(statement);
                                    myUpdatedCount++;
                                    DatabaseBuilderTask.updateHelpTables(myStoreSession, myUpdatedCount);
                                    myExistingIds.add(itemId);
                                } catch (SQLException e) {
                                    if (LOGGER.isErrorEnabled()) {
                                        LOGGER.error("Could not insert track \"" + link + "\" into database", e);
                                    }
                                }
                            }
                        } else {
                            LOGGER.error("Did not get redirect url from youtube url \"" + url + "\".");
                        }
                    }
                } else {
                    LOGGER.error("Received wrong content type \"" + contentType + "\" from youtube url \"" + url + "\".");
                }
            } else {
                LOGGER.error("Could not read from youtube url \"" + url + "\": " + method.getStatusText());
            }
        } catch (IOException e) {
            LOGGER.error("Could not read from youtube url \"" + url + "\".", e);
        } finally {
            method.releaseConnection();
        }
    }

    private Map<String, String> getRedirectUrlParams(String itemUrl) {
        HttpClient client = new HttpClient();
        GetMethod method = new GetMethod(itemUrl);
        try {
            if (client.executeMethod(method) == 200) {
                Matcher matcher = EMBED_URL_PATTERN.matcher(method.getResponseBodyAsString());
                if (matcher.matches() && matcher.groupCount() == 1) {
                    String embedUrl = matcher.group(1);
                    method.releaseConnection();
                    method = new GetMethod(embedUrl);
                    method.setFollowRedirects(false);
                    int sc = client.executeMethod(method);
                    if (sc == 301 || sc == 302 || sc == 303 || sc == 307) {
                        Map<String, String> redirectUrlParams = new HashMap<String, String>();
                        String location = method.getResponseHeader("Location").getValue();
                        redirectUrlParams.put("LOCATION", location);
                        String params = StringUtils.split(location, "?")[1];
                        for (String param : StringUtils.split(params, "&")) {
                            String[] keyValue = StringUtils.split(param, "=");
                            redirectUrlParams.put(keyValue[0], keyValue.length == 2 ? keyValue[1] : "");
                        }
                        return redirectUrlParams;
                    }
                }
            }
        } catch (HttpException e) {
            LOGGER.warn("Could not get redirect url params from item url \"" + itemUrl + "\".", e);
        } catch (IOException e) {
            LOGGER.warn("Could not get redirect url params from item url \"" + itemUrl + "\".", e);
        } finally {
            method.releaseConnection();
        }
        return null;
    }

    private String createItemId(String link) {
        try {
            return "ext_" + new String(Hex.encodeHex(MyTunesRss.SHA1_DIGEST.digest(link.getBytes("UTF-8"))));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not found!");
        }
    }
}