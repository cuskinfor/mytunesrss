/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.vaadin.component;

import com.vaadin.terminal.Resource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;

import java.io.File;
import java.util.regex.Pattern;

public abstract class ServerSideFileChooserWindow extends Window {

    public void show(Window parent) {
        parent.addWindow(this);
    }

    public ServerSideFileChooserWindow(float width, int units, Resource icon, String caption, File currentDir, Pattern allowedDirPattern, Pattern allowedFilePattern, boolean allowCreateFolders, String rootsLabel) {
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
        setContent(new ServerSideFileChooser(currentDir, allowedDirPattern, allowedFilePattern, allowCreateFolders, rootsLabel) {
            @Override
            protected void onCancel() {
                getWindow().getParent().removeWindow(getWindow());
            }

            @Override
            protected void onFileSelected(File file) {
                ServerSideFileChooserWindow.this.onFileSelected(file);
            }
        });
    }

    protected abstract void onFileSelected(File file);
}