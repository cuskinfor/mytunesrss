package de.codewave.mytunesrss.jmx;

/**
 * Statistic config mbean
 */
public interface StatisticConfigMBean {
    public int getEventsKeepDays();

    public void setEventsKeepDays(int days);

    public String sendAdminEmail(String fromDate, String toDate);
}