/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.vaadin.component;

import com.vaadin.terminal.Resource;
import com.vaadin.ui.*;

public abstract class OptionWindow extends Window implements Button.ClickListener {

    public void show(Window parent) {
        parent.addWindow(this);
    }

    public OptionWindow(float width, int units, Resource icon, String caption, String message, Button... buttons) {
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
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setMargin(true);
        verticalLayout.setSpacing(true);
        setContent(verticalLayout);
        Label label = new Label(message);
        addComponent(label);
        Panel panel = new Panel();
        panel.setStyleName("light");
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        panel.setContent(horizontalLayout);
        horizontalLayout.setSpacing(true);
        addComponent(panel);
        verticalLayout.setComponentAlignment(panel, Alignment.MIDDLE_RIGHT);
        for (Button button : buttons) {
            button.addListener(this);
            panel.addComponent(button);
        }
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        clicked(clickEvent.getButton());
        close();
    }

    protected abstract void clicked(Button button);
}
