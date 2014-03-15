/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.data.Validatable;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.StopWatch;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.mytunesrss.event.MyTunesRssEvent;
import de.codewave.mytunesrss.event.MyTunesRssEventManager;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.DataStoreStatement;
import de.codewave.utils.sql.SmartStatement;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;

public class ContentConfigPanel extends MyTunesRssConfigPanel {

    private static final Logger LOG = LoggerFactory.getLogger(ContentConfigPanel.class);

    private TreeTable myPlaylists;
    private Table myGenres;
    private Table myGenreMappings;
    private Button myAddGenreMapping;
    private Set<String> oldHiddenGenres = new HashSet<>();
    private Set<String> oldHiddenPlaylists = new HashSet<>();
    private Map<String, String> oldGenreMappings = new HashMap<>();

    public void attach() {
        super.attach();
        init(getBundleString("contentsConfigPanel.caption"), getComponentFactory().createGridLayout(1, 4, true, true));

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

        myGenreMappings = new Table();
        myGenreMappings.setCacheRate(50);
        myGenreMappings.setWidth(100, UNITS_PERCENTAGE);
        myGenreMappings.addContainerProperty("from", SmartTextField.class, null, getBundleString("contentsConfigPanel.genres.from"), null, null);
        myGenreMappings.addContainerProperty("to", SmartTextField.class, null, getBundleString("contentsConfigPanel.genres.to"), null, null);
        myGenreMappings.addContainerProperty("delete", Button.class, null, "", null, null);
        myGenreMappings.setSortContainerPropertyId("from");
        myGenreMappings.setEditable(false);
        panel = new Panel(getBundleString("contentsConfigPanel.genreMappings.caption"), getComponentFactory().createVerticalLayout(true, true));
        panel.addComponent(myGenreMappings);
        myAddGenreMapping = getComponentFactory().createButton("contentsConfigPanel.genreMappings.add", this);
        panel.addComponent(myAddGenreMapping);
        addComponent(panel);

        addDefaultComponents(0, 3, 0, 3, false);

        initFromConfig();
    }

    protected void setTablePageLengths() {
        myPlaylists.setPageLength(Math.min(myPlaylists.getItemIds().size(), 15));
        myGenres.setPageLength(Math.min(myGenres.getItemIds().size(), 15));
        myGenreMappings.setPageLength(Math.min(myGenreMappings.getItemIds().size(), 10));
    }

    protected void initFromConfig() {
        myPlaylists.removeAllItems();
        myGenres.removeAllItems();
        List<Playlist> playlists = null;
        List<Genre> genres = null;
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        try {
            playlists = session.executeQuery(new FindPlaylistQuery(Arrays.asList(PlaylistType.ITunes, PlaylistType.ITunesFolder, PlaylistType.M3uFile), null, null, true)).getResults();
            oldHiddenPlaylists.clear();
            for (Playlist playlist : playlists) {
                CheckBox visible = new CheckBox();
                visible.setValue(!playlist.isHidden());
                if (playlist.isHidden()) {
                    oldHiddenPlaylists.add(playlist.getId());
                }
                myPlaylists.addItem(new Object[]{visible, playlist.getName()}, playlist);
            }
            genres = session.executeQuery(new FindGenresQuery(null, true, -1)).getResults();
            oldHiddenGenres.clear();
            for (Genre genre : genres) {
                CheckBox visible = new CheckBox();
                visible.setValue(!genre.isHidden());
                if (genre.isHidden()) {
                    oldHiddenGenres.add(genre.getName());
                }
                myGenres.addItem(new Object[]{visible, genre.getName()}, genre);
            }
            oldGenreMappings.clear();
            for (Map.Entry<String, String> genreMapping : MyTunesRss.CONFIG.getGenreMappings().entrySet()) {
                addGenreMapping(genreMapping.getKey(), genreMapping.getValue());
                oldGenreMappings.put(genreMapping.getKey(), genreMapping.getValue());
            }
        } catch (SQLException e) {
            MyTunesRss.UNHANDLED_EXCEPTION.set(true);
        } finally {
            session.rollback();
        }
        getApplication().createPlaylistTreeTableHierarchy(myPlaylists, playlists);
        myPlaylists.sort();
        myGenres.sort();
        myGenreMappings.sort();
        setTablePageLengths();
    }

    @Override
    protected boolean beforeSave() {
        for (Object itemId : myGenreMappings.getItemIds()) {
            boolean valid = ((Validatable) getTableCellItemValue(myGenreMappings, itemId, "from")).isValid();
            valid &= ((Validatable) getTableCellItemValue(myGenreMappings, itemId, "to")).isValid();
            if (!valid) {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
                return false;
            }
        }
        return true;
    }

