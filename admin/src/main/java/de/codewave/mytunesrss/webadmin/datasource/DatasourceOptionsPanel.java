/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.datasource;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.ImageImportType;
import de.codewave.mytunesrss.config.*;
import de.codewave.mytunesrss.webadmin.MyTunesRssConfigPanel;
import de.codewave.vaadin.SmartField;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;
import de.codewave.vaadin.validation.ValidRegExpValidator;

import java.util.*;

public abstract class DatasourceOptionsPanel extends MyTunesRssConfigPanel {

    public class VideoTypeRepresentation {
        private VideoType myVideoType;

        public VideoTypeRepresentation(VideoType videoType) {
            myVideoType = videoType;
        }

        public VideoType getVideoType() {
            return myVideoType;
        }

        @Override
        public String toString() {
            return getBundleString("datasourceOptionsPanel.videoType." + myVideoType.name());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            VideoTypeRepresentation that = (VideoTypeRepresentation) o;

            if (myVideoType != that.myVideoType) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return myVideoType != null ? myVideoType.hashCode() : 0;
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
            return getBundleString("datasourceOptionsPanel.importType." + myImageImportType.name());
        }
    }

    public class Protection {
        boolean myProtected;

        public Protection(boolean aProtected) {
            myProtected = aProtected;
        }

        @Override
        public String toString() {
            if (myProtected) {
                return getBundleString("datasourceOptionsPanel.fileTypes.protection.true");
            } else {
                return getBundleString("datasourceOptionsPanel.fileTypes.protection.false");
            }
        }
    }

    protected final Protection PROTECTED = new Protection(true);
    protected final Protection UNPROTECTED = new Protection(false);

    protected final Map<ImageImportType, ImageImportTypeRepresentation> IMPORT_TYPE_MAPPINGS = new HashMap<ImageImportType, ImageImportTypeRepresentation>();

    protected Panel myImageMappingsPanel;
    protected Table myTrackImagePatternsTable;
    protected CheckBox myUseSingleImageInput;
    protected Button myAddTrackImageMapping;
    protected Select myTrackImageImportType;
    protected Select myPhotoThumbnailImportType;
    protected SmartTextField myArtistDropWords;
    protected SmartTextField myDisabledMp4Codecs;
    protected Panel myPathReplacementsPanel;
    protected Table myPathReplacements;
    protected Button myAddPathReplacement;
    private DatasourcesConfigPanel myDatasourceConfigPanel;
    protected Table myFileTypes;
    protected Button myAddFileType;
    protected Button myResetFileTypes;
    protected Panel myFileTypesPanel;
    private DatasourceConfig myDatasourceConfig;

    protected DatasourceOptionsPanel(DatasourcesConfigPanel datasourceConfigPanel, DatasourceConfig datasourceConfig) {
        myDatasourceConfigPanel = datasourceConfigPanel;
        myDatasourceConfig = datasourceConfig;
        IMPORT_TYPE_MAPPINGS.put(ImageImportType.Auto, new ImageImportTypeRepresentation(ImageImportType.Auto));
        IMPORT_TYPE_MAPPINGS.put(ImageImportType.Never, new ImageImportTypeRepresentation(ImageImportType.Never));
        IMPORT_TYPE_MAPPINGS.put(ImageImportType.OnDemand, new ImageImportTypeRepresentation(ImageImportType.OnDemand));
    }

