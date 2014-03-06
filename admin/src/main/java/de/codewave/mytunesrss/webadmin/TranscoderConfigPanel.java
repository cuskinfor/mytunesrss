/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.transcoder.*;
import de.codewave.mytunesrss.webadmin.transcoder.*;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TranscoderConfigPanel extends MyTunesRssConfigPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(TranscoderConfigPanel.class);
    private static final String TRANSCODER_NAME_REGEXP = "[a-zA-Z0-9 ]{1,40}";

    private StreamingConfigPanel myStreamingConfigPanel;
    private TranscoderConfig myTranscoderConfig;
    private Runnable myAfterWriteRunnable;

    private Form myForm;
    private SmartTextField myNameTextField;
    private SmartTextField mySuffixTextField;
    private SmartTextField myContentTypeTextField;
    private SmartTextField myMuxTextField;
    private SmartTextField myOptionsTextField;
    private Panel myActivationsPanel;
    private Button myAddFilenameActivationButton;
    private Button myAddMp4CodecActivationButton;
    private Button myAddMp3BitRateActivationButton;
    private ButtonBar myAddActivationButtons;
    private Button myAddMediaTypeActivationButton;

    public TranscoderConfigPanel(StreamingConfigPanel streamingConfigPanel, TranscoderConfig transcoderConfig, Runnable afterWriteRunnable) {
        myStreamingConfigPanel = streamingConfigPanel;
        myTranscoderConfig = transcoderConfig;
        myAfterWriteRunnable = afterWriteRunnable;
    }

    public void attach() {
        super.attach();
        init(getBundleString("transcoderConfigPanel.caption"), getComponentFactory().createGridLayout(1, 3, true, true));

        myForm = getComponentFactory().createForm(null, true);
        myNameTextField = getComponentFactory().createTextField("transcoderConfigPanel.name", new RegexpValidator(TRANSCODER_NAME_REGEXP, true, getBundleString("transcoderConfigPanel.error.invalidName", 40)));
        myNameTextField.setRequired(true);
        myForm.addField("name", myNameTextField);
        mySuffixTextField = getComponentFactory().createTextField("transcoderConfigPanel.suffix");
        mySuffixTextField.setRequired(true);
        myForm.addField("suffix", mySuffixTextField);
        myContentTypeTextField = getComponentFactory().createTextField("transcoderConfigPanel.contentType");
        myContentTypeTextField.setRequired(true);
        myForm.addField("contentType", myContentTypeTextField);
        myMuxTextField = getComponentFactory().createTextField("transcoderConfigPanel.mux");
        myForm.addField("mux", myMuxTextField);
        myOptionsTextField = getComponentFactory().createTextField("transcoderConfigPanel.options");
        myOptionsTextField.setRequired(true);
        myForm.addField("options", myOptionsTextField);
        addComponent(myForm);

        myActivationsPanel = new Panel(getBundleString("transcoderConfigPanel.activations"));
        VerticalLayout activationsPanelLayout = getComponentFactory().createVerticalLayout(true, true);
        myActivationsPanel.setContent(activationsPanelLayout);
        addComponent(myActivationsPanel);

        myAddFilenameActivationButton = getComponentFactory().createButton("transcoderConfigPanel.activation.addFilename", this);
        myAddMp4CodecActivationButton = getComponentFactory().createButton("transcoderConfigPanel.activation.addMp4Codecs", this);
        myAddMp3BitRateActivationButton = getComponentFactory().createButton("transcoderConfigPanel.activation.addMp3BitRate", this);
        myAddMediaTypeActivationButton = getComponentFactory().createButton("transcoderConfigPanel.activation.addMediaType", this);
        myAddActivationButtons = new ButtonBar(getComponentFactory(), myAddFilenameActivationButton, myAddMp4CodecActivationButton, myAddMp3BitRateActivationButton, myAddMediaTypeActivationButton);

        addDefaultComponents(0, 2, 0, 2, false);

        initFromConfig();
    }

    protected void initFromConfig() {
        myNameTextField.setValue(myTranscoderConfig.getName(), "");
        mySuffixTextField.setValue(myTranscoderConfig.getTargetSuffix(), "");
        myMuxTextField.setValue(myTranscoderConfig.getTargetMux(), "");
        myContentTypeTextField.setValue(myTranscoderConfig.getTargetContentType(), "");
        myOptionsTextField.setValue(myTranscoderConfig.getOptions(), "");
        myActivationsPanel.removeAllComponents();
        for (TranscoderActivation activation : myTranscoderConfig.getTranscoderActivations()) {
            Activation type = Activation.forActivation(activation);
            switch (type) {
                case FILENAME:
                    myActivationsPanel.addComponent(new FilenameActivationPanel(getApplication(), getComponentFactory(), (FilenameTranscoderActivation) activation));
                    break;
                case MP3_BIT_RATE:
                    myActivationsPanel.addComponent(new Mp3BitRateActivationPanel(getApplication(), getComponentFactory(), (Mp3BitRateTranscoderActivation) activation));
                    break;
                case MP4_CODEC:
                    myActivationsPanel.addComponent(new Mp4CodecActivationPanel(getApplication(), getComponentFactory(), (Mp4CodecTranscoderActivation) activation));
                    break;
                default:
                    LOGGER.warn("Ignoring unknown transcoder activation of type \"" + type + "\".");
            }
        }
        myActivationsPanel.addComponent(myAddActivationButtons);
    }

    private TranscoderConfig fillConfigFromForm(TranscoderConfig config) {
        config.setName(myNameTextField.getStringValue(""));
        config.setOptions(myOptionsTextField.getStringValue(""));
        config.setTargetContentType(myContentTypeTextField.getStringValue(""));
        config.setTargetMux(myMuxTextField.getStringValue(""));
        config.setTargetSuffix(mySuffixTextField.getStringValue(""));
        List<TranscoderActivation> activations = new ArrayList<>();
        Iterator<Component> componentIterator = myActivationsPanel.getComponentIterator();
        while (componentIterator.hasNext()) {
            Component component = componentIterator.next();
            if (component instanceof ActivationPanel) {
                ActivationPanel activationPanel = (ActivationPanel) component;
                activations.add(activationPanel.getConfig());
            }
        }
        config.setTranscoderActivations(activations);
        return config;
    }

    protected void writeToConfig() {
        fillConfigFromForm(myTranscoderConfig);
        MyTunesRss.CONFIG.save();
        myAfterWriteRunnable.run();
    }

    @Override
    protected boolean beforeSave() {
        boolean valid = VaadinUtils.isValid(myForm);
        if (!valid) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("streamingConfigPanel.error.duplicateName");
        } else {
            for (TranscoderConfig transcoderConfig : myStreamingConfigPanel.getTranscoderConfigs()) {
                if (transcoderConfig != myTranscoderConfig && StringUtils.equals(transcoderConfig.getName(), myNameTextField.getStringValue(""))) {
                    ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("streamingConfigPanel.error.duplicateName");
                    valid = false;
                }
            }
        }
        return valid;
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        if (clickEvent.getButton() == myAddFilenameActivationButton) {
            myActivationsPanel.removeComponent(myAddActivationButtons);
            myActivationsPanel.addComponent(new FilenameActivationPanel(getApplication(), getComponentFactory(), new FilenameTranscoderActivation()));
            myActivationsPanel.addComponent(myAddActivationButtons);
        } else if (clickEvent.getButton() == myAddMp4CodecActivationButton) {
            myActivationsPanel.removeComponent(myAddActivationButtons);
            myActivationsPanel.addComponent(new Mp4CodecActivationPanel(getApplication(), getComponentFactory(), new Mp4CodecTranscoderActivation()));
            myActivationsPanel.addComponent(myAddActivationButtons);
        } else if (clickEvent.getButton() == myAddMp3BitRateActivationButton) {
            myActivationsPanel.removeComponent(myAddActivationButtons);
            myActivationsPanel.addComponent(new Mp3BitRateActivationPanel(getApplication(), getComponentFactory(), new Mp3BitRateTranscoderActivation()));
            myActivationsPanel.addComponent(myAddActivationButtons);
        } else if (clickEvent.getButton() == myAddMediaTypeActivationButton) {
            myActivationsPanel.removeComponent(myAddActivationButtons);
            myActivationsPanel.addComponent(new MediaTypeActivationPanel(getApplication(), getComponentFactory(), new MediaTypeTranscoderActivation()));
            myActivationsPanel.addComponent(myAddActivationButtons);
        } else {
            super.buttonClick(clickEvent);
        }
    }

    @Override
    protected Component getSaveFollowUpComponent() {
        return myStreamingConfigPanel;
    }

    @Override
    protected Component getCancelFollowUpComponent() {
        return myStreamingConfigPanel;
    }
}
