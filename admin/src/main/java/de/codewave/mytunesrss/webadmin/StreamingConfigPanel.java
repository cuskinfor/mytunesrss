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
import de.codewave.mytunesrss.httplivestreaming.HttpLiveStreamingCacheItem;
import de.codewave.mytunesrss.httplivestreaming.HttpLiveStreamingPlaylist;
import de.codewave.mytunesrss.vlc.VlcPlayerException;
import de.codewave.mytunesrss.webadmin.transcoder.TranscoderPanel;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.ServerSideFileChooser;
import de.codewave.vaadin.component.ServerSideFileChooserWindow;
import de.codewave.vaadin.validation.MinMaxIntegerValidator;
import de.codewave.vaadin.validation.VlcExecutableFileValidator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.Collator;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class StreamingConfigPanel extends MyTunesRssConfigPanel {

    private static final Logger LOG = LoggerFactory.getLogger(StreamingConfigPanel.class);

    private Panel myTranscoderPanel;
    private Form myCacheForm;
    private Button myAddTranscoder;
    private SmartTextField myStreamingCacheTimeout;
    private SmartTextField myStreamingCacheMaxFiles;
    private AtomicLong myTranscoderNumberGenerator = new AtomicLong(1);
    private Panel myAddTranscoderButtons;
    private CheckBox myVlcEnabled;
    private SmartTextField myVlcBinary;
    private Button myVlcBinarySelect;
    private SmartTextField myVlcSocketTimeout;
    private SmartTextField myVlcRaopVolume;
    private Form myVlcForm;
    private Button myVlcHomepageButton;
    private Button myRestartVlcPlayer;

    public void attach() {
        super.attach();
        init(getBundleString("streamingConfigPanel.caption"), getComponentFactory().createGridLayout(1, 5, true, true));
        myVlcEnabled = getComponentFactory().createCheckBox("streamingConfigPanel.vlcEnabled");
        myVlcBinary = getComponentFactory().createTextField("streamingConfigPanel.vlcBinary", new VlcExecutableFileValidator(getBundleString("streamingConfigPanel.vlcBinary.invalidBinary")));
        myVlcBinary.setImmediate(false);
        myVlcBinarySelect = getComponentFactory().createButton("streamingConfigPanel.vlcBinary.select", this);
        myVlcSocketTimeout = getComponentFactory().createTextField("streamingConfigPanel.vlcTimeout", new MinMaxIntegerValidator(getBundleString("streamingConfigPanel.vlcTimeout.invalidTimeout", 1, 1000), 1, 1000));
        myVlcRaopVolume = getComponentFactory().createTextField("streamingConfigPanel.vlcRaopVolume", new MinMaxIntegerValidator(getBundleString("streamingConfigPanel.vlcRaopVolume.invalidVolume", 1, 100), 1, 100));
        myVlcHomepageButton = getComponentFactory().createButton("streamingConfigPanel.vlcHomepage", this);
        myRestartVlcPlayer = getComponentFactory().createButton("streamingConfigPanel.restartVlc", this);
        myVlcForm = getComponentFactory().createForm(null, true);
        myVlcForm.addField(myVlcEnabled, myVlcEnabled);
        myVlcForm.addField(myVlcBinary, myVlcBinary);
        myVlcForm.addField(myVlcBinarySelect, myVlcBinarySelect);
        myVlcForm.addField(myVlcSocketTimeout, myVlcSocketTimeout);
        myVlcForm.addField(myVlcRaopVolume, myVlcRaopVolume);
        myVlcForm.addField(myRestartVlcPlayer, myRestartVlcPlayer);
        myVlcForm.addField(myVlcHomepageButton, myVlcHomepageButton);
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
            createTranscoderPanel();
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

    private TranscoderPanel createTranscoderPanel() {
        myTranscoderPanel.removeComponent(myAddTranscoderButtons);
        TranscoderPanel transcoderPanel = new TranscoderPanel(getApplication(), getComponentFactory());
        myTranscoderPanel.addComponent(transcoderPanel);
        myTranscoderPanel.addComponent(myAddTranscoderButtons);
        return transcoderPanel;
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
            Component component = componentIterator.next();
            if (component instanceof TranscoderPanel) {
                ((TranscoderPanel)component).initFromConfig(config);
            }
        }
        myStreamingCacheTimeout.setValue(MyTunesRss.CONFIG.getStreamingCacheTimeout(), 0, 1440, "0");
        myStreamingCacheMaxFiles.setValue(MyTunesRss.CONFIG.getStreamingCacheMaxFiles(), 0, 10000, "0");
        myVlcEnabled.setValue(MyTunesRss.CONFIG.isVlcEnabled());
        myVlcBinary.setValue(MyTunesRss.CONFIG.getVlcExecutable() != null ? MyTunesRss.CONFIG.getVlcExecutable().getAbsolutePath() : "");
        myVlcSocketTimeout.setValue(MyTunesRss.CONFIG.getVlcSocketTimeout());
        myVlcRaopVolume.setValue(MyTunesRss.CONFIG.getVlcRaopVolume());
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
            Component component = componentIterator.next();
            if (component instanceof TranscoderPanel) {
                TranscoderConfig conf = ((TranscoderPanel)component).getConfig();
                obsoleteTranscoderNames.remove(conf.getName());
                configs.add(conf);
            }
        }
        truncateHttpLiveStreamingCache(obsoleteTranscoderNames);
        truncateTranscodingCache(obsoleteTranscoderNames);
        MyTunesRss.CONFIG.setStreamingCacheTimeout(myStreamingCacheTimeout.getIntegerValue(0));
        MyTunesRss.CONFIG.setStreamingCacheMaxFiles(myStreamingCacheMaxFiles.getIntegerValue(0));
        String vlcBinary = myVlcBinary.getStringValue(null);
        File vlcExecutable = vlcBinary != null ? new File(vlcBinary) : null;
        if (vlcExecutable != null && vlcExecutable.isDirectory() && SystemUtils.IS_OS_MAC_OSX && "vlc.app".equalsIgnoreCase(vlcExecutable.getName())) {
            vlcExecutable = new File(vlcExecutable, "Contents/MacOS/VLC");
        }
        boolean changes = (vlcExecutable == null && MyTunesRss.CONFIG.getVlcExecutable() != null) || (vlcExecutable != null && !vlcExecutable.equals(MyTunesRss.CONFIG.getVlcExecutable()));
        changes |= (myVlcEnabled.booleanValue() != MyTunesRss.CONFIG.isVlcEnabled());
        changes |= myVlcSocketTimeout.getIntegerValue(5) != MyTunesRss.CONFIG.getVlcSocketTimeout();
        changes |= myVlcRaopVolume.getIntegerValue(75) != MyTunesRss.CONFIG.getVlcRaopVolume();
        MyTunesRss.CONFIG.setVlcExecutable(vlcExecutable);
        MyTunesRss.CONFIG.setVlcEnabled(myVlcEnabled.booleanValue());
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
            createTranscoderPanel();
        }
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
            String name = getBundleString("transcoderPanel.defaultName", myTranscoderNumberGenerator.getAndIncrement());
            panel.setTranscoderName(name);
        } else {
            super.buttonClick(clickEvent);
        }
    }
}