    @Override
    public void attach() {
        super.attach();

        myImageMappingsPanel = new Panel(getBundleString("datasourceOptionsPanel.trackImagePattern.caption"), getComponentFactory().createVerticalLayout(true, true));
        myTrackImagePatternsTable = new Table();
        myTrackImagePatternsTable.setCacheRate(50);
        myTrackImagePatternsTable.addContainerProperty("pattern", TextField.class, null, getBundleString("datasourceOptionsPanel.imagePattern"), null, null);
        myTrackImagePatternsTable.addContainerProperty("delete", Button.class, null, "", null, null);
        myTrackImagePatternsTable.setEditable(false);
        myImageMappingsPanel.addComponent(myTrackImagePatternsTable);
        myAddTrackImageMapping = getComponentFactory().createButton("datasourceOptionsPanel.addImagePattern", this);
        myImageMappingsPanel.addComponent(getComponentFactory().createHorizontalButtons(false, true, myAddTrackImageMapping));
        myUseSingleImageInput = getComponentFactory().createCheckBox("datasourceOptionsPanel.imagePatternUseSingle");
        myImageMappingsPanel.addComponent(myUseSingleImageInput);
        myTrackImageImportType = getComponentFactory().createSelect("datasourceOptionsPanel.trackImageImportType", Arrays.asList(IMPORT_TYPE_MAPPINGS.get(ImageImportType.Auto), IMPORT_TYPE_MAPPINGS.get(ImageImportType.Never)));
        myPhotoThumbnailImportType = getComponentFactory().createSelect("datasourceOptionsPanel.photoThumbnailImportType", Arrays.asList(IMPORT_TYPE_MAPPINGS.get(ImageImportType.Auto), IMPORT_TYPE_MAPPINGS.get(ImageImportType.OnDemand)));
        myArtistDropWords = getComponentFactory().createTextField("datasourceOptionsPanel.artistDropWords");
        myDisabledMp4Codecs = getComponentFactory().createTextField("datasourceOptionsPanel.disabledMp4Codecs");
        myPathReplacementsPanel = new Panel(getBundleString("datasourceOptionsPanel.caption.replacements"), getComponentFactory().createVerticalLayout(true, true));
        myPathReplacements = new Table();
        myPathReplacements.setCacheRate(50);
        myPathReplacements.addContainerProperty("search", TextField.class, null, getBundleString("datasourceOptionsPanel.replaceSearch"), null, null);
        myPathReplacements.addContainerProperty("replace", TextField.class, null, getBundleString("datasourceOptionsPanel.replaceReplace"), null, null);
        myPathReplacements.addContainerProperty("delete", Button.class, null, "", null, null);
        myPathReplacements.setEditable(false);
        myPathReplacementsPanel.addComponent(myPathReplacements);
        myAddPathReplacement = getComponentFactory().createButton("datasourceOptionsPanel.addReplacement", this);
        myPathReplacementsPanel.addComponent(getComponentFactory().createHorizontalButtons(false, true, myAddPathReplacement));
        myFileTypesPanel = new Panel(getBundleString("datasourceOptionsPanel.caption.types"), getComponentFactory().createVerticalLayout(true, true));
        myFileTypes = new Table();
        myFileTypes.setCacheRate(50);
        myFileTypes.addContainerProperty("active", CheckBox.class, null, getBundleString("datasourceOptionsPanel.fileTypes.active"), null, null);
        myFileTypes.addContainerProperty("suffix", TextField.class, null, getBundleString("datasourceOptionsPanel.fileTypes.suffix"), null, null);
        myFileTypes.addContainerProperty("mimeType", TextField.class, null, getBundleString("datasourceOptionsPanel.fileTypes.mimeType"), null, null);
        myFileTypes.addContainerProperty("mediaType", Select.class, null, getBundleString("datasourceOptionsPanel.fileTypes.mediaType"), null, null);
        myFileTypes.addContainerProperty("protection", Select.class, null, getBundleString("datasourceOptionsPanel.fileTypes.protection"), null, null);
        myFileTypes.addContainerProperty("delete", Button.class, null, "", null, null);
        myFileTypes.setEditable(false);
        myFileTypesPanel.addComponent(myFileTypes);
        myAddFileType = getComponentFactory().createButton("datasourceOptionsPanel.fileTypes.add", this);
        myResetFileTypes = getComponentFactory().createButton("datasourceOptionsPanel.fileTypes.reset", this);
        myFileTypesPanel.addComponent(getComponentFactory().createHorizontalButtons(false, true, myAddFileType, myResetFileTypes));
    }

    protected void setTablePageLengths() {
        myPathReplacements.setPageLength(Math.min(myPathReplacements.getItemIds().size(), 5));
        myTrackImagePatternsTable.setPageLength(Math.min(myTrackImagePatternsTable.getItemIds().size(), 5));
        myFileTypes.setPageLength(Math.min(myFileTypes.size(), 10));
    }

    protected void addTrackImagePattern(String pattern) {
        SmartTextField searchTextField = new SmartTextField();
        searchTextField.setValue(pattern);
        searchTextField.setImmediate(true);
        myTrackImagePatternsTable.addItem(new Object[]{searchTextField, getComponentFactory().createButton("button.delete", this)}, myItemIdGenerator.getAndIncrement());
    }

    protected void addPathReplacement(ReplacementRule replacement) {
        SmartTextField searchTextField = new SmartTextField();
        searchTextField.setValue(replacement.getSearchPattern());
        searchTextField.addValidator(new ValidRegExpValidator("datasourcesConfigPanel.error.invalidSearchExpression"));
        searchTextField.setImmediate(true);
        myPathReplacements.addItem(new Object[]{searchTextField, new SmartTextField(null, replacement.getReplacement()), getComponentFactory().createButton("button.delete", this)}, myItemIdGenerator.getAndIncrement());
    }

    protected void setFileTypes(List<FileType> fileTypes) {
        myFileTypes.removeAllItems();
        for (FileType fileType : fileTypes) {
            addFileType(fileType);
        }
    }

