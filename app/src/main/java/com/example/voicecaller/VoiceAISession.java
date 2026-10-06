package com.example.voicecaller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.service.voice.VoiceInteractionSession;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class VoiceAISession extends VoiceInteractionSession {

    private final Handler handler =
            new Handler(Looper.getMainLooper());

    private TextView message;

    public VoiceAISession(Context context) {
        super(context);
    }

    @Override
    public View onCreateContentView() {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        message = new TextView(getContext());
        message.setText("Voice AI khul raha hai...");
        layout.addView(message);

        Button retry = new Button(getContext());
        retry.setText("Bolo");
        layout.addView(retry);
        retry.setOnClickListener(v -> openPanel());

        Button close = new Button(getContext());
        close.setText("Band karo");
        layout.addView(close);
        close.setOnClickListener(v -> hide());

        return layout;
    }

    @Override
    public void onShow(Bundle args, int flags) {
        super.onShow(args, flags);
        handler.post(this::openPanel);
    }

    private void openPanel() {
        Intent intent = new Intent(
                getContext(), AssistantActivity.class);

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        try {
            startAssistantActivity(intent);
            hide();
        } catch (RuntimeException e) {
            if (message != null) {
                message.setText(
                    "Voice AI app kholkar permissions allow karo, "
                    + "phir dobara try karo.");
            }
        }
    }

    @Override
    public void onHide() {
        handler.removeCallbacksAndMessages(null);
        super.onHide();
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
