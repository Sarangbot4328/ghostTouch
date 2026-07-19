package com.ghosttouch.app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.Random;

final class MacroScheduler {
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private static final Random RANDOM = new Random();
    private static final int PATTERN4_BREAK_SECONDS = 30;

    private static boolean running;
    private static Runnable scheduledTask;
    private static int actionsSinceBreak;
    private static int breakAfter;

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
        resetPattern4BreakState();
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
        actionsSinceBreak = 0;
        breakAfter = 0;
    }

    private static void resetPattern4BreakState() {
        actionsSinceBreak = 0;
        breakAfter = nextBreakAfter();
    }

    private static int nextBreakAfter() {
        // 4회 또는 5회 중 랜덤
        return 4 + RANDOM.nextInt(2);
    }

    private static void scheduleNext(Context appContext) {
        GhostTouchConfig config = GhostTouchConfig.load(appContext);
        int range = Math.max(0, config.maxSeconds - config.minSeconds);
        int nextSeconds = config.minSeconds + (range == 0 ? 0 : RANDOM.nextInt(range + 1));

        if (config.swipePattern == GhostTouchConfig.SWIPE_PATTERN_4
                && actionsSinceBreak >= breakAfter
                && breakAfter > 0) {
            nextSeconds += PATTERN4_BREAK_SECONDS;
            actionsSinceBreak = 0;
            breakAfter = nextBreakAfter();
        }

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

            GhostTouchConfig latestConfig = GhostTouchConfig.load(appContext);
            int patternToRun = resolvePattern(latestConfig.swipePattern);
            performPattern(service, patternToRun, appContext, latestConfig.swipePattern);
        };
        HANDLER.postDelayed(scheduledTask, nextSeconds * 1000L);
    }

    private static int resolvePattern(int swipePattern) {
        if (swipePattern == GhostTouchConfig.SWIPE_PATTERN_4) {
            return 1 + RANDOM.nextInt(3);
        }
        return swipePattern;
    }

    private static void performPattern(
            GhostAccessibilityService service,
            int swipePattern,
            Context appContext,
            int configuredPattern
    ) {
        if (swipePattern == GhostTouchConfig.SWIPE_PATTERN_3) {
            service.performStrongSwipeUp(() -> {
                if (!isRunning()) {
                    return;
                }
                service.performStrongSwipeUp(() -> scheduleAfterPattern(appContext, configuredPattern));
            });
            return;
        }

        if (swipePattern == GhostTouchConfig.SWIPE_PATTERN_2) {
            service.performSwipeRight(() -> {
                if (!isRunning()) {
                    return;
                }
                service.performSwipeLeft(() -> {
                    if (!isRunning()) {
                        return;
                    }
                    service.performSwipeUp(() -> scheduleAfterPattern(appContext, configuredPattern));
                });
            });
            return;
        }

        service.performSwipeUp(() -> scheduleAfterPattern(appContext, configuredPattern));
    }

    private static synchronized void scheduleAfterPattern(Context appContext, int configuredPattern) {
        if (!running) {
            return;
        }
        if (configuredPattern == GhostTouchConfig.SWIPE_PATTERN_4) {
            actionsSinceBreak++;
        }
        scheduleNext(appContext);
    }
}
