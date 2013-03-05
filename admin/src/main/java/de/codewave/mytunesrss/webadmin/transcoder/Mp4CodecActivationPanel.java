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

    public Mp4CodecActivationPanel(MyTunesRssWebAdmin application, ComponentFactory componentFactory) {
        VerticalLayout verticalLayout = componentFactory.createVerticalLayout(false, false);
        verticalLayout.setMargin(new Layout.MarginInfo(false, true, true, true));
        setContent(verticalLayout);

        Form form = componentFactory.createForm("TODO: MP4 Codec Matching", true);
        myCodecsTextField = componentFactory.createTextField("TODO: MP4 Codecs", new ValidRegExpValidator("streamingConfigPanel.error.invalidPattern"));
        myCodecsTextField.setRequired(true);
        form.addField("codecs", myCodecsTextField);
        myNegationCheckBox = componentFactory.createCheckBox("TODO: Negation");
        form.addField("negation", myNegationCheckBox);
        addComponent(form);

        Button deleteButton = componentFactory.createButton("TODO: Delete", this);
        addComponent(new ButtonBar(componentFactory, deleteButton));
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
