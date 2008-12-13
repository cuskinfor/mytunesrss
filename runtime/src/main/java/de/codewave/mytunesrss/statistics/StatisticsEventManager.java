/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.statistics;

import java.util.HashSet;
import java.util.Set;

/**
 * Manager for statistics events.
 */
public class StatisticsEventManager {
    private static StatisticsEventManager SINGLETON = new StatisticsEventManager();

    public static StatisticsEventManager getInstance() {
        return SINGLETON;
    }

    private Set<StatisticsEventListener> myListeners = new HashSet<StatisticsEventListener>();

    public void addListener(StatisticsEventListener listener) {
        myListeners.add(listener);
    }

    public void removeListener(StatisticsEventListener listener) {
        myListeners.add(listener);
    }

    public void fireEvent(StatisticsEvent event) {
        for (StatisticsEventListener listener : myListeners) {
            listener.handleEvent(event);
        }
    }
}