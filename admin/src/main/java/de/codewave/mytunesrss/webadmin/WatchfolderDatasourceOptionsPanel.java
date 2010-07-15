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

    private Form myForm;
    private SmartTextField myIncludePattern;
    private SmartTextField myExcludePattern;
    private SmartTextField myMinFileSize;
    private SmartTextField myMaxFileSize;
    private WatchfolderDatasourceConfig myConfig;

    public WatchfolderDatasourceOptionsPanel(WatchfolderDatasourceConfig config) {
        myConfig = config;
    }

    @Override
    public void attach() {
        init(null, getComponentFactory().createGridLayout(1, 2, true, true));

        myForm = getComponentFactory().createForm(null, true);
        myIncludePattern = getComponentFactory().createTextField("datasourceOptionsPanel.includePattern", new ValidRegExpValidator("datasourceOptionsPanel.error.invalidIncludePattern"));
        myExcludePattern = getComponentFactory().createTextField("datasourceOptionsPanel.excludePattern", new ValidRegExpValidator("datasourceOptionsPanel.error.invalidExcludePattern"));
        myMinFileSize = getComponentFactory().createTextField("datasourceOptionsPanel.minFileSize", getValidatorFactory().createMinMaxValidator(0, Integer.MAX_VALUE));
        myMaxFileSize = getComponentFactory().createTextField("datasourceOptionsPanel.maxFileSize", getValidatorFactory().createMinMaxValidator(0, Integer.MAX_VALUE));
        myForm.addField(myIncludePattern, myIncludePattern);
        myForm.addField(myExcludePattern, myExcludePattern);
        myForm.addField(myMinFileSize, myMinFileSize);
        myForm.addField(myMaxFileSize, myMaxFileSize);
        Panel panel = getComponentFactory().surroundWithPanel(myForm, FORM_PANEL_MARGIN_INFO, getBundleString("datasourceOptionsPanel.caption.options"));
        addComponent(panel);

        attach(0, 1, 0, 1);

        initFromConfig();
    }

    @Override
    protected void writeToConfig() {
        myConfig.setMinFileSize(myMinFileSize.getLongValue(0));
        myConfig.setMaxFileSize(myMaxFileSize.getLongValue(0));
        myConfig.setIncludePattern(myIncludePattern.getStringValue(null));
        myConfig.setExcludePattern(myExcludePattern.getStringValue(null));
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
    }

    protected boolean beforeSave() {
        if (!VaadinUtils.isValid(myForm)) {
            getApplication().showError("error.formInvalid");
        } else {
            writeToConfig();
            // TODO close window
        }
        return false; // make sure the default operation is not used
    }

    @Override
    protected boolean beforeCancel() {
        // TODO close window
        return false; // make sure the default operation is not used
    }
}