package com.dadi590.assist_c_a.Modules.Speech;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;

/**
 * <p>An instantiated class to serve as a multi-type array for the {@link TextToSpeech} speak() methods.</p>
 */
class TtsParamsObj {

	final Bundle bundle = new Bundle();
	final HashMap<String, String> hashmap = new HashMap<>(3);
}
