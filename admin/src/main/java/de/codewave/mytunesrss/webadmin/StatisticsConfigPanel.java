/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.ui.*;
import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.statistics.GetStatisticsEventsQuery;
import de.codewave.mytunesrss.statistics.StatEventType;
import de.codewave.mytunesrss.statistics.StatisticsEvent;
import de.codewave.mytunesrss.webadmin.statistics.*;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.vaadin.ResourceBundleSelectItemWrapper;
import de.codewave.vaadin.SmartTextField;
import de.codewave.vaadin.VaadinUtils;
import org.jfree.data.time.Day;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.addon.JFreeChartWrapper;

import java.sql.SQLException;
import java.util.*;

public class StatisticsConfigPanel extends MyTunesRssConfigPanel {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsConfigPanel.class);

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
        from.add(Calendar.DAY_OF_YEAR, -30);
        myReportFromDate = new DateField(getBundleString("statisticsConfigPanel.reportFrom"), from.getTime());
        myReportFromDate.setLenient(false);
        myReportFromDate.setDateFormat(MyTunesRssUtils.getBundleString(Locale.getDefault(), "common.dateFormat"));
        myReportFromDate.setResolution(DateField.RESOLUTION_DAY);
        Calendar to = new GregorianCalendar();
        myReportToDate = new DateField(getBundleString("statisticsConfigPanel.reportTo"), to.getTime());
        myReportToDate.setLenient(false);
        myReportToDate.setDateFormat(MyTunesRssUtils.getBundleString(Locale.getDefault(), "common.dateFormat"));
        myReportToDate.setResolution(DateField.RESOLUTION_DAY);
        myReportType = getComponentFactory().createSelect("statisticsConfigPanel.reportType", Arrays.asList(
                new ResourceBundleSelectItemWrapper<ReportChartGenerator>(new SessionsPerDayChartGenerator(), getApplication().getBundle()),
                new ResourceBundleSelectItemWrapper<ReportChartGenerator>(new SessionDurationPerDayChartGenerator(), getApplication().getBundle()),
                new ResourceBundleSelectItemWrapper<ReportChartGenerator>(new DownVolumePerDayChartGenerator(), getApplication().getBundle()),
                new ResourceBundleSelectItemWrapper<ReportChartGenerator>(new TopNameChartGenerator(), getApplication().getBundle()),
                new ResourceBundleSelectItemWrapper<ReportChartGenerator>(new TopArtistChartGenerator(), getApplication().getBundle())
        ));
        myReportType.setNullSelectionAllowed(false);
        myReportType.setNewItemsAllowed(false);
        myReportType.select(myReportType.getItemIds().iterator().next());
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
        myStatisticsKeepTime.setValue(MyTunesRss.CONFIG.getStatisticKeepTime(), 0, 999, 400);
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
                ReportChartGenerator generator = ((ResourceBundleSelectItemWrapper<ReportChartGenerator>) myReportType.getValue()).getItem();
                Map<Day, List<StatisticsEvent>> eventsPerDay = createEmptyEventsPerDayMap();
                for (StatisticsEvent event : selectData(generator.getEventTypes())) {
                    eventsPerDay.get(new Day(new Date(event.getEventTime()))).add(event);
                }
                JFreeChartWrapper wrapper = new JFreeChartWrapper(generator.generate(eventsPerDay, getApplication().getBundle()));
                Window chartWindow = new Window(myReportType.getValue().toString());
                Panel chartPanel = new Panel();
                chartPanel.addComponent(wrapper);
                chartPanel.setWidth((wrapper.getGraphWidth() + 50) + "px");
                chartWindow.setContent(chartPanel);
                getWindow().addWindow(chartWindow);
                chartWindow.center();
            } catch (SQLException e) {
                LOG.error("Could not create report chart.", e);
            }
        } else {
            super.buttonClick(clickEvent);
        }
    }

    private Map<Day, List<StatisticsEvent>> createEmptyEventsPerDayMap() {
        Map<Day, List<StatisticsEvent>> eventsPerDay = new HashMap<Day, List<StatisticsEvent>>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime((Date)myReportFromDate.getValue());
        while (calendar.getTime().compareTo((Date)myReportToDate.getValue()) <= 0) {
            eventsPerDay.put(new Day(calendar.getTime()), new ArrayList<StatisticsEvent>());
            calendar.add(Calendar.DATE, 1);
        }
        return eventsPerDay;
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