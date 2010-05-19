/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.FileType;
import de.codewave.mytunesrss.MediaType;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;

import java.util.Arrays;
import java.util.List;

public class DataImportConfigPanel extends MyTunesRssConfigPanel {

    public final Protection PROTECTED = new Protection(true);
    public final Protection UNPROTECTED = new Protection(false);

    private Table myFileTypes;
    private Button myAddFileType;
    private Button myResetFileTypes;
    private SmartTextField myArtistDropWords;
    private SmartTextField myId3v2TrackComment;
    private SmartTextField myDisabledMp4Codecs;
    private CheckBox myIgnoreArtwork;
    private CheckBox myIgnoreTimestamps;
    private Form myMiscForm;

    public void attach() {
        init(getBundleString("dataimportConfigPanel.caption"), getComponentFactory().createGridLayout(1, 3, true, true));
        Panel typesPanel = new Panel(getBundleString("dataimportConfigPanel.caption.types"), getComponentFactory().createVerticalLayout(true, true));
        addComponent(typesPanel);
        myFileTypes = new Table();
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
        myMiscForm = new Form();
        myMiscForm.setImmediate(true);
        addComponent(getComponentFactory().surroundWithPanel(myMiscForm, new Layout.MarginInfo(false, true, false, true), getBundleString("dataimportConfigPanel.misc.caption")));
        myArtistDropWords = getComponentFactory().createTextField("dataimportConfigPanel.artistDropWords");
        myId3v2TrackComment = getComponentFactory().createTextField("dataimportConfigPanel.id3v2TrackComment");
        myDisabledMp4Codecs = getComponentFactory().createTextField("dataimportConfigPanel.disabledMp4Codecs");
        myIgnoreArtwork = getComponentFactory().createCheckBox("dataimportConfigPanel.ignoreArtwork");
        myIgnoreTimestamps = getComponentFactory().createCheckBox("dataimportConfigPanel.ignoreTimestamps");
        myMiscForm.addField(myArtistDropWords, myArtistDropWords);
        myMiscForm.addField(myId3v2TrackComment, myId3v2TrackComment);
        myMiscForm.addField(myDisabledMp4Codecs, myDisabledMp4Codecs);
        myMiscForm.addField(myIgnoreArtwork, myIgnoreArtwork);
        myMiscForm.addField(myIgnoreTimestamps, myIgnoreTimestamps);

        attach(0, 2, 0, 2);

        initFromConfig();
    }

    protected void initFromConfig() {
        setFileTypes(MyTunesRss.CONFIG.getFileTypes());
        myArtistDropWords.setValue(MyTunesRss.CONFIG.getArtistDropWords());
        myId3v2TrackComment.setValue(MyTunesRss.CONFIG.getId3v2TrackComment());
        myDisabledMp4Codecs.setValue(MyTunesRss.CONFIG.getDisabledMp4Codecs());
        myIgnoreArtwork.setValue(MyTunesRss.CONFIG.isIgnoreArtwork());
        myIgnoreTimestamps.setValue(MyTunesRss.CONFIG.isIgnoreTimestamps());
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
        MyTunesRss.CONFIG.setArtistDropWords(myArtistDropWords.getStringValue(null));
        MyTunesRss.CONFIG.setId3v2TrackComment(myId3v2TrackComment.getStringValue(null));
        MyTunesRss.CONFIG.setDisabledMp4Codecs(myDisabledMp4Codecs.getStringValue(null));
        MyTunesRss.CONFIG.setIgnoreArtwork(myIgnoreArtwork.booleanValue());
        MyTunesRss.CONFIG.setIgnoreTimestamps(myIgnoreTimestamps.booleanValue());
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
            }.show(getApplication().getMainWindow());
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
            }.show(getApplication().getMainWindow());
        } else {
            super.buttonClick(clickEvent);
        }
    }

    protected boolean beforeSave() {
        if (!VaadinUtils.isValid(myFileTypes, myMiscForm)) {
            getApplication().showError("error.formInvalid");
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