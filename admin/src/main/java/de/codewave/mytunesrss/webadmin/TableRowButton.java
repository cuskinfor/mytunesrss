/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.ui.Button;

public class TableRowButton extends Button {
    private Object myItemId;
    private Object myData;

    public TableRowButton(String caption, ClickListener listener, Object itemId, Object data) {
        super(caption, listener);
        myItemId = itemId;
        myData = data;
    }

    public Object getItemId() {
        return myItemId;
    }

    public Object getData() {
        return myData;
    }

    public void deleteTableRow() {
        ((Container) getParent()).removeItem(myItemId);
    }

    public Item getItem() {
        return ((Container) getParent()).getItem(myItemId);
    }
}
