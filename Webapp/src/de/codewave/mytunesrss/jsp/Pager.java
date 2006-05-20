/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.jsp;

import java.util.*;

/**
 * de.codewave.mytunesrss.jsp.Pager
 */
public class Pager {
    private List<PagerItem> myItems;
    private List<PagerItem> myCurrentItems;
    private int myItemCountInBar;
    private int myFirstItemInBar;

    public Pager(List<PagerItem> items, int itemCountInBar) {
        myItems = items != null ? new ArrayList<PagerItem>(items) : new ArrayList<PagerItem>();
        myCurrentItems = new ArrayList<PagerItem>(myItemCountInBar);
        myItemCountInBar = itemCountInBar;
        myFirstItemInBar = 0;
        updateCurrentItems();
    }

    private void updateCurrentItems() {
        myCurrentItems.clear();
        for (ListIterator<PagerItem> iterator = myItems.listIterator(myFirstItemInBar);
                iterator.hasNext() && myCurrentItems.size() < myItemCountInBar;) {
            myCurrentItems.add(iterator.next());
        }
    }

    public void addItem(PagerItem item) {
        myItems.add(item);
    }

    public List<PagerItem> getCurrentItems() {
        return Collections.unmodifiableList(myCurrentItems);
    }

    public boolean isFirst() {
        return myFirstItemInBar == 0;
    }

    public boolean isLast() {
        return myFirstItemInBar + myItemCountInBar >= myItems.size();
    }

    public void moveToBegin() {
        if (!isFirst()) {
            myFirstItemInBar = 0;
            updateCurrentItems();
        }
    }

    public void moveBackward() {
        if (!isFirst()) {
            myFirstItemInBar = Math.max(0, myFirstItemInBar - myItemCountInBar);
            updateCurrentItems();
        }
    }

    public void moveForward() {
        if (!isLast()) {
            myFirstItemInBar += myItemCountInBar;
            updateCurrentItems();
        }
    }

    public void moveTo(int index) {
        myFirstItemInBar = myItemCountInBar * (index / myItemCountInBar);
        updateCurrentItems();
    }

    public static class PagerItem {
        private String myKey;
        private String myValue;
        private Map<String, Object> myUserData = new HashMap<String, Object>();

        public PagerItem(String key, String value) {
            myKey = key;
            myValue = value;
        }

        public PagerItem(String key, String value, Map<String, Object> userData) {
            this(key, value);
            myUserData = new HashMap<String, Object>(userData);
        }

        public String getKey() {
            return myKey;
        }

        public String getValue() {
            return myValue;
        }

        public Map<String, Object> getUserData() {
            return myUserData;
        }
    }
}