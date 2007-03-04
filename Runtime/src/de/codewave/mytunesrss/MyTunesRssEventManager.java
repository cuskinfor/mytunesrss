/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import java.util.*;

/**
 * de.codewave.mytunesrss.MyTunesRssEventManager
 */
public class MyTunesRssEventManager {
    private static MyTunesRssEventManager SINGLETON = new MyTunesRssEventManager();

    public static MyTunesRssEventManager getInstance() {
        return SINGLETON;
    }

    private Set<MyTunesRssEventListener> myListeners = new HashSet<MyTunesRssEventListener>();

    public void addListener(MyTunesRssEventListener listener) {
        myListeners.add(listener);
    }

    public void removeListener(MyTunesRssEventListener listener) {
        myListeners.add(listener);
    }

    public void fireEvent(MyTunesRssEvent event) {
        for (MyTunesRssEventListener listener : myListeners) {
            listener.handleEvent(event);
        }
    }
}