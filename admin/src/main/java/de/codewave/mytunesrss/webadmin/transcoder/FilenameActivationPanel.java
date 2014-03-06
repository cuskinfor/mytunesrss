package de.codewave.mytunesrss.webadmin.transcoder;

import com.vaadin.ui.*;
import de.codewave.mytunesrss.config.transcoder.FilenameTranscoderActivation;
import de.codewave.mytunesrss.webadmin.MyTunesRssWebAdmin;
import de.codewave.vaadin.ComponentFactory;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.validation.ValidRegExpValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilenameActivationPanel extends Panel implements Button.ClickListener, ActivationPanel<FilenameTranscoderActivation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilenameActivationPanel.class);

    private Form myForm;
    private SmartTextField myPatternTextField;
    private CheckBox myNegationCheckBox;

    public FilenameActivationPanel(MyTunesRssWebAdmin application, ComponentFactory componentFactory, FilenameTranscoderActivation activation) {
        VerticalLayout verticalLayout = componentFactory.createVerticalLayout(true, false);
        setContent(verticalLayout);

        myForm = componentFactory.createForm(application.getBundleString("transcoderConfigPanel.activation.filename.caption"), true);
        myPatternTextField = componentFactory.createTextField("transcoderConfigPanel.activation.filename.pattern", new ValidRegExpValidator(application.getBundleString("transcoderConfigPanel.activation.filename.error.invalidPattern")));
        myPatternTextField.setRequired(true);
        myForm.addField("pattern", myPatternTextField);
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

    public FilenameTranscoderActivation getConfig() {
        return new FilenameTranscoderActivation(myPatternTextField.getStringValue(""), myNegationCheckBox.booleanValue());
    }

    public void initFromConfig(FilenameTranscoderActivation config) {
        myPatternTextField.setValue(config.getPattern(), "");
        myNegationCheckBox.setValue(config.isNegation());
    }

    public boolean isValid() {
        LOGGER.debug("Validating filename activation panel.");
        return VaadinUtils.isValid(myForm);
    }
}
