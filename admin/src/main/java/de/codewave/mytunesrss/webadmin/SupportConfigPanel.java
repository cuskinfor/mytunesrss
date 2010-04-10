/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.Application;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.*;
import de.codewave.mytunesrss.task.SendSupportRequestTask;
import de.codewave.vaadin.ComponentFactory;
import de.codewave.vaadin.SmartTextField;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;

public class SupportConfigPanel extends MyTunesRssConfigPanel implements Upload.Receiver, Upload.SucceededListener, Upload.FailedListener {

    private Form mySupportForm;
    private Form myRegistrationForm;
    private Form mySysInfoForm;
    private Select myLogLevel;
    private SmartTextField myName;
    private SmartTextField myEmail;
    private SmartTextField myDescription;
    private CheckBox myIncludeItunesXml;
    private Button mySendSupport;
    private SmartTextField myRegName;
    private DateField myExpirationDate;
    private Upload myUploadLicense;
    private SmartTextField mySysInfo;
    private File myUploadDir;

    public SupportConfigPanel(Application application, ComponentFactory componentFactory) {
        super(application, getBundleString("supportConfigPanel.caption"), componentFactory.createGridLayout(1, 4, true, true), componentFactory);
    }

    protected void init(Application application) {
        mySupportForm = getComponentFactory().createForm(null, true);
        myName = getComponentFactory().createTextField("supportConfigPanel.name");
        myEmail = getComponentFactory().createTextField("supportConfigPanel.email");
        myDescription = getComponentFactory().createTextField("supportConfigPanel.description");
        myDescription.setRows(10);
        myIncludeItunesXml = getComponentFactory().createCheckBox("supportConfigPanel.includeItunesXml");
        mySendSupport = getComponentFactory().createButton("supportConfigPanel.sendSupport", this);
        mySupportForm.addField("logLevel", myLogLevel);
        mySupportForm.addField("name", myName);
        mySupportForm.addField("email", myEmail);
        mySupportForm.addField("description", myDescription);
        mySupportForm.addField("includeItunesXml", myIncludeItunesXml);
        mySupportForm.addField("sendSupport", mySendSupport);
        addComponent(getComponentFactory().surroundWithPanel(mySupportForm, FORM_PANEL_MARGIN_INFO, getBundleString("supportConfigPanel.caption.support")));
        myRegistrationForm = getComponentFactory().createForm(null, true);
        myRegName = getComponentFactory().createTextField("supportConfigPanel.regName");
        myRegName.setEnabled(false);
        myExpirationDate = new DateField(getBundleString("supportConfigPanel.expirationDate"));
        myExpirationDate.setDateFormat(MyTunesRssUtils.getBundleString("common.dateFormat"));
        myExpirationDate.setResolution(DateField.RESOLUTION_DAY);
        myExpirationDate.setEnabled(false);
        myUploadLicense = new Upload(null, this);
        myUploadLicense.setButtonCaption(getBundleString("supportConfigPanel.uploadLicense"));
        myUploadLicense.setImmediate(true);
        myUploadLicense.addListener((Upload.SucceededListener) this);
        myUploadLicense.addListener((Upload.FailedListener) this);
        myRegistrationForm.addField("regName", myRegName);
        myRegistrationForm.addField("expirationDate", myExpirationDate);
        Panel registrationPanel = getComponentFactory().surroundWithPanel(myRegistrationForm, new Layout.MarginInfo(false, true, true, true), getBundleString("supportConfigPanel.caption.registration"));
        registrationPanel.addComponent(myUploadLicense);
        addComponent(registrationPanel);
        mySysInfoForm = getComponentFactory().createForm(null, true);
        myLogLevel = getComponentFactory().createSelect("supportConfigPanel.logLevel", Arrays.asList(Level.OFF, Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG));
        mySysInfo = getComponentFactory().createTextField("supportConfigPanel.sysInfo");
        mySysInfo.setEnabled(false);
        mySysInfo.setRows(10);
        mySysInfoForm.addField("logLevel", myLogLevel);
        mySysInfoForm.addField("sysInfo", mySysInfo);
        addComponent(getComponentFactory().surroundWithPanel(mySysInfoForm, FORM_PANEL_MARGIN_INFO, getBundleString("supportConfigPanel.caption.sysInfo")));

        addMainButtons(0, 3, 0, 3);
    }

    protected void initFromConfig(Application application) {
        myLogLevel.setValue(MyTunesRss.CONFIG.getCodewaveLogLevel());
        myName.setValue(MyTunesRss.CONFIG.getSupportName());
        myEmail.setValue(MyTunesRss.CONFIG.getSupportEmail());
        myRegName.setValue(MyTunesRss.REGISTRATION.getName());
        if (MyTunesRss.REGISTRATION.isExpirationDate()) {
            myExpirationDate.setValue(new Date(MyTunesRss.REGISTRATION.getExpiration()));
        }
        mySysInfo.setValue(MyTunesRssUtils.getSystemInfo());
    }

    protected void writeToConfig() {
        MyTunesRss.CONFIG.setCodewaveLogLevel((Level) myLogLevel.getValue());
        MyTunesRss.CONFIG.setSupportName((String) myName.getValue());
        MyTunesRss.CONFIG.setSupportEmail((String) myEmail.getValue());
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        if (clickEvent.getSource() == mySendSupport) {
            if (StringUtils.isNotBlank((String) myName.getValue()) && StringUtils.isNotBlank((String) myEmail.getValue()) && StringUtils.isNotBlank((String) myDescription.getValue())) {
                SendSupportRequestTask requestTask = new SendSupportRequestTask((String) myName.getValue(), (String) myEmail.getValue(), (String) myDescription.getValue() + "\n\n\n", (Boolean) myIncludeItunesXml.getValue());
                requestTask.execute();
                if (requestTask.isSuccess()) {
                    getApplication().showInfo("supportConfigPanel.info.supportRequestSent");
                } else {
                    getApplication().showError("supportConfigPanel.error.supportRequestFailed");
                }
            } else {
                getApplication().showError("supportConfigPanel.error.allFieldsMandatoryForSupport");
            }
        } else {
            super.buttonClick(clickEvent);
        }
    }

    public OutputStream receiveUpload(String filename, String MIMEType) {
        try {
            myUploadDir = new File(MyTunesRssUtils.getCacheDataPath() + "/license-upload");
            if (!myUploadDir.isDirectory()) {
                myUploadDir.mkdir();
            }
            return new FileOutputStream(new File(myUploadDir, filename));
        } catch (IOException e) {
            throw new RuntimeException("Could not receive upload.", e);
        }
    }

    public void uploadFailed(Upload.FailedEvent event) {
        FileUtils.deleteQuietly(myUploadDir);
        getApplication().showError("supportConfigPanel.error.licenseUploadFailed");
    }

    public void uploadSucceeded(Upload.SucceededEvent event) {
        try {
            MyTunesRssRegistration registration = MyTunesRssRegistration.register(new File(myUploadDir, event.getFilename()));
            getApplication().showInfo("supportConfigPanel.info.licenseOk", registration.getName());
        } catch (MyTunesRssRegistrationException e) {
            switch (e.getErrror()) {
                case InvalidFile:
                    getApplication().showError("supportConfigPanel.error.invalidLicenseFile");
                    break;
                case LicenseExpired:
                    getApplication().showError("supportConfigPanel.error.licenseExpired");
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected error code \"" + e.getErrror() + "\".");
            }
        } finally {
            FileUtils.deleteQuietly(myUploadDir);
        }
    }
}