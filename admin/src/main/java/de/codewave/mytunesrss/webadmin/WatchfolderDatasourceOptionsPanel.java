/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.ImageImportType;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.ReplacementRule;
import de.codewave.mytunesrss.config.VideoType;
import de.codewave.mytunesrss.config.WatchfolderDatasourceConfig;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;
import de.codewave.vaadin.validation.ValidRegExpValidator;

import java.util.*;

public class WatchfolderDatasourceOptionsPanel extends MyTunesRssConfigPanel {

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

    private final Map<ImageImportType, ImageImportTypeRepresentation> IMPORT_TYPE_MAPPINGS = new HashMap<ImageImportType, ImageImportTypeRepresentation>();

    private Form myIncludeExcludeForm;
    private SmartTextField myIncludePattern;
    private SmartTextField myExcludePattern;
    private SmartTextField myMinFileSize;
    private SmartTextField myMaxFileSize;
    private Form myFallbackForm;
    private SmartTextField myTitleFallback;
    private SmartTextField myAlbumFallback;
    private SmartTextField myArtistFallback;
    private SmartTextField mySeriesFallback;
    private SmartTextField mySeasonFallback;
    private SmartTextField myEpisodeFallback;
    private SmartTextField myPhotoAlbumPattern;
    private Form myMiscOptionsForm;
    private Select myVideoType;
    private CheckBox myIgnoreFileMeta;
    private SmartTextField myArtistDropWords;
    private SmartTextField myId3v2TrackComment;
    private SmartTextField myDisabledMp4Codecs;
    private WatchfolderDatasourceConfig myConfig;
    private Table myTrackImageMappingsTable;
    private Button myAddTrackImageMapping;
    private Select myTrackImageImportType;
    private Select myPhotoThumbnailImportType;

    public WatchfolderDatasourceOptionsPanel(WatchfolderDatasourceConfig config) {
        IMPORT_TYPE_MAPPINGS.put(ImageImportType.Auto, new ImageImportTypeRepresentation(ImageImportType.Auto));
        IMPORT_TYPE_MAPPINGS.put(ImageImportType.Never, new ImageImportTypeRepresentation(ImageImportType.Never));
        IMPORT_TYPE_MAPPINGS.put(ImageImportType.OnDemand, new ImageImportTypeRepresentation(ImageImportType.OnDemand));
        myConfig = config;
    }

