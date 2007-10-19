package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.*;

import javax.swing.*;

/**
 * de.codewave.mytunesrss.settings.Content
 */
public class Content implements MyTunesRssEventListener {
    private JPanel myRootPanel;
    private JScrollPane myScrollPane;
    private JPanel myPlaylistsPanel;

    public void init() {
        myScrollPane.getViewport().setOpaque(false);
        MyTunesRssEventManager.getInstance().addListener(this);
    }

    public void handleEvent(MyTunesRssEvent event) {
    }
}