package ru.hartens.hdmiautostart;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String HDMI_3_URI =
        "content://android.media.tv/passthrough/com.mediatek.tvinput%2F.hdmi.HDMIInputService%2FHW6";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean wasStarted = false;
    private TextView status;
    private Runnable pendingLaunch;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(50, 30, 50, 30);
        panel.setBackgroundColor(Color.rgb(22, 27, 37));

        TextView heading = new TextView(this);
        heading.setText("HDMI AutoStart · HDMI 3");
        heading.setTextColor(Color.WHITE);
        heading.setTextSize(28);
        heading.setGravity(Gravity.CENTER);
        panel.addView(heading);

        status = new TextView(this);
        status.setTextColor(Color.LTGRAY);
        status.setTextSize(19);
        status.setGravity(Gravity.CENTER);
        status.setPadding(10, 22, 10, 22);
        panel.addView(status);

        Button hdmiButton = makeButton("Открыть HDMI 3");
        hdmiButton.setOnClickListener(v -> {
            cancelPending();
            openHdmi3();
        });
        panel.addView(hdmiButton);

        Button saluteButton = makeButton("Открыть Салют ТВ");
        saluteButton.setOnClickListener(v -> {
            cancelPending();
            openSalute();
        });
        panel.addView(saluteButton);

        Button settingButton = makeButton("Автопереход: переключить");
        settingButton.setOnClickListener(v -> {
            boolean next = !isAutoEnabled();
            getPreferences(MODE_PRIVATE).edit().putBoolean("auto_enabled", next).apply();
            cancelPending();
            status.setText(next ? "Автопереход включён. Нажмите HDMI 3 или Home." : "Автопереход отключён. Включить можно этой кнопкой.");
        });
        panel.addView(settingButton);

        setContentView(panel);
    }

    private Button makeButton(String caption) {
        Button button = new Button(this);
        button.setText(caption);
        button.setTextSize(19);
        button.setFocusable(true);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(480, 78);
        p.topMargin = 12;
        button.setLayoutParams(p);
        return button;
    }

    private boolean isAutoEnabled() {
        return getPreferences(MODE_PRIVATE).getBoolean("auto_enabled", true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isAutoEnabled()) {
            status.setText("Автопереход выключен. Выберите действие пультом.");
            return;
        }
        // HOME is launched in the foreground, avoiding Android 11 background-start restrictions.
        // A short delay allows the MediaTek TV input services to initialise after power-on.
        cancelPending();
        status.setText("Переключение на HDMI 3 через 6 секунд...");
        pendingLaunch = this::openHdmi3;
        handler.postDelayed(pendingLaunch, wasStarted ? 1500 : 6000);
        wasStarted = true;
    }

    private void cancelPending() {
        if (pendingLaunch != null) {
            handler.removeCallbacks(pendingLaunch);
            pendingLaunch = null;
        }
    }

    private void openHdmi3() {
        pendingLaunch = null;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse(HDMI_3_URI));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            status.setText("Отправлена команда HDMI 3");
        } catch (Exception e) {
            status.setText("Не удалось открыть HDMI 3: " + e.getClass().getSimpleName()
                + " · " + e.getMessage());
        }
    }

    private void openSalute() {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("ru.sberdevices.starlauncher",
                "ru.sberdevices.starlauncher.ui.mainscreen.MainActivity"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            status.setText("Не удалось открыть Салют ТВ: " + e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        cancelPending();
        super.onPause();
    }
}
