/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.datasource;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.ImageImportType;
import de.codewave.mytunesrss.config.ReplacementRule;
import de.codewave.mytunesrss.config.VideoType;
import de.codewave.mytunesrss.config.WatchfolderDatasourceConfig;
import de.codewave.mytunesrss.webadmin.MainWindow;
import de.codewave.mytunesrss.webadmin.MyTunesRssConfigPanel;
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
            return getBundleString("dataimportConfigPanel.importType." + myImageImportType.name());
        }
    }

    protected final Map<ImageImportType, ImageImportTypeRepresentation> IMPORT_TYPE_MAPPINGS = new HashMap<ImageImportType, ImageImportTypeRepresentation>();

    protected Panel myImageMappingsPanel;
    protected Table myTrackImageMappingsTable;
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

        myImageMappingsPanel = new Panel(getBundleString("datasourceOptionsPanel.trackImageMapping.caption"), getComponentFactory().createVerticalLayout(true, true));
        myTrackImageMappingsTable = new Table();
        myTrackImageMappingsTable.setCacheRate(50);
        myTrackImageMappingsTable.addContainerProperty("search", TextField.class, null, getBundleString("datasourceOptionsPanel.imageMappingSearch"), null, null);
        myTrackImageMappingsTable.addContainerProperty("replace", TextField.class, null, getBundleString("datasourceOptionsPanel.imageMappingReplace"), null, null);
        myTrackImageMappingsTable.addContainerProperty("delete", Button.class, null, "", null, null);
        myTrackImageMappingsTable.setEditable(false);
        myImageMappingsPanel.addComponent(myTrackImageMappingsTable);
        myAddTrackImageMapping = getComponentFactory().createButton("datasourceOptionsPanel.addImageMapping", this);
        myImageMappingsPanel.addComponent(getComponentFactory().createHorizontalButtons(false, true, myAddTrackImageMapping));
        myTrackImageImportType = getComponentFactory().createSelect("dataimportConfigPanel.trackImageImportType", Arrays.asList(IMPORT_TYPE_MAPPINGS.get(ImageImportType.Auto), IMPORT_TYPE_MAPPINGS.get(ImageImportType.Never)));
        myPhotoThumbnailImportType = getComponentFactory().createSelect("dataimportConfigPanel.photoThumbnailImportType", Arrays.asList(IMPORT_TYPE_MAPPINGS.get(ImageImportType.Auto), IMPORT_TYPE_MAPPINGS.get(ImageImportType.OnDemand)));
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
        myTrackImageMappingsTable.setPageLength(Math.min(myTrackImageMappingsTable.getItemIds().size(), 5));
    }

    protected void addTrackImageMapping(ReplacementRule replacement) {
        SmartTextField searchTextField = new SmartTextField();
        searchTextField.setValue(replacement.getSearchPattern());
        searchTextField.addValidator(new ValidRegExpValidator("datasourceOptionsPanel.error.invalidSearchExpression"));
        searchTextField.setImmediate(true);
        myTrackImageMappingsTable.addItem(new Object[]{searchTextField, new SmartTextField(null, replacement.getReplacement()), getComponentFactory().createButton("button.delete", this)}, myItemIdGenerator.getAndIncrement());
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
            addPathReplacement(new ReplacementRule("^.*$", "\\0"));
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
            addTrackImageMapping(new ReplacementRule("^.*$", "\\0"));
            setTablePageLengths();
        } else if (findTableItemWithObject(myTrackImageMappingsTable, clickEvent.getSource()) != null) {
            final Button yes = new Button(getBundleString("button.yes"));
            Button no = new Button(getBundleString("button.no"));
            new OptionWindow(30, Sizeable.UNITS_EM, null, getBundleString("datasourceOptionsPanel.optionWindowDeleteTrackImageMapping.caption"), getBundleString("datasourceOptionsPanel.optionWindowDeleteTrackImageMapping.message"), yes, no) {
                public void clicked(Button button) {
                    if (button == yes) {
                        myTrackImageMappingsTable.removeItem(findTableItemWithObject(myTrackImageMappingsTable, clickEvent.getSource()));
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