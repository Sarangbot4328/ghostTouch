package com.ghosttouch.app;

import android.content.Context;
import android.content.SharedPreferences;

final class GhostTouchConfig {
    static final int SWIPE_PATTERN_1 = 1;
    static final int SWIPE_PATTERN_2 = 2;
    static final int SWIPE_PATTERN_3 = 3;

    static final String PREFS_NAME = "ghost_touch_settings";
    private static final String KEY_SWIPE_ENABLED = "swipe_enabled";
    private static final String KEY_MIN_SECONDS = "swipe_min_seconds";
    private static final String KEY_MAX_SECONDS = "swipe_max_seconds";
    private static final String KEY_SWIPE_PATTERN = "swipe_pattern";

    final boolean swipeEnabled;
    final int minSeconds;
    final int maxSeconds;
    final int swipePattern;

    private GhostTouchConfig(boolean swipeEnabled, int minSeconds, int maxSeconds, int swipePattern) {
        this.swipeEnabled = swipeEnabled;
        this.minSeconds = minSeconds;
        this.maxSeconds = maxSeconds;
        this.swipePattern = swipePattern;
    }

    static GhostTouchConfig load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int minSeconds = prefs.getInt(KEY_MIN_SECONDS, 10);
        int maxSeconds = prefs.getInt(KEY_MAX_SECONDS, 30);
        if (minSeconds < 1) {
            minSeconds = 1;
        }
        if (maxSeconds < minSeconds) {
            maxSeconds = minSeconds;
        }
        int swipePattern = prefs.getInt(KEY_SWIPE_PATTERN, SWIPE_PATTERN_1);
        if (swipePattern != SWIPE_PATTERN_1
                && swipePattern != SWIPE_PATTERN_2
                && swipePattern != SWIPE_PATTERN_3) {
            swipePattern = SWIPE_PATTERN_1;
        }
        return new GhostTouchConfig(
                prefs.getBoolean(KEY_SWIPE_ENABLED, false),
                minSeconds,
                maxSeconds,
                swipePattern
        );
    }

    static void save(Context context, boolean swipeEnabled, int minSeconds, int maxSeconds, int swipePattern) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_SWIPE_ENABLED, swipeEnabled)
                .putInt(KEY_MIN_SECONDS, minSeconds)
                .putInt(KEY_MAX_SECONDS, maxSeconds)
                .putInt(KEY_SWIPE_PATTERN, swipePattern)
                .apply();
    }
}
