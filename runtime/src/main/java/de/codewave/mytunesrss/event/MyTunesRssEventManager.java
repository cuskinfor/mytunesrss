/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.event;

import java.util.HashSet;
import java.util.Set;

/**
 * de.codewave.mytunesrss.event.MyTunesRssEventManager
 */
public class MyTunesRssEventManager {
    private static MyTunesRssEventManager SINGLETON = new MyTunesRssEventManager();

    public static MyTunesRssEventManager getInstance() {
        return SINGLETON;
    }

    private Set<MyTunesRssEventListener> myListeners = new HashSet<>();

    public void addListener(MyTunesRssEventListener listener) {
        myListeners.add(listener);
    }

    public void removeListener(MyTunesRssEventListener listener) {
        myListeners.remove(listener);
    }

    public void fireEvent(MyTunesRssEvent event) {
        for (MyTunesRssEventListener listener : myListeners) {
            listener.handleEvent(event);
        }
    }
}