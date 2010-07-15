/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.vaadin.component;

import com.vaadin.terminal.Resource;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;

public class SinglePanelWindow extends Window {

    public void show(Window parent) {
        parent.addWindow(this);
    }

    public SinglePanelWindow(float width, int units, Resource icon, String caption, Panel panel) {
        super();
        if (caption != null) {
            setCaption(caption);
        }
        if (icon != null) {
            setIcon(icon);
        }
        setWidth(width, units);
        setModal(true);
        setClosable(false);
        setResizable(false);
        setDraggable(false);
        setContent(panel);
    }
}