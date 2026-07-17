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

    void performSwipeUp(Runnable onComplete) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float x = metrics.widthPixels * 0.5f;
        float startY = metrics.heightPixels * 0.76f;
        float endY = metrics.heightPixels * 0.24f;

        performSwipe(x, startY, x, endY, onComplete);
    }

    void performSwipeRight(Runnable onComplete) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float y = metrics.heightPixels * 0.5f;
        float startX = metrics.widthPixels * 0.24f;
        float endX = metrics.widthPixels * 0.76f;

        performSwipe(startX, y, endX, y, onComplete);
    }

    void performSwipeLeft(Runnable onComplete) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float y = metrics.heightPixels * 0.5f;
        float startX = metrics.widthPixels * 0.76f;
        float endX = metrics.widthPixels * 0.24f;

        performSwipe(startX, y, endX, y, onComplete);
    }

    private void performSwipe(float startX, float startY, float endX, float endY, Runnable onComplete) {

        Path path = new Path();
        path.moveTo(startX, startY);
        path.lineTo(endX, endY);

        GestureDescription gesture = new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, 0, 450))
                .build();

        GestureResultCallback callback = new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                runCompletion(onComplete);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                runCompletion(onComplete);
            }
        };

        if (!dispatchGesture(gesture, callback, null)) {
            runCompletion(onComplete);
        }
    }

    private void runCompletion(Runnable onComplete) {
        if (onComplete != null) {
            onComplete.run();
        }
    }
}
