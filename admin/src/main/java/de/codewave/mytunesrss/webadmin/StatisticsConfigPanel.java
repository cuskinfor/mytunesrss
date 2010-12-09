/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Form;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.webadmin.task.SendStatisticsTask;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.ProgressWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class StatisticsConfigPanel extends MyTunesRssConfigPanel {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsConfigPanel.class);

    private Form myConfigForm;
    private SmartTextField myStatisticsKeepTime;
    private DateField myReportFromDate;
    private DateField myReportToDate;
    private Button mySendButton;

    public void attach() {
        super.attach();
        init(getBundleString("statisticsConfigPanel.caption"), getComponentFactory().createGridLayout(1, 3, true, true));
        myConfigForm = getComponentFactory().createForm(null, true);
        myStatisticsKeepTime = getComponentFactory().createTextField("statisticsConfigPanel.statisticsKeepTime", getApplication().getValidatorFactory().createMinMaxValidator(1, 720));
        Calendar from = new GregorianCalendar();
        from.set(Calendar.DAY_OF_MONTH, from.getActualMinimum(Calendar.DAY_OF_MONTH));
        myReportFromDate = new DateField(getBundleString("statisticsConfigPanel.reportFrom"), from.getTime());
        myReportFromDate.setLenient(false);
        myReportFromDate.setDateFormat(MyTunesRssUtils.getBundleString(Locale.getDefault(), "common.dateFormat"));
        myReportFromDate.setResolution(DateField.RESOLUTION_DAY);
        Calendar to = new GregorianCalendar();
        to.set(Calendar.DAY_OF_MONTH, to.getActualMaximum(Calendar.DAY_OF_MONTH));
        myReportToDate = new DateField(getBundleString("statisticsConfigPanel.reportTo"), to.getTime());
        myReportToDate.setLenient(false);
        myReportToDate.setDateFormat(MyTunesRssUtils.getBundleString(Locale.getDefault(), "common.dateFormat"));
        myReportToDate.setResolution(DateField.RESOLUTION_DAY);
        myConfigForm.addField(myStatisticsKeepTime, myStatisticsKeepTime);
        mySendButton = getComponentFactory().createButton("statisticsConfigPanel.send", this);
        addComponent(getComponentFactory().surroundWithPanel(myConfigForm, FORM_PANEL_MARGIN_INFO, getBundleString("statisticsConfigPanel.config.caption")));
        Form sendForm = getComponentFactory().createForm(null, true);
        sendForm.addField(myReportFromDate, myReportFromDate);
        sendForm.addField(myReportToDate, myReportToDate);
        sendForm.addField(mySendButton, mySendButton);
        addComponent(getComponentFactory().surroundWithPanel(sendForm, FORM_PANEL_MARGIN_INFO, getBundleString("statisticsConfigPanel.send.caption")));

        attach(0, 2, 0, 2);

        initFromConfig();
    }

    protected void initFromConfig() {
        myStatisticsKeepTime.setValue(MyTunesRss.CONFIG.getStatisticKeepTime(), 0, 365, 60);
    }

    protected void writeToConfig() {
        MyTunesRss.CONFIG.setStatisticKeepTime(myStatisticsKeepTime.getIntegerValue(60));
        MyTunesRss.CONFIG.save();
    }

    @Override
    protected boolean beforeSave() {
        boolean valid = VaadinUtils.isValid(myConfigForm);
        if (!valid) {
            ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("error.formInvalid");
        }
        return valid;
    }

    public void buttonClick(final Button.ClickEvent clickEvent) {
        if (clickEvent.getButton() == mySendButton) {
            GregorianCalendar from = new GregorianCalendar();
            from.setTime((Date) myReportFromDate.getValue());
            GregorianCalendar to = new GregorianCalendar();
            to.setTime((Date) myReportToDate.getValue());
            if (from.compareTo(to) < 0) {
                new ProgressWindow(50, Sizeable.UNITS_EM, null, null, getBundleString("statisticsConfigPanel.task.message", MyTunesRss.CONFIG.getAdminEmail()), false, 2000, new SendStatisticsTask(((MainWindow) VaadinUtils.getApplicationWindow(this)), from, to)).show(getWindow());
            } else {
                ((MainWindow) VaadinUtils.getApplicationWindow(this)).showError("statisticsConfigPanel.error.illegalDateOrder");
            }
        } else {
            super.buttonClick(clickEvent);
        }
    }

}