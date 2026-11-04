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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String HDMI_3_URI = "content://android.media.tv/passthrough/com.mediatek.tvinput%2F.hdmi.HDMIInputService%2FHW6";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingLaunch;
    private TextView status;
    private LinearLayout controls;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(5894 | 1024 | 512);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        ImageView splash = new ImageView(this);
        splash.setImageResource(R.drawable.splash);
        splash.setScaleType(ImageView.ScaleType.FIT_CENTER);
        root.addView(splash, new FrameLayout.LayoutParams(-1, -1));

        controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.VERTICAL);
        controls.setGravity(Gravity.CENTER);
        controls.setPadding(20, 20, 20, 20);
        controls.setBackgroundColor(0xE6000000);
        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM);
        status = new TextView(this);
        status.setTextColor(Color.WHITE);
        status.setTextSize(18);
        status.setGravity(Gravity.CENTER);
        controls.addView(status);
        Button hdmi = makeButton("Открыть HDMI 3");
        hdmi.setOnClickListener(v -> { cancelPending(); openHdmi3(); });
        controls.addView(hdmi);
        Button salute = makeButton("Открыть Салют ТВ");
        salute.setOnClickListener(v -> { cancelPending(); openSalute(); });
        controls.addView(salute);
        Button toggle = makeButton("Включить / выключить автопереход");
        toggle.setOnClickListener(v -> {
            boolean enabled = !isAutoEnabled();
            getPreferences(MODE_PRIVATE).edit().putBoolean("auto_enabled", enabled).apply();
            cancelPending();
            status.setText(enabled ? "Автопереход включён. Нажмите HOME для запуска." : "Автопереход отключён.");
        });
        controls.addView(toggle);
        root.addView(controls, cp);
        setContentView(root);
    }

    private Button makeButton(String title) {
        Button b = new Button(this);
        b.setText(title);
        b.setFocusable(true);
        controlsLayout(b);
        return b;
    }
    private void controlsLayout(Button b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = 4;
        b.setLayoutParams(p);
    }
    private boolean isAutoEnabled() {
        return getPreferences(MODE_PRIVATE).getBoolean("auto_enabled", true);
    }
    @Override protected void onResume() {
        super.onResume();
        cancelPending();
        if (!isAutoEnabled()) {
            controls.setVisibility(View.VISIBLE);
            status.setText("Автопереход выключен. Выберите действие.");
            return;
        }
        controls.setVisibility(View.GONE);
        // 6 seconds on every HOME launch: give HDMI hardware time to initialise after power-on.
        pendingLaunch = this::openHdmi3;
        handler.postDelayed(pendingLaunch, 6000);
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
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(HDMI_3_URI));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (Exception e) {
            controls.setVisibility(View.VISIBLE);
            status.setText("Ошибка HDMI 3: " + e.getMessage());
        }
    }
    private void openSalute() {
        try {
            Intent i = new Intent();
            i.setComponent(new ComponentName("ru.sberdevices.starlauncher", "ru.sberdevices.starlauncher.ui.mainscreen.MainActivity"));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (Exception e) { status.setText("Ошибка Салют ТВ: " + e.getMessage()); }
    }
    @Override protected void onPause() { cancelPending(); super.onPause(); }
}
