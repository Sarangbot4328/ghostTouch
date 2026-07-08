package com.ghosttouch.app;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.util.DisplayMetrics;
import android.view.accessibility.AccessibilityEvent;

public class GhostAccessibilityService extends AccessibilityService {
    private static volatile GhostAccessibilityService instance;

    static boolean isReady() {
        return instance != null;
    }

    static GhostAccessibilityService getInstance() {
        return instance;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    @Override
    public void onDestroy() {
        if (instance == this) {
            instance = null;
        }
        MacroScheduler.stop();
        super.onDestroy();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }

    void performSwipeUp() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float x = metrics.widthPixels * 0.5f;
        float startY = metrics.heightPixels * 0.76f;
        float endY = metrics.heightPixels * 0.24f;

        Path path = new Path();
        path.moveTo(x, startY);
        path.lineTo(x, endY);

        GestureDescription gesture = new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, 0, 450))
                .build();

        dispatchGesture(gesture, null, null);
    }
}
