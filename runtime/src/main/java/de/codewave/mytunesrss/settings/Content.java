package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.sql.*;
import org.apache.commons.logging.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;

/**
 * de.codewave.mytunesrss.settings.Content
 */
public class Content implements MyTunesRssEventListener {
    private static final Log LOG = LogFactory.getLog(Content.class);

    private JPanel myRootPanel;
    private JScrollPane myScrollPane;
    private JPanel myPlaylistsPanel;

    public void init() {
        myScrollPane.getViewport().setOpaque(false);
        MyTunesRssEventManager.getInstance().addListener(this);
        initValues();
    }

    private void initValues() {
        refreshPlaylistList();
    }

    public void handleEvent(MyTunesRssEvent event) {
        if (event == MyTunesRssEvent.DATABASE_PLAYLIST_UPDATED || event == MyTunesRssEvent.DATABASE_UPDATE_FINISHED) {
            refreshPlaylistList();
        } else if (event == MyTunesRssEvent.CONFIGURATION_CHANGED) {
            initValues();
        }
    }

    private void refreshPlaylistList() {
        myPlaylistsPanel.removeAll();
        try {
            List<Playlist> playlists = MyTunesRss.STORE.executeQuery(new FindPlaylistQuery(null, null, true)).getResults();
            Collections.sort(playlists, new Comparator<Playlist>() {
                public int compare(Playlist o1, Playlist o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            for (Playlist playlist : playlists) {
                if (playlist.getType() == PlaylistType.ITunes || playlist.getType() == PlaylistType.M3uFile) {
                    addPlaylist(playlist);
                }
            }
            addPanelComponent(new JLabel(""), new GridBagConstraints(GridBagConstraints.RELATIVE,
                                                                     GridBagConstraints.RELATIVE,
                                                                     2,
                                                                     1,
                                                                     1.0,
                                                                     1.0,
                                                                     GridBagConstraints.WEST,
                                                                     GridBagConstraints.BOTH,
                                                                     new Insets(0, 0, 0, 0),
                                                                     0,
                                                                     0));
        } catch (SQLException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(null, e);
            }
        }
        myPlaylistsPanel.validate();
    }

    private void addPlaylist(final Playlist playlist) {
        GridBagConstraints gbcActive = new GridBagConstraints(GridBagConstraints.RELATIVE,
                                                              GridBagConstraints.RELATIVE,
                                                              1,
                                                              1,
                                                              0,
                                                              0,
                                                              GridBagConstraints.WEST,
                                                              GridBagConstraints.HORIZONTAL,
                                                              new Insets(5, 5, 0, 0),
                                                              0,
                                                              0);
        GridBagConstraints gbcName = new GridBagConstraints(GridBagConstraints.RELATIVE,
                                                            GridBagConstraints.RELATIVE,
                                                            GridBagConstraints.REMAINDER,
                                                            1,
                                                            1.0,
                                                            0,
                                                            GridBagConstraints.WEST,
                                                            GridBagConstraints.HORIZONTAL,
                                                            new Insets(5, 5, 0, 0),
                                                            0,
                                                            0);
        final JCheckBox active = new JCheckBox();
        active.setOpaque(false);
        active.setSelected(!playlist.isHidden());
        active.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                DataStoreSession session = MyTunesRss.STORE.getTransaction();
                SavePlaylistAttributesStatement statement = new SavePlaylistAttributesStatement();
                statement.setId(playlist.getId());
                statement.setHidden(!active.isSelected());
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
        });
        addPanelComponent(active, gbcActive);
        JLabel name = new JLabel(playlist.getName());
        name.setOpaque(false);
        addPanelComponent(name, gbcName);
    }

    private void addPanelComponent(JComponent component, GridBagConstraints gbcName) {
        myPlaylistsPanel.add(component);
        ((GridBagLayout)myPlaylistsPanel.getLayout()).setConstraints(component, gbcName);
    }
}