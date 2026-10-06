package com.example.voicecaller;

import android.content.Intent;
import android.os.Bundle;

public class AssistantActivity extends MainActivity {

    @Override
    public void onCreate(Bundle saved) {
        getIntent()
            .putExtra("assistant_mode", true)
            .putExtra("auto_listen", true);

        super.onCreate(saved);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        intent.putExtra("assistant_mode", true);
        intent.putExtra("auto_listen", true);

        super.onNewIntent(intent);
    }
}
