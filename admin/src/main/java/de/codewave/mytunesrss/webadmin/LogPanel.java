/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.webadmin;

import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class LogPanel extends Panel {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogPanel.class);

    private Label myLogText;

    @Override
    public void attach() {

        setSizeFull();

        GridLayout grid = new GridLayout(1, 1);
        grid.setSizeUndefined();
        setContent(grid);

        myLogText = new Label("", Label.CONTENT_PREFORMATTED);

        BufferedReader logReader = null;
        try {
            logReader = new BufferedReader(new FileReader(new File(System.getProperty("MyTunesRSS.logDir", "."), "/MyTunesRSS.log")));
            myLogText.setValue(tail(logReader, 2000));
        } catch (FileNotFoundException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not open log file for reading.", e);
            }
        } catch (IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Could not read log file.", e);
            }
        } finally {
            if (logReader != null) {
                try {
                    logReader.close();
                } catch (IOException e) {
                    throw new RuntimeException("Could not close log file.");
                }
            }
        }

        myLogText.setSizeUndefined();
        setScrollable(true);
        setScrollTop(Short.MAX_VALUE);

        getContent().addComponent(myLogText);

    }

    private String tail(BufferedReader reader, int size) throws IOException {
        List<String> lines = new ArrayList<String>(size + 1);
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            lines.add(line);
            if (lines.size() > size) {
                lines.remove(0);
            }
        }
        return StringUtils.join(lines, System.getProperty("line.separator"));
    }

}