    @Override
    public void attach() {
        super.attach();
        init(null, getComponentFactory().createGridLayout(1, 5, true, true));

        myIncludeExcludeForm = getComponentFactory().createForm(null, true);
        myIncludePattern = getComponentFactory().createTextField("datasourceOptionsPanel.includePattern", new ValidRegExpValidator("datasourceOptionsPanel.error.invalidIncludePattern"));
        myExcludePattern = getComponentFactory().createTextField("datasourceOptionsPanel.excludePattern", new ValidRegExpValidator("datasourceOptionsPanel.error.invalidExcludePattern"));
        myMinFileSize = getComponentFactory().createTextField("datasourceOptionsPanel.minFileSize", getValidatorFactory().createMinMaxValidator(0, Integer.MAX_VALUE));
        myMaxFileSize = getComponentFactory().createTextField("datasourceOptionsPanel.maxFileSize", getValidatorFactory().createMinMaxValidator(0, Integer.MAX_VALUE));
        myIncludeExcludeForm.addField(myIncludePattern, myIncludePattern);
        myIncludeExcludeForm.addField(myExcludePattern, myExcludePattern);
        myIncludeExcludeForm.addField(myMinFileSize, myMinFileSize);
        myIncludeExcludeForm.addField(myMaxFileSize, myMaxFileSize);
        Panel panel = getComponentFactory().surroundWithPanel(myIncludeExcludeForm, FORM_PANEL_MARGIN_INFO, getBundleString("datasourceOptionsPanel.caption.filter"));
        addComponent(panel);

        myFallbackForm = getComponentFactory().createForm(null, true);
        myTitleFallback = getComponentFactory().createTextField("datasourceOptionsPanel.titleFallback");
        myAlbumFallback = getComponentFactory().createTextField("datasourceOptionsPanel.albumFallback");
        myArtistFallback = getComponentFactory().createTextField("datasourceOptionsPanel.artistFallback");
        mySeriesFallback = getComponentFactory().createTextField("datasourceOptionsPanel.seriesFallback");
        mySeasonFallback = getComponentFactory().createTextField("datasourceOptionsPanel.seasonFallback");
        myEpisodeFallback = getComponentFactory().createTextField("datasourceOptionsPanel.episodeFallback");
        myPhotoAlbumPattern = getComponentFactory().createTextField("datasourceOptionsPanel.photoAlbumPattern");
        myFallbackForm.addField(myTitleFallback, myTitleFallback);
        myFallbackForm.addField(myAlbumFallback, myAlbumFallback);
        myFallbackForm.addField(myArtistFallback, myArtistFallback);
        myFallbackForm.addField(mySeriesFallback, mySeriesFallback);
        myFallbackForm.addField(mySeasonFallback, mySeasonFallback);
        myFallbackForm.addField(myEpisodeFallback, myEpisodeFallback);
        myFallbackForm.addField(myPhotoAlbumPattern, myPhotoAlbumPattern);
        addComponent(getComponentFactory().surroundWithPanel(myFallbackForm, FORM_PANEL_MARGIN_INFO, getBundleString("datasourceOptionsPanel.caption.fallbacks")));

        Panel imageMappingsPanel = new Panel(getBundleString("datasourceOptionsPanel.trackImageMapping.caption"), getComponentFactory().createVerticalLayout(true, true));
        addComponent(imageMappingsPanel);
        myTrackImageMappingsTable = new Table();
        myTrackImageMappingsTable.setCacheRate(50);
        myTrackImageMappingsTable.addContainerProperty("search", TextField.class, null, getBundleString("datasourceOptionsPanel.imageMappingSearch"), null, null);
        myTrackImageMappingsTable.addContainerProperty("replace", TextField.class, null, getBundleString("datasourceOptionsPanel.imageMappingReplace"), null, null);
        myTrackImageMappingsTable.addContainerProperty("delete", Button.class, null, "", null, null);
        myTrackImageMappingsTable.setEditable(false);
        imageMappingsPanel.addComponent(myTrackImageMappingsTable);
        myAddTrackImageMapping = getComponentFactory().createButton("datasourceOptionsPanel.addImageMapping", this);
        imageMappingsPanel.addComponent(getComponentFactory().createHorizontalButtons(false, true, myAddTrackImageMapping));

        myMiscOptionsForm = getComponentFactory().createForm(null, true);
        myVideoType = getComponentFactory().createSelect("datasourceOptionsPanel.watchfolderVideoType", Arrays.asList(new VideoTypeRepresentation[]{new VideoTypeRepresentation(VideoType.Movie), new VideoTypeRepresentation(VideoType.TvShow)}));
        myIgnoreFileMeta = getComponentFactory().createCheckBox("datasourceOptionsPanel.ignoreFileMeta");
        myArtistDropWords = getComponentFactory().createTextField("datasourceOptionsPanel.artistDropWords");
        myId3v2TrackComment = getComponentFactory().createTextField("datasourceOptionsPanel.id3v2TrackComment");
        myDisabledMp4Codecs = getComponentFactory().createTextField("datasourceOptionsPanel.disabledMp4Codecs");
        myTrackImageImportType = getComponentFactory().createSelect("dataimportConfigPanel.trackImageImportType", Arrays.asList(IMPORT_TYPE_MAPPINGS.get(ImageImportType.Auto), IMPORT_TYPE_MAPPINGS.get(ImageImportType.Never)));
        myPhotoThumbnailImportType = getComponentFactory().createSelect("dataimportConfigPanel.photoThumbnailImportType", Arrays.asList(IMPORT_TYPE_MAPPINGS.get(ImageImportType.Auto), IMPORT_TYPE_MAPPINGS.get(ImageImportType.OnDemand)));
        myMiscOptionsForm.addField(myVideoType, myVideoType);
        myMiscOptionsForm.addField(myIgnoreFileMeta, myIgnoreFileMeta);
        myMiscOptionsForm.addField(myArtistDropWords, myArtistDropWords);
        myMiscOptionsForm.addField(myId3v2TrackComment, myId3v2TrackComment);
        myMiscOptionsForm.addField(myDisabledMp4Codecs, myDisabledMp4Codecs);
        myMiscOptionsForm.addField(myTrackImageImportType, myTrackImageImportType);
        myMiscOptionsForm.addField(myPhotoThumbnailImportType, myPhotoThumbnailImportType);
        addComponent(getComponentFactory().surroundWithPanel(myMiscOptionsForm, FORM_PANEL_MARGIN_INFO, getBundleString("datasourceOptionsPanel.caption.misc")));

        addDefaultComponents(0, 4, 0, 4, false);

        initFromConfig();
    }

    private void addTrackImageMapping(ReplacementRule replacement) {
        SmartTextField searchTextField = new SmartTextField();
        searchTextField.setValue(replacement.getSearchPattern());
        searchTextField.addValidator(new ValidRegExpValidator("datasourceOptionsPanel.error.invalidSearchExpression"));
        searchTextField.setImmediate(true);
        myTrackImageMappingsTable.addItem(new Object[]{searchTextField, new SmartTextField(null, replacement.getReplacement()), getComponentFactory().createButton("button.delete", this)}, myItemIdGenerator.getAndIncrement());
    }

