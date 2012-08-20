/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.ImageImportType;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.FileType;
import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.config.ReplacementRule;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;
import de.codewave.vaadin.validation.ValidRegExpValidator;

import java.util.*;

public class DataImportConfigPanel extends MyTunesRssConfigPanel {

    public final Protection PROTECTED = new Protection(true);
    public final Protection UNPROTECTED = new Protection(false);

    private Table myFileTypes;
    private Button myAddFileType;
    private Button myResetFileTypes;

    public void attach() {
        super.attach();
        init(getBundleString("dataimportConfigPanel.caption"), getComponentFactory().createGridLayout(1, 2, true, true));
        Panel typesPanel = new Panel(getBundleString("dataimportConfigPanel.caption.types"), getComponentFactory().createVerticalLayout(true, true));
        addComponent(typesPanel);
        myFileTypes = new Table();
        myFileTypes.setCacheRate(50);
        myFileTypes.addContainerProperty("active", CheckBox.class, null, getBundleString("dataimportConfigPanel.fileTypes.active"), null, null);
        myFileTypes.addContainerProperty("suffix", TextField.class, null, getBundleString("dataimportConfigPanel.fileTypes.suffix"), null, null);
        myFileTypes.addContainerProperty("mimeType", TextField.class, null, getBundleString("dataimportConfigPanel.fileTypes.mimeType"), null, null);
        myFileTypes.addContainerProperty("mediaType", Select.class, null, getBundleString("dataimportConfigPanel.fileTypes.mediaType"), null, null);
        myFileTypes.addContainerProperty("protection", Select.class, null, getBundleString("dataimportConfigPanel.fileTypes.protection"), null, null);
        myFileTypes.addContainerProperty("delete", Button.class, null, "", null, null);
        myFileTypes.setEditable(false);
        typesPanel.addComponent(myFileTypes);
        myAddFileType = getComponentFactory().createButton("dataimportConfigPanel.fileTypes.add", this);
        myResetFileTypes = getComponentFactory().createButton("dataimportConfigPanel.fileTypes.reset", this);
        typesPanel.addComponent(getComponentFactory().createHorizontalButtons(false, true, myAddFileType, myResetFileTypes));

        addDefaultComponents(0, 1, 0, 1, false);

        initFromConfig();
    }

    protected void initFromConfig() {
        setFileTypes(MyTunesRss.CONFIG.getFileTypes());
        setTablePageLength();
    }

    private void setFileTypes(List<FileType> fileTypes) {
        myFileTypes.removeAllItems();
        for (FileType fileType : fileTypes) {
            addFileType(fileType);
        }
    }

    private void addFileType(FileType fileType) {
        CheckBox active = new CheckBox();
        active.setValue(fileType.isActive());
        SmartTextField suffix = new SmartTextField();
        suffix.setValue(fileType.getSuffix());
        SmartTextField mimeType = new SmartTextField();
        mimeType.setValue(fileType.getMimeType());
        Select mediaType = getComponentFactory().createSelect(null, Arrays.asList(MediaType.Audio, MediaType.Video, MediaType.Image, MediaType.Other));
        mediaType.setValue(fileType.getMediaType());
        Select protection = getComponentFactory().createSelect(null, Arrays.asList(PROTECTED, UNPROTECTED));
        protection.setValue(fileType.isProtected() ? PROTECTED : UNPROTECTED);
        Button delete = new Button(getBundleString("button.delete"), this);
        long id = myItemIdGenerator.getAndIncrement();
        delete.setData(id);
        myFileTypes.addItem(new Object[]{active, suffix, mimeType, mediaType, protection, delete}, id);
    }

    private void setTablePageLength() {
        myFileTypes.setPageLength(Math.min(myFileTypes.size(), 10));
    }

    protected void writeToConfig() {
        MyTunesRss.CONFIG.getFileTypes().clear();
        for (Object itemId : myFileTypes.getItemIds()) {
            Boolean active = (Boolean) getTableCellPropertyValue(myFileTypes, itemId, "active");
            String suffix = (String) getTableCellPropertyValue(myFileTypes, itemId, "suffix");
            String mimeType = (String) getTableCellPropertyValue(myFileTypes, itemId, "mimeType");
            MediaType mediaType = (MediaType) getTableCellPropertyValue(myFileTypes, itemId, "mediaType");
            Protection protection = (Protection) getTableCellPropertyValue(myFileTypes, itemId, "protection");
            MyTunesRss.CONFIG.getFileTypes().add(new FileType(active, suffix, mimeType, mediaType, protection == PROTECTED));
        }
        MyTunesRss.CONFIG.save();
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        if (clickEvent.getSource() == myAddFileType) {
            addFileType(new FileType(false, "", "application/octet-stream", MediaType.Other, false));
            setTablePageLength();
            myFileTypes.setCurrentPageFirstItemIndex(Math.max(myFileTypes.size() - 10, 0));
        } else if (clickEvent.getSource() == myResetFileTypes) {
            final Button yes = new Button(getBundleString("button.yes"));
            Button no = new Button(getBundleString("button.no"));
            new OptionWindow(30, Sizeable.UNITS_EM, null, getBundleString("dataimportConfigDialog.optionWindowResetFileType.caption"), getBundleString("dataimportConfigDialog.optionWindowResetFileType.message"), yes, no) {
                public void clicked(Button button) {
                    if (button == yes) {
                        setFileTypes(FileType.getDefaults());
                        setTablePageLength();
                    }
                }
            }.show(getWindow());
        } else if (clickEvent.getSource() instanceof Button && ((Component) clickEvent.getSource()).getParent() == myFileTypes) {
            final Button yes = new Button(getBundleString("button.yes"));
            Button no = new Button(getBundleString("button.no"));
            new OptionWindow(30, Sizeable.UNITS_EM, null, getBundleString("dataimportConfigDialog.optionWindowDeleteFileType.caption"), getBundleString("dataimportConfigDialog.optionWindowDeleteFileType.message"), yes, no) {
                public void clicked(Button button) {
                    if (button == yes) {
                        myFileTypes.removeItem(((AbstractComponent) clickEvent.getSource()).getData());
                        setTablePageLength();
                    }
                }
            }.show(getWindow());
        } else {
            super.buttonClick(clickEvent);
        }
    }

    protected boolean beforeSave() {
        if (!VaadinUtils.isValid(myFileTypes)) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
            return false;
        }
        return true;
    }

    public class Protection {

        boolean myProtected;

        public Protection(boolean aProtected) {
            myProtected = aProtected;
        }

        @Override
        public String toString() {
            if (myProtected) {
                return getBundleString("dataimportConfigPanel.fileTypes.protection.true");
            } else {
                return getBundleString("dataimportConfigPanel.fileTypes.protection.false");
            }
        }
    }
}