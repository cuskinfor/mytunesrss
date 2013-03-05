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

    public FilenameActivationPanel(MyTunesRssWebAdmin application, ComponentFactory componentFactory, FilenameTranscoderActivation activation) {
        VerticalLayout verticalLayout = componentFactory.createVerticalLayout(false, false);
        verticalLayout.setMargin(new Layout.MarginInfo(false, true, true, true));
        setContent(verticalLayout);

        Form form = componentFactory.createForm(application.getBundleString("transcoderPanel.activation.filename.caption"), true);
        myPatternTextField = componentFactory.createTextField("transcoderPanel.activation.filename.pattern", new ValidRegExpValidator(application.getBundleString("transcoderPanel.activation.filename.error.invalidPattern")));
        myPatternTextField.setRequired(true);
        form.addField("pattern", myPatternTextField);
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

    public FilenameTranscoderActivation getConfig() {
        return new FilenameTranscoderActivation(myPatternTextField.getStringValue(""), myNegationCheckBox.booleanValue());
    }

    public void initFromConfig(FilenameTranscoderActivation config) {
        myPatternTextField.setValue(config.getPattern(), "");
        myNegationCheckBox.setValue(config.isNegation());
    }
}