    @Override
    protected void writeToConfig() {
        myConfig.setMinFileSize(myMinFileSize.getLongValue(0));
        myConfig.setMaxFileSize(myMaxFileSize.getLongValue(0));
        myConfig.setIncludePattern(myIncludePattern.getStringValue(null));
        myConfig.setExcludePattern(myExcludePattern.getStringValue(null));
        myConfig.setTitleFallback(myTitleFallback.getStringValue(null));
        myConfig.setAlbumFallback(myAlbumFallback.getStringValue(null));
        myConfig.setArtistFallback(myArtistFallback.getStringValue(null));
        myConfig.setSeriesFallback(mySeriesFallback.getStringValue(null));
        myConfig.setSeasonFallback(mySeasonFallback.getStringValue(null));
        myConfig.setEpisodeFallback(myEpisodeFallback.getStringValue(null));
        myConfig.setVideoType(((VideoTypeRepresentation) myVideoType.getValue()).getVideoType());
        myConfig.setIgnoreFileMeta(myIgnoreFileMeta.booleanValue());
        myConfig.setPhotoAlbumPattern(myPhotoAlbumPattern.getStringValue(null));
        myConfig.setArtistDropWords(myArtistDropWords.getStringValue(null));
        myConfig.setId3v2TrackComment(myId3v2TrackComment.getStringValue(null));
        myConfig.setDisabledMp4Codecs(myDisabledMp4Codecs.getStringValue(null));
        List<ReplacementRule> mappings = new ArrayList<ReplacementRule>();
        for (Object itemId : myTrackImageMappingsTable.getItemIds()) {
            mappings.add(new ReplacementRule((String) getTableCellPropertyValue(myTrackImageMappingsTable, itemId, "search"), (String) getTableCellPropertyValue(myTrackImageMappingsTable, itemId, "replace")));
        }
        myConfig.setTrackImageMappings(mappings);
        myConfig.setTrackImageImportType(((ImageImportTypeRepresentation) myTrackImageImportType.getValue()).getImageImportType());
        myConfig.setPhotoThumbnailImportType(((ImageImportTypeRepresentation) myPhotoThumbnailImportType.getValue()).getImageImportType());
    }

    @Override
    protected void initFromConfig() {
        if (myConfig.getMaxFileSize() > 0) {
            myMinFileSize.setValue(myConfig.getMinFileSize());
        } else {
            myMinFileSize.setValue("");
        }
        if (myConfig.getMaxFileSize() > 0) {
            myMaxFileSize.setValue(myConfig.getMaxFileSize());
        } else {
            myMaxFileSize.setValue("");
        }
        myIncludePattern.setValue(myConfig.getIncludePattern(), "");
        myExcludePattern.setValue(myConfig.getExcludePattern(), "");
        myTitleFallback.setValue(myConfig.getTitleFallback());
        myAlbumFallback.setValue(myConfig.getAlbumFallback());
        myArtistFallback.setValue(myConfig.getArtistFallback());
        mySeriesFallback.setValue(myConfig.getSeriesFallback());
        mySeasonFallback.setValue(myConfig.getSeasonFallback());
        myEpisodeFallback.setValue(myConfig.getEpisodeFallback());
        myVideoType.setValue(new VideoTypeRepresentation(myConfig.getVideoType()));
        myIgnoreFileMeta.setValue(myConfig.isIgnoreFileMeta());
        myPhotoAlbumPattern.setValue(myConfig.getPhotoAlbumPattern());
        myArtistDropWords.setValue(myConfig.getArtistDropWords());
        myId3v2TrackComment.setValue(myConfig.getId3v2TrackComment());
        myDisabledMp4Codecs.setValue(myConfig.getDisabledMp4Codecs());
        myTrackImageImportType.setValue(IMPORT_TYPE_MAPPINGS.get(myConfig.getTrackImageImportType()));
        myPhotoThumbnailImportType.setValue(IMPORT_TYPE_MAPPINGS.get(myConfig.getPhotoThumbnailImportType()));
        myTrackImageMappingsTable.removeAllItems();
        for (ReplacementRule mapping : myConfig.getTrackImageMappings()) {
            addTrackImageMapping(mapping);
        }
        setTablePageLengths();
    }

    protected boolean beforeSave() {
        if (!VaadinUtils.isValid(myFallbackForm, myIncludeExcludeForm, myTrackImageMappingsTable)) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
        } else {
            writeToConfig();
            closeWindow();
        }
        return false; // make sure the default operation is not used
    }

    @Override
    protected boolean beforeCancel() {
        closeWindow();
        return false; // make sure the default operation is not used
    }

    private void closeWindow() {
        getWindow().getParent().removeWindow(getWindow());
    }

    @Override
    public void buttonClick(final Button.ClickEvent clickEvent) {
        if (clickEvent.getSource() == myAddTrackImageMapping) {
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

    private void setTablePageLengths() {
        myTrackImageMappingsTable.setPageLength(Math.min(myTrackImageMappingsTable.getItemIds().size(), 5));
    }

    private class VideoTypeRepresentation {
        private VideoType myVideoType;

        private VideoTypeRepresentation(VideoType videoType) {
            myVideoType = videoType;
        }

        private VideoType getVideoType() {
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
}