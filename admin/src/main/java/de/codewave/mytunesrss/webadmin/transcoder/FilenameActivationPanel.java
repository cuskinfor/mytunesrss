package de.codewave.mytunesrss.webadmin.transcoder;

import com.vaadin.ui.*;
import de.codewave.mytunesrss.config.transcoder.FilenameTranscoderActivation;
import de.codewave.mytunesrss.webadmin.MyTunesRssWebAdmin;
import de.codewave.vaadin.ComponentFactory;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.validation.ValidRegExpValidator;

public class FilenameActivationPanel extends Panel implements Button.ClickListener, ActivationPanel<FilenameTranscoderActivation> {

    private SmartTextField myPatternTextField;
    private CheckBox myNegationCheckBox;

    public FilenameActivationPanel(MyTunesRssWebAdmin application, ComponentFactory componentFactory) {
        VerticalLayout verticalLayout = componentFactory.createVerticalLayout(false, false);
        verticalLayout.setMargin(new Layout.MarginInfo(false, true, true, true));
        setContent(verticalLayout);

        Form form = componentFactory.createForm("TODO: Filename matching", true);
        myPatternTextField = componentFactory.createTextField("streamingConfigPanel.transcoder.pattern", new ValidRegExpValidator("streamingConfigPanel.error.invalidPattern"));
        myPatternTextField.setRequired(true);
        form.addField("pattern", myPatternTextField);
        myNegationCheckBox = componentFactory.createCheckBox("TODO: Negation");
        form.addField("negation", myNegationCheckBox);
        addComponent(form);

        Button deleteButton = componentFactory.createButton("TODO: Delete", this);
        addComponent(new ButtonBar(componentFactory, deleteButton));
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
}
