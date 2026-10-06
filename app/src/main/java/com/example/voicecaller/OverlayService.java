package com.example.voicecaller;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class OverlayService extends Service {

    private static final String CHANNEL = "voice_ai_button";
    private static final String STOP = "stop_overlay";

    private final Handler handler =
        new Handler(Looper.getMainLooper());

    private WindowManager windows;
    private TextView bubble;
    private WindowManager.LayoutParams params;

    private boolean holding, moved;
    private long started;
    private float downX, downY;
    private int originX, originY, touchSlop;

    private final Runnable countdown = new Runnable() {
        @Override
        public void run() {
            if (!holding || moved || bubble == null) return;

            long elapsed = SystemClock.uptimeMillis() - started;

            if (elapsed >= 5000) {
                holding = false;
                bubble.setText("AI");
                openAssistant();
            } else {
                bubble.setText(String.valueOf(
                    (5000 - elapsed + 999) / 1000));
                handler.postDelayed(this, 100);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        windows = (WindowManager) getSystemService(WINDOW_SERVICE);
        touchSlop = ViewConfiguration.get(this)
            .getScaledTouchSlop();
    }

    private Intent assistantIntent() {
        return new Intent(this, MainActivity.class)
            .putExtra("auto_listen", true)
            .addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && STOP.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (!Settings.canDrawOverlays(this)) {
            stopSelf();
            return START_NOT_STICKY;
        }

        NotificationManager manager = (NotificationManager)
            getSystemService(NOTIFICATION_SERVICE);

        manager.createNotificationChannel(
            new NotificationChannel(
                CHANNEL,
                "Floating AI button",
                NotificationManager.IMPORTANCE_LOW));

        PendingIntent open = PendingIntent.getActivity(
            this, 0, assistantIntent(),
            PendingIntent.FLAG_UPDATE_CURRENT
                | PendingIntent.FLAG_IMMUTABLE);

        PendingIntent stop = PendingIntent.getService(
            this, 1,
            new Intent(this, OverlayService.class).setAction(STOP),
            PendingIntent.FLAG_UPDATE_CURRENT
                | PendingIntent.FLAG_IMMUTABLE);

        Notification notification =
            new Notification.Builder(this, CHANNEL)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setContentTitle("Voice AI ON")
                .setContentText(
                    "AI button 5 second hold karo. Yahan tap karke bhi bolo.")
                .setOngoing(true)
                .setContentIntent(open)
                .addAction(new Notification.Action.Builder(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "AI OFF", stop).build())
                .build();

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(
                1, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(1, notification);
        }

        if (bubble == null) addBubble();
        return START_NOT_STICKY;
