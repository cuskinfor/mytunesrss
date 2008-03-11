/*
 * Copyright (c) 2006, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * de.codewave.mytunesrss.jsp.Pager
 */
public class Pager {
    private List<Page> myPages;
    private List<Page> myCurrentPages;
    private int myPageCountInBar;
    private int myFirstPageInBar;

    public Pager(List<Page> pages, int pageCountInBar) {
        myPages = pages != null ? new ArrayList<Page>(pages) : new ArrayList<Page>();
        myCurrentPages = new ArrayList<Page>(myPageCountInBar);
        myPageCountInBar = pageCountInBar;
        myFirstPageInBar = 0;
        updateCurrentPages();
    }

    private void updateCurrentPages() {
        myCurrentPages.clear();
        for (ListIterator<Page> iterator = myPages.listIterator(myFirstPageInBar); iterator.hasNext() && myCurrentPages.size() < myPageCountInBar;) {
            myCurrentPages.add(iterator.next());
        }
    }

    public List<Page> getCurrentPages() {
        return Collections.unmodifiableList(myCurrentPages);
    }

    public boolean isFirst() {
        return myFirstPageInBar == 0;
    }

    public Page getFirstPage() {
        return myPages.get(0);
    }

    public Page getPreviousPage() {
        if (!isFirst()) {
            return myPages.get(myFirstPageInBar - 1);
        }
        return getFirstPage();
    }

    public boolean isLast() {
        return myFirstPageInBar + myPageCountInBar >= myPages.size();
    }

    public Page getLastPage() {
        return myPages.get(myPages.size() - 1);
    }

    public Page getNextPage() {
        if (!isLast()) {
            return myPages.get(myFirstPageInBar + myPageCountInBar);
        }
        return getLastPage();
    }

    public void moveToPage(int page) {
        myFirstPageInBar = myPageCountInBar * (page / myPageCountInBar);
        updateCurrentPages();
    }

    public static class Page<T> {
        private String myKey;
        private String myValue;
        private T myUserData;

        public Page(String key, String value) {
            myKey = key;
            myValue = value;
        }

        public Page(String key, String value, T userData) {
            myKey = key;
            myValue = value;
            myUserData = userData;
        }

        public String getKey() {
            return myKey;
        }

        public String getValue() {
            return myValue;
        }

        public T getUserData() {
            return myUserData;
        }
    }
}