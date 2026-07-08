package com.ghosttouch.app;

import android.content.Context;
import android.content.SharedPreferences;

final class GhostTouchConfig {
    static final String PREFS_NAME = "ghost_touch_settings";
    private static final String KEY_SWIPE_ENABLED = "swipe_enabled";
    private static final String KEY_MIN_SECONDS = "swipe_min_seconds";
    private static final String KEY_MAX_SECONDS = "swipe_max_seconds";

    final boolean swipeEnabled;
    final int minSeconds;
    final int maxSeconds;

    private GhostTouchConfig(boolean swipeEnabled, int minSeconds, int maxSeconds) {
        this.swipeEnabled = swipeEnabled;
        this.minSeconds = minSeconds;
        this.maxSeconds = maxSeconds;
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
        return new GhostTouchConfig(
                prefs.getBoolean(KEY_SWIPE_ENABLED, false),
                minSeconds,
                maxSeconds
        );
    }

    static void save(Context context, boolean swipeEnabled, int minSeconds, int maxSeconds) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_SWIPE_ENABLED, swipeEnabled)
                .putInt(KEY_MIN_SECONDS, minSeconds)
                .putInt(KEY_MAX_SECONDS, maxSeconds)
                .apply();
    }
}
