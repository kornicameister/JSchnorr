package org.kornicameister.crypto.utils;

import java.util.concurrent.TimeUnit;

/**
 * @author kornicameister
 * @since 0.0.1
 */
public class TimeUtils {
    public static long elapsedFromTime(long startTime) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
    }
}
