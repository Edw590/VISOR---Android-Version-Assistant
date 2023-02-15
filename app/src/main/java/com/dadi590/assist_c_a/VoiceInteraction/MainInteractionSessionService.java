package com.dadi590.assist_c_a.VoiceInteraction;

import android.os.Build;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.service.voice.VoiceInteractionSessionService;

import androidx.annotation.RequiresApi;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class MainInteractionSessionService extends VoiceInteractionSessionService {
    @Override
    public VoiceInteractionSession onNewSession(Bundle args) {
        System.out.println("RRRRRRRRRRRRRRRRRRRRRRRRRRRR");
        return new MainInteractionSession(this);
    }
}
