package com.ghosttouch.app;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
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
    private Button runStopButton;
    private Button closeButton;
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
            overlayView = null;
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

        Button settingsButton = overlayButton("설정", Color.rgb(44, 58, 68));
        Button moveButton = overlayButton("이동", Color.rgb(73, 88, 104));
        runStopButton = overlayButton("실행", Color.rgb(0, 137, 123));
        closeButton = overlayButton("끄기", Color.rgb(72, 78, 86));

        settingsButton.setOnClickListener(v -> {
            showButtonFeedback(settingsButton);
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        moveButton.setContentDescription("고스트터치 UI 이동");
        moveButton.setOnTouchListener(this::dragOverlay);

        runStopButton.setOnClickListener(v -> {
            if (MacroScheduler.isRunning()) {
                MacroScheduler.stop();
                setRunningUi(false);
                Toast.makeText(this, "넘기기 중지됨", Toast.LENGTH_SHORT).show();
                return;
            }

            String error = MacroScheduler.start(this);
            if (error == null) {
                setRunningUi(true);
                Toast.makeText(this, "넘기기 실행 중", Toast.LENGTH_SHORT).show();
            } else {
                showButtonFeedback(runStopButton);
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        closeButton.setOnClickListener(v -> {
            showButtonFeedback(closeButton);
            MacroScheduler.stop();
            Toast.makeText(this, "고스트터치 UI를 닫았습니다.", Toast.LENGTH_SHORT).show();
            stopSelf();
        });

        panel.addView(settingsButton, buttonLayout());
        panel.addView(runStopButton, buttonLayout());
        panel.addView(moveButton, buttonLayout());
        panel.addView(closeButton, buttonLayout());

        setRunningUi(MacroScheduler.isRunning());
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
        layoutParams.x = dp(12);
        layoutParams.y = dp(96);
        return layoutParams;
    }

    private boolean dragOverlay(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                view.setPressed(true);
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
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                view.setPressed(false);
                return true;
            default:
                return false;
        }
    }

    private Button overlayButton(String text, int normalColor) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(13);
        button.setTextColor(Color.WHITE);
        button.setMinWidth(dp(54));
        button.setMinHeight(dp(42));
        button.setPadding(dp(8), 0, dp(8), 0);
        button.setBackground(buttonBackground(normalColor));
        return button;
    }

    private void setRunningUi(boolean running) {
        if (runStopButton == null) {
            return;
        }

        if (running) {
            runStopButton.setText("중지");
            runStopButton.setBackground(buttonBackground(Color.rgb(158, 56, 56)));
        } else {
            runStopButton.setText("실행");
            runStopButton.setBackground(buttonBackground(Color.rgb(0, 137, 123)));
        }
    }

    private void showButtonFeedback(Button button) {
        button.setPressed(true);
        button.postDelayed(() -> button.setPressed(false), 140);
    }

    private StateListDrawable buttonBackground(int normalColor) {
        StateListDrawable states = new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed}, roundedDrawable(lighten(normalColor), Color.WHITE));
        states.addState(new int[]{android.R.attr.state_focused}, roundedDrawable(lighten(normalColor), Color.WHITE));
        states.addState(new int[]{}, roundedDrawable(normalColor, Color.rgb(113, 130, 140)));
        return states;
    }

    private GradientDrawable roundedDrawable(int fillColor, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setStroke(dp(1), strokeColor);
        drawable.setCornerRadius(dp(8));
        return drawable;
    }

    private int lighten(int color) {
        int red = Math.min(255, Color.red(color) + 44);
        int green = Math.min(255, Color.green(color) + 44);
        int blue = Math.min(255, Color.blue(color) + 44);
        return Color.rgb(red, green, blue);
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
