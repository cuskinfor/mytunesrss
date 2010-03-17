/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.Application;
import com.vaadin.data.Validatable;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.TranscoderConfig;
import de.codewave.vaadin.ComponentFactory;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;
import de.codewave.vaadin.component.ServerSideFileChooser;
import de.codewave.vaadin.component.ServerSideFileChooserWindow;
import de.codewave.vaadin.validation.FileValidator;
import de.codewave.vaadin.validation.ValidRegExpValidator;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class StreamingConfigPanel extends MyTunesRssConfigPanel {

    public static class TranscoderNameValidator extends AbstractStringValidator {
        public TranscoderNameValidator() {
            super(getBundleString("streamingConfigPanel.error.invalidName", 40));
        }

        @Override
        protected boolean isValidString(String s) {
            return StringUtils.isNotBlank(s) && StringUtils.isAlphanumericSpace(s) && s.length() <= 40;
        }
    }

    private Panel myTranscoderAccordionPanel;
    private Panel myTranscoderPanel;
    private Form myCacheForm;
    private Button myAddTranscoder;
    private TextField myStreamingCacheTimeout;
    private TextField myStreamingCacheMaxFiles;
    private AtomicLong myTranscoderNumberGenerator = new AtomicLong(1);

    public StreamingConfigPanel(Application application, ComponentFactory componentFactory) {
        super(application, getBundleString("streamingConfigPanel.caption"), componentFactory.createGridLayout(1, 3, true, true), componentFactory);
    }

    protected void init(Application application) {
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
        myCacheForm = getComponentFactory().createForm(null, true);
        myStreamingCacheTimeout = getComponentFactory().createTextField("streamingConfigPanel.cache.streamingCacheTimeout", ValidatorFactory.createMinMaxValidator(0, 1440));
        myStreamingCacheMaxFiles = getComponentFactory().createTextField("streamingConfigPanel.cache.streamingCacheMaxFiles", ValidatorFactory.createMinMaxValidator(0, 10000));
        myCacheForm.addField("timeout", myStreamingCacheTimeout);
        myCacheForm.addField("limit", myStreamingCacheMaxFiles);
        addComponent(getComponentFactory().surroundWithPanel(myCacheForm, FORM_PANEL_MARGIN_INFO, getBundleString("streamingConfigPanel.caption.cache")));

        addMainButtons(0, 2, 0, 2);

    }

    private Form createTranscoder() {
        Form form = getComponentFactory().createForm(null, true);
        form.addField("name", getComponentFactory().createTextField("streamingConfigPanel.transcoder.name", new TranscoderNameValidator()));
        form.addField("pattern", getComponentFactory().createTextField("streamingConfigPanel.transcoder.pattern", new ValidRegExpValidator("streamingConfigPanel.error.invalidPattern")));
        form.addField("codecs", getComponentFactory().createTextField("streamingConfigPanel.transcoder.codecs"));
        form.addField("suffix", getComponentFactory().createTextField("streamingConfigPanel.transcoder.suffix"));
        form.addField("contentType", getComponentFactory().createTextField("streamingConfigPanel.transcoder.contentType"));
        form.addField("binary", getComponentFactory().createTextField("streamingConfigPanel.transcoder.binary", new FileValidator(getBundleString("streamingConfigPanel.error.invalidBinary"), null, FileValidator.PATTERN_ALL)));
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

    protected void initFromConfig(Application application) {
        Iterator<Component> formsIterator = myTranscoderAccordionPanel.getComponentIterator();
        for (int i = 0; i < MyTunesRss.CONFIG.getTranscoderConfigs().size(); i++) {
            TranscoderConfig config = MyTunesRss.CONFIG.getTranscoderConfigs().get(i);
            Panel panel = (Panel) formsIterator.next();
            VaadinUtils.getAncestor(panel, TabSheet.class).getTab(panel).setCaption(config.getName());
            Form form = (Form) panel.getComponentIterator().next();
            form.getField("name").setValue(config.getName());
            form.getField("pattern").setValue(config.getPattern());
            form.getField("codecs").setValue(config.getMp4Codecs());
            form.getField("suffix").setValue(config.getTargetSuffix());
            form.getField("contentType").setValue(config.getTargetContentType());
            form.getField("binary").setValue(config.getBinary());
            form.getField("options").setValue(config.getOptions());
        }
        myStreamingCacheTimeout.setValue(MyTunesRssUtils.getValueString(MyTunesRss.CONFIG.getStreamingCacheTimeout(), 0, 1440, "0"));
        myStreamingCacheMaxFiles.setValue(MyTunesRssUtils.getValueString(MyTunesRss.CONFIG.getStreamingCacheMaxFiles(), 0, 10000, "0"));
    }

    protected void writeToConfig() {
        List<TranscoderConfig> configs = MyTunesRss.CONFIG.getTranscoderConfigs();
        configs.clear();
        Iterator<Component> formIterator = myTranscoderAccordionPanel.getComponentIterator();
        while (formIterator.hasNext()) {
            Form form = (Form) formIterator.next();
            TranscoderConfig conf = new TranscoderConfig();
            conf.setName((String) form.getField("name").getValue());
            conf.setPattern((String) form.getField("pattern").getValue());
            conf.setMp4Codecs((String) form.getField("codecs").getValue());
            conf.setTargetSuffix((String) form.getField("suffix").getValue());
            conf.setTargetContentType((String) form.getField("contentType").getValue());
            conf.setBinary((String) form.getField("binary").getValue());
            conf.setOptions((String) form.getField("options").getValue());
            configs.add(conf);
        }
        MyTunesRss.CONFIG.setStreamingCacheTimeout((Integer) myStreamingCacheTimeout.getValue());
        MyTunesRss.CONFIG.setStreamingCacheMaxFiles((Integer) myStreamingCacheMaxFiles.getValue());
    }

    @Override
    protected boolean beforeSave() {
        boolean valid = VaadinUtils.isValid(myCacheForm);
        Iterator<Component> formIterator = myTranscoderAccordionPanel.getComponentIterator();
        while (formIterator.hasNext()) {
            valid &= VaadinUtils.isValid((Validatable) formIterator.next());
        }
        if (!valid) {
            getApplication().showError("error.formInvalid");
        }
        return valid;
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
        } else if (VaadinUtils.isChild(myTranscoderAccordionPanel, clickEvent.getButton())) {
            final Form buttonForm = (Form) clickEvent.getButton().getData();
            if (buttonForm.getField("selectBinary") == clickEvent.getButton()) {
                new ServerSideFileChooserWindow(50, Sizeable.UNITS_EM, null, getBundleString("streamingConfigPanel.caption.selectBinary"), new File((String) buttonForm.getField("binary").getValue()), null, ServerSideFileChooser.PATTERN_ALL, false) {
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
