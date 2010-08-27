package de.codewave.mytunesrss.webadmin;

import com.vaadin.ui.Form;
import de.codewave.mytunesrss.FlashPlayerConfig;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;

public class FlashPlayerEditPanel extends MyTunesRssConfigPanel {

    private AddonsConfigPanel myAddonsConfigPanel;
    private FlashPlayerConfig myFlashPlayerConfig;
    private Form myForm;
    private SmartTextField myName;
    private SmartTextField myHtml;
    // TODO Upload for player files. Subdir with UUID? How to reference in HTML?

    public FlashPlayerEditPanel(AddonsConfigPanel addonsConfigPanel, FlashPlayerConfig flashPlayerConfig) {
        myAddonsConfigPanel = addonsConfigPanel;
        myFlashPlayerConfig = flashPlayerConfig;
    }

    public void attach() {
        init(null, getComponentFactory().createGridLayout(1, 2, true, true));

        myForm = getComponentFactory().createForm(null, true);
        myName = getComponentFactory().createTextField("flashPlayerEditPanel.name");
        setRequired(myName);
        myForm.addField("name", myName);
        myHtml = getComponentFactory().createTextField("flashPlayerEditPanel.html");
        setRequired(myHtml);
        myHtml.setRows(10);
        myForm.addField("html", myHtml);

        addComponent(getComponentFactory().surroundWithPanel(myForm, FORM_PANEL_MARGIN_INFO, getBundleString("flashPlayerEditPanel.caption.form")));

        attach(0, 1, 0, 1);

        initFromConfig();
    }

    @Override
    protected void writeToConfig() {
        myFlashPlayerConfig.setName(myName.getStringValue("Unknown player"));
        myFlashPlayerConfig.setHtml(myHtml.getStringValue("<!-- missing flash player html -->"));
        myAddonsConfigPanel.addOrUpdatePlayer(myFlashPlayerConfig);
    }

    @Override
    protected void initFromConfig() {
        myName.setValue(myFlashPlayerConfig.getName());
        myHtml.setValue(myFlashPlayerConfig.getHtml());
    }

    protected boolean beforeSave() {
        if (!VaadinUtils.isValid(myForm)) {
            getApplication().showError("error.formInvalid");
        } else {
            writeToConfig();
            closeWindow();
        }
        return false; // make sure the default operation is not used
    }

    @Override
    protected boolean beforeCancel() {
        closeWindow();
        return false; // make sure the default operation is not used
    }

    private void closeWindow() {
        getWindow().getParent().getWindow().removeWindow(getWindow());
    }
}
