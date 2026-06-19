package com.example.cinemabookingapp.core.utils;

public final class DataParser {
    private DataParser() {
    }

    public static long getLong(Object o) {
        try {
            if (o instanceof Long) return (Long) o;
            if (o instanceof String) return Long.parseLong((String) o);
        } catch (Exception ignored) {
        }
        return 0L;
    }

    public static double getDouble(Object o) {
        try {
            if (o instanceof Double) return (Double) o;
            if (o instanceof Long) return ((Long) o).doubleValue();
            if (o instanceof String) return Double.parseDouble((String) o);
        } catch (Exception ignored) {
        }
        return 0.0;
    }
}
