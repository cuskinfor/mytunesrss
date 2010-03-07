/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.vaadin.component;

import com.vaadin.data.util.MethodProperty;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public abstract class ServerSideFileChooser extends CustomComponent implements Button.ClickListener, ItemClickEvent.ItemClickListener {

    private Table myChooser;
    private File myCurrentDir;
    private Button myOk;
    private Button myCancel;
    private Button myCreateDir;
    private boolean myAllowSelectDirectory;
    private boolean myAllowSelectFile;
    private Pattern myAllowedNamePattern;
    private Label myCurrentDirLabel;

    public ServerSideFileChooser(File currentDir, boolean allowSelectDirectory, boolean allowSelectFile, Pattern allowedNamePattern, boolean allowCreateDir) {
        myAllowSelectDirectory = allowSelectDirectory;
        myAllowSelectFile = allowSelectFile;
        myAllowedNamePattern = allowedNamePattern;
        if (currentDir.exists()) {
            myCurrentDir = currentDir.isDirectory() ? currentDir : currentDir.getParentFile();
        } else {
            myCurrentDir = new File("/");
        }
        Panel panel = new Panel();
        ((Layout) panel.getContent()).setMargin(true);
        ((Layout.SpacingHandler) panel.getContent()).setSpacing(true);
        myCurrentDirLabel = new Label();
        panel.addComponent(myCurrentDirLabel);
        myChooser = new Table();
        myChooser.setWidth(100f, Sizeable.UNITS_PERCENTAGE);
        myChooser.setSelectable(true);
        myChooser.addContainerProperty("File", String.class, "");
        myChooser.addContainerProperty("Date", String.class, null);
        myChooser.setColumnExpandRatio("File", 1f);
        myChooser.addListener(this);
        myOk = new Button("Ok", this); // TODO i18n
        myOk.setEnabled(false);
        myCancel = new Button("Cancel", this); // TODO i18n
        myCreateDir = new Button("Create folder", this); // TODO i18n
        panel.addComponent(myChooser);
        Panel buttonPanel = new Panel(new HorizontalLayout());
        ((Layout.SpacingHandler) buttonPanel.getContent()).setSpacing(true);
        buttonPanel.addStyleName("light");
        if (allowCreateDir) {
            buttonPanel.addComponent(myCreateDir);
        }
        buttonPanel.addComponent(myOk);
        buttonPanel.addComponent(myCancel);
        panel.addComponent(buttonPanel);
        setCompositionRoot(panel);
        setFiles();
    }

    private void setFiles() {
        myCurrentDirLabel.setValue(myCurrentDir.getAbsolutePath());
        myOk.setEnabled(false);
        myChooser.removeAllItems();
        if (myCurrentDir.getParentFile() != null) {
            myChooser.addItem(new Object[]{"[..]", null}, myCurrentDir.getParentFile());
        }
        for (File file : myCurrentDir.listFiles()) {
            myChooser.addItem(new Object[]{file.getName(), new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(file.lastModified()))}, file);
        }
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        if (clickEvent.getButton() == myOk) {
            File file = (File) myChooser.getValue();
            if (isAllowedSelect(file)) {
                onFileSelected(file);
            }
        } else if (clickEvent.getButton() == myCancel) {
            onCancel();
        } else if (clickEvent.getButton() == myCreateDir) {
            new TextFieldWindow(30, Sizeable.UNITS_EM, null, "Create folder", "Please enter the name of the new folder.", "Create", "Cancel") {
                @Override
                protected void onOk(String text) {
                    ServerSideFileChooser.this.getApplication().getMainWindow().removeWindow(this);
                    new File(myCurrentDir, text).mkdir();
                    setFiles();
                }
            }.show(getApplication().getMainWindow());
        }
    }

    private boolean isAllowedSelect(File file) {
        if (file != null && file.exists()) {
            if (file.isFile() && myAllowSelectFile || file.isDirectory() && myAllowSelectDirectory) {
                return myAllowedNamePattern == null || myAllowedNamePattern.matcher(file.getName()).matches();
            }
        }
        return false;
    }

    public void itemClick(ItemClickEvent itemClickEvent) {
        File file = (File) itemClickEvent.getItemId();
        myOk.setEnabled(isAllowedSelect(file));
        if (itemClickEvent.isDoubleClick()) {
            if (file.isDirectory()) {
                myCurrentDir = file;
                setFiles();
            }
        }
    }

    protected abstract void onCancel();

    protected abstract void onFileSelected(File file);
}
