/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.config.transcoder.TranscoderConfig;
import de.codewave.mytunesrss.vlc.VlcPlayerException;
import de.codewave.mytunesrss.vlc.VlcVersion;
import de.codewave.mytunesrss.webadmin.transcoder.TranscoderPanel;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.ServerSideFileChooser;
import de.codewave.vaadin.component.ServerSideFileChooserWindow;
import de.codewave.vaadin.validation.MinMaxIntegerValidator;
import de.codewave.vaadin.validation.VlcExecutableFileValidator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.Collator;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class StreamingConfigPanel extends MyTunesRssConfigPanel {

    private static final Logger LOG = LoggerFactory.getLogger(StreamingConfigPanel.class);

    public class VlcVersionRepresentation {
        private VlcVersion myVlcVersion;

        public VlcVersionRepresentation(VlcVersion vlcVersion) {
            myVlcVersion = vlcVersion;
        }

        public VlcVersion getVlcVersion() {
            return myVlcVersion;
        }

        @Override
        public String toString() {
            return getBundleString("streamingConfigPanel.vlcVersion." + myVlcVersion.name());
        }
    }

    private Panel myTranscoderPanel;
    private Accordion myTranscoderAccordion;
    private Form myCacheForm;
    private Button myAddTranscoder;
    private Button myRestoreDefaultTranscoders;
    private SmartTextField myTranscodingCacheMaxGiB;
    private SmartTextField myHttpLiveStreamCacheMaxGiB;
    private AtomicLong myTranscoderNumberGenerator = new AtomicLong(1);
    private Panel myAddTranscoderButtons;
    private CheckBox myVlcEnabled;
    private SmartTextField myVlcBinary;
    private Select myVlcVersion;
    private Button myVlcBinarySelect;
    private SmartTextField myVlcSocketTimeout;
    private SmartTextField myVlcRaopVolume;
    private Form myVlcForm;
    private Button myVlcHomepageButton;
    private Button myRestartVlcPlayer;
    private Button myClearAllCachesButton;
    private Map<VlcVersion, VlcVersionRepresentation> myVlcVersionMap = new LinkedHashMap<>();

    public void attach() {
        super.attach();
        init(getBundleString("streamingConfigPanel.caption"), getComponentFactory().createGridLayout(1, 5, true, true));
        myVlcEnabled = getComponentFactory().createCheckBox("streamingConfigPanel.vlcEnabled");
        myVlcBinary = getComponentFactory().createTextField("streamingConfigPanel.vlcBinary", new VlcExecutableFileValidator(getBundleString("streamingConfigPanel.vlcBinary.invalidBinary")));
        myVlcBinary.setImmediate(false);
        myVlcVersionMap.put(VlcVersion.V20, new VlcVersionRepresentation(VlcVersion.V20));
        myVlcVersionMap.put(VlcVersion.V21, new VlcVersionRepresentation(VlcVersion.V21));
        myVlcVersion = getComponentFactory().createSelect("streamingConfigPanel.vlcVersion", myVlcVersionMap.values());
        myVlcBinarySelect = getComponentFactory().createButton("streamingConfigPanel.vlcBinary.select", this);
        myVlcSocketTimeout = getComponentFactory().createTextField("streamingConfigPanel.vlcTimeout", new MinMaxIntegerValidator(getBundleString("streamingConfigPanel.vlcTimeout.invalidTimeout", 1, 1000), 1, 1000));
        myVlcRaopVolume = getComponentFactory().createTextField("streamingConfigPanel.vlcRaopVolume", new MinMaxIntegerValidator(getBundleString("streamingConfigPanel.vlcRaopVolume.invalidVolume", 1, 100), 1, 100));
        myVlcHomepageButton = getComponentFactory().createButton("streamingConfigPanel.vlcHomepage", this);
        myRestartVlcPlayer = getComponentFactory().createButton("streamingConfigPanel.restartVlc", this);
        myVlcForm = getComponentFactory().createForm(null, true);
        myVlcForm.addField(myVlcEnabled, myVlcEnabled);
        myVlcForm.addField(myVlcBinary, myVlcBinary);
        myVlcForm.addField(myVlcBinarySelect, myVlcBinarySelect);
        myVlcForm.addField(myVlcVersion, myVlcVersion);
        myVlcForm.addField(myVlcSocketTimeout, myVlcSocketTimeout);
        myVlcForm.addField(myVlcRaopVolume, myVlcRaopVolume);
        myVlcForm.addField(myRestartVlcPlayer, myRestartVlcPlayer);
        myVlcForm.addField(myVlcHomepageButton, myVlcHomepageButton);
        Panel vlcPanel = getComponentFactory().surroundWithPanel(myVlcForm, FORM_PANEL_MARGIN_INFO, getBundleString("streamingConfigPanel.caption.vlc"));
        addComponent(vlcPanel);
        myTranscoderPanel = new Panel(getBundleString("streamingConfigPanel.caption.transcoder"));
        myTranscoderPanel.setContent(getComponentFactory().createVerticalLayout(true, true));
        myAddTranscoderButtons = new Panel();
        myAddTranscoderButtons.addStyleName("light");
        myAddTranscoderButtons.setContent(getApplication().getComponentFactory().createHorizontalLayout(false, true));
        myAddTranscoder = getComponentFactory().createButton("streamingConfigPanel.transcoder.add", this);
        myAddTranscoderButtons.addComponent(myAddTranscoder);
        myRestoreDefaultTranscoders = getComponentFactory().createButton("streamingConfigPanel.transcoder.restoreDefault", this);
        myAddTranscoderButtons.addComponent(myRestoreDefaultTranscoders);
        myTranscoderAccordion = new Accordion();
        myTranscoderPanel.addComponent(myTranscoderAccordion);
        myTranscoderPanel.addComponent(myAddTranscoderButtons);
        addComponent(myTranscoderPanel);
        myCacheForm = getComponentFactory().createForm(null, true);
        myTranscodingCacheMaxGiB = getComponentFactory().createTextField("streamingConfigPanel.cache.transcodingCacheMaxGiB", getApplication().getValidatorFactory().createMinMaxValidator(1, 1024));
        myCacheForm.addField("limitTranscoding", myTranscodingCacheMaxGiB);
        myHttpLiveStreamCacheMaxGiB = getComponentFactory().createTextField("streamingConfigPanel.cache.httpLiveStreamCacheMaxGiB", getApplication().getValidatorFactory().createMinMaxValidator(1, 1024));
        myCacheForm.addField("limitHttpLiveStream", myHttpLiveStreamCacheMaxGiB);
        myClearAllCachesButton = getComponentFactory().createButton("streamingConfigPanel.clearAllCaches", this);
        myCacheForm.addField(myClearAllCachesButton, myClearAllCachesButton);
        addComponent(getComponentFactory().surroundWithPanel(myCacheForm, FORM_PANEL_MARGIN_INFO, getBundleString("streamingConfigPanel.caption.cache")));

        addDefaultComponents(0, 4, 0, 4, false);

        initFromConfig();
    }

    private TranscoderPanel createTranscoderPanel() {
        TranscoderPanel transcoderPanel = new TranscoderPanel(this, getApplication(), getComponentFactory());
        myTranscoderAccordion.addTab(transcoderPanel, "new one", null);
        return transcoderPanel;
    }

    protected void initFromConfig() {
        List<TranscoderConfig> transcoderConfigs = new ArrayList<>(MyTunesRss.CONFIG.getTranscoderConfigs());
        if (!transcoderConfigs.isEmpty()) {
            Collections.sort(transcoderConfigs, new Comparator<TranscoderConfig>() {
                Collator myCollator = Collator.getInstance(getLocale());

                public int compare(TranscoderConfig o1, TranscoderConfig o2) {
                    return myCollator.compare(o1.getName(), o2.getName());
                }
            });
            for (TranscoderConfig config : transcoderConfigs) {
                TranscoderPanel transcoderPanel = createTranscoderPanel();
                myTranscoderAccordion.addTab(transcoderPanel);
                transcoderPanel.initFromConfig(config);
            }
            myTranscoderAccordion.setVisible(true);
        } else {
            myTranscoderAccordion.setVisible(false);
        }
        myTranscodingCacheMaxGiB.setValue(MyTunesRss.CONFIG.getTranscodingCacheMaxGiB(), 1, 1024, "1");
        myHttpLiveStreamCacheMaxGiB.setValue(MyTunesRss.CONFIG.getHttpLiveStreamCacheMaxGiB(), 1, 1024, "5");
        myVlcEnabled.setValue(MyTunesRss.CONFIG.isVlcEnabled());
        myVlcBinary.setValue(MyTunesRss.CONFIG.getVlcExecutable() != null ? MyTunesRss.CONFIG.getVlcExecutable().getAbsolutePath() : "");
        myVlcVersion.setValue(myVlcVersionMap.get(MyTunesRss.CONFIG.getVlcVersion()));
        myVlcSocketTimeout.setValue(MyTunesRss.CONFIG.getVlcSocketTimeout());
        myVlcRaopVolume.setValue(MyTunesRss.CONFIG.getVlcRaopVolume());
    }

    protected void writeToConfig() {
        Collection<TranscoderConfig> configs = new ArrayList<>();
        Set<String> obsoleteTranscoderNames = new HashSet<>();
        for (TranscoderConfig config : MyTunesRss.CONFIG.getTranscoderConfigs()) {
            obsoleteTranscoderNames.add(config.getName());
        }
        Iterator<Component> iterator = myTranscoderAccordion.getComponentIterator();
        while (iterator.hasNext()) {
            Component component = iterator.next();
            if (component instanceof TranscoderPanel) {
                TranscoderConfig conf = ((TranscoderPanel)component).getConfig();
                obsoleteTranscoderNames.remove(conf.getName());
                configs.add(conf);
            }
        }
        truncateTranscodingCache(obsoleteTranscoderNames);
        for (TranscoderConfig config : configs) {
            LOG.debug("Checking for existing transcoder config \"" + config.getName() + "\".");
            TranscoderConfig existingConfig = findExistingTranscoderConfig(config.getName());
            if (!isSameConfig(config, existingConfig)) {
                LOG.debug("Transcoder config \"" + config.getName() + "\" has changed, truncating cache.");
                MyTunesRss.TRANSCODER_CACHE.deleteByPrefix(config.getCacheFilePrefix()); // transcoder config has changed
            }
        }
        MyTunesRss.CONFIG.setTranscoderConfigs(configs);
        int maxGiB = myTranscodingCacheMaxGiB.getIntegerValue(1);
        MyTunesRss.CONFIG.setTranscodingCacheMaxGiB(maxGiB);
        MyTunesRss.TRANSCODER_CACHE.setMaxSizeBytes((long)maxGiB * 1024L * 1024L * 1024L);
        maxGiB = myHttpLiveStreamCacheMaxGiB.getIntegerValue(5);
        MyTunesRss.CONFIG.setHttpLiveStreamCacheMaxGiB(maxGiB);
        MyTunesRss.HTTP_LIVE_STREAMING_CACHE.setMaxSizeBytes((long)maxGiB * 1024L * 1024L * 1024L);
        String vlcBinary = myVlcBinary.getStringValue(null);
        File vlcExecutable = vlcBinary != null ? new File(vlcBinary) : null;
        if (vlcExecutable != null && vlcExecutable.isDirectory() && SystemUtils.IS_OS_MAC_OSX && "vlc.app".equalsIgnoreCase(vlcExecutable.getName())) {
            vlcExecutable = new File(vlcExecutable, "Contents/MacOS/VLC");
        }
        boolean changes = (vlcExecutable == null && MyTunesRss.CONFIG.getVlcExecutable() != null) || (vlcExecutable != null && !vlcExecutable.equals(MyTunesRss.CONFIG.getVlcExecutable()));
        changes |= (myVlcEnabled.booleanValue() != MyTunesRss.CONFIG.isVlcEnabled());
        changes |= ((VlcVersionRepresentation) myVlcVersion.getValue()).getVlcVersion() != MyTunesRss.CONFIG.getVlcVersion();
        changes |= myVlcSocketTimeout.getIntegerValue(5) != MyTunesRss.CONFIG.getVlcSocketTimeout();
        changes |= myVlcRaopVolume.getIntegerValue(75) != MyTunesRss.CONFIG.getVlcRaopVolume();
        MyTunesRss.CONFIG.setVlcExecutable(vlcExecutable);
        MyTunesRss.CONFIG.setVlcEnabled(myVlcEnabled.booleanValue());
        MyTunesRss.CONFIG.setVlcVersion(((VlcVersionRepresentation) myVlcVersion.getValue()).getVlcVersion());
        MyTunesRss.CONFIG.setVlcSocketTimeout(myVlcSocketTimeout.getIntegerValue(5));
        MyTunesRss.CONFIG.setVlcRaopVolume(myVlcRaopVolume.getIntegerValue(75));
        if (changes) {
            MyTunesRss.EXECUTOR_SERVICE.execute(new Runnable() {
                public void run() {
                    try {
                        MyTunesRss.VLC_PLAYER.destroy();
                    } catch (VlcPlayerException e) {
                        LOG.warn("Could not destroy VLC player.", e);
                    }
                    try {
                        MyTunesRss.VLC_PLAYER.init();
                    } catch (VlcPlayerException e) {
                        LOG.warn("Could not initialize VLC player.", e);
                    }
                }
            });
        }
        MyTunesRss.CONFIG.save();
    }

    private boolean isSameConfig(TranscoderConfig c1, TranscoderConfig c2) {
        return c1 != null && c2 != null && StringUtils.equalsIgnoreCase(c1.getOptions(), c2.getOptions()) && StringUtils.equalsIgnoreCase(c1.getTargetMux(), c2.getTargetMux());
    }

    private TranscoderConfig findExistingTranscoderConfig(String name) {
        for (TranscoderConfig config : MyTunesRss.CONFIG.getTranscoderConfigs()) {
            if (config.getName().equals(name)) {
                return config;
            }
        }
        return null;
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
        for (String name : obsoleteTranscoderNames) {
            TranscoderConfig dummy = new TranscoderConfig();
            dummy.setName(name);
            MyTunesRss.TRANSCODER_CACHE.deleteByPrefix(dummy.getCacheFilePrefix());
        }
    }

    @Override
    protected boolean beforeSave() {
        boolean valid = VaadinUtils.isValid(myCacheForm, myVlcForm);
        Iterator<Component> iterator = myTranscoderAccordion.getComponentIterator();
        Set<String> transcoderNames = new HashSet<>();
        boolean duplicateName = false;
        while (iterator.hasNext()) {
            Component component = iterator.next();
            if (component instanceof TranscoderPanel) {
                TranscoderPanel transcoderPanel = (TranscoderPanel) component;
                valid &= transcoderPanel.isValid();
                String name = transcoderPanel.getConfig().getName();
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

    @Override
    protected boolean beforeReset() {
        myTranscoderNumberGenerator.set(1);
        return true;
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        if (clickEvent.getButton() == myVlcHomepageButton) {
            getWindow().open(new ExternalResource("http://www.videolan.org"));
        } else if (clickEvent.getButton() == myRestartVlcPlayer) {
            if (MyTunesRss.CONFIG.isVlcEnabled() && MyTunesRss.CONFIG.getVlcExecutable() != null && MyTunesRssUtils.canExecute(MyTunesRss.CONFIG.getVlcExecutable())) {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showInfo("streamingConfigPanel.error.restartingVlc");
                MyTunesRss.EXECUTOR_SERVICE.execute(new Runnable() {
                    public void run() {
                        try {
                            MyTunesRss.VLC_PLAYER.destroy();
                            MyTunesRss.VLC_PLAYER.init();
                        } catch (VlcPlayerException e) {
                            LOG.warn("Could not restart VLC player.", e);
                            ((MainWindow) VaadinUtils.getApplicationWindow(StreamingConfigPanel.this)).showError("streamingConfigPanel.error.vlcRestartFailed");
                        }
                    }
                });
            } else {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("streamingConfigPanel.error.cannotRestartVlc");
            }
        } else if (clickEvent.getButton() == myVlcBinarySelect) {
            File dir = StringUtils.isNotBlank((String) myVlcBinary.getValue()) ? new File((String) myVlcBinary.getValue()) : null;
            new ServerSideFileChooserWindow(50, Sizeable.UNITS_EM, null, getBundleString("streamingConfigPanel.caption.selectVlcBinary"), dir, null, ServerSideFileChooser.PATTERN_ALL, false, getApplication().getServerSideFileChooserLabels()) {
                @Override
                protected void onFileSelected(File file) {
                    myVlcBinary.setValue(file.getAbsolutePath());
                    getWindow().getParent().removeWindow(this);
                }
            }.show(getWindow());
        } else if (clickEvent.getButton() == myAddTranscoder) {
            TranscoderPanel panel = createTranscoderPanel();
            TranscoderConfig config = new TranscoderConfig();
            config.setName(getBundleString("transcoderPanel.defaultName", myTranscoderNumberGenerator.getAndIncrement()));
            panel.initFromConfig(config);
            myTranscoderAccordion.setVisible(true);
            myTranscoderAccordion.setSelectedTab(panel);
        } else if (clickEvent.getButton() == myRestoreDefaultTranscoders) {
            Collection<TranscoderConfig> defaultTranscoders = TranscoderConfig.getDefaultTranscoders();
            for (Iterator<TranscoderConfig> tcIter = defaultTranscoders.iterator(); tcIter.hasNext(); ) {
                TranscoderConfig config = tcIter.next();
                Iterator<Component> formIterator = myTranscoderAccordion.getComponentIterator();
                while (formIterator.hasNext()) {
                    Component component = formIterator.next();
                    if (component instanceof TranscoderPanel) {
                        TranscoderPanel transcoderPanel = (TranscoderPanel) component;
                        if (config.getName().equals(transcoderPanel.getConfig().getName())) {
                            transcoderPanel.initFromConfig(config); // replace existing transcoder in GUI
                            tcIter.remove();
                        }
                    }
                }
            }
            // now add remaining default transcoders as new ones to the GUI
            for (TranscoderConfig config : defaultTranscoders) {
                TranscoderPanel panel = createTranscoderPanel();
                panel.initFromConfig(config);
            }
            myTranscoderAccordion.setVisible(true);
        } else if (clickEvent.getButton() == myClearAllCachesButton) {
            boolean success = MyTunesRss.TRANSCODER_CACHE.clear() & MyTunesRss.HTTP_LIVE_STREAMING_CACHE.clear() & MyTunesRss.TEMP_CACHE.clear();
            if (success) {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showInfo("streamingConfigPanel.clearAllCaches.done");
            } else {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showWarning("streamingConfigPanel.clearAllCaches.warn");
            }
        } else {
            super.buttonClick(clickEvent);
        }
    }

    public void removeTranscoder(TranscoderPanel transcoderPanel) {
        myTranscoderAccordion.removeTab(myTranscoderAccordion.getTab(transcoderPanel));
        myTranscoderAccordion.setVisible(myTranscoderAccordion.getComponentCount() > 0);
    }
}
