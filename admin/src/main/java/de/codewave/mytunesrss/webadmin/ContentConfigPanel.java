/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.addon.treetable.TreeTable;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class ContentConfigPanel extends MyTunesRssConfigPanel {

    private static final Logger LOG = LoggerFactory.getLogger(ContentConfigPanel.class);

    private TreeTable myPlaylists;

    private Table myGenres;

    public void attach() {
        super.attach();
        init(getBundleString("contentsConfigPanel.caption"), getComponentFactory().createGridLayout(1, 3, true, true));
        myPlaylists = new TreeTable();
        myPlaylists.setCacheRate(50);
        myPlaylists.setWidth(100, UNITS_PERCENTAGE);
        myPlaylists.addContainerProperty("visible", CheckBox.class, null, getBundleString("contentsConfigPanel.playlists.visible"), null, null);
        myPlaylists.addContainerProperty("name", String.class, null, getBundleString("contentsConfigPanel.playlists.name"), null, null);
        myPlaylists.setHierarchyColumn("name");
        myPlaylists.setColumnExpandRatio("name", 1);
        myPlaylists.setSortContainerPropertyId("name");
        myPlaylists.setEditable(false);
        Panel panel = new Panel(getBundleString("contentsConfigPanel.visiblePlaylists.caption"));
        panel.addComponent(myPlaylists);
        addComponent(panel);
        myGenres = new Table();
        myGenres.setCacheRate(50);
        myGenres.setWidth(100, UNITS_PERCENTAGE);
        myGenres.addContainerProperty("visible", CheckBox.class, null, getBundleString("contentsConfigPanel.genres.visible"), null, null);
        myGenres.addContainerProperty("name", String.class, null, getBundleString("contentsConfigPanel.genres.name"), null, null);
        myGenres.setColumnExpandRatio("name", 1);
        myGenres.setSortContainerPropertyId("name");
        myGenres.setEditable(false);
        panel = new Panel(getBundleString("contentsConfigPanel.visibleGenres.caption"));
        panel.addComponent(myGenres);
        addComponent(panel);

        addDefaultComponents(0, 2, 0, 2, false);

        initFromConfig();
    }

    protected void initFromConfig() {
        myPlaylists.removeAllItems();
        myGenres.removeAllItems();
        List<Playlist> playlists = null;
        List<Genre> genres = null;
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        try {
            playlists = session.executeQuery(new FindPlaylistQuery(Arrays.asList(PlaylistType.ITunes, PlaylistType.ITunesFolder, PlaylistType.M3uFile), null, null, true)).getResults();
            for (Playlist playlist : playlists) {
                CheckBox visible = new CheckBox();
                visible.setValue(!playlist.isHidden());
                myPlaylists.addItem(new Object[]{visible, playlist.getName()}, playlist);
            }
            genres = session.executeQuery(new FindGenreQuery(null, true, -1)).getResults();
            for (Genre genre : genres) {
                CheckBox visible = new CheckBox();
                visible.setValue(!genre.isHidden());
                myGenres.addItem(new Object[]{visible, genre.getName()}, genre);
            }
        } catch (SQLException e) {
            MyTunesRss.UNHANDLED_EXCEPTION.set(true);
        } finally {
            session.rollback();
        }
        getApplication().createPlaylistTreeTableHierarchy(myPlaylists, playlists);
        myPlaylists.sort();
        myPlaylists.setPageLength(Math.min(MyTunesRssUtils.getRootPlaylistCount(playlists), 20));
        myGenres.sort();
        myGenres.setPageLength(Math.min(genres.size(), 20));
    }

    protected void writeToConfig() {
        SavePlaylistAttributesStatement statement = new SavePlaylistAttributesStatement();
        for (Object itemId : myPlaylists.getItemIds()) {
            Playlist playlist = (Playlist) itemId;
            statement.setId(playlist.getId());
            statement.setHidden(!((Boolean) getTableCellPropertyValue(myPlaylists, playlist, "visible")));
            statement.setName(playlist.getName());
            statement.setUserOwner(playlist.getUserOwner());
            statement.setUserPrivate(playlist.isUserPrivate());
            DataStoreSession session = MyTunesRss.STORE.getTransaction();
            try {
                session.executeStatement(statement);
                session.commit();
            } catch (SQLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not update playlist attributes.", e);
                }
                session.rollback();
            }
        }
        for (Object itemId : myGenres.getItemIds()) {
            final Genre genre = (Genre) itemId;
            DataStoreSession session = MyTunesRss.STORE.getTransaction();
            try {
                session.executeStatement(new DataStoreStatement() {
                    public void execute(Connection connection) throws SQLException {
                        SmartStatement stmt = MyTunesRssUtils.createStatement(connection, "updateGenreHiddenAttribute");
                        stmt.setString("name", genre.getName());
                        stmt.setBoolean("hidden", !((Boolean) getTableCellPropertyValue(myGenres, genre, "visible")));
                        stmt.execute();
                    }
                });
                session.commit();
            } catch (SQLException e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not update genre hidden attribute.", e);
                }
                session.rollback();
            }
        }
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        super.buttonClick(clickEvent);
    }
}