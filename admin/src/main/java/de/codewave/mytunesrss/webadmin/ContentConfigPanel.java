/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.mytunesrss.datastore.statement.PlaylistType;
import de.codewave.mytunesrss.datastore.statement.SavePlaylistAttributesStatement;
import de.codewave.utils.sql.DataStoreSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class ContentConfigPanel extends MyTunesRssConfigPanel {

    private static final Logger LOG = LoggerFactory.getLogger(ContentConfigPanel.class);

    private Table myPlaylists;

    public void attach() {
        super.attach();
        init(getBundleString("contentsConfigPanel.caption"), getComponentFactory().createGridLayout(1, 2, true, true));
        myPlaylists = new Table();
        myPlaylists.addContainerProperty("visible", CheckBox.class, null, getBundleString("contentsConfigPanel.playlists.visible"), null, null);
        myPlaylists.addContainerProperty("name", String.class, null, getBundleString("contentsConfigPanel.playlists.name"), null, null);
        myPlaylists.setSortContainerPropertyId("name");
        myPlaylists.setEditable(false);
        Panel panel = new Panel(getBundleString("contentsConfigPanel.visiblePlaylists.caption"));
        panel.addComponent(myPlaylists);
        addComponent(myPlaylists);

        addDefaultComponents(0, 1, 0, 1, false);

        initFromConfig();
    }

    protected void initFromConfig() {
        myPlaylists.removeAllItems();
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        List<Playlist> playlists = null;
        try {
            playlists = session.executeQuery(new FindPlaylistQuery(Arrays.asList(PlaylistType.ITunes, PlaylistType.ITunesFolder, PlaylistType.M3uFile), null, null, true)).getResults();
            for (Playlist playlist : playlists) {
                CheckBox visible = new CheckBox();
                visible.setValue(!playlist.isHidden());
                StringBuilder name = new StringBuilder();
                for (Playlist pathElement : MyTunesRssUtils.getPlaylistPath(playlist, playlists)) {
                    name.append(" \u21E8 ").append(pathElement.getName());
                }
                myPlaylists.addItem(new Object[]{visible, name.substring(3)}, playlist);
            }
        } catch (SQLException e) {
            MyTunesRss.UNHANDLED_EXCEPTION.set(true);
        }
        myPlaylists.sort();
        myPlaylists.setPageLength(Math.min(playlists.size(), 20));
    }

    protected void writeToConfig() {
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        SavePlaylistAttributesStatement statement = new SavePlaylistAttributesStatement();
        for (Object itemId : myPlaylists.getItemIds()) {
            Playlist playlist = (Playlist) itemId;
            statement.setId(playlist.getId());
            statement.setHidden(!((Boolean) getTableCellPropertyValue(myPlaylists, playlist, "visible")));
            statement.setName(playlist.getName());
            statement.setUserOwner(playlist.getUserOwner());
            statement.setUserPrivate(playlist.isUserPrivate());
            try {
                session.executeStatement(statement);
                session.commit();
            } catch (SQLException e1) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not update playlist attributes.", e1);
                }
                try {
                    session.rollback();
                } catch (SQLException e2) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not rollback transaction.", e2);
                    }
                }
            }
        }
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        super.buttonClick(clickEvent);
    }
}