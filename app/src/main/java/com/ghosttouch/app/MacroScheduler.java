package com.ghosttouch.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.Random;

final class MacroScheduler {
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private static final Random RANDOM = new Random();

    private static boolean running;
    private static Runnable scheduledTask;

    private MacroScheduler() {
    }

    static synchronized boolean isRunning() {
        return running;
    }

    static synchronized String start(Context context) {
        GhostTouchConfig config = GhostTouchConfig.load(context);
        if (!config.swipeEnabled) {
            return "설정에서 넘기기 기능을 체크해주세요.";
        }
        if (!GhostAccessibilityService.isReady()) {
            return "접근성 설정에서 고스트터치 제스처 서비스를 켜주세요.";
        }

        stopLocked();
        running = true;
        scheduleNext(context.getApplicationContext());
        return null;
    }

    static synchronized void stop() {
        stopLocked();
    }

    private static void stopLocked() {
        running = false;
        if (scheduledTask != null) {
            HANDLER.removeCallbacks(scheduledTask);
            scheduledTask = null;
        }
    }

    private static void scheduleNext(Context appContext) {
        GhostTouchConfig config = GhostTouchConfig.load(appContext);
        int range = Math.max(0, config.maxSeconds - config.minSeconds);
        int nextSeconds = config.minSeconds + (range == 0 ? 0 : RANDOM.nextInt(range + 1));

        scheduledTask = () -> {
            synchronized (MacroScheduler.class) {
                if (!running) {
                    return;
                }
            }

            GhostAccessibilityService service = GhostAccessibilityService.getInstance();
            if (service == null) {
                synchronized (MacroScheduler.class) {
                    stopLocked();
                }
                return;
            }

            service.performSwipeUp();

            synchronized (MacroScheduler.class) {
                if (running) {
                    scheduleNext(appContext);
                }
            }
        };
        HANDLER.postDelayed(scheduledTask, nextSeconds * 1000L);
    }
}
