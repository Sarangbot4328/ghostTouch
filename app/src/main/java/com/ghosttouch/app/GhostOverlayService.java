package com.ghosttouch.app;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class GhostOverlayService extends Service {
    private WindowManager windowManager;
    private View overlayView;
    private WindowManager.LayoutParams params;
    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!Settings.canDrawOverlays(this)) {
            stopSelf();
            return;
        }
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlayView = createOverlayView();
        params = createLayoutParams();
        windowManager.addView(overlayView, params);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        MacroScheduler.stop();
        if (windowManager != null && overlayView != null) {
            windowManager.removeView(overlayView);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private View createOverlayView() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.HORIZONTAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(dp(8), dp(8), dp(8), dp(8));

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(30, 42, 50));
        background.setStroke(dp(1), Color.rgb(82, 99, 110));
        background.setCornerRadius(dp(8));
        panel.setBackground(background);

        Button settings = overlayButton("설정");
        Button run = overlayButton("실행");
        Button stop = overlayButton("중지");

        settings.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
        run.setOnClickListener(v -> {
            String error = MacroScheduler.start(this);
            Toast.makeText(this, error == null ? "넘기기를 시작했습니다." : error, Toast.LENGTH_SHORT).show();
        });
        stop.setOnClickListener(v -> {
            MacroScheduler.stop();
            Toast.makeText(this, "넘기기를 중지했습니다.", Toast.LENGTH_SHORT).show();
        });

        panel.addView(settings, buttonLayout());
        panel.addView(run, buttonLayout());
        panel.addView(stop, buttonLayout());

        panel.setOnTouchListener(this::dragOverlay);
        return panel;
    }

    private WindowManager.LayoutParams createLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        layoutParams.gravity = Gravity.TOP | Gravity.START;
        layoutParams.x = dp(16);
        layoutParams.y = dp(96);
        return layoutParams;
    }

    private boolean dragOverlay(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = params.x;
                initialY = params.y;
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                return true;
            case MotionEvent.ACTION_MOVE:
                params.x = initialX + Math.round(event.getRawX() - initialTouchX);
                params.y = initialY + Math.round(event.getRawY() - initialTouchY);
                windowManager.updateViewLayout(overlayView, params);
                return true;
            default:
                return false;
        }
    }

    private Button overlayButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(13);
        button.setTextColor(Color.WHITE);
        button.setMinWidth(dp(58));
        button.setMinHeight(dp(42));
        button.setBackgroundResource(R.drawable.bg_button_dark);
        return button;
    }

    private LinearLayout.LayoutParams buttonLayout() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.leftMargin = dp(3);
        layoutParams.rightMargin = dp(3);
        return layoutParams;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
