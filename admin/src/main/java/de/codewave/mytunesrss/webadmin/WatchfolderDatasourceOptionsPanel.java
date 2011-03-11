/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.ui.Form;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Select;
import de.codewave.mytunesrss.VideoType;
import de.codewave.mytunesrss.WatchfolderDatasourceConfig;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.validation.ValidRegExpValidator;

import java.util.Arrays;

public class WatchfolderDatasourceOptionsPanel extends MyTunesRssConfigPanel {

    private Form myIncludeExcludeForm;
    private SmartTextField myIncludePattern;
    private SmartTextField myExcludePattern;
    private SmartTextField myMinFileSize;
    private SmartTextField myMaxFileSize;
    private Form myFallbackForm;
    private SmartTextField myAlbumFallback;
    private SmartTextField myArtistFallback;
    private SmartTextField mySeriesFallback;
    private SmartTextField mySeasonFallback;
    private SmartTextField myEpisodeFallback;
    private SmartTextField myPhotoAlbumPattern;
    private Form myMiscOptionsForm;
    private Select myVideoType;
    private WatchfolderDatasourceConfig myConfig;

    public WatchfolderDatasourceOptionsPanel(WatchfolderDatasourceConfig config) {
        myConfig = config;
    }

    @Override
    public void attach() {
        super.attach();
        init(null, getComponentFactory().createGridLayout(1, 4, true, true));

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
        myAlbumFallback = getComponentFactory().createTextField("datasourceOptionsPanel.albumFallback");
        myArtistFallback = getComponentFactory().createTextField("datasourceOptionsPanel.artistFallback");
        mySeriesFallback = getComponentFactory().createTextField("datasourceOptionsPanel.seriesFallback");
        mySeasonFallback = getComponentFactory().createTextField("datasourceOptionsPanel.seasonFallback");
        myEpisodeFallback = getComponentFactory().createTextField("datasourceOptionsPanel.episodeFallback");
        myPhotoAlbumPattern = getComponentFactory().createTextField("datasourceOptionsPanel.photoAlbumPattern");
        myFallbackForm.addField(myAlbumFallback, myAlbumFallback);
        myFallbackForm.addField(myArtistFallback, myArtistFallback);
        myFallbackForm.addField(mySeriesFallback, mySeriesFallback);
        myFallbackForm.addField(mySeasonFallback, mySeasonFallback);
        myFallbackForm.addField(myEpisodeFallback, myEpisodeFallback);
        myFallbackForm.addField(myPhotoAlbumPattern, myPhotoAlbumPattern);
        addComponent(getComponentFactory().surroundWithPanel(myFallbackForm, FORM_PANEL_MARGIN_INFO, getBundleString("datasourceOptionsPanel.caption.fallbacks")));

        myMiscOptionsForm = getComponentFactory().createForm(null, true);
        myVideoType = getComponentFactory().createSelect("datasourceOptionsPanel.watchfolderVideoType", Arrays.asList(new VideoTypeRepresentation[]{new VideoTypeRepresentation(VideoType.Movie), new VideoTypeRepresentation(VideoType.TvShow)}));
        myMiscOptionsForm.addField(myVideoType, myVideoType);
        addComponent(getComponentFactory().surroundWithPanel(myMiscOptionsForm, FORM_PANEL_MARGIN_INFO, getBundleString("datasourceOptionsPanel.caption.misc")));

        addDefaultComponents(0, 3, 0, 3, false);

        initFromConfig();
    }

    @Override
    protected void writeToConfig() {
        myConfig.setMinFileSize(myMinFileSize.getLongValue(0));
        myConfig.setMaxFileSize(myMaxFileSize.getLongValue(0));
        myConfig.setIncludePattern(myIncludePattern.getStringValue(null));
        myConfig.setExcludePattern(myExcludePattern.getStringValue(null));
        myConfig.setAlbumFallback(myAlbumFallback.getStringValue(null));
        myConfig.setArtistFallback(myArtistFallback.getStringValue(null));
        myConfig.setSeriesFallback(mySeriesFallback.getStringValue(null));
        myConfig.setSeasonFallback(mySeasonFallback.getStringValue(null));
        myConfig.setEpisodeFallback(myEpisodeFallback.getStringValue(null));
        myConfig.setVideoType(((VideoTypeRepresentation) myVideoType.getValue()).getVideoType());
        myConfig.setPhotoAlbumPattern(myPhotoAlbumPattern.getStringValue(null));
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
        myAlbumFallback.setValue(myConfig.getAlbumFallback());
        myArtistFallback.setValue(myConfig.getArtistFallback());
        mySeriesFallback.setValue(myConfig.getSeriesFallback());
        mySeasonFallback.setValue(myConfig.getSeasonFallback());
        myEpisodeFallback.setValue(myConfig.getEpisodeFallback());
        myVideoType.setValue(new VideoTypeRepresentation(myConfig.getVideoType()));
        myPhotoAlbumPattern.setValue(myConfig.getPhotoAlbumPattern());
    }

    protected boolean beforeSave() {
        if (!VaadinUtils.isValid(myFallbackForm, myIncludeExcludeForm)) {
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