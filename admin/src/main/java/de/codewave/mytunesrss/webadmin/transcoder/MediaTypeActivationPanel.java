/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.transcoder;

import com.vaadin.ui.*;
import de.codewave.mytunesrss.config.MediaType;
import de.codewave.mytunesrss.config.transcoder.FilenameTranscoderActivation;
import de.codewave.mytunesrss.config.transcoder.MediaTypeTranscoderActivation;
import de.codewave.mytunesrss.webadmin.MyTunesRssWebAdmin;
import de.codewave.vaadin.ComponentFactory;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.validation.ValidRegExpValidator;
import org.seamless.util.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MediaTypeActivationPanel extends Panel implements Button.ClickListener, ActivationPanel<MediaTypeTranscoderActivation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MediaTypeActivationPanel.class);

    private Form myForm;
    private CheckBox myAudioCheckbox;
    private CheckBox myVideoCheckbox;
    private CheckBox myNegationCheckBox;

    public MediaTypeActivationPanel(MyTunesRssWebAdmin application, ComponentFactory componentFactory, MediaTypeTranscoderActivation activation) {
        VerticalLayout verticalLayout = componentFactory.createVerticalLayout(true, false);
        setContent(verticalLayout);

        myForm = componentFactory.createForm(application.getBundleString("transcoderConfigPanel.activation.mediatype.caption"), true);
        myAudioCheckbox = componentFactory.createCheckBox("transcoderConfigPanel.activation.mediatype.audio");
        myVideoCheckbox = componentFactory.createCheckBox("transcoderConfigPanel.activation.mediatype.video");
        myForm.addField("audio", myAudioCheckbox);
        myForm.addField("video", myVideoCheckbox);
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

    public MediaTypeTranscoderActivation getConfig() {
        List<MediaType> types = new ArrayList<>();
        if (myAudioCheckbox.booleanValue()) {
            types.add(MediaType.Audio);
        }
        if (myVideoCheckbox.booleanValue()) {
            types.add(MediaType.Video);
        }
        return new MediaTypeTranscoderActivation(types, myNegationCheckBox.booleanValue());
    }

    public void initFromConfig(MediaTypeTranscoderActivation config) {
        myAudioCheckbox.setValue(config.getMediaTypes().contains(MediaType.Audio));
        myVideoCheckbox.setValue(config.getMediaTypes().contains(MediaType.Video));
        myNegationCheckBox.setValue(config.isNegation());
    }

    public boolean isValid() {
        LOGGER.debug("Validating media type activation panel.");
        return VaadinUtils.isValid(myForm);
    }
}
