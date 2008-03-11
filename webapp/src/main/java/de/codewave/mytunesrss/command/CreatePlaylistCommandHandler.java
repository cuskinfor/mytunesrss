/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.jsp.BundleError;
import de.codewave.mytunesrss.servlet.WebConfig;
import de.codewave.utils.sql.DataStoreQuery;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * de.codewave.mytunesrss.command.CreatePlaylistCommandHandler
 */
public class CreatePlaylistCommandHandler extends CreatePlaylistBaseCommandHandler {

    @Override
    public void executeAuthorized() throws SQLException, IOException, ServletException {
        if (getAuthUser().isPlaylist() || Boolean.parseBoolean(getRequestParameter("playerRequest", "false"))) {
            DataStoreQuery.QueryResult<Track> tracks = getTracks();
            if (tracks.getResultSize() > 0) {
                getRequest().setAttribute("tracks", tracks.getResults());
                String playlistType = getRequestParameter("type", null);
                if (StringUtils.isEmpty(playlistType)) {
                    forward(getWebConfig().getPlaylistTemplateResource());
                } else {
                    forward(WebConfig.PlaylistType.valueOf(playlistType).getTemplateResource());
                }
            } else {
                addError(new BundleError("error.emptyFeed"));
                forward(MyTunesRssCommand.ShowPortal);// todo: redirect to backUrl
            }
        } else {
            getResponse().setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

}