/*
 * Copyright (c) 2012. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin.statistics;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.config.DatabaseType;
import de.codewave.mytunesrss.config.MyTunesRssConfig;
import de.codewave.mytunesrss.datastore.MyTunesRssDataStore;
import de.codewave.mytunesrss.statistics.GetStatisticsEventsQuery;
import de.codewave.mytunesrss.statistics.StatEventType;
import de.codewave.mytunesrss.statistics.StatisticsEvent;
import de.codewave.mytunesrss.task.InitializeDatabaseCallable;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.sql.DataStoreStatement;
import org.apache.commons.io.IOUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Day;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.List;

public class AllChartsGeneratorTest {

    private Date myFrom;
    private Date myTo;

    @Before
    public void before() throws ClassNotFoundException, IOException, SQLException {
        //noinspection AssignmentToStaticFieldFromInstanceMethod
        MyTunesRss.VERSION = Integer.toString(Integer.MAX_VALUE);
        //noinspection AssignmentToStaticFieldFromInstanceMethod
        MyTunesRss.CONFIG = new MyTunesRssConfig();
        MyTunesRss.CONFIG.setDatabaseType(DatabaseType.h2);
        MyTunesRss.CONFIG.setDatabaseConnection("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        MyTunesRss.CONFIG.setDatabaseUser("sa");
        MyTunesRss.CONFIG.setDatabasePassword("");
        Class.forName("org.h2.Driver");
        //noinspection AssignmentToStaticFieldFromInstanceMethod
        MyTunesRss.STORE = new MyTunesRssDataStore();
        new InitializeDatabaseCallable().call();
        DataStoreSession tx = MyTunesRss.STORE.getTransaction();
        try {
            for (final String line : IOUtils.readLines(getClass().getResourceAsStream("/statistics.sql"))) {
                tx.executeStatement(new DataStoreStatement() {
                    @Override
                    public void execute(Connection connection) throws SQLException {
                        Statement statement = connection.createStatement();
                        statement.execute(line);
                        statement.close();
                    }
                });
            }
        } finally {
            tx.commit();
        }
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_YEAR, -30);
        myFrom = calendar.getTime();
        myTo = new Date();
    }

    @After
    public void after() {
        MyTunesRss.STORE.destroy();
    }

    private Map<Day, List<StatisticsEvent>> createEmptyEventsPerDayMap() {
        Map<Day, List<StatisticsEvent>> eventsPerDay = new HashMap<>();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(myFrom);
        while (calendar.getTime().compareTo(new Date()) <= 0) {
            eventsPerDay.put(new Day(calendar.getTime()), new ArrayList<StatisticsEvent>());
            calendar.add(Calendar.DATE, 1);
        }
        return eventsPerDay;
    }

    private List<StatisticsEvent> selectData(StatEventType... types) throws SQLException {
        DataStoreSession tx = MyTunesRss.STORE.getTransaction();
        try {
            return tx.executeQuery(new GetStatisticsEventsQuery(
                    myFrom.getTime(),
                    myTo.getTime() + (1000L * 3600L * 24L) - 1L,
                    types
            )).getResults();
        } finally {
            tx.rollback();
        }
    }

    public static void saveToFile(JFreeChart chart,
                                  File file,
                                  int width,
                                  int height) throws IOException {
        BufferedImage img = draw(chart, width, height);

        ImageIO.write(img, "jpeg", file);
    }

    protected static BufferedImage draw(JFreeChart chart, int width, int height) {
        BufferedImage img =
                new BufferedImage(width, height,
                        BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        chart.draw(g2, new Rectangle2D.Double(0, 0, width, height));
        g2.dispose();
        return img;
    }

    @Test
    public void testAllCharts() throws SQLException, IOException {
        ReportChartGenerator[] generators = new ReportChartGenerator[]{
                new DownVolumePerDayChartGenerator(),
                new SessionDurationPerDayChartGenerator(),
                new SessionsPerDayChartGenerator(),
                //new TopArtistChartGenerator(),
                //new TopNameChartGenerator(),
                new TopUserSessionChartGenerator(),
                new TopUserDownVolumeChartGenerator()
        };
        for (ReportChartGenerator generator : generators) {
            Calendar from = new GregorianCalendar();
            from.setTime(new Date());
            from.roll(Calendar.DAY_OF_YEAR, -30);
            Map<Day, List<StatisticsEvent>> eventsPerDay = createEmptyEventsPerDayMap();
            for (StatisticsEvent event : selectData(generator.getEventTypes())) {
                eventsPerDay.get(new Day(new Date(event.getEventTime()))).add(event);
            }
            JFreeChart chart = generator.generate(eventsPerDay, PropertyResourceBundle.getBundle("de.codewave.mytunesrss.webadmin.MyTunesRssAdmin"));
            File file = File.createTempFile("MyTunesRSS_" + generator.getClass().getSimpleName(), ".jpg");
            file.deleteOnExit();
            System.out.println("writing file \"" + file + "\".");
            saveToFile(chart, file, 1024, 768);
        }
    }
}
