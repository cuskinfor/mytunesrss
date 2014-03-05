package de.codewave.mytunesrss.webadmin;

import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Form;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.MyTunesRssConfig;
import de.codewave.mytunesrss.config.transcoder.TranscoderConfig;
import de.codewave.mytunesrss.mediaserver.MediaServerClientProfile;
import de.codewave.mytunesrss.mediaserver.MediaServerConfig;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class UpnpServerClientProfileConfigPanel extends MyTunesRssConfigPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpnpServerClientProfileConfigPanel.class);

    private UpnpServerConfigPanel myUpnpServerConfigPanel;
    private Set<String> myUsedProfileNames;
    private Runnable mySaveRunnable;
    private MediaServerClientProfile myMediaServerClientProfile;
    private SmartTextField myNameField;
    private SmartTextField myUserAgentPatternField;
    private SmartTextField myPhotoSizesField;
    private LinkedHashMap<String, CheckBox> myTranscoderCheckboxes = new LinkedHashMap<>();
    private Form myGeneralForm;
    private Form myPhotosForm;
    private Form myTranscodersForm;

    public UpnpServerClientProfileConfigPanel(UpnpServerConfigPanel upnpServerConfigPanel, Set<String> usedProfileNames, Runnable saveRunnable, MediaServerClientProfile mediaServerClientProfile) {
        myUpnpServerConfigPanel = upnpServerConfigPanel;
        myUsedProfileNames = usedProfileNames;
        mySaveRunnable = saveRunnable;
        myMediaServerClientProfile = mediaServerClientProfile;
    }

    public void attach() {
        super.attach();
        init(getBundleString("upnpServerConfigPanel.clientProfileConfigPanel.caption"), getComponentFactory().createGridLayout(1, 4, true, true));

        myGeneralForm = getComponentFactory().createForm(null, false);
        myNameField = getComponentFactory().createTextField("upnpServerConfigPanel.clientProfileConfigPanel.name", new StringLengthValidator(getBundleString("upnpServerConfigPanel.clientProfileConfigPanel.error.name", 1, 100), 1, 100, false));
        myGeneralForm.addField(myNameField, myNameField);
        myUserAgentPatternField = getComponentFactory().createTextField("upnpServerConfigPanel.clientProfileConfigPanel.userAgentPattern", new StringLengthValidator(getBundleString("upnpServerConfigPanel.clientProfileConfigPanel.error.userAgentPattern"), 1, 100, false));
        myGeneralForm.addField(myUserAgentPatternField, myUserAgentPatternField);
        addComponent(getComponentFactory().surroundWithPanel(myGeneralForm, FORM_PANEL_MARGIN_INFO, getBundleString("upnpServerConfigPanel.clientProfileConfigPanel.general.caption")));
        myPhotosForm = getComponentFactory().createForm(null, false);
        myPhotoSizesField = getComponentFactory().createTextField("upnpServerConfigPanel.clientProfileConfigPanel.photoSizes", new AbstractValidator(getBundleString("upnpServerConfigPanel.clientProfileConfigPanel.error.photoSizes")) {
            @Override
            public boolean isValid(Object value) {
                int sizes = 0;
                for (String s : StringUtils.split((String)value, ",")) {
                    try {
                        Integer.parseInt(s);
                        sizes++;
                    } catch (NumberFormatException ignore) {
                        return false;
                    }
                }
                return sizes > 0;
            }
        });
        myPhotosForm.addField(myPhotoSizesField, myPhotoSizesField);
        addComponent(getComponentFactory().surroundWithPanel(myPhotosForm, FORM_PANEL_MARGIN_INFO, getBundleString("upnpServerConfigPanel.clientProfileConfigPanel.photos.caption")));
        myTranscodersForm = getComponentFactory().createForm(null, false);
        for (TranscoderConfig transcoderConfig : MyTunesRss.CONFIG.getEffectiveTranscoderConfigs()) {
            CheckBox checkbox = new CheckBox(transcoderConfig.getName());
            myTranscoderCheckboxes.put(transcoderConfig.getName(), checkbox);
            myTranscodersForm.addField(checkbox, checkbox);
        }
        addComponent(getComponentFactory().surroundWithPanel(myTranscodersForm, FORM_PANEL_MARGIN_INFO, getBundleString("upnpServerConfigPanel.clientProfileConfigPanel.transcoders.caption")));
        addDefaultComponents(0, 3, 0, 3, false);
        initFromConfig();
    }

    @Override
    protected boolean beforeSave() {
        boolean valid = VaadinUtils.isValid(myGeneralForm, myPhotosForm, myTranscodersForm);
        if (!valid) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
        } else if (myUsedProfileNames.contains(myNameField.getStringValue(null))) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("upnpServerConfigPanel.clientProfileConfigPanel.error.duplicateName", myNameField.getStringValue(null));
            valid = false;
        }
        return valid;
    }

    @Override
    protected void writeToConfig() {
        myMediaServerClientProfile.setName(myNameField.getStringValue(null));
        myMediaServerClientProfile.setUserAgentPattern(myUserAgentPatternField.getStringValue("*"));
        List<Integer> photoSizes = new ArrayList<>();
        for (String size : StringUtils.split(myPhotoSizesField.getStringValue(""), ",")) {
            photoSizes.add(Integer.parseInt(size));
        }
        myMediaServerClientProfile.setPhotoSizes(photoSizes);
        List<String> transcoders = new ArrayList<>();
        for (Map.Entry<String, CheckBox> transcoderEntry : myTranscoderCheckboxes.entrySet()) {
            if (transcoderEntry.getValue().booleanValue()) {
                transcoders.add(transcoderEntry.getKey());
            }
        }
        myMediaServerClientProfile.setTranscoders(transcoders);
        if (mySaveRunnable != null) {
            mySaveRunnable.run();
        }
        try {
            MediaServerConfig.save(MyTunesRss.MEDIA_SERVER_CONFIG);
        } catch (IOException e) {
            LOGGER.error("Could not save media server config!", e);
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("upnpServerConfigPanel.clientProfileConfigPanel.error.save");
        }
    }

    @Override
    protected void initFromConfig() {
        myNameField.setValue(myMediaServerClientProfile.getName(), "");
        myUserAgentPatternField.setValue(myMediaServerClientProfile.getUserAgentPattern(), "*");
        myPhotoSizesField.setValue(StringUtils.join(myMediaServerClientProfile.getPhotoSizes(), ","), "1024,0");
        for (Map.Entry<String, CheckBox> transcoder : myTranscoderCheckboxes.entrySet()) {
            transcoder.getValue().setValue(myMediaServerClientProfile.getTranscoders().contains(transcoder.getKey()));
        }
    }

    @Override
    protected Component getSaveFollowUpComponent() {
        return myUpnpServerConfigPanel;
    }

    @Override
    protected Component getCancelFollowUpComponent() {
        return myUpnpServerConfigPanel;
    }
}
