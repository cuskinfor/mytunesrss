package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.config.VideoType;
import de.codewave.mytunesrss.datastore.statement.RefreshSmartPlaylistsStatement;
import de.codewave.mytunesrss.datastore.statement.SaveMyTunesSmartPlaylistStatement;
import de.codewave.mytunesrss.datastore.statement.SmartFieldType;
import de.codewave.mytunesrss.datastore.statement.SmartInfo;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

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
            Collection<SmartInfo> smartInfos = new ArrayList<SmartInfo>();
            smartInfos.add(new SmartInfo(SmartFieldType.album, getRequestParameter("smartPlaylist.smartFields.album", null), false));
            smartInfos.add(new SmartInfo(SmartFieldType.artist, getRequestParameter("smartPlaylist.smartFields.artist", null), false));
            smartInfos.add(new SmartInfo(SmartFieldType.tvshow, getRequestParameter("smartPlaylist.smartFields.tvshow", null), false));
            smartInfos.add(new SmartInfo(SmartFieldType.genre, getRequestParameter("smartPlaylist.smartFields.genre", null), false));
            smartInfos.add(new SmartInfo(SmartFieldType.title, getRequestParameter("smartPlaylist.smartFields.title", null), false));
            smartInfos.add(new SmartInfo(SmartFieldType.file, getRequestParameter("smartPlaylist.smartFields.file", null), false));
            smartInfos.add(new SmartInfo(SmartFieldType.tag, getRequestParameter("smartPlaylist.smartFields.tag", null), false));
            smartInfos.add(new SmartInfo(SmartFieldType.comment, getRequestParameter("smartPlaylist.smartFields.comment", null), false));
            smartInfos.add(new SmartInfo(SmartFieldType.composer, getRequestParameter("smartPlaylist.smartFields.composer", null), false));
            smartInfos.add(new SmartInfo(SmartFieldType.mintime, getRequestParameter("smartPlaylist.smartFields.mintime", null), false));
            smartInfos.add(new SmartInfo(SmartFieldType.maxtime, getRequestParameter("smartPlaylist.smartFields.maxtime", null), false));
            smartInfos.add(new SmartInfo(SmartFieldType.mediatype, getRequestParameter("smartPlaylist.smartFields.mediatype", null), false));
            smartInfos.add(new SmartInfo(SmartFieldType.videotype, getRequestParameter("smartPlaylist.smartFields.videotype", null), false));
            smartInfos.add(new SmartInfo(SmartFieldType.protection, getRequestParameter("smartPlaylist.smartFields.protection", null), false));
            for (Iterator<SmartInfo> iter = smartInfos.iterator(); iter.hasNext(); ) {
                SmartInfo smartInfo = iter.next();
                if (StringUtils.isBlank(smartInfo.getPattern())) {
                    iter.remove();
                }
            }
            SaveMyTunesSmartPlaylistStatement statement = new SaveMyTunesSmartPlaylistStatement(getAuthUser().getName(), getBooleanRequestParameter(
                    "smartPlaylist.playlist.userPrivate",
                    false), smartInfos);
            statement.setId(getRequestParameter("smartPlaylist.playlist.id", null));
            statement.setName(getRequestParameter("smartPlaylist.playlist.name", null));
            statement.setTrackIds(Collections.<String>emptyList());
            getTransaction().executeStatement(statement);
            getTransaction().executeStatement(new RefreshSmartPlaylistsStatement(smartInfos, statement.getPlaylistIdAfterExecute()));
            forward(MyTunesRssCommand.ShowPlaylistManager);
        } else {
            createParameterModel("smartPlaylist.playlist.id",
                                 "smartPlaylist.playlist.name",
                                 "smartPlaylist.playlist.userPrivate",
                                 "smartPlaylist.smartFields.album",
                                 "smartPlaylist.smartFields.artist",
                                 "smartPlaylist.smartFields.tvshow",
                                 "smartPlaylist.smartFields.genre",
                                 "smartPlaylist.smartFields.composer",
                                 "smartPlaylist.smartFields.title",
                                 "smartPlaylist.smartFields.file",
                                 "smartPlaylist.smartFields.tag",
                                 "smartPlaylist.smartFields.comment",
                                 "smartPlaylist.smartFields.mintime",
                                 "smartPlaylist.smartFields.maxtime",
                                 "smartPlaylist.smartFields.protection",
                                 "smartPlaylist.smartFields.videotype",
                                 "smartPlaylist.smartFields.mediatype");
            getRequest().setAttribute("fields", EditSmartPlaylistCommandHandler.getFields());
            forward(MyTunesRssResource.EditSmartPlaylist);
        }
    }
}