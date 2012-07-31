/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.ImageImportType;
import de.codewave.mytunesrss.config.FileType;
import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.ReplacementRule;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;
import de.codewave.vaadin.validation.ValidRegExpValidator;

import java.util.*;

public class DataImportConfigPanel extends MyTunesRssConfigPanel {

    private final Map<ImageImportType, ImageImportTypeRepresentation> IMPORT_TYPE_MAPPINGS = new HashMap<ImageImportType, ImageImportTypeRepresentation>();
    public final Protection PROTECTED = new Protection(true);
    public final Protection UNPROTECTED = new Protection(false);

    private Table myFileTypes;
    private Button myAddFileType;
    private Button myResetFileTypes;
    private SmartTextField myArtistDropWords;
    private SmartTextField myId3v2TrackComment;
    private SmartTextField myDisabledMp4Codecs;
    private Select myTrackImageImportType;
    private Select myPhotoThumbnailImportType;
    private Form myMiscForm;
    private Table myTrackImageMappingsTable;
    private Button myAddTrackImageMapping;

    public DataImportConfigPanel() {
        IMPORT_TYPE_MAPPINGS.put(ImageImportType.Auto, new ImageImportTypeRepresentation(ImageImportType.Auto));
        IMPORT_TYPE_MAPPINGS.put(ImageImportType.Never, new ImageImportTypeRepresentation(ImageImportType.Never));
        IMPORT_TYPE_MAPPINGS.put(ImageImportType.OnDemand, new ImageImportTypeRepresentation(ImageImportType.OnDemand));
    }

    public void attach() {
        super.attach();
        init(getBundleString("dataimportConfigPanel.caption"), getComponentFactory().createGridLayout(1, 4, true, true));
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
        myMiscForm = new Form();
        myMiscForm.setImmediate(true);
        addComponent(getComponentFactory().surroundWithPanel(myMiscForm, new Layout.MarginInfo(false, true, false, true), getBundleString("dataimportConfigPanel.misc.caption")));
        myArtistDropWords = getComponentFactory().createTextField("dataimportConfigPanel.artistDropWords");
        myId3v2TrackComment = getComponentFactory().createTextField("dataimportConfigPanel.id3v2TrackComment");
        myDisabledMp4Codecs = getComponentFactory().createTextField("dataimportConfigPanel.disabledMp4Codecs");
        myTrackImageImportType = getComponentFactory().createSelect("dataimportConfigPanel.trackImageImportType", Arrays.asList(IMPORT_TYPE_MAPPINGS.get(ImageImportType.Auto), IMPORT_TYPE_MAPPINGS.get(ImageImportType.Never)));
        myPhotoThumbnailImportType = getComponentFactory().createSelect("dataimportConfigPanel.photoThumbnailImportType", Arrays.asList(IMPORT_TYPE_MAPPINGS.get(ImageImportType.Auto), IMPORT_TYPE_MAPPINGS.get(ImageImportType.OnDemand)));
        myMiscForm.addField(myArtistDropWords, myArtistDropWords);
        myMiscForm.addField(myId3v2TrackComment, myId3v2TrackComment);
        myMiscForm.addField(myDisabledMp4Codecs, myDisabledMp4Codecs);
        myMiscForm.addField(myTrackImageImportType, myTrackImageImportType);
        myMiscForm.addField(myPhotoThumbnailImportType, myPhotoThumbnailImportType);
        Panel imageMappingsPanel = new Panel(getBundleString("dataimportConfigPanel.trackImageMapping.caption"), getComponentFactory().createVerticalLayout(true, true));
        addComponent(imageMappingsPanel);
        myTrackImageMappingsTable = new Table();
        myTrackImageMappingsTable.setCacheRate(50);
        myTrackImageMappingsTable.addContainerProperty("search", TextField.class, null, getBundleString("dataimportConfigPanel.imageMappingSearch"), null, null);
        myTrackImageMappingsTable.addContainerProperty("replace", TextField.class, null, getBundleString("dataimportConfigPanel.imageMappingReplace"), null, null);
        myTrackImageMappingsTable.addContainerProperty("delete", Button.class, null, "", null, null);
        myTrackImageMappingsTable.setEditable(false);
        imageMappingsPanel.addComponent(myTrackImageMappingsTable);
        myAddTrackImageMapping = getComponentFactory().createButton("dataimportConfigPanel.addImageMapping", this);
        imageMappingsPanel.addComponent(getComponentFactory().createHorizontalButtons(false, true, myAddTrackImageMapping));
        addDefaultComponents(0, 3, 0, 3, false);

        initFromConfig();
    }

