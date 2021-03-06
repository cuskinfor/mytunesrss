/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.datasource;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Form;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Select;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.VideoType;
import de.codewave.mytunesrss.config.WatchfolderDatasourceConfig;
import de.codewave.mytunesrss.webadmin.MainWindow;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.validation.ValidRegExpValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WatchfolderDatasourceOptionsPanel extends DatasourceOptionsPanel {

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
    private SmartTextField myId3v2TrackComment;
    private CheckBox myImportPlaylists;
    private WatchfolderDatasourceConfig myConfig;

    public WatchfolderDatasourceOptionsPanel(DatasourcesConfigPanel datasourcesConfigPanel, WatchfolderDatasourceConfig config) {
        super(datasourcesConfigPanel);
        myConfig = config;
    }

    @Override
    public void attach() {
        super.attach();
        init(getBundleString("datasourceOptionsPanel.caption", myConfig.getDefinition()), getComponentFactory().createGridLayout(1, 5, true, true));

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
        myTitleFallback = getComponentFactory().createTextField("datasourceOptionsPanel.titleFallback", new FallbackPatternValidator("datasourceOptionsPanel.error.invalidFallbackPattern"));
        myAlbumFallback = getComponentFactory().createTextField("datasourceOptionsPanel.albumFallback", new FallbackPatternValidator("datasourceOptionsPanel.error.invalidFallbackPattern"));
        myArtistFallback = getComponentFactory().createTextField("datasourceOptionsPanel.artistFallback", new FallbackPatternValidator("datasourceOptionsPanel.error.invalidFallbackPattern"));
        mySeriesFallback = getComponentFactory().createTextField("datasourceOptionsPanel.seriesFallback", new FallbackPatternValidator("datasourceOptionsPanel.error.invalidFallbackPattern"));
        mySeasonFallback = getComponentFactory().createTextField("datasourceOptionsPanel.seasonFallback", new FallbackPatternValidator("datasourceOptionsPanel.error.invalidFallbackPattern"));
        myEpisodeFallback = getComponentFactory().createTextField("datasourceOptionsPanel.episodeFallback", new FallbackPatternValidator("datasourceOptionsPanel.error.invalidFallbackPattern"));
        myPhotoAlbumPattern = getComponentFactory().createTextField("datasourceOptionsPanel.photoAlbumPattern", new FallbackPatternValidator("datasourceOptionsPanel.error.invalidFallbackPattern"));
        myImportPlaylists = getComponentFactory().createCheckBox("datasourceOptionsPanel.importPlaylist");
        myFallbackForm.addField(myTitleFallback, myTitleFallback);
        myFallbackForm.addField(myAlbumFallback, myAlbumFallback);
        myFallbackForm.addField(myArtistFallback, myArtistFallback);
        myFallbackForm.addField(mySeriesFallback, mySeriesFallback);
        myFallbackForm.addField(mySeasonFallback, mySeasonFallback);
        myFallbackForm.addField(myEpisodeFallback, myEpisodeFallback);
        myFallbackForm.addField(myPhotoAlbumPattern, myPhotoAlbumPattern);
        addComponent(getComponentFactory().surroundWithPanel(myFallbackForm, FORM_PANEL_MARGIN_INFO, getBundleString("datasourceOptionsPanel.caption.fallbacks")));

        addComponent(myImageMappingsPanel);

        myMiscOptionsForm = getComponentFactory().createForm(null, true);
        myVideoType = getComponentFactory().createSelect("datasourceOptionsPanel.watchfolderVideoType", Arrays.asList(new VideoTypeRepresentation[]{new VideoTypeRepresentation(VideoType.Movie), new VideoTypeRepresentation(VideoType.TvShow)}));
        myIgnoreFileMeta = getComponentFactory().createCheckBox("datasourceOptionsPanel.ignoreFileMeta");
        myId3v2TrackComment = getComponentFactory().createTextField("datasourceOptionsPanel.id3v2TrackComment");
        myMiscOptionsForm.addField(myVideoType, myVideoType);
        myMiscOptionsForm.addField(myIgnoreFileMeta, myIgnoreFileMeta);
        myMiscOptionsForm.addField(myArtistDropWords, myArtistDropWords);
        myMiscOptionsForm.addField(myId3v2TrackComment, myId3v2TrackComment);
        myMiscOptionsForm.addField(myDisabledMp4Codecs, myDisabledMp4Codecs);
        myMiscOptionsForm.addField(myTrackImageImportType, myTrackImageImportType);
        myMiscOptionsForm.addField(myPhotoThumbnailImportType, myPhotoThumbnailImportType);
        myMiscOptionsForm.addField(myImportPlaylists, myImportPlaylists);
        addComponent(getComponentFactory().surroundWithPanel(myMiscOptionsForm, FORM_PANEL_MARGIN_INFO, getBundleString("datasourceOptionsPanel.caption.misc")));

        addDefaultComponents(0, 4, 0, 4, false);

        initFromConfig();
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
        myConfig.setArtistDropWords(myArtistDropWords.getStringValue(""));
        myConfig.setId3v2TrackComment(myId3v2TrackComment.getStringValue(""));
        myConfig.setDisabledMp4Codecs(myDisabledMp4Codecs.getStringValue(""));
        List<String> patterns = new ArrayList<>();
        for (Object itemId : myTrackImagePatternsTable.getItemIds()) {
            patterns.add((String) getTableCellPropertyValue(myTrackImagePatternsTable, itemId, "pattern"));
        }
        myConfig.setUseSingleImageInFolder(myUseSingleImageInput.booleanValue());
        myConfig.setTrackImagePatterns(patterns);
        myConfig.setTrackImageImportType(((ImageImportTypeRepresentation) myTrackImageImportType.getValue()).getImageImportType());
        myConfig.setPhotoThumbnailImportType(((ImageImportTypeRepresentation) myPhotoThumbnailImportType.getValue()).getImageImportType());
        myConfig.setImportPlaylists(myImportPlaylists.booleanValue());
        MyTunesRss.CONFIG.replaceDatasourceConfig(myConfig);
        MyTunesRss.CONFIG.save();
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
        myImportPlaylists.setValue(myConfig.isImportPlaylists());
        myTrackImagePatternsTable.removeAllItems();
        for (String pattern : myConfig.getTrackImagePatterns()) {
            addTrackImagePattern(pattern);
        }
        myUseSingleImageInput.setValue(myConfig.isUseSingleImageInFolder());
        setTablePageLengths();
    }

    @Override
    protected boolean beforeSave() {
        if (!VaadinUtils.isValid(myFallbackForm, myIncludeExcludeForm, myTrackImagePatternsTable)) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
            return false;
        }
        return true;
    }

}
