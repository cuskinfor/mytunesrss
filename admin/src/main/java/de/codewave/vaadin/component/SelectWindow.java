/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.vaadin.component;

import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;

import java.util.Collection;

public abstract class SelectWindow<T> extends Window implements Button.ClickListener {

    private Select mySelect;
    private Button myOkButton;
    private Button myCancelButton;

    public void show(Window parent) {
        parent.addWindow(this);
    }

    public SelectWindow(float width, int units, Collection<T> items, T selectedItem, Resource icon, String caption, String message, String okButtonText, String cancelButtonText) {
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
        mySelect = new Select(null, items);
        mySelect.setNullSelectionAllowed(false);
        mySelect.setValue(selectedItem != null ? selectedItem : items.iterator().next());
        mySelect.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        addComponent(mySelect);
        Panel panel = new Panel();
        addComponent(panel);
        verticalLayout.setComponentAlignment(panel, Alignment.MIDDLE_RIGHT);
        panel.addStyleName("light");
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        panel.setContent(horizontalLayout);
        horizontalLayout.setSpacing(true);
        verticalLayout.setComponentAlignment(panel, Alignment.MIDDLE_RIGHT);
        myCancelButton = new Button(cancelButtonText, this);
        panel.addComponent(myCancelButton);
        myOkButton = new Button(okButtonText, this);
        panel.addComponent(myOkButton);
    }

    @Override
    public void buttonClick(Button.ClickEvent clickEvent) {
        if (clickEvent.getSource() == myOkButton) {
            onOk((T) mySelect.getValue());
        } else {
            onCancel();
        }
    }

    protected void onCancel() {
        getParent().removeWindow(this);
    }

    protected abstract void onOk(T selectedItem);


}