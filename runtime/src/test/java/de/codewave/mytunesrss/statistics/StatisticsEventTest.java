/*
 * Copyright (c) 2011. Codewave Software Michael Descher.
 * All rights reserved.
 */

package de.codewave.mytunesrss.statistics;

import org.junit.Test;

import java.io.IOException;

public class StatisticsEventTest {

    @Test
    public void testJsonString() throws IOException {
        SessionStartEvent sessionStartEvent = new SessionStartEvent("harry", "sidsidsid");
        System.out.println(sessionStartEvent.toJson());
    }

}
