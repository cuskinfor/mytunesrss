package de.codewave.mytunesrss.webadmin;

import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Form;
import com.vaadin.ui.Select;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.User;
import de.codewave.mytunesrss.config.transcoder.TranscoderConfig;
import de.codewave.mytunesrss.mediaserver.MediaServerClientProfile;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.validation.NetworkValidator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class UpnpServerClientProfileConfigPanel extends MyTunesRssConfigPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpnpServerClientProfileConfigPanel.class);

    private UpnpServerConfigPanel myUpnpServerConfigPanel;
    private Set<String> myUsedProfileNames;
    private Runnable mySaveRunnable;
    private MediaServerClientProfile myMediaServerClientProfile;
    private SmartTextField myNameField;
    private Select myUserSelect;
    private SmartTextField myUserAgentPatternField;
    private SmartTextField myNetworkField;
    private SmartTextField myPhotoSizesField;
    private LinkedHashMap<String, CheckBox> myTranscoderCheckboxes = new LinkedHashMap<>();
    private Form myGeneralForm;
    private Form myActivationForm;
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
        init(getBundleString("upnpServerConfigPanel.clientProfileConfigPanel.caption"), getComponentFactory().createGridLayout(1, 5, true, true));

        myGeneralForm = getComponentFactory().createForm(null, false);
        myNameField = getComponentFactory().createTextField("upnpServerConfigPanel.clientProfileConfigPanel.name", new StringLengthValidator(getBundleString("upnpServerConfigPanel.clientProfileConfigPanel.error.name", 1, 100), 1, 100, false));
        myUserSelect = getComponentFactory().createSelect("upnpServerConfigPanel.clientProfileConfigPanel.user", MyTunesRss.CONFIG.getUsers());
        myGeneralForm.addField("name", myNameField);
        myGeneralForm.addField("user", myUserSelect);
        addComponent(getComponentFactory().surroundWithPanel(myGeneralForm, FORM_PANEL_MARGIN_INFO, getBundleString("upnpServerConfigPanel.clientProfileConfigPanel.general.caption")));
        myActivationForm = getComponentFactory().createForm(null, false);
        myUserAgentPatternField = getComponentFactory().createTextField("upnpServerConfigPanel.clientProfileConfigPanel.userAgentPattern", new StringLengthValidator(getBundleString("upnpServerConfigPanel.clientProfileConfigPanel.error.userAgentPattern"), 1, 100, false));
        myActivationForm.addField("useragent", myUserAgentPatternField);
        myNetworkField = getComponentFactory().createTextField("upnpServerConfigPanel.clientProfileConfigPanel.network", new NetworkValidator(getBundleString("upnpServerConfigPanel.clientProfileConfigPanel.error.network")));
        myActivationForm.addField("network", myNetworkField);
        addComponent(getComponentFactory().surroundWithPanel(myActivationForm, FORM_PANEL_MARGIN_INFO, getBundleString("upnpServerConfigPanel.clientProfileConfigPanel.activation.caption")));
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
        addDefaultComponents(0, 4, 0, 4, false);
        initFromConfig();
    }

    @Override
    protected boolean beforeSave() {
        boolean valid = VaadinUtils.isValid(myGeneralForm, myActivationForm, myPhotosForm, myTranscodersForm);
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
        myMediaServerClientProfile.setUsername(((User)myUserSelect.getValue()).getName());
        myMediaServerClientProfile.setUserAgentPattern(myUserAgentPatternField.getStringValue("*"));
        myMediaServerClientProfile.setNetwork(StringUtils.trimToNull(myNetworkField.getStringValue(null)));
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
    }

    @Override
    protected void initFromConfig() {
        myNameField.setValue(myMediaServerClientProfile.getName(), "");
        myUserSelect.select(MyTunesRss.CONFIG.getUser(myMediaServerClientProfile.getUsername()));
        myUserAgentPatternField.setValue(myMediaServerClientProfile.getUserAgentPattern(), "*");
        myNetworkField.setValue(myMediaServerClientProfile.getNetwork(), "");
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