    private void addTrackImageMapping(ReplacementRule replacement) {
        SmartTextField searchTextField = new SmartTextField();
        searchTextField.setValue(replacement.getSearchPattern());
        searchTextField.addValidator(new ValidRegExpValidator("dataimportConfigPanel.error.invalidSearchExpression"));
        searchTextField.setImmediate(true);
        myTrackImageMappingsTable.addItem(new Object[]{searchTextField, new SmartTextField(null, replacement.getReplacement()), getComponentFactory().createButton("button.delete", this)}, myItemIdGenerator.getAndIncrement());
    }

    protected void initFromConfig() {
        setFileTypes(MyTunesRss.CONFIG.getFileTypes());
        myArtistDropWords.setValue(MyTunesRss.CONFIG.getArtistDropWords());
        myId3v2TrackComment.setValue(MyTunesRss.CONFIG.getId3v2TrackComment());
        myDisabledMp4Codecs.setValue(MyTunesRss.CONFIG.getDisabledMp4Codecs());
        myTrackImageImportType.setValue(IMPORT_TYPE_MAPPINGS.get(MyTunesRss.CONFIG.getTrackImageImportType()));
        myPhotoThumbnailImportType.setValue(IMPORT_TYPE_MAPPINGS.get(MyTunesRss.CONFIG.getPhotoThumbnailImportType()));
        myTrackImageMappingsTable.removeAllItems();
        for (ReplacementRule mapping : MyTunesRss.CONFIG.getTrackImageMappings()) {
            addTrackImageMapping(mapping);
        }
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
        myTrackImageMappingsTable.setPageLength(Math.min(myTrackImageMappingsTable.getItemIds().size(), 5));
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
        MyTunesRss.CONFIG.setTrackImageImportType(((ImageImportTypeRepresentation) myTrackImageImportType.getValue()).getImageImportType());
        MyTunesRss.CONFIG.setPhotoThumbnailImportType(((ImageImportTypeRepresentation) myPhotoThumbnailImportType.getValue()).getImageImportType());
        List<ReplacementRule> mappings = new ArrayList<ReplacementRule>();
        for (Object itemId : myTrackImageMappingsTable.getItemIds()) {
            mappings.add(new ReplacementRule((String) getTableCellPropertyValue(myTrackImageMappingsTable, itemId, "search"), (String) getTableCellPropertyValue(myTrackImageMappingsTable, itemId, "replace")));
        }
        MyTunesRss.CONFIG.setTrackImageMappings(mappings);
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
        } else if (clickEvent.getSource() == myAddTrackImageMapping) {
            addTrackImageMapping(new ReplacementRule("^.*$", "\\0"));
            setTablePageLength();
        } else if (findTableItemWithObject(myTrackImageMappingsTable, clickEvent.getSource()) != null) {
            final Button yes = new Button(getBundleString("button.yes"));
            Button no = new Button(getBundleString("button.no"));
            new OptionWindow(30, Sizeable.UNITS_EM, null, getBundleString("dataimportConfigDialog.optionWindowDeleteTrackImageMapping.caption"), getBundleString("dataimportConfigDialog.optionWindowDeleteTrackImageMapping.message"), yes, no) {
                public void clicked(Button button) {
                    if (button == yes) {
                        myTrackImageMappingsTable.removeItem(findTableItemWithObject(myTrackImageMappingsTable, clickEvent.getSource()));
                        setTablePageLength();
                    }
                }
            }.show(VaadinUtils.getApplicationWindow(this));
        } else {
            super.buttonClick(clickEvent);
        }
    }

    protected boolean beforeSave() {
        if (!VaadinUtils.isValid(myTrackImageMappingsTable, myFileTypes, myMiscForm)) {
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

    public class ImageImportTypeRepresentation {

        private ImageImportType myImageImportType;

        public ImageImportTypeRepresentation(ImageImportType imageImportType) {
            myImageImportType = imageImportType;
        }

        public ImageImportType getImageImportType() {
            return myImageImportType;
        }

        @Override
        public String toString() {
            return getBundleString("dataimportConfigPanel.importType." + myImageImportType.name());
        }
    }
}