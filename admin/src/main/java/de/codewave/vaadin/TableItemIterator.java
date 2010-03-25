/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.vaadin;

import com.vaadin.data.Item;
import com.vaadin.ui.Table;

import java.util.Iterator;

public class TableItemIterator implements Iterator<Item> {
    private Table myTable;
    private Iterator myItemIdsIterator;

    public TableItemIterator(Table table) {
        myTable = table;
        myItemIdsIterator = table.getItemIds().iterator();
    }

    public boolean hasNext() {
        return myItemIdsIterator.hasNext();
    }

    public Item next() {
        return myTable.getItem(myItemIdsIterator.next());
    }

    public void remove() {
        throw new UnsupportedOperationException("Remove is not supported by this iterator.");
    }
}
