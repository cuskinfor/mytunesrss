/*
 * Copyright (c) 2007, Codewave Software. All Rights Reserved.
 */

package de.codewave.mytunesrss.datastore.statement;

import de.codewave.mytunesrss.Pager;

import java.util.ArrayList;
import java.util.List;

/**
 * de.codewave.mytunesrss.datastore.statement.PagerConfig
 */
public class PagerConfig {
    public static final String[] CONDITION =
            new String[]{"first_char < 'a' OR first_char > 'z'", "first_char >= 'a' AND first_char < 'd'", "first_char >= 'd' AND first_char < 'g'",
                    "first_char >= 'g' AND first_char < 'j'", "first_char >= 'j' AND first_char < 'm'",
                    "first_char >= 'm' AND first_char < 'p'", "first_char >= 'p' AND first_char < 't'",
                    "first_char >= 't' AND first_char < 'w'", "first_char >= 'w' AND first_char <= 'z'"};

    public static final List<Pager.Page> PAGES = new ArrayList<Pager.Page>();

    static {
        PAGES.add(new Pager.Page("0", "0 - 9"));
        PAGES.add(new Pager.Page("1", "A - C"));
        PAGES.add(new Pager.Page("2", "D - F"));
        PAGES.add(new Pager.Page("3", "G - I"));
        PAGES.add(new Pager.Page("4", "J - L"));
        PAGES.add(new Pager.Page("5", "M - O"));
        PAGES.add(new Pager.Page("6", "P - S"));
        PAGES.add(new Pager.Page("7", "T - V"));
        PAGES.add(new Pager.Page("8", "W - Z"));
    }
}
