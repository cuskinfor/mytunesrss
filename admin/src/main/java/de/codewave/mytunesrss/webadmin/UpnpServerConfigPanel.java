/*
 * Copyright (c) 2014. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.event.MyTunesRssEvent;
import de.codewave.mytunesrss.event.MyTunesRssEventManager;
import de.codewave.mytunesrss.mediaserver.MediaServerClientProfile;
import de.codewave.mytunesrss.mediaserver.MediaServerConfig;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.OptionWindow;
import de.codewave.vaadin.validation.MinMaxIntegerValidator;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

public class UpnpServerConfigPanel extends MyTunesRssConfigPanel {

    private Form myServerForm;
    private CheckBox myServerActiveCheckbox;
    private SmartTextField myServerName;
    private SmartTextField myLockTimeout;
    private Table myProfilesTable;
    private MediaServerClientProfile myDefaultProfile;
    private Set<MediaServerClientProfile> myProfiles;

    public UpnpServerConfigPanel() {
        beforeReset();
    }

    public void attach() {
        super.attach();
        init(getBundleString("upnpServerConfigPanel.caption"), getComponentFactory().createGridLayout(1, 3, true, true));
        myServerActiveCheckbox = getComponentFactory().createCheckBox("upnpServerConfigPanel.server.active");
        myServerName = getComponentFactory().createTextField("upnpServerConfigPanel.server.name", new StringLengthValidator(getBundleString("upnpServerConfigPanel.error.name", 100), 0, 100, true));
        myLockTimeout = getComponentFactory().createTextField("upnpServerConfigPanel.server.lockTimeout", new MinMaxIntegerValidator(getBundleString("upnpServerConfigPanel.error.lockTimeout", 1, 60), 1, 60));
        myLockTimeout.setRequired(true);
        myServerForm = getComponentFactory().createForm(null, false);
        myServerForm.addField("serverActive", myServerActiveCheckbox);
        myServerForm.addField("serverName", myServerName);
        myServerForm.addField("lockTimeout", myLockTimeout);
        addComponent(getComponentFactory().surroundWithPanel(myServerForm, FORM_PANEL_MARGIN_INFO, getBundleString("upnpServerConfigPanel.caption.server")));
        Panel profilesPanel = new Panel(getBundleString("upnpServerConfigPanel.caption.profiles"));
        profilesPanel.setContent(getComponentFactory().createVerticalLayout(true, true));
        Panel profilesButtonsPanel = new Panel();
        profilesButtonsPanel.addStyleName("light");
        profilesButtonsPanel.setContent(getApplication().getComponentFactory().createHorizontalLayout(false, true));
        Button addProfileButton = getComponentFactory().createButton("upnpServerConfigPanel.profile.add", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                addMediaServerClientProfile();
            }
        });
        profilesButtonsPanel.addComponent(addProfileButton);
        Button editDefaultProfileButton = getComponentFactory().createButton("upnpServerConfigPanel.profile.editDefault", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                editMediaServerClientProfile(myDefaultProfile, Collections.<String>emptySet(), new Runnable() {
                    @Override
                    public void run() {
                        myDefaultProfile.setName("");
                    }
                }, true);
            }
        });
        profilesButtonsPanel.addComponent(editDefaultProfileButton);
        myProfilesTable = new Table();
        myProfilesTable.setCacheRate(50);
        myProfilesTable.addContainerProperty("name", String.class, null, getBundleString("upnpServerConfigPanel.profile.name"), null, null);
        myProfilesTable.addContainerProperty("edit", Button.class, null, "", null, null);
        myProfilesTable.addContainerProperty("delete", Button.class, null, "", null, null);
        myProfilesTable.setSortContainerPropertyId("name");
        profilesPanel.addComponent(myProfilesTable);
        profilesPanel.addComponent(profilesButtonsPanel);
        addComponent(profilesPanel);
        addDefaultComponents(0, 2, 0, 2, false);
        initFromConfig();
    }

    @Override
    protected boolean beforeSave() {
        boolean valid = VaadinUtils.isValid(myServerForm);
        if (myServerActiveCheckbox.booleanValue()) {
            boolean allProfilesValid = myDefaultProfile.getUser() != null;
            for (MediaServerClientProfile profile : myProfiles) {
                allProfilesValid &= profile.getUser() != null;
            }
            if (!allProfilesValid) {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("upnpServerConfigPanel.error.missingUserInProfile");
                return false;
            }
        }
        if (!valid) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
        }
        return valid;
    }

    @Override
    protected void writeToConfig() {
        try {
            String oldServerName = StringUtils.trimToEmpty(MyTunesRss.CONFIG.getUpnpMediaServerName());
            boolean oldServerState = MyTunesRss.CONFIG.isUpnpMediaServerActive();
            MyTunesRss.CONFIG.setUpnpMediaServerActive(myServerActiveCheckbox.booleanValue());
            MyTunesRss.CONFIG.setUpnpMediaServerName(StringUtils.trimToNull(myServerName.getStringValue(null)));
            MyTunesRss.CONFIG.setUpnpMediaServerLockTimeoutSeconds(myLockTimeout.getIntegerValue(10));
            MyTunesRss.MEDIA_SERVER_CONFIG.setDefaultClientProfile(myDefaultProfile);
            MyTunesRss.MEDIA_SERVER_CONFIG.setClientProfiles(new ArrayList<>(myProfiles));
            if (MyTunesRss.CONFIG.isUpnpMediaServerActive() != oldServerState || !StringUtils.equals(oldServerName, StringUtils.trimToEmpty(MyTunesRss.CONFIG.getUpnpMediaServerName()))) {
                MyTunesRss.stopUpnpMediaServer();
                MyTunesRss.startUpnpMediaServer();
            }
            try {
                MediaServerConfig.save(MyTunesRss.MEDIA_SERVER_CONFIG);
                MyTunesRss.CONFIG.save();
            } catch (IOException ignored) {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("upnpServerConfigPanel.error.save");
            }
        } finally {
            MyTunesRssEventManager.getInstance().fireEvent(MyTunesRssEvent.create(MyTunesRssEvent.EventType.MEDIA_SERVER_UPDATE));
        }
    }

    @Override
    protected void initFromConfig() {
        myServerActiveCheckbox.setValue(MyTunesRss.CONFIG.isUpnpMediaServerActive());
        myServerName.setValue(StringUtils.trimToEmpty(MyTunesRss.CONFIG.getUpnpMediaServerName()));
        myLockTimeout.setValue(MyTunesRss.CONFIG.getUpnpMediaServerLockTimeoutSeconds());
        myProfilesTable.removeAllItems();
        for (MediaServerClientProfile mediaServerClientProfile : myProfiles) {
            addClientProfileTableItem(mediaServerClientProfile);
        }
        setTablePageLengths();
    }

    private void addClientProfileTableItem(final MediaServerClientProfile mediaServerClientProfile) {
        Button edit = getComponentFactory().createButton("upnpServerConfigPanel.profile.edit", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Set<String> usedProfileNames = getProfileNames();
                usedProfileNames.remove(mediaServerClientProfile.getName());
                editMediaServerClientProfile(mediaServerClientProfile, usedProfileNames, null, false);
            }
        });
        Button delete = getComponentFactory().createButton("upnpServerConfigPanel.profile.delete", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                final Button yes = new Button(getBundleString("button.yes"));
                Button no = new Button(getBundleString("button.no"));
                new OptionWindow(30, Sizeable.UNITS_EM, null, getBundleString("upnpServerConfigPanel.profile.delete.caption"), getBundleString("upnpServerConfigPanel.profile.delete.message", mediaServerClientProfile.getName()), yes, no) {
                    public void clicked(Button button) {
                        if (button == yes) {
                            myProfilesTable.removeItem(mediaServerClientProfile);
                            myProfiles.remove(mediaServerClientProfile);
                            setTablePageLengths();
                        }
                    }
                }.show(getWindow());
            }
        });
        myProfilesTable.addItem(new Object[] {mediaServerClientProfile.getName(), edit, delete}, mediaServerClientProfile);
    }

    private void setTablePageLengths() {
        myProfilesTable.setPageLength(Math.min(myProfilesTable.getItemIds().size(), 10));
    }

    private void addMediaServerClientProfile() {
        final MediaServerClientProfile clientProfile = new MediaServerClientProfile();
        editMediaServerClientProfile(clientProfile, getProfileNames(), new Runnable() {
            @Override
            public void run() {
                myProfiles.add(clientProfile);
            }
        }, false);
    }

    private void editMediaServerClientProfile(MediaServerClientProfile clientProfile, Set<String> usedProfileNames, Runnable saveRunnable, boolean defaultProfile) {
        UpnpServerClientProfileConfigPanel upnpServerClientProfileConfigPanel = new UpnpServerClientProfileConfigPanel(this, usedProfileNames, saveRunnable, clientProfile, defaultProfile);
        ((MainWindow) VaadinUtils.getApplicationWindow(this)).showComponent(upnpServerClientProfileConfigPanel);
    }

    private Set<String> getProfileNames() {
        Set<String> names = new HashSet<>();
        for (MediaServerClientProfile mediaServerClientProfile : myProfiles) {
            names.add(mediaServerClientProfile.getName());
        }
        return names;
    }

    @Override
    protected boolean beforeReset() {
        myDefaultProfile = MyTunesRss.MEDIA_SERVER_CONFIG.getDefaultClientProfile().clone();
        myProfiles = new TreeSet<>();
        for (MediaServerClientProfile mediaServerClientProfile : MyTunesRss.MEDIA_SERVER_CONFIG.getClientProfiles()) {
            myProfiles.add(mediaServerClientProfile.clone());
        }
        return true;
    }
}
