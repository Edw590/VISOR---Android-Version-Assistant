package com.dadi590.assist_c_a.VoiceInteraction;

import android.content.Intent;
import android.os.Build;
import android.speech.RecognitionService;

import androidx.annotation.RequiresApi;

/**
 * Stub recognition service needed to be a complete voice interactor.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class MainRecognitionService extends RecognitionService {

    private static final String TAG = "MainRecognitionService";

    @Override
    public void onCreate() {
        super.onCreate();
        //Log.ii(TAG, "onCreate");
    }

    @Override
    protected void onStartListening(Intent recognizerIntent, Callback listener) {
        //Log.id(TAG, "onStartListening");
    }

    @Override
    protected void onCancel(Callback listener) {
        //Log.id(TAG, "onCancel");
    }

    @Override
    protected void onStopListening(Callback listener) {
        //Log.id(TAG, "onStopListening");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.id(TAG, "onDestroy");
    }
}
