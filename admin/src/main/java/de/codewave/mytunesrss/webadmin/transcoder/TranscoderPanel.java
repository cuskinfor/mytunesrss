package de.codewave.mytunesrss.webadmin.transcoder;

import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.event.FieldEvents;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.config.transcoder.*;
import de.codewave.mytunesrss.webadmin.MyTunesRssWebAdmin;
import de.codewave.mytunesrss.webadmin.StreamingConfigPanel;
import de.codewave.vaadin.ComponentFactory;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TranscoderPanel extends Panel implements Button.ClickListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TranscoderPanel.class);

    private static final String TRANSCODER_NAME_REGEXP = "[a-zA-Z0-9 ]{1,40}";

    private StreamingConfigPanel myStreamingConfigPanel;
    private MyTunesRssWebAdmin myApplication;
    private ComponentFactory myComponentFactory;
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
    private Button myDeleteButton;

    public TranscoderPanel(StreamingConfigPanel streamingConfigPanel, MyTunesRssWebAdmin application, ComponentFactory componentFactory) {
        myStreamingConfigPanel = streamingConfigPanel;
        myApplication = application;
        myComponentFactory = componentFactory;
        addStyleName("light");
        VerticalLayout verticalLayout = componentFactory.createVerticalLayout(false, true);
        verticalLayout.setMargin(new Layout.MarginInfo(false, true, true, true));
        setContent(verticalLayout);

        myForm = componentFactory.createForm(null, true);
        myNameTextField = componentFactory.createTextField("transcoderPanel.name", new RegexpValidator(TRANSCODER_NAME_REGEXP, true, application.getBundleString("transcoderPanel.error.invalidName", 40)));
        myNameTextField.setRequired(true);
        myNameTextField.addListener(new FieldEvents.TextChangeListener() {
            public void textChange(FieldEvents.TextChangeEvent event) {
                ((TabSheet) getParent()).getTab(TranscoderPanel.this).setCaption(event.getText());
            }
        });
        myForm.addField("name", myNameTextField);
        mySuffixTextField = componentFactory.createTextField("transcoderPanel.suffix");
        mySuffixTextField.setRequired(true);
        myForm.addField("suffix", mySuffixTextField);
        myContentTypeTextField = componentFactory.createTextField("transcoderPanel.contentType");
        myContentTypeTextField.setRequired(true);
        myForm.addField("contentType", myContentTypeTextField);
        myMuxTextField = componentFactory.createTextField("transcoderPanel.mux");
        myForm.addField("mux", myMuxTextField);
        myOptionsTextField = componentFactory.createTextField("transcoderPanel.options");
        myOptionsTextField.setRequired(true);
        myForm.addField("options", myOptionsTextField);
        addComponent(myForm);

        myActivationsPanel = new Panel(application.getBundleString("transcoderPanel.activations"));
        VerticalLayout activationsPanelLayout = componentFactory.createVerticalLayout(true, true);
        myActivationsPanel.setContent(activationsPanelLayout);
        addComponent(myActivationsPanel);

        myDeleteButton = componentFactory.createButton("transcoderPanel.delete", this);
        addComponent(myDeleteButton);

        myAddFilenameActivationButton = componentFactory.createButton("transcoderPanel.activation.addFilename", this);
        myAddMp4CodecActivationButton = componentFactory.createButton("transcoderPanel.activation.addMp4Codecs", this);
        myAddMp3BitRateActivationButton = componentFactory.createButton("transcoderPanel.activation.addMp3BitRate", this);
        myAddActivationButtons = new ButtonBar(componentFactory, myAddFilenameActivationButton, myAddMp4CodecActivationButton, myAddMp3BitRateActivationButton);
    }

    public TranscoderConfig getConfig() {
        TranscoderConfig config = new TranscoderConfig();
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
                    myActivationsPanel.addComponent(new FilenameActivationPanel(myApplication, myComponentFactory, (FilenameTranscoderActivation) activation));
                    break;
                case MP3_BIT_RATE:
                    myActivationsPanel.addComponent(new Mp3BitRateActivationPanel(myApplication, myComponentFactory, (Mp3BitRateTranscoderActivation) activation));
                    break;
                case MP4_CODEC:
                    myActivationsPanel.addComponent(new Mp4CodecActivationPanel(myApplication, myComponentFactory, (Mp4CodecTranscoderActivation) activation));
                    break;
                default:
                    LOGGER.warn("Ignoring unknown transcoder activation of type \"" + type + "\".");
            }
        }
        myActivationsPanel.addComponent(myAddActivationButtons);
        ((TabSheet) getParent()).getTab(this).setCaption(myNameTextField.getStringValue(""));
    }

    public void buttonClick(Button.ClickEvent event) {
        if (event.getButton() == myDeleteButton) {
            final Button yes = new Button(myApplication.getBundleString("button.yes"));
            Button no = new Button(myApplication.getBundleString("button.no"));
            new OptionWindow(30, Sizeable.UNITS_EM, null, myApplication.getBundleString("transcoderPanel.deleteConfirmation.caption"), myApplication.getBundleString("transcoderPanel.deleteConfirmation.message", myNameTextField.getStringValue("")), yes, no) {
                public void clicked(Button button) {
                    if (button == yes) {
                        myStreamingConfigPanel.removeTranscoder(TranscoderPanel.this);
                    }
                }
            }.show(getWindow());
        } else if (event.getButton() == myAddFilenameActivationButton) {
            myActivationsPanel.removeComponent(myAddActivationButtons);
            myActivationsPanel.addComponent(new FilenameActivationPanel(myApplication, myComponentFactory, new FilenameTranscoderActivation()));
            myActivationsPanel.addComponent(myAddActivationButtons);
        } else if (event.getButton() == myAddMp4CodecActivationButton) {
            myActivationsPanel.removeComponent(myAddActivationButtons);
            myActivationsPanel.addComponent(new Mp4CodecActivationPanel(myApplication, myComponentFactory, new Mp4CodecTranscoderActivation()));
            myActivationsPanel.addComponent(myAddActivationButtons);
        } else if (event.getButton() == myAddMp3BitRateActivationButton) {
            myActivationsPanel.removeComponent(myAddActivationButtons);
            myActivationsPanel.addComponent(new Mp3BitRateActivationPanel(myApplication, myComponentFactory, new Mp3BitRateTranscoderActivation()));
            myActivationsPanel.addComponent(myAddActivationButtons);
        }
    }

    public boolean isValid() {
        LOGGER.debug("Validating transcoder panel.");
        boolean valid = VaadinUtils.isValid(myForm);
        Iterator<Component> componentIterator = myActivationsPanel.getComponentIterator();
        while (componentIterator.hasNext()) {
            Component component = componentIterator.next();
            if (component instanceof ActivationPanel) {
                valid &= ((ActivationPanel)component).isValid();
            }
        }
        return valid;
    }
}
