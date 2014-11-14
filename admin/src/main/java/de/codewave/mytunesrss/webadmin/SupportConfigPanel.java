/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Form;
import com.vaadin.ui.Label;
import com.vaadin.ui.Select;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.UUID;

public class SupportConfigPanel extends MyTunesRssConfigPanel {

    private static final Logger LOGGER = LoggerFactory.getLogger(SupportConfigPanel.class);

    private Form mySupportForm;
    private Form mySysInfoForm;
    private Select myLogLevel;
    private Button myShowLog;
    private Button myGetLogs;

    public void attach() {
        super.attach();
        init(getBundleString("supportConfigPanel.caption"), getComponentFactory().createGridLayout(1, 3, true, true));
        mySupportForm = getComponentFactory().createForm(null, true);
        myGetLogs = getComponentFactory().createButton("supportConfigPanel.getLogs", this);
        mySupportForm.addField("getLogs", myGetLogs);
        addComponent(getComponentFactory().surroundWithPanel(mySupportForm, FORM_PANEL_MARGIN_INFO, getBundleString("supportConfigPanel.caption.support")));
        mySysInfoForm = getComponentFactory().createForm(null, true);
        myLogLevel = getComponentFactory().createSelect("supportConfigPanel.logLevel", Arrays.asList(Level.OFF, Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG, Level.TRACE));
        myShowLog = getComponentFactory().createButton("supportConfigPanel.showLog", this);
        mySysInfoForm.addField("logLevel", myLogLevel);
        mySysInfoForm.addField("showLog", myShowLog);
        addComponent(getComponentFactory().surroundWithPanel(mySysInfoForm, FORM_PANEL_MARGIN_INFO, getBundleString("supportConfigPanel.caption.sysInfo")));

        addDefaultComponents(0, 2, 0, 2, false);

        initFromConfig();
    }

    protected void initFromConfig() {
        myLogLevel.setValue(MyTunesRss.CONFIG.getCodewaveLogLevel());
    }

    protected void writeToConfig() {
        if (!MyTunesRss.CONFIG.getCodewaveLogLevel().equals(myLogLevel.getValue())) {
            MyTunesRss.CONFIG.setCodewaveLogLevel((Level) myLogLevel.getValue());
            MyTunesRssUtils.setCodewaveLogLevel((Level) myLogLevel.getValue());
        }
        MyTunesRss.CONFIG.save();
    }

    public void buttonClick(Button.ClickEvent clickEvent) {
        if (clickEvent.getSource() == myGetLogs) {
            getWindow().open(new StreamResource(new StreamResource.StreamSource() {
                @Override
                public InputStream getStream() {
                    try {
                        PipedOutputStream pipedOutputStream = new PipedOutputStream();
                        MyTunesRssUtils.writeSupportArchiveAsync("mytunesrss_" + MyTunesRss.VERSION + "_support", pipedOutputStream);
                        return new PipedInputStream(pipedOutputStream);
                    } catch (IOException e) {
                        LOGGER.error("Could not get support archive!", e);
                    }
                    return new NullInputStream(0L);
                }
            }, "mytunesrss_" + MyTunesRss.VERSION + "_support.zip", getApplication()));
        } else if (clickEvent.getSource() == myShowLog) {
            getWindow().open(new ExternalResource("/-system/log"), UUID.randomUUID().toString());
        } else {
            super.buttonClick(clickEvent);
        }
    }

}
