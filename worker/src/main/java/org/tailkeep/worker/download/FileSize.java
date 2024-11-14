package org.tailkeep.worker.download;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileSize {
    private static final Map<String, Long> SIZE_SUFFIXES = new LinkedHashMap<>();
    private static final Map<Long, String> SIZE_SUFFIXES_INVERSE = new LinkedHashMap<>();

    private FileSize() {
        // Prevent instantiation of utility class
    }

    static {
        SIZE_SUFFIXES.put("B", 1L);
        SIZE_SUFFIXES.put("KB", 1000L);
        SIZE_SUFFIXES.put("KIB", 1024L);
        SIZE_SUFFIXES.put("MB", (long) Math.pow(1000, 2));
        SIZE_SUFFIXES.put("MIB", (long) Math.pow(1024, 2));
        SIZE_SUFFIXES.put("GB", (long) Math.pow(1000, 3));
        SIZE_SUFFIXES.put("GIB", (long) Math.pow(1024, 3));
        SIZE_SUFFIXES.put("TB", (long) Math.pow(1000, 4));
        SIZE_SUFFIXES.put("TIB", (long) Math.pow(1024, 4));
        SIZE_SUFFIXES.put("PB", (long) Math.pow(1000, 5));
        SIZE_SUFFIXES.put("PIB", (long) Math.pow(1024, 5));

        SIZE_SUFFIXES_INVERSE.put(1L, "B");
        SIZE_SUFFIXES_INVERSE.put(1024L, "KiB");
        SIZE_SUFFIXES_INVERSE.put((long) Math.pow(1024, 2), "MiB");
        SIZE_SUFFIXES_INVERSE.put((long) Math.pow(1024, 3), "GiB");
        SIZE_SUFFIXES_INVERSE.put((long) Math.pow(1024, 4), "TiB");
        SIZE_SUFFIXES_INVERSE.put((long) Math.pow(1024, 5), "PiB");
    }

    public static long parseSize(String sizeStr) {
        Pattern pattern = Pattern.compile("^(\\d+(?:\\.\\d+)?)\\s*([a-zA-Z]+)$");
        Matcher matcher = pattern.matcher(sizeStr.toUpperCase());

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid size string: " + sizeStr);
        }

        double value = Double.parseDouble(matcher.group(1));
        String unit = matcher.group(2);

        if (!SIZE_SUFFIXES.containsKey(unit)) {
            throw new IllegalArgumentException("Unknown unit: " + unit);
        }

        return (long) (value * SIZE_SUFFIXES.get(unit));
    }

    public static String formatSize(long bytes) {
        if (bytes == 0)
            return "0 B";

        double value = bytes;
        String unit = "B";

        for (Map.Entry<Long, String> entry : SIZE_SUFFIXES_INVERSE.entrySet()) {
            if (bytes >= entry.getKey()) {
                value = (double) bytes / entry.getKey();
                unit = entry.getValue();
            } else {
                break;
            }
        }

        return String.format("%.2f%s", value, unit);
    }
}
