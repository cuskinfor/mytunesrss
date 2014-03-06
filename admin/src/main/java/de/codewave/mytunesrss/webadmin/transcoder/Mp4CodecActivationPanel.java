package de.codewave.mytunesrss.webadmin.transcoder;

import com.vaadin.ui.*;
import de.codewave.mytunesrss.config.transcoder.Mp4CodecTranscoderActivation;
import de.codewave.mytunesrss.webadmin.MyTunesRssWebAdmin;
import de.codewave.vaadin.ComponentFactory;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.validation.ValidRegExpValidator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mp4CodecActivationPanel extends Panel implements Button.ClickListener, ActivationPanel<Mp4CodecTranscoderActivation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mp4CodecTranscoderActivation.class);

    private Form myForm;
    private SmartTextField myCodecsTextField;
    private CheckBox myNegationCheckBox;

    public Mp4CodecActivationPanel(MyTunesRssWebAdmin application, ComponentFactory componentFactory, Mp4CodecTranscoderActivation activation) {
        VerticalLayout verticalLayout = componentFactory.createVerticalLayout(true, false);
        setContent(verticalLayout);

        myForm = componentFactory.createForm(application.getBundleString("transcoderConfigPanel.activation.mp4codec.caption"), true);
        myCodecsTextField = componentFactory.createTextField("transcoderConfigPanel.activation.mp4codec.codecs", new ValidRegExpValidator(application.getBundleString("transcoderConfigPanel.activation.mp4codec.error.codecs")));
        myCodecsTextField.setRequired(true);
        myForm.addField("codecs", myCodecsTextField);
        myNegationCheckBox = componentFactory.createCheckBox("transcoderConfigPanel.activation.negation");
        myForm.addField("negation", myNegationCheckBox);
        addComponent(myForm);

        Button deleteButton = componentFactory.createButton("transcoderConfigPanel.activation.delete", this);
        addComponent(new ButtonBar(componentFactory, deleteButton));

        initFromConfig(activation);
    }

    public void buttonClick(Button.ClickEvent event) {
        ((ComponentContainer) getParent()).removeComponent(this);
    }


    public Mp4CodecTranscoderActivation getConfig() {
        return new Mp4CodecTranscoderActivation(myCodecsTextField.getStringValue(""), myNegationCheckBox.booleanValue());
    }

    public void initFromConfig(Mp4CodecTranscoderActivation config) {
        myCodecsTextField.setValue(StringUtils.join(config.getCodecs(), ','), "");
        myNegationCheckBox.setValue(config.isNegation());
    }

    public boolean isValid() {
        LOGGER.debug("Validating MP4 codec activation panel.");
        return VaadinUtils.isValid(myForm);
    }
}
