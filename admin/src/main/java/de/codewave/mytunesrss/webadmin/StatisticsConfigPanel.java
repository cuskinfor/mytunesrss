/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;
import de.codewave.mytunesrss.ItunesDatasourceConfig;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.datastore.statement.Track;
import de.codewave.mytunesrss.statistics.GetStatisticsEventsQuery;
import de.codewave.mytunesrss.statistics.SessionStartEvent;
import de.codewave.mytunesrss.statistics.StatEventType;
import de.codewave.mytunesrss.statistics.StatisticsEvent;
import de.codewave.utils.sql.*;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import de.codewave.vaadin.component.ProgressWindow;
import de.codewave.vaadin.component.SinglePanelWindow;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultXYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.JFreeChartWrapper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class StatisticsConfigPanel extends MyTunesRssConfigPanel {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsConfigPanel.class);

    static enum ReportType {
        Sessions("sessions", StatEventType.SESSION_START);

        private String myLabelKey;
        private StatEventType[] myStatEventTypes;

        private ReportType(String labelKey, StatEventType... statEventTypes) {
            myLabelKey = labelKey;
            myStatEventTypes = statEventTypes;
        }

        public StatEventType[] getStatEventTypes() {
            return myStatEventTypes;
        }

        @Override
        public String toString() {
            return "statisticsConfigPanel.reportType." + myLabelKey;
        }
    }

    private Form myConfigForm;
    private SmartTextField myStatisticsKeepTime;
    private DateField myReportFromDate;
    private DateField myReportToDate;
    private Select myReportType;
    private Button myGenerateReport;

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
        myReportType = getComponentFactory().createSelect("statisticsConfigPanel.reportType", Arrays.asList(ReportType.values()));
        myGenerateReport = getComponentFactory().createButton("statisticsConfigPanel.createReport", this);
        myConfigForm.addField(myStatisticsKeepTime, myStatisticsKeepTime);
        addComponent(getComponentFactory().surroundWithPanel(myConfigForm, FORM_PANEL_MARGIN_INFO, getBundleString("statisticsConfigPanel.config.caption")));
        Form sendForm = getComponentFactory().createForm(null, true);
        sendForm.addField(myReportFromDate, myReportFromDate);
        sendForm.addField(myReportToDate, myReportToDate);
        sendForm.addField(myReportType, myReportType);
        sendForm.addField(myGenerateReport, myGenerateReport);
        addComponent(getComponentFactory().surroundWithPanel(sendForm, FORM_PANEL_MARGIN_INFO, getBundleString("statisticsConfigPanel.send.caption")));

        addDefaultComponents(0, 2, 0, 2, false);

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

    @Override
    public void buttonClick(Button.ClickEvent clickEvent) {
        if (clickEvent.getButton() == myGenerateReport) {
            //generateReport();
            try {
                JFreeChartWrapper wrapper = new JFreeChartWrapper(generateReport((ReportType)myReportType.getValue()));
                Window chartWindow = new Window("@todo chart");
                Panel chartPanel = new Panel();
                chartPanel.addComponent(wrapper);
                chartWindow.setContent(chartPanel);
                getWindow().addWindow(chartWindow);
            } catch (SQLException e) {
                LOG.error("Could not create report chart.", e);
            }
        } else {
            super.buttonClick(clickEvent);
        }
    }

    private JFreeChart generateReport(ReportType reportType) throws SQLException {
        if (reportType == ReportType.Sessions) {
            List<StatisticsEvent> events = selectData(((ReportType)myReportType.getValue()).getStatEventTypes());
            TimeSeries ts = new TimeSeries("@todo sessions");
            for (Day day : getReportDays()) {
                long sessionsPerDay = 0;
                for (StatisticsEvent event : events) {
                    if (day.getFirstMillisecond() <= event.getEventTime() && day.getLastMillisecond() >= event.getEventTime()) {
                        sessionsPerDay++;
                    }
                }
                ts.add(day, sessionsPerDay);
            }
            TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection(ts);
            return ChartFactory.createTimeSeriesChart("@todo sessions per day", "@todo date", "@todo sessions", timeSeriesCollection, false, true, false);
        } else {
            throw new IllegalArgumentException("Report type \"" + reportType + "\" cannot be generated.");
        }
    }

    private List<Day> getReportDays() {
        List<Day> reportDays = new ArrayList<Day>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime((Date)myReportFromDate.getValue());
        while (calendar.getTime().compareTo((Date)myReportToDate.getValue()) <= 0) {
            reportDays.add(new Day(calendar.getTime()));
            calendar.add(Calendar.DATE, 1);
        }
        return reportDays;
    }

    private List<StatisticsEvent> selectData(StatEventType... types) throws SQLException {
        DataStoreSession tx = MyTunesRss.STORE.getTransaction();
        try {
            return tx.executeQuery(new GetStatisticsEventsQuery(
                    ((Date)myReportFromDate.getValue()).getTime(),
                    ((Date)myReportToDate.getValue()).getTime() + (1000L * 3600L * 24L) - 1L,
                    types
            )).getResults();
        } finally {
            tx.rollback();
        }
    }
}