package de.codewave.mytunesrss.settings;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.datastore.statement.FindPlaylistQuery;
import de.codewave.mytunesrss.datastore.statement.Playlist;
import de.codewave.mytunesrss.datastore.statement.PlaylistType;
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

public abstract class PlaylistsDialog {
    private static final Logger LOG = LoggerFactory.getLogger(PlaylistsDialog.class);

    protected abstract JPanel getPlaylistsPanel();

    protected abstract List<PlaylistType> getTypes();

    protected void refreshPlaylistList() {
        SwingUtils.invokeAndWait(new Runnable() {
            public void run() {
                getPlaylistsPanel().removeAll();
                DataStoreSession session = MyTunesRss.STORE.getTransaction();
                try {
                    List<Playlist> playlists = session.executeQuery(new FindPlaylistQuery(getTypes(), null, null, true)).getResults();
                    Collections.sort(playlists, new Comparator<Playlist>() {
                        public int compare(Playlist o1, Playlist o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
                    getPlaylistsPanel().setLayout(new GridLayoutManager(playlists.size() + 1, 2));
                    int row = 0;
                    for (Playlist playlist : playlists) {
                        addPlaylist(playlist, row++);
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
                getPlaylistsPanel().validate();
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
        active.setSelected(isSelected(playlist));
        active.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                PlaylistsDialog.this.stateChanged(playlist, active.isSelected());
            }
        });
        addPanelComponent(active, gbcActive);
        JLabel name = new JLabel(playlist.getName());
        name.setOpaque(false);
        addPanelComponent(name, gbcName);
    }

    protected abstract boolean isSelected(Playlist playlist);

    protected abstract void stateChanged(Playlist playlist, boolean selected);

    private void addPanelComponent(JComponent component, GridConstraints gridConstraints) {
        getPlaylistsPanel().add(component, gridConstraints);
    }
}
