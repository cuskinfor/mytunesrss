package de.codewave.mytunesrss.settings;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssEvent;
import de.codewave.mytunesrss.MyTunesRssEventListener;
import de.codewave.mytunesrss.MyTunesRssEventManager;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.mytunesrss.datastore.statement.PlaylistType;
import de.codewave.mytunesrss.datastore.statement.SavePlaylistAttributesStatement;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.swing.SwingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * de.codewave.mytunesrss.settings.Content
 */
public class Content implements MyTunesRssEventListener, SettingsForm {
    private static final Logger LOG = LoggerFactory.getLogger(Content.class);

    private JPanel myRootPanel;
    private JScrollPane myScrollPane;
    private JPanel myPlaylistsPanel;

    public void init() {
        myScrollPane.getViewport().setOpaque(false);
        MyTunesRssEventManager.getInstance().addListener(this);
        initValues();
    }

    public void setGuiMode(GuiMode mode) {
        // intentionally left blank
    }

    public String updateConfigFromGui() {
        // intentionally left blank
        return null;
    }

    public JPanel getRootPanel() {
        return myRootPanel;
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
                    List<Playlist> playlists = session.executeQuery(new FindPlaylistQuery(null, null, null, true)).getResults();
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
                                                                          GridConstraints.FILL_BOTH,
                                                                          GridConstraints.SIZEPOLICY_WANT_GROW,
                                                                          GridConstraints.SIZEPOLICY_WANT_GROW,
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

    // todo: get name from i18n properties
    public String toString() {
        return "Content settings";
    }
}