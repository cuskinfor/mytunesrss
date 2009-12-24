package de.codewave.mytunesrss.settings;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import de.codewave.mytunesrss.*;
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
public class Content extends PlaylistsDialog implements MyTunesRssEventListener, SettingsForm {
    private static final Logger LOG = LoggerFactory.getLogger(Content.class);

    private JPanel myRootPanel;
    private JScrollPane myScrollPane;
    private JPanel myPlaylistsPanel;

    public Content() {
        myScrollPane.getViewport().setOpaque(false);
        MyTunesRssEventManager.getInstance().addListener(this);
    }

    public String updateConfigFromGui() {
        // intentionally left blank
        return null;
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public void initValues() {
        refreshPlaylistList();
    }

    public void handleEvent(final MyTunesRssEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (event.getType() == MyTunesRssEvent.EventType.DATABASE_PLAYLIST_UPDATED || event.getType() == MyTunesRssEvent.EventType.DATABASE_UPDATE_FINISHED) {
                    refreshPlaylistList();
                } else if (event.getType() == MyTunesRssEvent.EventType.CONFIGURATION_CHANGED) {
                    initValues();
                }
            }
        });
    }

    public String getDialogTitle() {
        return MyTunesRssUtils.getBundleString("dialog.content.title");
    }

    protected JPanel getPlaylistsPanel() {
        return myPlaylistsPanel;
    }

    protected boolean isSelected(Playlist playlist) {
        return !playlist.isHidden();
    }

    protected void stateChanged(Playlist playlist, boolean selected) {
        DataStoreSession session = MyTunesRss.STORE.getTransaction();
        SavePlaylistAttributesStatement statement = new SavePlaylistAttributesStatement();
        statement.setId(playlist.getId());
        statement.setHidden(!selected);
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