package de.codewave.mytunesrss.webadmin.transcoder;

import com.vaadin.ui.*;
import de.codewave.mytunesrss.config.transcoder.Mp4CodecTranscoderActivation;
import de.codewave.mytunesrss.config.transcoder.TranscoderActivation;
import de.codewave.mytunesrss.webadmin.MyTunesRssWebAdmin;
import de.codewave.vaadin.ComponentFactory;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.validation.ValidRegExpValidator;
import org.apache.commons.lang.StringUtils;

public class Mp4CodecActivationPanel extends Panel implements Button.ClickListener, ActivationPanel<Mp4CodecTranscoderActivation> {

    private SmartTextField myCodecsTextField;
    private CheckBox myNegationCheckBox;

    public Mp4CodecActivationPanel(MyTunesRssWebAdmin application, ComponentFactory componentFactory, Mp4CodecTranscoderActivation activation) {
        VerticalLayout verticalLayout = componentFactory.createVerticalLayout(true, false);
        setContent(verticalLayout);

        Form form = componentFactory.createForm(application.getBundleString("transcoderPanel.activation.mp4codec.caption"), true);
        myCodecsTextField = componentFactory.createTextField("transcoderPanel.activation.mp4codec.codecs", new ValidRegExpValidator(application.getBundleString("transcoderPanel.activation.mp4codec.error.codecs")));
        myCodecsTextField.setRequired(true);
        form.addField("codecs", myCodecsTextField);
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


    public Mp4CodecTranscoderActivation getConfig() {
        return new Mp4CodecTranscoderActivation(myCodecsTextField.getStringValue(""), myNegationCheckBox.booleanValue());
    }

    public void initFromConfig(Mp4CodecTranscoderActivation config) {
        myCodecsTextField.setValue(StringUtils.join(config.getCodecs(), ','), "");
        myNegationCheckBox.setValue(config.isNegation());
    }
}
