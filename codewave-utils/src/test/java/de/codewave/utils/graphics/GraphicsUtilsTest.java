/*
 * Copyright (c) 2005 Codewave Software. All Rights Reserved.
 */
package de.codewave.utils.graphics;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.awt.*;

/**
 * de.codewave.utils.graphics.GraphicsUtilsTest
 */
public class GraphicsUtilsTest extends TestCase {
    public static Test suite() {
        return new TestSuite(GraphicsUtilsTest.class);
    }

    public GraphicsUtilsTest(String string) {
        super(string);
    }

    public void testSortByDepth() {
        DisplayMode mode8 = new DisplayMode(800, 600, 8, 0);
        DisplayMode mode16 = new DisplayMode(800, 600, 16, 0);
        DisplayMode mode32 = new DisplayMode(800, 600, 32, 0);
        DisplayMode[] unsortedModes = new DisplayMode[] {mode32, mode8, mode16};
        DisplayMode[] sortedModes = GraphicsUtils.sortByDepth(unsortedModes);
        assertTrue("modes not sorted", sortedModes[0] == mode8);
        assertTrue("modes not sorted", sortedModes[1] == mode16);
        assertTrue("modes not sorted", sortedModes[2] == mode32);
    }
}