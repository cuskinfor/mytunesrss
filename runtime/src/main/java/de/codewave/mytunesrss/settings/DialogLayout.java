package de.codewave.mytunesrss.settings;

/**
 * de.codewave.mytunesrss.settings.DialogLayout
 */
public class DialogLayout {
    private int myX = -1;
    private int myY = -1;
    private int myWidth = -1;
    private int myHeight = -1;

    public int getHeight() {
        return myHeight;
    }

    public void setHeight(int height) {
        myHeight = height;
    }

    public int getWidth() {
        return myWidth;
    }

    public void setWidth(int width) {
        myWidth = width;
    }

    public int getX() {
        return myX;
    }

    public void setX(int x) {
        myX = x;
    }

    public int getY() {
        return myY;
    }

    public void setY(int y) {
        myY = y;
    }

    public boolean isValid() {
        return myX != -1 && myY != -1 && myWidth != -1 && myHeight != -1;
    }
}