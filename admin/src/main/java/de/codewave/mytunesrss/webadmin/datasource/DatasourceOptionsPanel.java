/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.datasource;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.ImageImportType;
import de.codewave.mytunesrss.config.DatasourceConfig;
import de.codewave.mytunesrss.config.ReplacementRule;
import de.codewave.mytunesrss.config.VideoType;
import de.codewave.mytunesrss.webadmin.MyTunesRssConfigPanel;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;
import de.codewave.vaadin.validation.ValidRegExpValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class DatasourceOptionsPanel extends MyTunesRssConfigPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasourceOptionsPanel.class);

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

    protected final Map<ImageImportType, ImageImportTypeRepresentation> IMPORT_TYPE_MAPPINGS = new HashMap<>();

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

    protected DatasourceOptionsPanel(DatasourcesConfigPanel datasourceConfigPanel) {
        myDatasourceConfigPanel = datasourceConfigPanel;
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
    }

    protected void setTablePageLengths() {
        myPathReplacements.setPageLength(Math.min(myPathReplacements.getItemIds().size(), 5));
        myTrackImagePatternsTable.setPageLength(Math.min(myTrackImagePatternsTable.getItemIds().size(), 5));
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

    public void buttonClick(final Button.ClickEvent clickEvent) {
        if (clickEvent.getSource() == myAddPathReplacement) {
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

}