    protected void addFileType(FileType fileType) {
        CheckBox active = new CheckBox();
        active.setValue(fileType.isActive());
        SmartField suffix = new SmartTextField();
        suffix.setValue(fileType.getSuffix());
        SmartField mimeType = new SmartTextField();
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

    public void buttonClick(final Button.ClickEvent clickEvent) {
        if (clickEvent.getSource() == myAddFileType) {
            addFileType(new FileType(false, "", "application/octet-stream", MediaType.Other, false));
            setTablePageLengths();
            myFileTypes.setCurrentPageFirstItemIndex(Math.max(myFileTypes.size() - 10, 0));
        } else if (clickEvent.getSource() == myResetFileTypes) {
            final Button yes = new Button(getBundleString("button.yes"));
            Button no = new Button(getBundleString("button.no"));
            new OptionWindow(30, Sizeable.UNITS_EM, null, getBundleString("datasourceOptionsPanel.optionWindowResetFileType.caption"), getBundleString("datasourceOptionsPanel.optionWindowResetFileType.message"), yes, no) {
                public void clicked(Button button) {
                    if (button == yes) {
                        setFileTypes(myDatasourceConfig.getDefaultFileTypes());
                        setTablePageLengths();
                    }
                }
            }.show(getWindow());
        } else if (clickEvent.getSource() instanceof Button && ((Component) clickEvent.getSource()).getParent() == myFileTypes) {
            final Button yes = new Button(getBundleString("button.yes"));
            Button no = new Button(getBundleString("button.no"));
            new OptionWindow(30, Sizeable.UNITS_EM, null, getBundleString("datasourceOptionsPanel.optionWindowDeleteFileType.caption"), getBundleString("datasourceOptionsPanel.optionWindowDeleteFileType.message"), yes, no) {
                public void clicked(Button button) {
                    if (button == yes) {
                        myFileTypes.removeItem(((AbstractComponent) clickEvent.getSource()).getData());
                        setTablePageLengths();
                    }
                }
            }.show(getWindow());
        } else if (clickEvent.getSource() == myAddPathReplacement) {
            addPathReplacement(new ReplacementRule("^.*$", "$0"));
            setTablePageLengths();
        } else if (findTableItemWithObject(myPathReplacements, clickEvent.getSource()) != null) {
            final Button yes = new Button(getBundleString("button.yes"));
            Button no = new Button(getBundleString("button.no"));
            new OptionWindow(30, Sizeable.UNITS_EM, null, getBundleString("datasourcesConfigDialog.optionWindowDeletePathReplacement.caption"), getBundleString("datasourcesConfigDialog.optionWindowDeletePathReplacement.message"), yes, no) {
                public void clicked(Button button) {
                    if (button == yes) {
                        myPathReplacements.removeItem(findTableItemWithObject(myPathReplacements, clickEvent.getSource()));
                        setTablePageLengths();
                    }
                }
            }.show(VaadinUtils.getApplicationWindow(this));
        } else if (clickEvent.getSource() == myAddTrackImageMapping) {
            addTrackImagePattern("");
            setTablePageLengths();
        } else if (findTableItemWithObject(myTrackImagePatternsTable, clickEvent.getSource()) != null) {
            final Button yes = new Button(getBundleString("button.yes"));
            Button no = new Button(getBundleString("button.no"));
            new OptionWindow(30, Sizeable.UNITS_EM, null, getBundleString("datasourceOptionsPanel.optionWindowDeleteTrackImageMapping.caption"), getBundleString("datasourceOptionsPanel.optionWindowDeleteTrackImageMapping.message"), yes, no) {
                public void clicked(Button button) {
                    if (button == yes) {
                        myTrackImagePatternsTable.removeItem(findTableItemWithObject(myTrackImagePatternsTable, clickEvent.getSource()));
                        setTablePageLengths();
                    }
                }
            }.show(VaadinUtils.getApplicationWindow(this));
        } else {
            super.buttonClick(clickEvent);
        }
    }

    @Override
    protected Component getSaveFollowUpComponent() {
        return myDatasourceConfigPanel;
    }

    @Override
    protected Component getCancelFollowUpComponent() {
        return myDatasourceConfigPanel;
    }

    protected List<FileType> getFileTypesAsList() {
        List<FileType> fileTypes = new ArrayList<FileType>();
        for (Object itemId : myFileTypes.getItemIds()) {
            Boolean active = (Boolean) getTableCellPropertyValue(myFileTypes, itemId, "active");
            String suffix = (String) getTableCellPropertyValue(myFileTypes, itemId, "suffix");
            String mimeType = (String) getTableCellPropertyValue(myFileTypes, itemId, "mimeType");
            MediaType mediaType = (MediaType) getTableCellPropertyValue(myFileTypes, itemId, "mediaType");
            Protection protection = (Protection) getTableCellPropertyValue(myFileTypes, itemId, "protection");
            fileTypes.add(new FileType(active, suffix, mimeType, mediaType, protection == PROTECTED));
        }
        return fileTypes;
    }
}
