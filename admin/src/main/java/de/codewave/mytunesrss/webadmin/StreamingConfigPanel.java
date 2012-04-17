/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.TranscoderConfig;
import de.codewave.mytunesrss.httplivestreaming.HttpLiveStreamingCacheItem;
import de.codewave.mytunesrss.httplivestreaming.HttpLiveStreamingPlaylist;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;
import de.codewave.vaadin.component.ServerSideFileChooser;
import de.codewave.vaadin.component.ServerSideFileChooserWindow;
import de.codewave.vaadin.validation.ExecutableFileValidator;
import de.codewave.vaadin.validation.FileValidator;
import de.codewave.vaadin.validation.ValidRegExpValidator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.Collator;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class StreamingConfigPanel extends MyTunesRssConfigPanel {

    private static final Logger LOG = LoggerFactory.getLogger(StreamingConfigPanel.class);

    private static final String TRANSCODER_NAME_REGEXP = "[a-zA-Z0-9 ]{1,40}";

    private Panel myTranscoderPanel;
    private Form myCacheForm;
    private Button myAddTranscoder;
    private SmartTextField myStreamingCacheTimeout;
    private SmartTextField myStreamingCacheMaxFiles;
    private AtomicLong myTranscoderNumberGenerator = new AtomicLong(1);
    Panel myAddTranscoderButtons;
    private SmartTextField myVlcBinary;
    private Button myVlcBinarySelect;
    private Form myVlcForm;

    public void attach() {
        super.attach();
        init(getBundleString("streamingConfigPanel.caption"), getComponentFactory().createGridLayout(1, 5, true, true));
        myVlcBinary = getComponentFactory().createTextField("streamingConfigPanel.vlcBinary", new ExecutableFileValidator(getBundleString("streamingConfigPanel.vlcBinary.invalidBinary"), null, FileValidator.PATTERN_ALL));
        myVlcBinarySelect = getComponentFactory().createButton("streamingConfigPanel.vlcBinary.select", this);
        myVlcForm = getComponentFactory().createForm(null, true);
        myVlcForm.addField(myVlcBinary, myVlcBinary);
        myVlcForm.addField(myVlcBinarySelect, myVlcBinarySelect);
        Panel vlcPanel = getComponentFactory().surroundWithPanel(myVlcForm, FORM_PANEL_MARGIN_INFO, getBundleString("streamingConfigPanel.caption.vlc"));
        addComponent(vlcPanel);
        myTranscoderPanel = new Panel(getBundleString("streamingConfigPanel.caption.transcoder"));
        ((Layout) myTranscoderPanel.getContent()).setMargin(true);
        ((Layout.SpacingHandler) myTranscoderPanel.getContent()).setSpacing(true);
        myAddTranscoderButtons = new Panel();
        myAddTranscoderButtons.addStyleName("light");
        myAddTranscoderButtons.setContent(getApplication().getComponentFactory().createHorizontalLayout(false, true));
        myAddTranscoder = getComponentFactory().createButton("streamingConfigPanel.transcoder.add", this);
        myAddTranscoderButtons.addComponent(myAddTranscoder);
        for (int i = 0; i < MyTunesRss.CONFIG.getTranscoderConfigs().size(); i++) {
            createTranscoder();
        }
        myTranscoderPanel.addComponent(myAddTranscoderButtons);
        addComponent(myTranscoderPanel);
        myCacheForm = getComponentFactory().createForm(null, true);
        myStreamingCacheTimeout = getComponentFactory().createTextField("streamingConfigPanel.cache.streamingCacheTimeout", getApplication().getValidatorFactory().createMinMaxValidator(0, 1440));
        myStreamingCacheMaxFiles = getComponentFactory().createTextField("streamingConfigPanel.cache.streamingCacheMaxFiles", getApplication().getValidatorFactory().createMinMaxValidator(0, 10000));
        myCacheForm.addField("timeout", myStreamingCacheTimeout);
        myCacheForm.addField("limit", myStreamingCacheMaxFiles);
        addComponent(getComponentFactory().surroundWithPanel(myCacheForm, FORM_PANEL_MARGIN_INFO, getBundleString("streamingConfigPanel.caption.cache")));

        addDefaultComponents(0, 4, 0, 4, false);

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
        SmartTextField muxTextField = getComponentFactory().createTextField("streamingConfigPanel.transcoder.mux");
        form.addField("mux", muxTextField);
        SmartTextField optionsTextField = getComponentFactory().createTextField("streamingConfigPanel.transcoder.options");
        optionsTextField.setRequired(true);
        form.addField("options", optionsTextField);
        Button delete = getComponentFactory().createButton("streamingConfigPanel.transcoder.delete", this);
        delete.setData(form);
        Panel panel = getComponentFactory().surroundWithPanel(form, new Layout.MarginInfo(false, true, true, true), null);
        myTranscoderPanel.removeComponent(myAddTranscoderButtons);
        myTranscoderPanel.addComponent(panel);
        myTranscoderPanel.addComponent(myAddTranscoderButtons);
        panel.addComponent(delete);
        return form;
    }

    protected void initFromConfig() {
        Iterator<Component> componentIterator = myTranscoderPanel.getComponentIterator();
        List<TranscoderConfig> transcoderConfigs = new ArrayList<TranscoderConfig>(MyTunesRss.CONFIG.getTranscoderConfigs());
        Collections.sort(transcoderConfigs, new Comparator<TranscoderConfig>() {
            Collator myCollator = Collator.getInstance(getLocale());

            public int compare(TranscoderConfig o1, TranscoderConfig o2) {
                return myCollator.compare(o1.getName(), o2.getName());
            }
        });
        for (int i = 0; i < transcoderConfigs.size(); i++) {
            TranscoderConfig config = transcoderConfigs.get(i);
            Panel panel = (Panel) componentIterator.next();
            Form form = getTranscoderForm(panel);
            form.getField("name").setValue(config.getName());
            form.getField("pattern").setValue(config.getPattern());
            form.getField("codecs").setValue(config.getMp4Codecs());
            form.getField("suffix").setValue(config.getTargetSuffix());
            form.getField("contentType").setValue(config.getTargetContentType());
            form.getField("mux").setValue(config.getTargetMux());
            form.getField("options").setValue(config.getOptions());
        }
        myStreamingCacheTimeout.setValue(MyTunesRss.CONFIG.getStreamingCacheTimeout(), 0, 1440, "0");
        myStreamingCacheMaxFiles.setValue(MyTunesRss.CONFIG.getStreamingCacheMaxFiles(), 0, 10000, "0");
        myVlcBinary.setValue(MyTunesRss.CONFIG.getVlcExecutable());
    }

    protected void writeToConfig() {
        List<TranscoderConfig> configs = MyTunesRss.CONFIG.getTranscoderConfigs();
        Set<String> obsoleteTranscoderNames = new HashSet<String>();
        for (TranscoderConfig config : configs) {
            obsoleteTranscoderNames.add(config.getName());
        }
        configs.clear();
        Iterator<Component> componentIterator = myTranscoderPanel.getComponentIterator();
        while (componentIterator.hasNext()) {
            Form form = getTranscoderForm((Panel) componentIterator.next());
            if (form != null) {
                TranscoderConfig conf = new TranscoderConfig();
                String name = ((SmartTextField) form.getField("name")).getStringValue(null);
                conf.setName(name);
                obsoleteTranscoderNames.remove(name);
                conf.setPattern(((SmartTextField) form.getField("pattern")).getStringValue(null));
                conf.setMp4Codecs(((SmartTextField) form.getField("codecs")).getStringValue(null));
                conf.setTargetSuffix(((SmartTextField) form.getField("suffix")).getStringValue(null));
                conf.setTargetContentType(((SmartTextField) form.getField("contentType")).getStringValue(null));
                conf.setTargetMux(((SmartTextField) form.getField("mux")).getStringValue(null));
                conf.setOptions(((SmartTextField) form.getField("options")).getStringValue(null));
                configs.add(conf);
            }
        }
        truncateHttpLiveStreamingCache(obsoleteTranscoderNames);
        truncateTranscodingCache(obsoleteTranscoderNames);
        MyTunesRss.CONFIG.setStreamingCacheTimeout(myStreamingCacheTimeout.getIntegerValue(0));
        MyTunesRss.CONFIG.setStreamingCacheMaxFiles(myStreamingCacheMaxFiles.getIntegerValue(0));
        String vlcBinary = myVlcBinary.getStringValue(null);
        MyTunesRss.CONFIG.setVlcExecutable(vlcBinary != null ? new File(vlcBinary) : null);
        MyTunesRss.CONFIG.save();
    }

    /**
     * Remove cached http live stream files for obsolete transcoder names.
     *
     * @param obsoleteTranscoderNames Set with the obsolete transcoder names.
     */
    private void truncateHttpLiveStreamingCache(Set<String> obsoleteTranscoderNames) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Truncating http live streaming cache.");
        }
        for (String key : MyTunesRss.HTTP_LIVE_STREAMING_CACHE.keySet()) {
            HttpLiveStreamingCacheItem cacheItem = MyTunesRss.HTTP_LIVE_STREAMING_CACHE.get(key);
            for (String transcoderName : obsoleteTranscoderNames) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Removing playlist with key \"" + transcoderName + "\" from cache item with key \"" + key + "\".");
                }
                HttpLiveStreamingPlaylist playlist = cacheItem.getPlaylist(transcoderName);
                if (playlist != null) {
                    playlist.destroy();
                }
                cacheItem.removePlaylist(transcoderName);
            }
        }
    }

    /**
     * Remove cached transcoded files for obsolete transcoder names.
     *
     * @param obsoleteTranscoderNames Set with the obsolete transcoder names.
     */
    private void truncateTranscodingCache(Set<String> obsoleteTranscoderNames) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Truncating streaming cache.");
        }
        for (String transcoderName : obsoleteTranscoderNames) {
            for (String key : MyTunesRss.STREAMING_CACHE.keySet()) {
                if (key.endsWith("_" + transcoderName)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Removing cache item with key \"" + key + "\".");
                    }
                    MyTunesRss.STREAMING_CACHE.remove(key);
                }
            }
        }
    }

    @Override
    protected boolean beforeSave() {
        boolean valid = VaadinUtils.isValid(myCacheForm, myVlcForm);
        Iterator<Component> formIterator = myTranscoderPanel.getComponentIterator();
        Set<String> transcoderNames = new HashSet<String>();
        boolean duplicateName = false;
        while (formIterator.hasNext()) {
            Panel panel = (Panel) formIterator.next();
            Form form = getTranscoderForm(panel);
            if (form != null) {
                valid &= VaadinUtils.isValid(form);
                String name = ((SmartTextField) form.getField("name")).getStringValue(null);
                if (transcoderNames.contains(name)) {
                    duplicateName = true;
                }
                transcoderNames.add(name);
            }
        }
        if (!valid) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
        } else if (duplicateName) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("streamingConfigPanel.error.duplicateName");
            valid = false;
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
        return null;
    }

    @Override
    protected boolean beforeReset() {
        myTranscoderPanel.removeAllComponents();
        for (int i = 0; i < MyTunesRss.CONFIG.getTranscoderConfigs().size(); i++) {
            createTranscoder();
        }
        myTranscoderNumberGenerator.set(1);
        return true;
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        if (clickEvent.getButton() == myVlcBinarySelect) {
            File dir = StringUtils.isNotBlank((String) myVlcBinary.getValue()) ? new File((String) myVlcBinary.getValue()) : null;
            new ServerSideFileChooserWindow(50, Sizeable.UNITS_EM, null, getBundleString("streamingConfigPanel.caption.selectVlcBinary"), dir, null, ServerSideFileChooser.PATTERN_ALL, false, getApplication().getServerSideFileChooserLabels()) {
                @Override
                protected void onFileSelected(File file) {
                    myVlcBinary.setValue(file.getAbsolutePath());
                    getWindow().getParent().removeWindow(this);
                }
            }.show(getWindow());
        } else if (clickEvent.getButton() == myAddTranscoder) {
            Form form = createTranscoder();
            String name = getBundleString("streamingConfigPanel.transcoder.defaultName", myTranscoderNumberGenerator.getAndIncrement());
            form.getField("name").setValue(name);
        } else if (VaadinUtils.isChild(myTranscoderPanel, clickEvent.getButton())) {
            final Form buttonForm = (Form) clickEvent.getButton().getData();
            final Button yes = new Button(getBundleString("button.yes"));
            Button no = new Button(getBundleString("button.no"));
            new OptionWindow(30, Sizeable.UNITS_EM, null, getBundleString("streamingConfigDialog.deleteConfirmation.caption"), getBundleString("streamingConfigDialog.deleteConfirmation.message", buttonForm.getField("name").getValue()), yes, no) {
                public void clicked(Button button) {
                    if (button == yes) {
                        myTranscoderPanel.removeComponent(VaadinUtils.getAncestor(buttonForm, Panel.class));
                    }
                }
            }.show(getWindow());
        } else {
            super.buttonClick(clickEvent);
        }
    }
}
