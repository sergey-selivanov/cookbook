package org.sergeys.cookbook.logic;

public class Settings {

    public static class WindowPosition{
        private double x;
        private double y;
        private double width;
        private double height;

        //public WindowPosition() {}

        public void setValues(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getWidth() {
            return width;
        }

        public void setWidth(double width) {
            this.width = width;
        }

        public double getHeight() {
            return height;
        }

        public void setHeight(double height) {
            this.height = height;
        }
    }

    private WindowPosition windowPosition = new WindowPosition();
    private double winDividerPosition = 0;
    private String lastFilechooserLocation = "";

    public WindowPosition getWindowPosition() {
        return windowPosition;
    }
    public void setWindowPosition(WindowPosition windowPosition) {
        this.windowPosition = windowPosition;
    }
    public double getWinDividerPosition() {
        return winDividerPosition;
    }
    public void setWinDividerPosition(double winDividerPosition) {
        this.winDividerPosition = winDividerPosition;
    }
    public String getLastFilechooserLocation() {
        return lastFilechooserLocation;
    }
    public void setLastFilechooserLocation(String lastFilechooserLocation) {
        this.lastFilechooserLocation = lastFilechooserLocation;
    }
}
