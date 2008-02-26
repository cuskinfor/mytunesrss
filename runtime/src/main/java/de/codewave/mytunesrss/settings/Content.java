package de.codewave.mytunesrss.settings;

import com.intellij.uiDesigner.core.*;
import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.datastore.statement.*;
import de.codewave.utils.sql.*;
import de.codewave.utils.swing.*;
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

    public void handleEvent(final MyTunesRssEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (event == MyTunesRssEvent.DATABASE_PLAYLIST_UPDATED || event == MyTunesRssEvent.DATABASE_UPDATE_FINISHED) {
                    refreshPlaylistList();
                } else if (event == MyTunesRssEvent.CONFIGURATION_CHANGED) {
                    initValues();
                }
            }
        });
    }

    private void refreshPlaylistList() {
        SwingUtils.invokeAndWait(new Runnable() {
            public void run() {
                myPlaylistsPanel.removeAll();
                DataStoreSession session = MyTunesRss.STORE.getTransaction();
                try {
                    List<Playlist> playlists = session.executeQuery(new FindPlaylistQuery(null, null, true)).getResults();
                    Collections.sort(playlists, new Comparator<Playlist>() {
                        public int compare(Playlist o1, Playlist o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
                    myPlaylistsPanel.setLayout(new GridLayoutManager(playlists.size() + 1, 2));
                    int row = 0;
                    for (Playlist playlist : playlists) {
                        if (playlist.getType() == PlaylistType.ITunes || playlist.getType() == PlaylistType.M3uFile) {
                            addPlaylist(playlist, row++);
                        }
                    }
                    addPanelComponent(new JLabel(""), new GridConstraints(row,
                                                                          0,
                                                                          1,
                                                                          2,
                                                                          GridConstraints.ANCHOR_WEST,
                                                                          GridConstraints.FILL_HORIZONTAL,
                                                                          GridConstraints.SIZEPOLICY_WANT_GROW,
                                                                          GridConstraints.SIZEPOLICY_FIXED,
                                                                          null,
                                                                          null,
                                                                          null));
                } catch (SQLException e) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(null, e);
                    }
                } finally {
                    session.commit();
                }
                myPlaylistsPanel.validate();
            }
        });
    }

    private void addPlaylist(final Playlist playlist, int row) {
        GridConstraints gbcName = new GridConstraints(row,
                                                      1,
                                                      1,
                                                      1,
                                                      GridConstraints.ANCHOR_WEST,
                                                      GridConstraints.FILL_HORIZONTAL,
                                                      GridConstraints.SIZEPOLICY_WANT_GROW,
                                                      GridConstraints.SIZEPOLICY_FIXED,
                                                      null,
                                                      null,
                                                      null);
        GridConstraints gbcActive = new GridConstraints(row,
                                                        0,
                                                        1,
                                                        1,
                                                        GridConstraints.ANCHOR_WEST,
                                                        GridConstraints.FILL_HORIZONTAL,
                                                        GridConstraints.SIZEPOLICY_FIXED,
                                                        GridConstraints.SIZEPOLICY_FIXED,
                                                        null,
                                                        null,
                                                        null);
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

    private void addPanelComponent(JComponent component, GridConstraints gridConstraints) {
        myPlaylistsPanel.add(component, gridConstraints);
    }
}