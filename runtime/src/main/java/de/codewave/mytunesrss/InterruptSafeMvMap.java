/*
 * Copyright (c) 2013. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss;

import org.h2.mvstore.MVMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class InterruptSafeMvMap<K, V> implements Map<K, V> {

    private final MVMap<K, V> myDelegate;

    public InterruptSafeMvMap(MVMap<K, V> delegate) {
        myDelegate = delegate;
    }

    @Override
    public int size() {
        boolean interrupted = Thread.interrupted();
        try {
            return myDelegate.size();
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public boolean isEmpty() {
        boolean interrupted = Thread.interrupted();
        try {
            return myDelegate.isEmpty();
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public boolean containsKey(Object o) {
        boolean interrupted = Thread.interrupted();
        try {
            return myDelegate.containsKey(o);
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public boolean containsValue(Object o) {
        boolean interrupted = Thread.interrupted();
        try {
            return myDelegate.containsValue(o);
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public V get(Object o) {
        boolean interrupted = Thread.interrupted();
        try {
            return myDelegate.get(o);
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public V put(K k, V v) {
        boolean interrupted = Thread.interrupted();
        try {
            return myDelegate.put(k, v);
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public V remove(Object o) {
        boolean interrupted = Thread.interrupted();
        try {
            return myDelegate.remove(o);
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        boolean interrupted = Thread.interrupted();
        try {
            myDelegate.putAll(map);
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void clear() {
        boolean interrupted = Thread.interrupted();
        try {
            myDelegate.clear();
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public Set<K> keySet() {
        boolean interrupted = Thread.interrupted();
        try {
            return myDelegate.keySet();
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public Collection<V> values() {
        boolean interrupted = Thread.interrupted();
        try {
            return myDelegate.values();
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        boolean interrupted = Thread.interrupted();
        try {
            return myDelegate.entrySet();
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
