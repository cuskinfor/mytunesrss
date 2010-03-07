/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.vaadin;

import com.vaadin.data.Validator;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.ResourceBundle;

public class ComponentFactory {

    private ResourceBundle myBundle;

    public ComponentFactory(ResourceBundle bundle) {
        myBundle = bundle;
    }

    private String getBundleString(String key, Object... parameters) {
        if (parameters == null || parameters.length == 0) {
            return myBundle.getString(key);
        }
        return MessageFormat.format(myBundle.getString(key), parameters);
    }


    public TextField createTextField(String labelKey) {
        return createTextField(labelKey, null);
    }

    public TextField createTextField(String labelKey, Validator validator) {
        TextField textField = new TextField(getBundleString(labelKey));
        textField.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        if (validator != null) {
            textField.addValidator(validator);
        }
        return textField;
    }

    public void setRequired(Field field, String requiredErrorKey, Object... parameters) {
        field.setRequired(true);
        field.setRequiredError(getBundleString(requiredErrorKey, parameters));
    }

    public void setOptional(Field field) {
        field.setRequired(false);
        field.setRequiredError(null);
    }

    public TextField createPasswordTextField(String labelKey) {
        TextField textField = createTextField(labelKey);
        textField.setSecret(true);
        return textField;
    }

    public Button createButton(String textKey, Button.ClickListener listener) {
        Button button = new Button(getBundleString(textKey));
        button.addListener(listener);
        return button;
    }

    public CheckBox createCheckBox(String labelKey) {
        return new CheckBox(getBundleString(labelKey));
    }

    public Select createSelect(String labelKey, Collection<? extends Object> options) {
        String caption = labelKey != null ? getBundleString(labelKey) : null;
        Select select = new Select(caption, options);
        select.setNullSelectionAllowed(false);
        return select;
    }

    public GridLayout createGridLayout(int columns, int rows, boolean margin, boolean spacing) {
        GridLayout gridLayout = new GridLayout(columns, rows);
        gridLayout.setMargin(margin);
        gridLayout.setSpacing(spacing);
        return gridLayout;
    }

    public Form createForm(String caption, boolean immediate) {
        Form form = new Form();
        form.setImmediate(immediate);
        if (caption != null) {
            form.setCaption(caption);
        }
        return form;
    }

    public HorizontalLayout createHorizontalLayout(boolean margin, boolean spacing) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setMargin(margin);
        layout.setSpacing(spacing);
        return layout;
    }

    public VerticalLayout createVerticalLayout(boolean margin, boolean spacing) {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(margin);
        layout.setSpacing(spacing);
        return layout;
    }

    public Panel surroundWithPanel(Component component, Layout.MarginInfo marginInfo, String caption) {
        VerticalLayout verticalLayout = createVerticalLayout(false, false);
        verticalLayout.setMargin(marginInfo);
        Panel panel = new Panel(verticalLayout);
        if (caption != null) {
            panel.setCaption(caption);
        }
        panel.addComponent(component);
        return panel;
    }

    public Panel createHorizontalButtons(boolean margin, boolean spacing, Button... buttons) {
        Panel panel = new Panel(createHorizontalLayout(margin, spacing));
        panel.setStyleName("light");
        for (Button button : buttons) {
            panel.addComponent(button);
        }
        return panel;
    }
}
