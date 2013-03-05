package de.codewave.mytunesrss.webadmin.transcoder;

import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.config.transcoder.Activation;
import de.codewave.mytunesrss.config.transcoder.TranscoderActivation;
import de.codewave.mytunesrss.config.transcoder.TranscoderConfig;
import de.codewave.mytunesrss.webadmin.MyTunesRssWebAdmin;
import de.codewave.vaadin.ComponentFactory;
import de.codewave.vaadin.SmartTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TranscoderPanel extends Panel implements Button.ClickListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TranscoderPanel.class);

    private static final String TRANSCODER_NAME_REGEXP = "[a-zA-Z0-9 ]{1,40}";

    private MyTunesRssWebAdmin myApplication;
    private ComponentFactory myComponentFactory;
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
    private Button myDeleteButton;

    public TranscoderPanel(MyTunesRssWebAdmin application, ComponentFactory componentFactory) {
        myApplication = application;
        myComponentFactory = componentFactory;
        VerticalLayout verticalLayout = componentFactory.createVerticalLayout(false, false);
        verticalLayout.setMargin(new Layout.MarginInfo(false, true, true, true));
        setContent(verticalLayout);

        myActivationsPanel = new Panel("TODO: Activations");
        addComponent(myActivationsPanel);

        Form form = componentFactory.createForm(null, true);
        myNameTextField = componentFactory.createTextField("streamingConfigPanel.transcoder.name", new RegexpValidator(TRANSCODER_NAME_REGEXP, true, application.getBundleString("streamingConfigPanel.error.invalidName", 40)));
        myNameTextField.setRequired(true);
        form.addField("name", myNameTextField);
        mySuffixTextField = componentFactory.createTextField("streamingConfigPanel.transcoder.suffix");
        mySuffixTextField.setRequired(true);
        form.addField("suffix", mySuffixTextField);
        myContentTypeTextField = componentFactory.createTextField("streamingConfigPanel.transcoder.contentType");
        myContentTypeTextField.setRequired(true);
        form.addField("contentType", myContentTypeTextField);
        myMuxTextField = componentFactory.createTextField("streamingConfigPanel.transcoder.mux");
        form.addField("mux", myMuxTextField);
        myOptionsTextField = componentFactory.createTextField("streamingConfigPanel.transcoder.options");
        myOptionsTextField.setRequired(true);
        form.addField("options", myOptionsTextField);
        addComponent(form);
        myDeleteButton = componentFactory.createButton("streamingConfigPanel.transcoder.delete", this);
        addComponent(myDeleteButton);
        myAddFilenameActivationButton = componentFactory.createButton("streamingConfigPanel.transcoder.add", this);
        myAddMp4CodecActivationButton = componentFactory.createButton("streamingConfigPanel.transcoder.add", this);
        myAddMp3BitRateActivationButton = componentFactory.createButton("streamingConfigPanel.transcoder.add", this);
        myAddActivationButtons = new ButtonBar(componentFactory, myAddFilenameActivationButton, myAddMp4CodecActivationButton, myAddMp3BitRateActivationButton);
    }

    public TranscoderConfig getConfig() {
        TranscoderConfig config = new TranscoderConfig();
        config.setName(myNameTextField.getStringValue(""));
        config.setOptions(myOptionsTextField.getStringValue(""));
        config.setTargetContentType(myContentTypeTextField.getStringValue(""));
        config.setTargetMux(myMuxTextField.getStringValue(""));
        config.setTargetSuffix(mySuffixTextField.getStringValue(""));
        List<TranscoderActivation> activations = new ArrayList<TranscoderActivation>();
        Iterator<Component> componentIterator = myActivationsPanel.getComponentIterator();
        while (componentIterator.hasNext()) {
            ActivationPanel activationPanel = (ActivationPanel) componentIterator.next();
            activations.add(activationPanel.getConfig());
        }
        config.setTranscoderActivations(activations);
        return config;
    }
    
    public void initFromConfig(TranscoderConfig config) {
        myNameTextField.setValue(config.getName(), "");
        mySuffixTextField.setValue(config.getTargetSuffix(), "");
        myMuxTextField.setValue(config.getTargetMux(), "");
        myContentTypeTextField.setValue(config.getTargetContentType(), "");
        myOptionsTextField.setValue(config.getOptions(), "");
        myActivationsPanel.removeAllComponents();
        for (TranscoderActivation activation : config.getTranscoderActivations()) {
            Activation type = Activation.forActivation(activation);
            switch (type) {
                case FILENAME:
                    myActivationsPanel.addComponent(new FilenameActivationPanel(myApplication, myComponentFactory));
                    break;
                case MP3_BIT_RATE:
                    myActivationsPanel.addComponent(new Mp3BitRateActivationPanel(myApplication, myComponentFactory));
                    break;
                case MP4_CODEC:
                    myActivationsPanel.addComponent(new Mp4CodecActivationPanel(myApplication, myComponentFactory));
                    break;
                default:
                    LOGGER.warn("Ignoring unknown transcoder activation of type \"" + type + "\".");
            }
        }
        myActivationsPanel.addComponent(myAddActivationButtons);
    }

    public void setTranscoderName(String name) {
        myNameTextField.setValue(name, "");
    }

    public void buttonClick(Button.ClickEvent event) {
        if (event.getButton() == myDeleteButton) {
            ((ComponentContainer)getParent()).removeComponent(this);
        } else if (event.getButton() == myAddFilenameActivationButton) {
            myActivationsPanel.removeComponent(myAddActivationButtons);
            myActivationsPanel.addComponent(new FilenameActivationPanel(myApplication, myComponentFactory));
            myActivationsPanel.addComponent(myAddActivationButtons);
        } else if (event.getButton() == myAddMp4CodecActivationButton) {
            myActivationsPanel.removeComponent(myAddActivationButtons);
            myActivationsPanel.addComponent(new Mp4CodecActivationPanel(myApplication, myComponentFactory));
            myActivationsPanel.addComponent(myAddActivationButtons);
        } else if (event.getButton() == myAddMp3BitRateActivationButton) {
            myActivationsPanel.removeComponent(myAddActivationButtons);
            myActivationsPanel.addComponent(new Mp3BitRateActivationPanel(myApplication, myComponentFactory));
            myActivationsPanel.addComponent(myAddActivationButtons);
        }
    }
}
