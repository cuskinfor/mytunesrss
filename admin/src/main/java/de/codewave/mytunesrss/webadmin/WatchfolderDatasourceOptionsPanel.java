/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.ui.Form;
import com.vaadin.ui.Panel;
import de.codewave.mytunesrss.WatchfolderDatasourceConfig;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.validation.ValidRegExpValidator;

public class WatchfolderDatasourceOptionsPanel extends MyTunesRssConfigPanel {

    private Form myIncludeExcludeForm;
    private SmartTextField myIncludePattern;
    private SmartTextField myExcludePattern;
    private SmartTextField myMinFileSize;
    private SmartTextField myMaxFileSize;
    private Form myFallbackForm;
    private SmartTextField myAlbumFallback;
    private SmartTextField myArtistFallback;
    private WatchfolderDatasourceConfig myConfig;

    public WatchfolderDatasourceOptionsPanel(WatchfolderDatasourceConfig config) {
        myConfig = config;
    }

    @Override
    public void attach() {
        init(null, getComponentFactory().createGridLayout(1, 3, true, true));

        myIncludeExcludeForm = getComponentFactory().createForm(null, true);
        myIncludePattern = getComponentFactory().createTextField("datasourceOptionsPanel.includePattern", new ValidRegExpValidator("datasourceOptionsPanel.error.invalidIncludePattern"));
        myExcludePattern = getComponentFactory().createTextField("datasourceOptionsPanel.excludePattern", new ValidRegExpValidator("datasourceOptionsPanel.error.invalidExcludePattern"));
        myMinFileSize = getComponentFactory().createTextField("datasourceOptionsPanel.minFileSize", getValidatorFactory().createMinMaxValidator(0, Integer.MAX_VALUE));
        myMaxFileSize = getComponentFactory().createTextField("datasourceOptionsPanel.maxFileSize", getValidatorFactory().createMinMaxValidator(0, Integer.MAX_VALUE));
        myIncludeExcludeForm.addField(myIncludePattern, myIncludePattern);
        myIncludeExcludeForm.addField(myExcludePattern, myExcludePattern);
        myIncludeExcludeForm.addField(myMinFileSize, myMinFileSize);
        myIncludeExcludeForm.addField(myMaxFileSize, myMaxFileSize);
        Panel panel = getComponentFactory().surroundWithPanel(myIncludeExcludeForm, FORM_PANEL_MARGIN_INFO, getBundleString("datasourceOptionsPanel.caption.options"));
        addComponent(panel);

        myFallbackForm = getComponentFactory().createForm(null, true);
        myAlbumFallback = getComponentFactory().createTextField("datasourceOptionsPanel.albumFallback");
        myArtistFallback = getComponentFactory().createTextField("datasourceOptionsPanel.artistFallback");
        myFallbackForm.addField(myAlbumFallback, myAlbumFallback);
        myFallbackForm.addField(myArtistFallback, myArtistFallback);
        addComponent(getComponentFactory().surroundWithPanel(myFallbackForm, FORM_PANEL_MARGIN_INFO, getBundleString("datasourceOptionsPanel.caption.fallbacks")));

        attach(0, 2, 0, 2);

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
    }

    protected boolean beforeSave() {
        if (!VaadinUtils.isValid(myFallbackForm, myIncludeExcludeForm)) {
            getApplication().showError("error.formInvalid");
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
        getWindow().getParent().getWindow().removeWindow(getWindow());
    }
}