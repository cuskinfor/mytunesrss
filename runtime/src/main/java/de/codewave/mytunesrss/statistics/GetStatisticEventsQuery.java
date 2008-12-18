package de.codewave.mytunesrss.statistics;

import de.codewave.mytunesrss.MyTunesRssUtils;
import de.codewave.utils.sql.DataStoreQuery;
import de.codewave.utils.sql.SmartStatement;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * de.codewave.mytunesrss.statistics.GetStatisticEventsQuery
 */
public class GetStatisticEventsQuery extends DataStoreQuery<List<String>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetStatisticEventsQuery.class);
    private static final String[] HEADERS = new String[] {"date", "user", "upBytes", "downBytes", "logins"};
    private static final int IDX_UP_BYTES = 0;
    private static final int IDX_DOWN_BYTES = 1;
    private static final int IDX_LOGINS = 2;

    private long myFrom;
    private long myTo;
    private SimpleDateFormat myDateFormat;

    public GetStatisticEventsQuery(long from, long to, String dateFormat) {
        myFrom = from;
        myTo = to;
        myDateFormat = new SimpleDateFormat(dateFormat);
    }

    public List<String> execute(Connection connection) throws SQLException {
        SmartStatement statement = MyTunesRssUtils.createStatement(connection, "getStatisticEvents");
        statement.setLong("ts_from", myFrom);
        statement.setLong("ts_to", myTo);
        LOGGER.debug("Getting statistics with timestamp from " + myFrom + " to " + myTo);
        ResultSet rs = statement.executeQuery();
        Map<String, long[]> valuesMap = new LinkedHashMap<String, long[]>();
        while (rs.next()) {
            try {
                String date = myDateFormat.format(new Date(rs.getLong("ts")));
                StatisticsEvent event = (StatisticsEvent)new ObjectInputStream(new ByteArrayInputStream(rs.getBytes("data"))).readObject();
                String key = date + "," + event.getUser().replace(',', '_');
                LOGGER.debug("Processing entry with key \"" + key + "\".");
                valuesMap.put(key, calcValues(valuesMap.get(key), event));
            } catch (IOException e) {
                LOGGER.error("Could not create event from bytes.", e);
            } catch (ClassNotFoundException e) {
                LOGGER.error("Could not create event from bytes.", e);
            }
        }
        LOGGER.debug("Created " + valuesMap.size() + " statistic entries.");
        List<String> csv = new ArrayList<String>(valuesMap.size());
        csv.add(StringUtils.join(HEADERS, ','));
        for (Map.Entry<String, long[]> entry : valuesMap.entrySet()) {
            StringBuilder csvLine = new StringBuilder();
            csvLine.append(entry.getKey());
            for (long value : entry.getValue()) {
                csvLine.append(",").append(value);
            }
            LOGGER.debug("Adding to CSV: \"" + csvLine.toString() + "\".");
            csv.add(csvLine.toString());
        }
        return csv;
    }

    private long[] calcValues(long[] values, StatisticsEvent event) {
        if (values == null) {
            // upload, download, logins
            values = new long[] {0, 0, 0};
        }
        if (event instanceof UploadEvent) {
            values[IDX_UP_BYTES] += ((UploadEvent)event).getBytes();
        } else if (event instanceof DownloadEvent) {
            values[IDX_DOWN_BYTES] += ((DownloadEvent)event).getBytes();
        } else if (event instanceof LoginEvent) {
            values[IDX_LOGINS]++;
        } else {
            throw new IllegalArgumentException("Unsupported event type \"" + event.getClass().getSimpleName() + "\".");
        }
        return values;
    }
}