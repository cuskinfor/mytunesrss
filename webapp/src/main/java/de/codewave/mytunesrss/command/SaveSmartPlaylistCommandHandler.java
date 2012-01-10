package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.MediaType;
import de.codewave.mytunesrss.VideoType;
import de.codewave.mytunesrss.datastore.statement.RefreshSmartPlaylistsStatement;
import de.codewave.mytunesrss.datastore.statement.SaveMyTunesSmartPlaylistStatement;
import de.codewave.mytunesrss.datastore.statement.SmartInfo;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;

/**
 * de.codewave.mytunesrss.command.SaveSmartPlaylistCommandHandler
 */
public class SaveSmartPlaylistCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws Exception {
        if (StringUtils.isBlank(getRequestParameter("smartPlaylist.playlist.name", null))) {
            addError(new BundleError("error.needPlaylistNameForSave"));
        }
        if (!isError()) {
            SmartInfo smartInfo = new SmartInfo();
            smartInfo.setAlbumPattern(getRequestParameter("smartPlaylist.smartInfo.albumPattern", null));
            smartInfo.setArtistPattern(getRequestParameter("smartPlaylist.smartInfo.artistPattern", null));
            smartInfo.setSeriesPattern(getRequestParameter("smartPlaylist.smartInfo.seriesPattern", null));
            smartInfo.setGenrePattern(getRequestParameter("smartPlaylist.smartInfo.genrePattern", null));
            smartInfo.setTitlePattern(getRequestParameter("smartPlaylist.smartInfo.titlePattern", null));
            smartInfo.setFilePattern(getRequestParameter("smartPlaylist.smartInfo.filePattern", null));
            smartInfo.setTagPattern(getRequestParameter("smartPlaylist.smartInfo.tagPattern", null));
            smartInfo.setCommentPattern(getRequestParameter("smartPlaylist.smartInfo.commentPattern", null));
            smartInfo.setComposerPattern(getRequestParameter("smartPlaylist.smartInfo.composerPattern", null));
            if (StringUtils.isNotBlank(getRequestParameter("smartPlaylist.smartInfo.timeMin", null))) {
                smartInfo.setTimeMin(getIntegerRequestParameter("smartPlaylist.smartInfo.timeMin", 0));
            }
            if (StringUtils.isNotBlank(getRequestParameter("smartPlaylist.smartInfo.timeMax", null))) {
                smartInfo.setTimeMax(getIntegerRequestParameter("smartPlaylist.smartInfo.timeMax", 0));
            }
            if (StringUtils.isNotBlank(getRequestParameter("smartPlaylist.smartInfo.mediaType", null))) {
                smartInfo.setMediaType(MediaType.valueOf(getRequestParameter("smartPlaylist.smartInfo.mediaType", MediaType.Other.name())));
            }
            if (StringUtils.isNotBlank(getRequestParameter("smartPlaylist.smartInfo.videoType", null))) {
                smartInfo.setVideoType(VideoType.valueOf(getRequestParameter("smartPlaylist.smartInfo.videoType", null)));
            }
            if (StringUtils.isNotBlank(getRequestParameter("smartPlaylist.smartInfo.protected", null))) {
                smartInfo.setProtected(getBooleanRequestParameter("smartPlaylist.smartInfo.protected", false));
            }
            SaveMyTunesSmartPlaylistStatement statement = new SaveMyTunesSmartPlaylistStatement(getAuthUser().getName(), getBooleanRequestParameter(
                    "smartPlaylist.playlist.userPrivate",
                    false), smartInfo);
            statement.setId(getRequestParameter("smartPlaylist.playlist.id", null));
            statement.setName(getRequestParameter("smartPlaylist.playlist.name", null));
            statement.setTrackIds(Collections.<String>emptyList());
            getTransaction().executeStatement(statement);
            getTransaction().executeStatement(new RefreshSmartPlaylistsStatement());
            forward(MyTunesRssCommand.ShowPlaylistManager);
        } else {
            createParameterModel("smartPlaylist.playlist.id",
                                 "smartPlaylist.playlist.name",
                                 "smartPlaylist.playlist.userPrivate",
                                 "smartPlaylist.smartInfo.albumPattern",
                                 "smartPlaylist.smartInfo.artistPattern",
                                 "smartPlaylist.smartInfo.seriesPattern",
                                 "smartPlaylist.smartInfo.genrePattern",
                                 "smartPlaylist.smartInfo.composerPattern",
                                 "smartPlaylist.smartInfo.titlePattern",
                                 "smartPlaylist.smartInfo.filePattern",
                                 "smartPlaylist.smartInfo.tagPattern",
                                 "smartPlaylist.smartInfo.commentPattern",
                                 "smartPlaylist.smartInfo.timeMin",
                                 "smartPlaylist.smartInfo.timeMax",
                                 "smartPlaylist.smartInfo.protected",
                                 "smartPlaylist.smartInfo.mediaType");
            getRequest().setAttribute("fields", EditSmartPlaylistCommandHandler.getFields());
            forward(MyTunesRssResource.EditSmartPlaylist);
        }
    }
}