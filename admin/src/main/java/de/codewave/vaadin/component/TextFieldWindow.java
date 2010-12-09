/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.vaadin.component;

import com.vaadin.Application;
import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;

public abstract class TextFieldWindow extends Window implements Button.ClickListener {

    private TextField myTextField;
    private Button myOkButton;
    private Button myCancelButton;

    public void show(Window parent) {
        parent.addWindow(this);
    }

    public TextFieldWindow(float width, int units, String value, Resource icon, String caption, String message, String okButtonText, String cancelButtonText) {
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
        myTextField = new TextField();
        myTextField.setValue(value);
        myTextField.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        addComponent(myTextField);
        Panel panel = new Panel();
        addComponent(panel);
        verticalLayout.setComponentAlignment(panel, Alignment.MIDDLE_RIGHT);
        panel.setStyleName("light");
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        panel.setContent(horizontalLayout);
        horizontalLayout.setSpacing(true);
        verticalLayout.setComponentAlignment(panel, Alignment.MIDDLE_RIGHT);
        myCancelButton = new Button(cancelButtonText, this);
        panel.addComponent(myCancelButton);
        myOkButton = new Button(okButtonText, this);
        panel.addComponent(myOkButton);
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        if (clickEvent.getSource() == myOkButton) {
            onOk((String) myTextField.getValue());
        } else {
            onCancel();
        }
    }

    protected void onCancel() {
        getParent().removeWindow(this);
    }

    protected abstract void onOk(String text);


}