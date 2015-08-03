/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.command;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.jsp.MyTunesRssResource;
import de.codewave.mytunesrss.jsp.OpmlItem;
import de.codewave.utils.MiscUtils;
import de.codewave.utils.jsp.CodewaveFunctions;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.ResultSetType;

import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * de.codewave.mytunesrss.command.CreatePlaylistOpmlCommandHandler
 */
public class CreatePlaylistOpmlCommandHandler extends MyTunesRssCommandHandler {

    @Override
    public void executeAuthorized() throws SQLException, IOException, ServletException {
        FindPlaylistQuery query = new FindPlaylistQuery(getAuthUser(), null, null, null, false, false);
        query.setFetchOptions(ResultSetType.TYPE_FORWARD_ONLY, 1000);
        final List<OpmlItem> items = new ArrayList<>();
        getTransaction().executeQuery(query).processRemainingResults(new DataStoreQuery.ResultProcessor<Playlist>() {
            @Override
            public void process(Playlist playlist) {
                String filename = playlist.getName() + ".xml";
                String pathInfo = "playlist=" + playlist.getId() + "/_cdi=" + filename;
                String xmlUrl = MyTunesRssWebUtils.getAuthCommandCall(getRequest(), MyTunesRssCommand.CreateRss) + "/" + MyTunesRssUtils.encryptPathInfo(pathInfo) + "/" + MiscUtils.getUtf8UrlEncoded(filename);
                OpmlItem item = new OpmlItem(playlist.getName(), xmlUrl);
                items.add(item);
            }
        });
        getRequest().setAttribute("creationDate", System.currentTimeMillis());
        getRequest().setAttribute("title", "MyTunesRSS");
        getRequest().setAttribute("items", items);
        forward(MyTunesRssResource.Opml);
    }

}
