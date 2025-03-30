package utils.file_parsing_utils;

public enum Zone {
    TITLE(0.3, 0),
    AUTHORS(0.2, 1),
    BODY(0.5, 2);

    public static final int NUM_ZONES = 3;

    private final double value;
    private final int index;

    Zone(double value, int index) {
        this.value = value;
        this.index = index;
    }

    public double getValue() {
        return value;
    }

    public int getIndex() {
        return index;
    }

    public static Zone fromIndex(int index) {
        for (Zone zone : values()) {
            if (zone.index == index) {
                return zone;
            }
        }
        throw new IllegalArgumentException("Invalid index: " + index);
    }
}