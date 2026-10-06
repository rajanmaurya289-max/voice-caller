package com.example.voicecaller;

import android.service.voice.VoiceInteractionService;
import android.service.voice.VoiceInteractionSession;

public class VoiceAIService extends VoiceInteractionService {

    @Override
    public void onReady() {
        super.onReady();

        setDisabledShowContext(
            VoiceInteractionSession.SHOW_WITH_ASSIST
            | VoiceInteractionSession.SHOW_WITH_SCREENSHOT
        );
    }
}
