package de.codewave.mytunesrss.settings;

import de.codewave.mytunesrss.MyTunesRss;
import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.mytunesrss.task.InitializeDatabaseTask;
import de.codewave.mytunesrss.statistics.GetStatisticEventsQuery;
import de.codewave.mytunesrss.statistics.RemoveOldEventsStatement;
import de.codewave.utils.sql.DataStoreSession;
import de.codewave.utils.swing.JTextFieldValidation;
import de.codewave.utils.swing.MinMaxValueTextFieldValidation;
import de.codewave.utils.swing.pleasewait.PleaseWaitTask;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * de.codewave.mytunesrss.settings.Statistics
 */
public class Statistics implements SettingsForm {
    private static final Logger LOGGER = LoggerFactory.getLogger(Statistics.class);

    private JPanel myRootPanel;
    private JTextField myKeepTimeInput;
    private JComboBox myFromDayInput;
    private JComboBox myFromMonthInput;
    private JComboBox myFromYearInput;
    private JComboBox myToDayInput;
    private JComboBox myToMonthInput;
    private JComboBox myToYearInput;
    private JButton mySendButton;


    public Statistics() {
        initNumberSelect(myFromDayInput, 1, 31);
        initNumberSelect(myFromMonthInput, 1, 12);
        initNumberSelect(myToDayInput, 1, 31);
        initNumberSelect(myToMonthInput, 1, 12);
        JTextFieldValidation.setValidation(new MinMaxValueTextFieldValidation(myKeepTimeInput, 0, 1000, true, MyTunesRssUtils.getBundleString(
                "error.invalidStatisticsKeepTime")));
        mySendButton.addActionListener(new SendStatisticsButtonListener());
    }

    private void initNumberSelect(JComboBox comboBox, int from, int to) {
        comboBox.removeAllItems();
        for (int i = from; i <= to; i++) {
            comboBox.addItem(i);
        }
    }

    public void initValues() {
        myKeepTimeInput.setText(Integer.toString(MyTunesRss.CONFIG.getStatisticKeepTime()));
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        int currentYear = calendar.get(Calendar.YEAR);
        initNumberSelect(myFromYearInput, 2008, currentYear);
        initNumberSelect(myToYearInput, 2008, currentYear);
        myFromDayInput.setSelectedItem(1);
        myFromMonthInput.setSelectedItem(calendar.get(Calendar.MONTH) + 1);
        myFromYearInput.setSelectedItem(calendar.get(Calendar.YEAR));
        myToDayInput.setSelectedItem(calendar.get(Calendar.DAY_OF_MONTH));
        myToMonthInput.setSelectedItem(calendar.get(Calendar.MONTH) + 1);
        myToYearInput.setSelectedItem(calendar.get(Calendar.YEAR));
    }

    public String updateConfigFromGui() {
        String messages = JTextFieldValidation.getAllValidationFailureMessage(getRootPanel());
        if (messages != null) {
            return messages;
        }
        MyTunesRss.CONFIG.setStatisticKeepTime(MyTunesRssUtils.getTextFieldInteger(myKeepTimeInput, 0));
        return null;
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    public String getDialogTitle() {
        return MyTunesRssUtils.getBundleString("dialog.statistics.title");
    }

    public class SendStatisticsButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            final Calendar from = getDate(myFromDayInput, myFromMonthInput, myFromYearInput);
            final Calendar to = getDate(myToDayInput, myToMonthInput, myToYearInput);
            if (from == null) {
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.statisticsInvalidFromDate"));
            } else if (to == null) {
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.statisticsInvalidToDate"));
            } else if (StringUtils.isBlank(MyTunesRss.CONFIG.getAdminEmail())) {
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.statisticsMissingAdminEmail"));
            } else if (!MyTunesRss.CONFIG.isValidMailConfig()) {
                MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.statisticsMissingEmailConfig"));
            } else {
                MyTunesRssUtils.executeTask(null, MyTunesRssUtils.getBundleString("pleaseWait.sendingStatisticsMail"), null, false, new PleaseWaitTask() {
                    public void execute() throws Exception {
                        sendAdminStatistics(from, to);
                    }
                });
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
                    MyTunesRss.MAILER.sendMail(MyTunesRss.CONFIG.getAdminEmail(), MyTunesRssUtils.getBundleString("email.subject.statistics",
                                                                                                                  Integer.toString(from.get(Calendar.DAY_OF_MONTH)),
                                                                                                                  Integer.toString(
                                                                                                                          from.get(Calendar.MONTH) + 1),
                                                                                                                  Integer.toString(from.get(Calendar.YEAR)),
                                                                                                                  Integer.toString(to.get(Calendar.DAY_OF_MONTH)),
                                                                                                                  Integer.toString(
                                                                                                                          to.get(Calendar.MONTH) + 1),
                                                                                                                  Integer.toString(to.get(Calendar.YEAR))),
                                               statisticsMailBody);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            MyTunesRssUtils.showInfoMessage(MyTunesRssUtils.getBundleString("info.sendStatisticsDone"));
                        }
                    });
                } catch (final MailException e) {
                    LOGGER.error("Could not send statistics email.", e);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.couldNotSendMail", e.getMessage()));
                        }
                    });

                }
            } catch (final SQLException e) {
                try {
                    tx.rollback();
                } catch (SQLException e1) {
                    LOGGER.error("Could not rollback transaction.", e1);
                }
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        MyTunesRssUtils.showErrorMessage(MyTunesRssUtils.getBundleString("error.couldNotSendStatistics", e.getMessage()));
                    }
                });
            }
        }

        private Calendar getDate(JComboBox day, JComboBox month, JComboBox year) {
            Calendar calendar = new GregorianCalendar();
            calendar.setLenient(false);
            try {
                calendar.set(Calendar.DAY_OF_MONTH, (Integer)day.getSelectedItem());
                calendar.set(Calendar.MONTH, (Integer)month.getSelectedItem() - 1);
                calendar.set(Calendar.YEAR, (Integer)year.getSelectedItem());
                return calendar;
            } catch (Exception e) {
                // setting non-lenient calendar field failed
                return null;

            }
        }
    }
}