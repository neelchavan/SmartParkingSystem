package com.airtribe.parking.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TimeUtil {

    public static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private TimeUtil() {}

    /** Duration in whole hours, rounded up. Minimum: 1 hour. */
    public static long durationInHoursCeiling(LocalDateTime start, LocalDateTime end) {
        long minutes = Duration.between(start, end).toMinutes();
        long hours = (minutes + 59) / 60;
        return Math.max(1, hours);
    }

    public static String format(LocalDateTime dt) {
        return dt == null ? "—" : dt.format(DISPLAY_FORMAT);
    }
}
