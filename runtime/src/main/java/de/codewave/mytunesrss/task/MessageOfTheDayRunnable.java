/*
 * Copyright (c) 2010. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.task;

import de.codewave.mytunesrss.MessageOfTheDay;
import de.codewave.mytunesrss.MyTunesRss;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageOfTheDayRunnable implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageOfTheDayRunnable.class);

    private Queue<MessageOfTheDay> myMessageOfTheDay = new ConcurrentLinkedQueue<>();

    public void run() {
        try {
            URI uri = null;
            try {
                uri = MyTunesRss.REGISTRATION.isReleaseVersion() && !MyTunesRss.REGISTRATION.isUnregistered() ? new URI("http://www.codewave.de/tools/motd/mytunesrss.xml") : new URI("http://www.codewave.de/tools/motd/mytunesrss_unregistered.xml");
            } catch (URISyntaxException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Invalid message of the day URI.");
                }

            }
            myMessageOfTheDay.offer(JAXB.unmarshal(uri, MessageOfTheDay.class));
        } catch (RuntimeException e) {
            LOGGER.warn("Encountered unexpected exception. Caught to keep scheduled task alive.", e);
        }
    }

    public MessageOfTheDay get() {
        return myMessageOfTheDay.poll();
    }
}
