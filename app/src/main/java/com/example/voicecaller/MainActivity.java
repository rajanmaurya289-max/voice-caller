package com.example.voicecaller;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.role.RoleManager;
import android.content.ComponentName;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.service.voice.VoiceInteractionService;
import android.view.View;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity
        implements RecognitionListener {

    private static final int VOICE_PERMISSION = 10;
    private static final int CALL_PERMISSION = 11;

    private final ExecutorService worker =
            Executors.newSingleThreadExecutor();
    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    private SpeechRecognizer recognizer;
    private TextView status, assistantState;
    private Button speak, search;
    private EditText typedName;
    private CheckBox direct;
    private Spinner language;

    private boolean listening, active;
    private boolean autoListen, waitingOverlay;
    private int lookupGeneration;
    private String pendingNumber;

    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);

        autoListen = getIntent()
                .getBooleanExtra("auto_listen", false);
        boolean assistantMode = getIntent()
                .getBooleanExtra("assistant_mode", false);

        ScrollView scroll = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        int pad = (int) (24
                * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            layout.setPadding(
                pad + insets.getSystemWindowInsetLeft(),
                pad + insets.getSystemWindowInsetTop(),
                pad + insets.getSystemWindowInsetRight(),
                pad + insets.getSystemWindowInsetBottom());
            return insets;
        });
        scroll.addView(layout);

        TextView title = new TextView(this);
        title.setText("Voice AI");
        title.setTextSize(30);
        layout.addView(title);

        TextView help = new TextView(this);
        help.setText(assistantMode
            ? "Saved contact ka naam bolo: Rajan ko call karo."
            : "Pehle permissions allow karo, phir default assistant banao."
              + "\nPhone settings mein power button ke liye Digital assistant chuno."
              + "\nPhone unlocked rakho. Speech ke liye internet lag sakta hai."
              + "\nCustom wake word aur AI chat abhi add nahi hue hain.");
        layout.addView(help);

        assistantState = new TextView(this);
        layout.addView(assistantState);

        Button permissions = new Button(this);
        permissions.setText("1. Permissions allow karo");
        layout.addView(permissions);
        permissions.setOnClickListener(v ->
            requestPermissions(new String[]{
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CALL_PHONE
            }, 14));

        Button chooseAssistant = new Button(this);
        chooseAssistant.setText("2. Default assistant banao");
        layout.addView(chooseAssistant);
        chooseAssistant.setOnClickListener(v ->
                chooseDefaultAssistant());

        Button shortcut = new Button(this);
        shortcut.setText("3. Power-button setting kholo");
        layout.addView(shortcut);
        shortcut.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            } catch (ActivityNotFoundException e) {
                status.setText(
                    "Phone Settings mein Power button search karo.");
            }
        });

        if (assistantMode) {
            assistantState.setVisibility(View.GONE);
            permissions.setVisibility(View.GONE);
            chooseAssistant.setVisibility(View.GONE);
            shortcut.setVisibility(View.GONE);
        }

        language = new Spinner(this);
        language.setAdapter(new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            new String[]{"English (India)", "Hindi (India)"}));
        layout.addView
