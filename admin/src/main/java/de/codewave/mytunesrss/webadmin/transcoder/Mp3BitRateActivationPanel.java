package de.codewave.mytunesrss.webadmin.transcoder;

import com.vaadin.ui.*;
import de.codewave.mytunesrss.config.transcoder.Mp3BitRateTranscoderActivation;
import de.codewave.mytunesrss.webadmin.MyTunesRssWebAdmin;
import de.codewave.vaadin.ComponentFactory;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.validation.MinMaxIntegerValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mp3BitRateActivationPanel extends Panel implements Button.ClickListener, ActivationPanel<Mp3BitRateTranscoderActivation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mp3BitRateActivationPanel.class);

    private Form myForm;
    private SmartTextField myMinBitRateTextField;
    private SmartTextField myMaxBitRateTextField;
    private CheckBox myNegationCheckBox;

    public Mp3BitRateActivationPanel(MyTunesRssWebAdmin application, ComponentFactory componentFactory, Mp3BitRateTranscoderActivation activation) {
        VerticalLayout verticalLayout = componentFactory.createVerticalLayout(true, false);
        setContent(verticalLayout);

        myForm = componentFactory.createForm(application.getBundleString("transcoderConfigPanel.activation.mp3bitrate.caption"), true);
        myMinBitRateTextField = componentFactory.createTextField("transcoderConfigPanel.activation.mp3bitrate.min", new MinMaxIntegerValidator(application.getBundleString("transcoderConfigPanel.activation.mp3bitrate.minError", 0, Integer.MAX_VALUE), 0, Integer.MAX_VALUE));
        myMinBitRateTextField.setRequired(true);
        myForm.addField("minBitRate", myMinBitRateTextField);
        myMaxBitRateTextField = componentFactory.createTextField("transcoderConfigPanel.activation.mp3bitrate.max", new MinMaxIntegerValidator(application.getBundleString("transcoderConfigPanel.activation.mp3bitrate.maxError", 0, Integer.MAX_VALUE), 0, Integer.MAX_VALUE));
        myMaxBitRateTextField.setRequired(true);
        myForm.addField("maxBitRate", myMaxBitRateTextField);
        myNegationCheckBox = componentFactory.createCheckBox("transcoderConfigPanel.activation.negation");
        myForm.addField("negation", myNegationCheckBox);
        addComponent(myForm);

        Button deleteButton = componentFactory.createButton("transcoderConfigPanel.activation.delete", this);
        addComponent(new ButtonBar(componentFactory, deleteButton));

        initFromConfig(activation);
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        ((ComponentContainer) getParent()).removeComponent(this);
    }

    @Override
    public Mp3BitRateTranscoderActivation getConfig() {
        return new Mp3BitRateTranscoderActivation(myMinBitRateTextField.getIntegerValue(0), myMaxBitRateTextField.getIntegerValue(Integer.MAX_VALUE), myNegationCheckBox.booleanValue());
    }

    @Override
    public void initFromConfig(Mp3BitRateTranscoderActivation config) {
        myMinBitRateTextField.setValue(config.getMinBitRate());
        myMaxBitRateTextField.setValue(config.getMaxBitRate());
        myNegationCheckBox.setValue(config.isNegation());
    }

    @Override
    public boolean isValid() {
        LOGGER.debug("Validating MP3 bitrate activation panel.");
        return VaadinUtils.isValid(myForm);
    }
}
