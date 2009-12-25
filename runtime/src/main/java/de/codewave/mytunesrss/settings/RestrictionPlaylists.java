/*
 * Copyright (c) 2009. Codewave Software Michael Descher.
 * All rights reserved.
 */

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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.awt.*;

/**
 * de.codewave.mytunesrss.settings.RestrictionPlaylists
 */
public class RestrictionPlaylists extends PlaylistsDialog implements MyTunesRssEventListener, SettingsForm {
    private static final List<PlaylistType> PLAYLIST_TYPES = Arrays.asList(new PlaylistType[]{PlaylistType.ITunes, PlaylistType.ITunesFolder, PlaylistType.M3uFile, PlaylistType.MyTunes, PlaylistType.MyTunesSmart});

    private JPanel myRootPanel;
    private JScrollPane myScrollPane;
    private JPanel myPlaylistsPanel;
    private User myUser;

    public RestrictionPlaylists(User user) {
        myUser = user;
        myScrollPane.getViewport().setOpaque(false);
    }

    public void initValues() {
        refreshPlaylistList();
    }

    public String updateConfigFromGui() {
        // nothing to do here
        return null;
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public String getDialogTitle() {
        return MyTunesRssUtils.getBundleString("restrictionPlaylistsTitle", myUser.getName());
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

    protected JPanel getPlaylistsPanel() {
        return myPlaylistsPanel;
    }

    @Override
    protected List<PlaylistType> getTypes() {
        return PLAYLIST_TYPES;
    }

    protected boolean isSelected(Playlist playlist) {
        return myUser.getPlaylistIds().contains(playlist.getId());
    }

    protected void stateChanged(Playlist playlist, boolean selected) {
        if (selected) {
            myUser.addPlaylistId(playlist.getId());
        } else {
            myUser.removePlaylistId(playlist.getId());
        }
    }
}