    protected void writeToConfig() {
        MyTunesRss.CONFIG.clearGenreMappings();
        for (Object itemId : myGenreMappings.getItemIds()) {
            MyTunesRss.CONFIG.addGenreMapping((String) getTableCellPropertyValue(myGenreMappings, itemId, "from"), (String) getTableCellPropertyValue(myGenreMappings, itemId, "to"));
        }
        final boolean genreMappingsChanged = isGenreMappingsChanged();
        final boolean hiddenPlaylistsChanged = isHiddenPlaylistsChanged();
        final boolean hiddenGenresChanged = isHiddenGenresChanged();
        LOG.debug("genreMappingsChanged = " + genreMappingsChanged + ", hiddenPlaylistsChanged = " + hiddenPlaylistsChanged + ", hiddenGenresChanged = " + hiddenGenresChanged + ".");
        if (hiddenGenresChanged || hiddenPlaylistsChanged || genreMappingsChanged) {
            final MainWindow applicationWindow = (MainWindow) VaadinUtils.getApplicationWindow(this);
            applicationWindow.showBlockingMessage("contentConfigPanel.info.updatingDatabase");
            MyTunesRss.EXECUTOR_SERVICE.scheduleDatabaseJob(new Callable<Void>() {
                public Void call() {
                    try {
                        if (hiddenPlaylistsChanged) {
                            StopWatch.start("Updating hidden playlists after saving content configuration");
                            try {
                                SavePlaylistAttributesStatement statement = new SavePlaylistAttributesStatement();
                                for (Object itemId : myPlaylists.getItemIds()) {
                                    if (Thread.interrupted()) {
                                        return null;
                                    }
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
                            } finally {
                                StopWatch.stop();
                            }
                        }
                        if (hiddenGenresChanged) {
                            StopWatch.start("Updating hidden genres after saving content configuration");
                            try {
                                for (Object itemId : myGenres.getItemIds()) {
                                    if (Thread.interrupted()) {
                                        return null;
                                    }
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
                            } finally {
                                StopWatch.stop();
                            }
                        }
                        if (genreMappingsChanged) {
                            try {
                                StopWatch.start("Updating genre mappings after saving content configuration");
                                MyTunesRss.STORE.executeStatement(new RenameGenresStatement(MyTunesRss.CONFIG.getGenreMappings()));
                            } catch (SQLException e) {
                                if (LOG.isErrorEnabled()) {
                                    LOG.error("Could not rename genres.", e);
                                }
                            } finally {
                                StopWatch.stop();
                            }
                        }
                    } catch (RuntimeException e) {
                        LOG.warn("Unhandled exception during database update after content config change.", e);
                    } finally {
                        MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.MEDIA_SERVER_UPDATE));
                        applicationWindow.hideBlockingMessage();
                    }
                    return null;
                }
            });
        }
        MyTunesRss.CONFIG.save();
    }

    private boolean isGenreMappingsChanged() {
        Map<String, String> genreMappings = new HashMap<>();
        for (Object itemId : myGenreMappings.getItemIds()) {
            genreMappings.put((String) getTableCellPropertyValue(myGenreMappings, itemId, "from"), (String) getTableCellPropertyValue(myGenreMappings, itemId, "to"));
        }
        if (oldGenreMappings.size() != genreMappings.size()) {
            return true;
        }
        for (Map.Entry<String, String> entry : oldGenreMappings.entrySet()) {
            if (!entry.getValue().equals(genreMappings.get(entry.getKey()))) {
                return true;
            }
        }
        return false;
    }

    private boolean isHiddenGenresChanged() {
        Set<String> hiddenGenres = new HashSet<>();
        for (Object itemId : myGenres.getItemIds()) {
            Genre genre = (Genre) itemId;
            if (!((Boolean) getTableCellPropertyValue(myGenres, genre, "visible"))) {
                hiddenGenres.add(genre.getName());
            }
        }
        return !CollectionUtils.isEqualCollection(hiddenGenres, oldHiddenGenres);
    }

    private boolean isHiddenPlaylistsChanged() {
        Set<String> hiddenPlaylists = new HashSet<>();
        for (Object itemId : myPlaylists.getItemIds()) {
            Playlist playlist = (Playlist) itemId;
            if (!((Boolean) getTableCellPropertyValue(myPlaylists, playlist, "visible"))) {
                hiddenPlaylists.add(playlist.getId());
            }
        }
        return !CollectionUtils.isEqualCollection(hiddenPlaylists, oldHiddenPlaylists);
    }

    private void addGenreMapping(String from, String to) {
        SmartTextField fromTextField = getComponentFactory().createTextField(null, new StringLengthValidator(getBundleString("contentsConfigPanel.genreMappings.errorEmpty", 1, 255), 1, 255, false));
        fromTextField.setValue(from);
        fromTextField.setMaxLength(255);
        fromTextField.setWidth(100f, UNITS_PERCENTAGE);
        fromTextField.setRequired(true);
        SmartTextField toTextField = getComponentFactory().createTextField(null, new StringLengthValidator(getBundleString("contentsConfigPanel.genreMappings.errorEmpty", 1, 255), 1, 255, false));
        toTextField.setValue(to);
        toTextField.setMaxLength(255);
        toTextField.setWidth(100f, UNITS_PERCENTAGE);
        fromTextField.setRequired(true);
        Button delButton = getComponentFactory().createButton("contentsConfigPanel.genreMappings.delete", this);
        myGenreMappings.addItem(new Object[]{fromTextField, toTextField, delButton}, UUID.randomUUID().toString());
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        if (clickEvent.getSource() == myAddGenreMapping) {
            addGenreMapping("", "");
            setTablePageLengths();
        } else if (findTableItemWithObject(myGenreMappings, clickEvent.getSource()) != null) {
            myGenreMappings.removeItem(findTableItemWithObject(myGenreMappings, clickEvent.getSource()));
            setTablePageLengths();
        } else {
            super.buttonClick(clickEvent);
        }
    }
}
