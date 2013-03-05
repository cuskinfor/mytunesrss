package de.codewave.mytunesrss.webadmin.transcoder;

import com.vaadin.ui.*;
import de.codewave.mytunesrss.config.transcoder.Mp3BitRateTranscoderActivation;
import de.codewave.mytunesrss.webadmin.MyTunesRssWebAdmin;
import de.codewave.vaadin.ComponentFactory;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.validation.MinMaxIntegerValidator;
import de.codewave.vaadin.validation.ValidRegExpValidator;

public class Mp3BitRateActivationPanel extends Panel implements Button.ClickListener, ActivationPanel<Mp3BitRateTranscoderActivation> {

    private SmartTextField myMinBitRateTextField;
    private SmartTextField myMaxBitRateTextField;
    private CheckBox myNegationCheckBox;

    public Mp3BitRateActivationPanel(MyTunesRssWebAdmin application, ComponentFactory componentFactory, Mp3BitRateTranscoderActivation activation) {
        VerticalLayout verticalLayout = componentFactory.createVerticalLayout(false, false);
        verticalLayout.setMargin(new Layout.MarginInfo(false, true, true, true));
        setContent(verticalLayout);

        Form form = componentFactory.createForm(application.getBundleString("transcoderPanel.activation.mp3bitrate.caption"), true);
        myMinBitRateTextField = componentFactory.createTextField("transcoderPanel.activation.mp3bitrate.min", new MinMaxIntegerValidator(application.getBundleString("transcoderPanel.activation.mp3bitrate.minError", 0, Integer.MAX_VALUE), 0, Integer.MAX_VALUE));
        myMinBitRateTextField.setRequired(true);
        form.addField("minBitRate", myMinBitRateTextField);
        myMaxBitRateTextField = componentFactory.createTextField("transcoderPanel.activation.mp3bitrate.max", new MinMaxIntegerValidator(application.getBundleString("transcoderPanel.activation.mp3bitrate.maxError", 0, Integer.MAX_VALUE), 0, Integer.MAX_VALUE));
        myMaxBitRateTextField.setRequired(true);
        form.addField("maxBitRate", myMaxBitRateTextField);
        myNegationCheckBox = componentFactory.createCheckBox("transcoderPanel.activation.negation");
        form.addField("negation", myNegationCheckBox);
        addComponent(form);

        Button deleteButton = componentFactory.createButton("transcoderPanel.activation.delete", this);
        addComponent(new ButtonBar(componentFactory, deleteButton));

        initFromConfig(activation);
    }

    public void buttonClick(Button.ClickEvent event) {
        ((ComponentContainer) getParent()).removeComponent(this);
    }

    public Mp3BitRateTranscoderActivation getConfig() {
        return new Mp3BitRateTranscoderActivation(myMinBitRateTextField.getIntegerValue(0), myMaxBitRateTextField.getIntegerValue(Integer.MAX_VALUE), myNegationCheckBox.booleanValue());
    }

    public void initFromConfig(Mp3BitRateTranscoderActivation config) {
        myMinBitRateTextField.setValue(config.getMinBitRate());
        myMaxBitRateTextField.setValue(config.getMaxBitRate());
        myNegationCheckBox.setValue(config.isNegation());
    }
}
