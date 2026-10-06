package com.example.voicecaller;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognitionService;
import android.speech.SpeechRecognizer;

public class SpeechBridgeService extends RecognitionService {

    private SpeechRecognizer engine;
    private Callback current;

    private interface Reply {
        void send(Callback callback) throws RemoteException;
    }

    private void reply(Callback expected, Reply response) {
        if (current != expected) return;

        try {
            response.send(expected);
        } catch (RemoteException e) {
            release();
        }
    }

    private void release() {
        current = null;

        if (engine != null) {
            engine.destroy();
            engine = null;
        }
    }

    @Override
    protected void onStartListening(
            Intent intent, Callback callback) {

        release();
        current = callback;

        ComponentName provider = SpeechProvider.find(this);

        if (provider == null) {
            reply(callback, c ->
                c.error(SpeechRecognizer.ERROR_CLIENT));
            release();
            return;
        }

        engine = SpeechRecognizer.createSpeechRecognizer(
            this, provider);

        engine.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle params) {
                reply(callback, c -> c.readyForSpeech(params));
            }

            @Override
            public void onBeginningOfSpeech() {
                reply(callback, c -> c.beginningOfSpeech());
            }

            @Override
            public void onRmsChanged(float rms) {
                reply(callback, c -> c.rmsChanged(rms));
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                reply(callback, c -> c.bufferReceived(buffer));
            }

            @Override
            public void onEndOfSpeech() {
                reply(callback, c -> c.endOfSpeech());
            }

            @Override
            public void onError(int error) {
                reply(callback, c -> c.error(error));
                if (current == callback) release();
            }

            @Override
            public void onResults(Bundle results) {
                reply(callback, c -> c.results(results));
                if (current == callback) release();
            }

            @Override
            public void onPartialResults(Bundle results) {
                reply(callback, c -> c.partialResults(results));
            }

            @Override
            public void onEvent(int type, Bundle params) {}
        });

        try {
            engine.startListening(new Intent(intent));
        } catch (RuntimeException e) {
            reply(callback, c ->
                c.error(SpeechRecognizer.ERROR_CLIENT));
            release();
        }
    }

    @Override
    protected void onStopListening(Callback callback) {
        if (current == callback && engine != null) {
            engine.stopListening();
        }
    }

    @Override
    protected void onCancel(Callback callback) {
        if (current == callback) release();
    }

    @Override
    public void onDestroy() {
        release();
        super.onDestroy();
    }
}
