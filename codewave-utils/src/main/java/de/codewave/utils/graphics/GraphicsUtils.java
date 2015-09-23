/*
 * Copyright (c) 2005 Codewave Software. All Rights Reserved.
 */
package de.codewave.utils.graphics;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/**
 * Graphics related utilities.
 */
public class GraphicsUtils {
    /**
     * Get all available display modes of the default screen device which match the specified
     * arguments. Arguments set to a value less than or equal zero are considered "don't care'
     * arguments, i.e. all modes are matching.
     *
     * @param width  The required width of the display mode or a value less than or equal zero to
     *               not care about the width.
     * @param height The required height of the display mode or a value less than or equal zero to
     *               not care about the height.
     * @param depth  The required depth of the display mode or a value less than or equal zero to
     *               not care about the depth.
     *
     * @return An array of all display modes matching the specified criteria.
     */
    public static DisplayMode[] getDisplayModes(int width, int height, int depth) {
        DisplayMode[] displayModes = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayModes();
        List<DisplayMode> matchingModes = new ArrayList<DisplayMode>();
        for (int i = 0; i < displayModes.length; i++) {
            DisplayMode displayMode = displayModes[i];
            if ((width <= 0 || displayMode.getWidth() == width) &&
                    (height <= 0 || displayMode.getHeight() == height) &&
                    (depth <= 0 || displayMode.getBitDepth() == depth)) {
                matchingModes.add(displayMode);
            }
        }
        return matchingModes.toArray(new DisplayMode[matchingModes.size()]);
    }

    /**
     * Sort the specified display modes by their color depth, starting with the one with the
     * smallest depth value.
     *
     * @param displayModes The unsorted display modes.
     *
     * @return The sorted display modes.
     */
    public static DisplayMode[] sortByDepth(DisplayMode[] displayModes) {
        Collections.sort(Arrays.asList(displayModes), new Comparator<DisplayMode>() {
            public int compare(DisplayMode displayMode1, DisplayMode displayMode2) {
                return displayMode1.getBitDepth() - displayMode2.getBitDepth();
            }
        });
        return displayModes;
    }

    /**
     * Get the bounds for drawing the specified text in the specified graphics context. The bounds
     * returned contain the correct width and height for the text. The x position is always set to 0
     * and the y position specifies the maximum text ascend. So drawing the text with the x and y
     * coordinate from the returned rectangle makes the text appear in the upper left corner of the
     * target context.
     *
     * @param graphics A graphics context.
     * @param text     A text.
     *
     * @return The bounds as described above.
     */
    public static Rectangle getTextBounds(Graphics graphics, String text) {
        FontMetrics metrics = graphics.getFontMetrics();
        Rectangle2D textBounds = metrics.getStringBounds(text, graphics);
        int textAscend = metrics.getMaxAscent();
        return new Rectangle(0, textAscend, (int)textBounds.getWidth(), textAscend + metrics.getMaxDescent());
    }

    /**
     * Draw a text to a graphics context. The specified point is the upper left corner of the text
     * bounds which are calculated from the text by calling {@link #getTextBounds(java.awt.Graphics,
     * String)}. The method returns the text bounds that have been used for drawing in screen
     * coordinates. This means the upper left corner of the returned rectangle is the point
     * specified when calling the method.
     *
     * @param graphics A graphics context.
     * @param x        The x coordinate.
     * @param y        The y coordinate.
     * @param text     The text to draw.
     *
     * @return The text bounds of the text drawn on the screen.
     */
    public static Rectangle drawText(Graphics graphics, int x, int y, String text) {
        Rectangle bounds = getTextBounds(graphics, text);
        graphics.drawString(text, x + bounds.x, y + bounds.y);
        bounds.y = 0;
        bounds.translate(x, y);
        return bounds;
    }

    /**
     * Draw an outlined text. This method simply draws the text multiple times. For an outline width
     * of 1, the text is drawn 9 times. It is drawn shifted 1 pixel up and left, shifted one pixel
     * up, one pixel up and right, one pixel left, etc. and finally once on the original position.
     * This is 9 times for an outline width of 1. The bigger the outline width, the more often the
     * text is drawn, so performance gets worse with the outline size.
     *
     * @param graphics     The graphics context.
     * @param text         The text.
     * @param textColor    The text color (the inside).
     * @param x            The text position x coordinate.
     * @param y            The text position y coordinate.
     * @param outlineColor The outline color.
     * @param outlineWidth The width of the outline.
     */
    public static void drawOutlineText(Graphics graphics, String text, Color textColor, int x, int y, Color outlineColor, int outlineWidth) {
        graphics.setColor(outlineColor);
        for (int dy = -outlineWidth; dy <= outlineWidth; dy++) {
            for (int dx = -outlineWidth; dx <= outlineWidth; dx++) {
                if (dx != 0 && dy != 0) {
                    graphics.drawString(text, x + dx, y + dy);
                }
            }
        }
        graphics.setColor(textColor);
        graphics.drawString(text, x, y);
    }

    public static void annotateRectangle(Graphics graphics, Rectangle rectangle, String annotation, Font font, Color color, AnnotationPosition position, int spacing) {
        Rectangle bounds = getTextBounds(graphics, annotation);
        double x;
        double y;
        switch (position) {
            case Above:
                x = rectangle.x + (rectangle.width / 2) - (bounds.width / 2);
                y = rectangle.y - bounds.height - spacing + bounds.y;
                break;
            case Below:
                x = rectangle.x + (rectangle.width / 2) - (bounds.width / 2);
                y = rectangle.y + rectangle.height + bounds.y + spacing;
                break;
            case Left:
                x = rectangle.x - bounds.width - spacing;
                y = rectangle.y + (rectangle.height / 2) - (bounds.width / 2) + bounds.y;
                break;
            case Right:
                x = rectangle.x + rectangle.width + spacing;
                y = rectangle.y + (rectangle.height / 2) - (bounds.width / 2) + bounds.y;
                break;
            case Inside:
                x = rectangle.x + (rectangle.width / 2) - (bounds.width / 2);
                y = rectangle.y + (rectangle.height / 2) - (bounds.width / 2) + bounds.y;
                break;
            default:
                throw new IllegalArgumentException("Illegal annotation position \"" + position.name() + "\".");
        }
        graphics.setFont(font);
        graphics.setColor(color);
        graphics.drawString(annotation, (int)x, (int)y);
    }
}