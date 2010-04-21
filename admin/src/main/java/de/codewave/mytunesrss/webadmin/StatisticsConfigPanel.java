/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.ui.Button;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Form;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.statistics.GetStatisticEventsQuery;
import de.codewave.mytunesrss.statistics.RemoveOldEventsStatement;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;

import java.sql.SQLException;
import java.util.*;

public class StatisticsConfigPanel extends MyTunesRssConfigPanel {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsConfigPanel.class);

    private Form myConfigForm;
    private SmartTextField myStatisticsKeepTime;
    private DateField myReportFromDate;
    private DateField myReportToDate;
    private Button mySendButton;

    public void attach() {
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

        addMainButtons(0, 2, 0, 2);

        initFromConfig();
    }

    protected void initFromConfig() {
        myStatisticsKeepTime.setValue(MyTunesRss.CONFIG.getStatisticKeepTime(), 0, 365, 60);
    }

    protected void writeToConfig() {
        MyTunesRss.CONFIG.setStatisticKeepTime(myStatisticsKeepTime.getIntegerValue(60));
    }

    @Override
    protected boolean beforeSave() {
        boolean valid = VaadinUtils.isValid(myConfigForm);
        if (!valid) {
            getApplication().showError("error.formInvalid");
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
                sendAdminStatistics(from, to);
            } else {
                getApplication().showError("statisticsConfigPanel.error.illegalDateOrder");
            }
        } else {
            super.buttonClick(clickEvent);
        }
    }

    private void sendAdminStatistics(Calendar from, Calendar to) {
        DataStoreSession tx = MyTunesRss.STORE.getTransaction();
        try {
            tx.executeStatement(new RemoveOldEventsStatement());
            Calendar nextDay = new GregorianCalendar();
            nextDay.setTimeInMillis(to.getTimeInMillis());
            nextDay.add(Calendar.DATE, 1);
            List<String> csv = tx.executeQuery(new GetStatisticEventsQuery(from.getTimeInMillis(), nextDay.getTimeInMillis(), "yyyy-MM-dd"));
            tx.commit();
            String statisticsMailBody = StringUtils.join(csv, System.getProperty("line.separator"));
            try {
                MyTunesRss.MAILER.sendMail(MyTunesRss.CONFIG.getAdminEmail(), MyTunesRssUtils.getBundleString(Locale.getDefault(), "email.subject.statistics",
                        Integer.toString(from.get(Calendar.DAY_OF_MONTH)),
                        Integer.toString(from.get(Calendar.MONTH) + 1),
                        Integer.toString(from.get(Calendar.YEAR)),
                        Integer.toString(to.get(Calendar.DAY_OF_MONTH)),
                        Integer.toString(to.get(Calendar.MONTH) + 1),
                        Integer.toString(to.get(Calendar.YEAR))),
                        statisticsMailBody);
                getApplication().showInfo("statisticsConfigPanel.info.statisticsSentSuccessfully");
            } catch (final MailException e) {
                LOG.error("Could not send statistics email.", e);
                getApplication().showError("statisticsConfigPanel.error.statisticsNotSent");
            }
        } catch (final SQLException e) {
            try {
                tx.rollback();
            } catch (SQLException e1) {
                LOG.error("Could not rollback transaction.", e1);
            }
            getApplication().showError("statisticsConfigPanel.error.statisticsNotSent");
        }
    }
}