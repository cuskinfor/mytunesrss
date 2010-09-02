/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.TranscoderConfig;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;
import de.codewave.vaadin.component.ServerSideFileChooser;
import de.codewave.vaadin.component.ServerSideFileChooserWindow;
import de.codewave.vaadin.validation.FileValidator;
import de.codewave.vaadin.validation.ValidRegExpValidator;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class StreamingConfigPanel extends MyTunesRssConfigPanel {

    private static final String TRANSCODER_NAME_REGEXP = "[a-zA-Z0-9 ]{1,40}";

    private Panel myTranscoderAccordionPanel;
    private Panel myTranscoderPanel;
    private Form myHttpLiveStreamingForm;
    private SmartTextField myHttpLiveStreamingBinary;
    private Button mySelectHttpLiveStreamingBinary;
    private SmartTextField myHttpLiveStreamingOptions;
    private Form myCacheForm;
    private Button myAddTranscoder;
    private SmartTextField myStreamingCacheTimeout;
    private SmartTextField myStreamingCacheMaxFiles;
    private AtomicLong myTranscoderNumberGenerator = new AtomicLong(1);

    public void attach() {
        init(getBundleString("streamingConfigPanel.caption"), getComponentFactory().createGridLayout(1, 4, true, true));
        myTranscoderPanel = new Panel(getBundleString("streamingConfigPanel.caption.transcoder"));
        ((Layout) myTranscoderPanel.getContent()).setMargin(true);
        ((Layout.SpacingHandler) myTranscoderPanel.getContent()).setSpacing(true);
        myTranscoderAccordionPanel = new Panel();
        Accordion accordion = new Accordion();
        myTranscoderAccordionPanel.setContent(accordion);
        for (int i = 0; i < MyTunesRss.CONFIG.getTranscoderConfigs().size(); i++) {
            createTranscoder();
        }
        myTranscoderPanel.addComponent(myTranscoderAccordionPanel);
        myAddTranscoder = getComponentFactory().createButton("streamingConfigPanel.transcoder.add", this);
        myTranscoderPanel.addComponent(myAddTranscoder);
        addComponent(myTranscoderPanel);
        myHttpLiveStreamingForm = getComponentFactory().createForm(null, true);
        myHttpLiveStreamingBinary = getComponentFactory().createTextField("streamingConfigPanel.httpLiveStreaming.binary", new FileValidator(getBundleString("streamingConfigPanel.error.invalidBinary", null), FileValidator.PATTERN_ALL, FileValidator.PATTERN_ALL));
        mySelectHttpLiveStreamingBinary = getComponentFactory().createButton("streamingConfigPanel.httpLiveStreaming.selectBinary", this);
        myHttpLiveStreamingOptions = getComponentFactory().createTextField("streamingConfigPanel.httpLiveStreaming.options");
        myHttpLiveStreamingForm.addField("binary", myHttpLiveStreamingBinary);
        myHttpLiveStreamingForm.addField("select", mySelectHttpLiveStreamingBinary);
        addComponent(getComponentFactory().surroundWithPanel(myHttpLiveStreamingForm, FORM_PANEL_MARGIN_INFO, getBundleString("streamingConfigPanel.caption.httpLiveStreaming")));
        myCacheForm = getComponentFactory().createForm(null, true);
        myStreamingCacheTimeout = getComponentFactory().createTextField("streamingConfigPanel.cache.streamingCacheTimeout", getApplication().getValidatorFactory().createMinMaxValidator(0, 1440));
        myStreamingCacheMaxFiles = getComponentFactory().createTextField("streamingConfigPanel.cache.streamingCacheMaxFiles", getApplication().getValidatorFactory().createMinMaxValidator(0, 10000));
        myCacheForm.addField("timeout", myStreamingCacheTimeout);
        myCacheForm.addField("limit", myStreamingCacheMaxFiles);
        addComponent(getComponentFactory().surroundWithPanel(myCacheForm, FORM_PANEL_MARGIN_INFO, getBundleString("streamingConfigPanel.caption.cache")));

        attach(0, 3, 0, 3);

        initFromConfig();
    }

    private Form createTranscoder() {
        Form form = getComponentFactory().createForm(null, true);
        SmartTextField nameTextField = getComponentFactory().createTextField("streamingConfigPanel.transcoder.name", new RegexpValidator(TRANSCODER_NAME_REGEXP, true, getBundleString("streamingConfigPanel.error.invalidName", 40)));
        nameTextField.setRequired(true);
        form.addField("name", nameTextField);
        SmartTextField patternTextField = getComponentFactory().createTextField("streamingConfigPanel.transcoder.pattern", new ValidRegExpValidator("streamingConfigPanel.error.invalidPattern"));
        patternTextField.setRequired(true);
        form.addField("pattern", patternTextField);
        form.addField("codecs", getComponentFactory().createTextField("streamingConfigPanel.transcoder.codecs"));
        SmartTextField suffixTextField = getComponentFactory().createTextField("streamingConfigPanel.transcoder.suffix");
        suffixTextField.setRequired(true);
        form.addField("suffix", suffixTextField);
        SmartTextField contentTypeTextField = getComponentFactory().createTextField("streamingConfigPanel.transcoder.contentType");
        contentTypeTextField.setRequired(true);
        form.addField("contentType", contentTypeTextField);
        SmartTextField binaryTextField = getComponentFactory().createTextField("streamingConfigPanel.transcoder.binary", new FileValidator(getBundleString("streamingConfigPanel.error.invalidBinary"), null, FileValidator.PATTERN_ALL));
        binaryTextField.setRequired(true);
        form.addField("binary", binaryTextField);
        Button selectBinary = getComponentFactory().createButton("streamingConfigPanel.transcoder.select", this);
        selectBinary.setData(form);
        form.addField("selectBinary", selectBinary);
        form.addField("options", getComponentFactory().createTextField("streamingConfigPanel.transcoder.options"));
        Button delete = getComponentFactory().createButton("streamingConfigPanel.transcoder.delete", this);
        delete.setData(form);
        Panel panel = getComponentFactory().surroundWithPanel(form, new Layout.MarginInfo(false, true, true, true), null);
        myTranscoderAccordionPanel.addComponent(panel);
        panel.addComponent(delete);
        return form;
    }

    protected void initFromConfig() {
        Iterator<Component> componentIterator = myTranscoderAccordionPanel.getComponentIterator();
        for (int i = 0; i < MyTunesRss.CONFIG.getTranscoderConfigs().size(); i++) {
            TranscoderConfig config = MyTunesRss.CONFIG.getTranscoderConfigs().get(i);
            Panel panel = (Panel) componentIterator.next();
            VaadinUtils.getAncestor(panel, TabSheet.class).getTab(panel).setCaption(config.getName());
            Form form = getTranscoderForm(panel);
            form.getField("name").setValue(config.getName());
            form.getField("pattern").setValue(config.getPattern());
            form.getField("codecs").setValue(config.getMp4Codecs());
            form.getField("suffix").setValue(config.getTargetSuffix());
            form.getField("contentType").setValue(config.getTargetContentType());
            form.getField("binary").setValue(config.getBinary());
            form.getField("options").setValue(config.getOptions());
        }
        myHttpLiveStreamingBinary.setValue(MyTunesRss.CONFIG.getHttpLiveStreamingBinary());
        myHttpLiveStreamingOptions.setValue(MyTunesRss.CONFIG.getHttpLiveStreamingOptions());
        myStreamingCacheTimeout.setValue(MyTunesRss.CONFIG.getStreamingCacheTimeout(), 0, 1440, "0");
        myStreamingCacheMaxFiles.setValue(MyTunesRss.CONFIG.getStreamingCacheMaxFiles(), 0, 10000, "0");
    }

    protected void writeToConfig() {
        List<TranscoderConfig> configs = MyTunesRss.CONFIG.getTranscoderConfigs();
        configs.clear();
        Iterator<Component> componentIterator = myTranscoderAccordionPanel.getComponentIterator();
        while (componentIterator.hasNext()) {
            Form form = getTranscoderForm((Panel) componentIterator.next());
            TranscoderConfig conf = new TranscoderConfig();
            conf.setName(((SmartTextField) form.getField("name")).getStringValue(null));
            conf.setPattern(((SmartTextField) form.getField("pattern")).getStringValue(null));
            conf.setMp4Codecs(((SmartTextField) form.getField("codecs")).getStringValue(null));
            conf.setTargetSuffix(((SmartTextField) form.getField("suffix")).getStringValue(null));
            conf.setTargetContentType(((SmartTextField) form.getField("contentType")).getStringValue(null));
            conf.setBinary(((SmartTextField) form.getField("binary")).getStringValue(null));
            conf.setOptions(((SmartTextField) form.getField("options")).getStringValue(null));
            configs.add(conf);
        }
        MyTunesRss.CONFIG.setHttpLiveStreamingBinary(myHttpLiveStreamingBinary.getStringValue(null));
        MyTunesRss.CONFIG.setHttpLiveStreamingOptions(myHttpLiveStreamingOptions.getStringValue(null));
        MyTunesRss.CONFIG.setStreamingCacheTimeout(myStreamingCacheTimeout.getIntegerValue(0));
        MyTunesRss.CONFIG.setStreamingCacheMaxFiles(myStreamingCacheMaxFiles.getIntegerValue(0));
        MyTunesRss.CONFIG.save();
    }

    @Override
    protected boolean beforeSave() {
        boolean valid = VaadinUtils.isValid(myHttpLiveStreamingForm, myCacheForm);
        Iterator<Component> formIterator = myTranscoderAccordionPanel.getComponentIterator();
        while (formIterator.hasNext()) {
            Panel panel = (Panel) formIterator.next();
            Form form = getTranscoderForm(panel);
            valid &= VaadinUtils.isValid(form);
        }
        if (!valid) {
            getApplication().showError("error.formInvalid");
        }
        return valid;
    }

    private Form getTranscoderForm(Panel panel) {
        for (Iterator<Component> componentIterator = panel.getComponentIterator(); componentIterator.hasNext();) {
            Component component = componentIterator.next();
            if (component instanceof Form) {
                return (Form) component;
            }
        }
        throw new IllegalArgumentException("No transcoder form found in panel.");
    }

    @Override
    protected boolean beforeReset() {
        myTranscoderAccordionPanel.removeAllComponents();
        for (int i = 0; i < MyTunesRss.CONFIG.getTranscoderConfigs().size(); i++) {
            createTranscoder();
        }
        myTranscoderNumberGenerator.set(1);
        return true;
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        if (clickEvent.getButton() == myAddTranscoder) {
            Form form = createTranscoder();
            String name = getBundleString("transcoderConfigPanel.transcoder.defaultName", myTranscoderNumberGenerator.getAndIncrement());
            form.getField("name").setValue(name);
            Panel panel = VaadinUtils.getAncestor(form, Panel.class);
            ((TabSheet) panel.getParent()).getTab(panel).setCaption(name);
        } else if (clickEvent.getButton() == mySelectHttpLiveStreamingBinary) {
            new ServerSideFileChooserWindow(50, Sizeable.UNITS_EM, null, getBundleString("streamingConfigPanel.caption.selectHttpLiveStreamingBinary"), new File((String) myHttpLiveStreamingBinary.getValue()), null, ServerSideFileChooser.PATTERN_ALL, false, "Roots") { // TODO i18n

                @Override
                protected void onFileSelected(File file) {
                    myHttpLiveStreamingBinary.setValue(file.getAbsolutePath());
                    getApplication().getMainWindow().removeWindow(this);
                }
            }.show(getApplication().getMainWindow());
        } else if (VaadinUtils.isChild(myTranscoderAccordionPanel, clickEvent.getButton())) {
            final Form buttonForm = (Form) clickEvent.getButton().getData();
            if (buttonForm.getField("selectBinary") == clickEvent.getButton()) {
                new ServerSideFileChooserWindow(50, Sizeable.UNITS_EM, null, getBundleString("streamingConfigPanel.caption.selectBinary"), new File((String) buttonForm.getField("binary").getValue()), null, ServerSideFileChooser.PATTERN_ALL, false, "Roots") { // TODO i18n

                    @Override
                    protected void onFileSelected(File file) {
                        buttonForm.getField("binary").setValue(file.getAbsolutePath());
                        getApplication().getMainWindow().removeWindow(this);
                    }
                }.show(getApplication().getMainWindow());
            } else {
                final Button yes = new Button(getBundleString("button.yes"));
                Button no = new Button(getBundleString("button.no"));
                new OptionWindow(30, Sizeable.UNITS_EM, null, getBundleString("streamingConfigDialog.deleteConfirmation.caption"), getBundleString("streamingConfigDialog.deleteConfirmation.message", buttonForm.getField("name").getValue()), yes, no) {
                    public void clicked(Button button) {
                        if (button == yes) {
                            myTranscoderAccordionPanel.removeComponent(VaadinUtils.getAncestor(buttonForm, Panel.class));
                        }
                    }
                }.show(getApplication().getMainWindow());
            }
        } else {
            super.buttonClick(clickEvent);
        }
    }
}
