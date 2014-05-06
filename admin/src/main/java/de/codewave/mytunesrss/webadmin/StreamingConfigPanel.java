/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.google.common.collect.ImmutableList;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.transcoder.TranscoderConfig;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;
import de.codewave.vaadin.component.ServerSideFileChooser;
import de.codewave.vaadin.component.ServerSideFileChooserWindow;
import de.codewave.vaadin.validation.VlcExecutableFileValidator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class StreamingConfigPanel extends MyTunesRssConfigPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamingConfigPanel.class);

    private List<TranscoderConfig> myTranscoderConfigs;
    private Table myTranscoderTable;
    private Form myCacheForm;
    private Button myAddTranscoder;
    private SmartTextField myTranscodingCacheMaxGiB;
    private SmartTextField myHttpLiveStreamCacheMaxGiB;
    private AtomicLong myTranscoderNumberGenerator = new AtomicLong(1);
    private CheckBox myVlcEnabled;
    private SmartTextField myVlcBinary;
    private Button myVlcBinarySelect;
    private Form myVlcForm;
    private Button myVlcHomepageButton;
    private Button myClearAllCachesButton;

    public StreamingConfigPanel() {
        beforeReset();
    }

    public void attach() {
        super.attach();
        init(getBundleString("streamingConfigPanel.caption"), getComponentFactory().createGridLayout(1, 5, true, true));
        myVlcEnabled = getComponentFactory().createCheckBox("streamingConfigPanel.vlcEnabled");
        myVlcBinary = getComponentFactory().createTextField("streamingConfigPanel.vlcBinary", new VlcExecutableFileValidator(getBundleString("streamingConfigPanel.vlcBinary.invalidBinary")));
        myVlcBinary.setImmediate(false);
        myVlcBinarySelect = getComponentFactory().createButton("streamingConfigPanel.vlcBinary.select", this);
        myVlcHomepageButton = getComponentFactory().createButton("streamingConfigPanel.vlcHomepage", this);
        myVlcForm = getComponentFactory().createForm(null, true);
        myVlcForm.addField(myVlcEnabled, myVlcEnabled);
        myVlcForm.addField(myVlcBinary, myVlcBinary);
        myVlcForm.addField(myVlcBinarySelect, myVlcBinarySelect);
        myVlcForm.addField(myVlcHomepageButton, myVlcHomepageButton);
        Panel vlcPanel = getComponentFactory().surroundWithPanel(myVlcForm, FORM_PANEL_MARGIN_INFO, getBundleString("streamingConfigPanel.caption.vlc"));
        addComponent(vlcPanel);
        Panel transcoderPanel = new Panel(getBundleString("streamingConfigPanel.caption.transcoder"));
        transcoderPanel.setContent(getComponentFactory().createVerticalLayout(true, true));
        Panel addTranscoderButtons = new Panel();
        addTranscoderButtons.addStyleName("light");
        addTranscoderButtons.setContent(getApplication().getComponentFactory().createHorizontalLayout(false, true));
        myAddTranscoder = getComponentFactory().createButton("streamingConfigPanel.transcoder.add", this);
        addTranscoderButtons.addComponent(myAddTranscoder);
        myTranscoderTable = new Table();
        myTranscoderTable.setCacheRate(50);
        myTranscoderTable.addContainerProperty("name", String.class, null, getBundleString("streamingConfigPanel.transcoder.name"), null, null);
        myTranscoderTable.addContainerProperty("edit", Button.class, null, "", null, null);
        myTranscoderTable.addContainerProperty("delete", Button.class, null, "", null, null);
        myTranscoderTable.setSortContainerPropertyId("name");
        transcoderPanel.addComponent(myTranscoderTable);
        transcoderPanel.addComponent(addTranscoderButtons);
        addComponent(transcoderPanel);
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

    protected void initFromConfig() {
        myTranscoderTable.removeAllItems();
        for (TranscoderConfig transcoderConfig : myTranscoderConfigs) {
            addTranscoderConfigTableItem(transcoderConfig);
        }
        myTranscodingCacheMaxGiB.setValue(MyTunesRss.CONFIG.getTranscodingCacheMaxGiB(), 1, 1024, "1");
        myHttpLiveStreamCacheMaxGiB.setValue(MyTunesRss.CONFIG.getHttpLiveStreamCacheMaxGiB(), 1, 1024, "5");
        myVlcEnabled.setValue(MyTunesRss.CONFIG.isVlcEnabled());
        myVlcBinary.setValue(MyTunesRss.CONFIG.getVlcExecutable() != null ? MyTunesRss.CONFIG.getVlcExecutable().getAbsolutePath() : "");
        setTablePageLengths();
    }

    private void addTranscoderConfigTableItem(final TranscoderConfig transcoderConfig) {
        Button editButton = null;
        editButton = getComponentFactory().createButton("streamingConfigPanel.transcoder.edit", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                editTranscoderConfig(transcoderConfig, null);
            }
        });
        Button deleteButton = null;
        deleteButton = getComponentFactory().createButton("streamingConfigPanel.transcoder.delete", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                final Button yes = new Button(getBundleString("button.yes"));
                Button no = new Button(getBundleString("button.no"));
                new OptionWindow(30, Sizeable.UNITS_EM, null, getBundleString("streamingConfigPanel.transcoder.deleteConfirmation.caption"), getBundleString("streamingConfigPanel.transcoder.deleteConfirmation.message", transcoderConfig.getName()), yes, no) {
                    public void clicked(Button button) {
                        if (button == yes) {
                            myTranscoderConfigs.remove(transcoderConfig);
                            myTranscoderTable.removeItem(transcoderConfig);
                            setTablePageLengths();
                        }
                    }
                }.show(getWindow());
            }
        });
        myTranscoderTable.addItem(new Object[] {transcoderConfig.getName(), editButton, deleteButton}, transcoderConfig);
    }

    protected void writeToConfig() {
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
        MyTunesRss.CONFIG.setVlcExecutable(vlcExecutable);
        MyTunesRss.CONFIG.setVlcEnabled(myVlcEnabled.booleanValue());
        Set<TranscoderConfig> deletedTranscoders = new HashSet<>();
        for (TranscoderConfig transcoderConfig : MyTunesRss.CONFIG.getTranscoderConfigs()) {
            deletedTranscoders.add(transcoderConfig);
        }
        for (TranscoderConfig transcoderConfig : myTranscoderConfigs) {
            deletedTranscoders.remove(transcoderConfig);
        }
        for (TranscoderConfig deletedTranscoder : deletedTranscoders) {
            LOGGER.debug("Transcoder config \"" + deletedTranscoder.getName() + "\" has been removed, truncating cache.");
            MyTunesRss.TRANSCODER_CACHE.deleteByPrefix(deletedTranscoder.getCacheFilePrefix());
        }
        MyTunesRss.CONFIG.setTranscoderConfigs(myTranscoderConfigs);
        MyTunesRss.CONFIG.save();
    }

    @Override
    protected boolean beforeSave() {
        boolean valid = VaadinUtils.isValid(myCacheForm, myVlcForm);
        if (!valid) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
        }
        return valid;
    }

    @Override
    protected boolean beforeReset() {
        myTranscoderNumberGenerator.set(1);
        myTranscoderConfigs = new ArrayList<>();
        for (TranscoderConfig transcoderConfig : MyTunesRss.CONFIG.getTranscoderConfigs()) {
            myTranscoderConfigs.add((TranscoderConfig) transcoderConfig.clone());
        }
        return true;
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        if (clickEvent.getButton() == myVlcHomepageButton) {
            getWindow().open(new ExternalResource("http://www.videolan.org"));
        } else if (clickEvent.getButton() == myVlcBinarySelect) {
            File dir = StringUtils.isNotBlank((CharSequence) myVlcBinary.getValue()) ? new File((String) myVlcBinary.getValue()) : null;
            new ServerSideFileChooserWindow(50, Sizeable.UNITS_EM, null, getBundleString("streamingConfigPanel.caption.selectVlcBinary"), dir, null, ServerSideFileChooser.PATTERN_ALL, false, getApplication().getServerSideFileChooserLabels()) {
                @Override
                protected void onFileSelected(File file) {
                    myVlcBinary.setValue(file.getAbsolutePath());
                    getWindow().getParent().removeWindow(this);
                }
            }.show(getWindow());
        } else if (clickEvent.getButton() == myAddTranscoder) {
            final TranscoderConfig config = new TranscoderConfig();
            config.setName(getBundleString("transcoderConfigPanel.defaultName", myTranscoderNumberGenerator.getAndIncrement()));
            editTranscoderConfig(config, new Runnable() {
                @Override
                public void run() {
                    myTranscoderConfigs.add(config);
                }
            });
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

    private void editTranscoderConfig(TranscoderConfig transcoderConfig, Runnable successRunnable) {
        TranscoderConfigPanel transcoderConfigPanel = new TranscoderConfigPanel(this, transcoderConfig, successRunnable);
        ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(transcoderConfigPanel);
    }

    private void setTablePageLengths() {
        myTranscoderTable.setPageLength(Math.min(myTranscoderTable.getItemIds().size(), 10));
    }

    List<TranscoderConfig> getTranscoderConfigs() {
        return ImmutableList.copyOf(myTranscoderConfigs);
    }
}
