package com.ghosttouch.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private TextView overlayStatus;
    private TextView accessibilityStatus;
    private TextView featureStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.rgb(16, 24, 32));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(28), dp(20), dp(28));
        scrollView.addView(root);

        TextView title = text("고스트터치", 30, Color.WHITE);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(title, matchWrap());

        TextView subtitle = text("다른 앱 위에 플로팅 UI를 띄우고, 설정한 랜덤 시간마다 넘기기 제스처를 실행합니다.", 15, Color.rgb(182, 194, 201));
        subtitle.setPadding(0, dp(8), 0, dp(20));
        root.addView(subtitle, matchWrap());

        overlayStatus = statusText();
        accessibilityStatus = statusText();
        featureStatus = statusText();
        root.addView(overlayStatus, matchWrap());
        root.addView(accessibilityStatus, matchWrap());
        root.addView(featureStatus, matchWrap());

        root.addView(button("오버레이 권한 열기", v -> openOverlaySettings()), matchWrapWithTopMargin(14));
        root.addView(button("접근성 설정 열기", v -> openAccessibilitySettings()), matchWrapWithTopMargin(10));
        root.addView(button("고스트터치 UI 띄우기", v -> startOverlay()), matchWrapWithTopMargin(10));
        root.addView(button("넘기기 설정", v -> startActivity(new Intent(this, SettingsActivity.class))), matchWrapWithTopMargin(10));

        setContentView(scrollView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    private void updateStatus() {
        GhostTouchConfig config = GhostTouchConfig.load(this);
        overlayStatus.setText("오버레이 권한: " + (Settings.canDrawOverlays(this) ? "허용됨" : "필요함"));
        accessibilityStatus.setText("접근성 서비스: " + (GhostAccessibilityService.isReady() ? "켜짐" : "꺼짐"));
        String patternName;
        if (config.swipePattern == GhostTouchConfig.SWIPE_PATTERN_3) {
            patternName = "패턴 3";
        } else if (config.swipePattern == GhostTouchConfig.SWIPE_PATTERN_2) {
            patternName = "패턴 2";
        } else {
            patternName = "패턴 1";
        }
        featureStatus.setText("넘기기: " + (config.swipeEnabled
                ? "사용, " + patternName + ", " + config.minSeconds + "~" + config.maxSeconds + "초"
                : "꺼짐"));
    }

    private void openOverlaySettings() {
        Intent intent = new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName())
        );
        startActivity(intent);
    }

    private void openAccessibilitySettings() {
        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }

    private void startOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "먼저 오버레이 권한을 허용해주세요.", Toast.LENGTH_SHORT).show();
            openOverlaySettings();
            return;
        }
        startService(new Intent(this, GhostOverlayService.class));
        Toast.makeText(this, "다른 앱 위에 고스트터치 UI를 띄웠습니다.", Toast.LENGTH_SHORT).show();
    }

    private TextView text(String value, int sp, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setLineSpacing(0, 1.12f);
        return view;
    }

    private TextView statusText() {
        TextView view = text("", 16, Color.WHITE);
        view.setPadding(0, dp(6), 0, dp(6));
        return view;
    }

    private Button button(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextColor(Color.WHITE);
        button.setBackgroundResource(R.drawable.bg_button_dark);
        button.setOnClickListener(listener);
        return button;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private LinearLayout.LayoutParams matchWrapWithTopMargin(int topDp) {
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(topDp);
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
