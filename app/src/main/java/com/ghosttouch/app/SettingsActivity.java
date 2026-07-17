package com.ghosttouch.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    private CheckBox swipeEnabled;
    private EditText minSeconds;
    private EditText maxSeconds;
    private RadioGroup swipePattern;
    private RadioButton pattern1;
    private RadioButton pattern2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GhostTouchConfig config = GhostTouchConfig.load(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(28), dp(20), dp(28));
        root.setBackgroundColor(Color.rgb(16, 24, 32));

        TextView title = new TextView(this);
        title.setText("설정");
        title.setTextSize(28);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(title, matchWrap());

        swipeEnabled = new CheckBox(this);
        swipeEnabled.setText("넘기기 기능 사용");
        swipeEnabled.setTextSize(18);
        swipeEnabled.setTextColor(Color.WHITE);
        swipeEnabled.setChecked(config.swipeEnabled);
        root.addView(swipeEnabled, matchWrapWithTopMargin(22));

        TextView patternLabel = label("넘기기 패턴");
        root.addView(patternLabel, matchWrapWithTopMargin(18));

        swipePattern = new RadioGroup(this);
        swipePattern.setOrientation(LinearLayout.VERTICAL);
        pattern1 = patternOption("패턴 1: 아래로 넘기기 1회 (기존 동작)");
        pattern2 = patternOption("패턴 2: 오른쪽 → 왼쪽 → 아래 순서로 넘기기");
        swipePattern.addView(pattern1, matchWrap());
        swipePattern.addView(pattern2, matchWrap());
        if (config.swipePattern == GhostTouchConfig.SWIPE_PATTERN_2) {
            pattern2.setChecked(true);
        } else {
            pattern1.setChecked(true);
        }
        root.addView(swipePattern, matchWrapWithTopMargin(6));

        TextView rangeLabel = label("랜덤 실행 시간");
        root.addView(rangeLabel, matchWrapWithTopMargin(18));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        minSeconds = numberInput(String.valueOf(config.minSeconds));
        maxSeconds = numberInput(String.valueOf(config.maxSeconds));
        TextView between = label(" ~ ");
        TextView seconds = label(" 초");

        row.addView(minSeconds, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        row.addView(between, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        row.addView(maxSeconds, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        row.addView(seconds, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        root.addView(row, matchWrapWithTopMargin(8));

        Button save = new Button(this);
        save.setText("저장하고 닫기");
        save.setAllCaps(false);
        save.setTextColor(Color.WHITE);
        save.setBackgroundResource(R.drawable.bg_button);
        save.setOnClickListener(v -> saveAndClose());
        root.addView(save, matchWrapWithTopMargin(24));

        setContentView(root);
    }

    private void saveAndClose() {
        int min = readSeconds(minSeconds, 10);
        int max = readSeconds(maxSeconds, 30);

        if (min < 1) {
            Toast.makeText(this, "최소 시간은 1초 이상이어야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (max < min) {
            Toast.makeText(this, "최대 시간은 최소 시간보다 크거나 같아야 합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedPattern = pattern2.isChecked()
                ? GhostTouchConfig.SWIPE_PATTERN_2
                : GhostTouchConfig.SWIPE_PATTERN_1;
        GhostTouchConfig.save(this, swipeEnabled.isChecked(), min, max, selectedPattern);
        Toast.makeText(this, "설정을 저장했습니다.", Toast.LENGTH_SHORT).show();
        finish();
    }

    private int readSeconds(EditText editText, int fallback) {
        String value = editText.getText().toString().trim();
        if (value.isEmpty()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private TextView label(String value) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(16);
        view.setTextColor(Color.rgb(226, 232, 240));
        return view;
    }

    private EditText numberInput(String value) {
        EditText input = new EditText(this);
        input.setText(value);
        input.setSelectAllOnFocus(true);
        input.setTextSize(18);
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.rgb(182, 194, 201));
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        return input;
    }

    private RadioButton patternOption(String value) {
        RadioButton option = new RadioButton(this);
        option.setText(value);
        option.setTextSize(16);
        option.setTextColor(Color.WHITE);
        option.setPadding(0, dp(4), 0, dp(4));
        return option;
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
