package com.dadi590.assist_c_a.VoiceInteraction;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.voice.AlwaysOnHotwordDetector;
import android.service.voice.AlwaysOnHotwordDetector.Callback;
import android.service.voice.AlwaysOnHotwordDetector.EventPayload;
import android.service.voice.VoiceInteractionService;
import android.service.voice.VoiceInteractionSession;

import androidx.annotation.RequiresApi;

import java.util.Locale;

@RequiresApi(Build.VERSION_CODES.L)
public class MainInteractionService extends VoiceInteractionService {
	static final String TAG = "MainInteractionService";

	private final Callback mHotwordCallback = new Callback() {
		@Override
		public void onAvailabilityChanged(int status) {
			//Log.ii(TAG, "onAvailabilityChanged(" + status + ")");
			hotwordAvailabilityChangeHelper(status);
		}

		@RequiresApi(Build.VERSION_CODES.M)
		@Override
		public void onDetected(EventPayload eventPayload) {
			//Log.ii(TAG, "onDetected");
			showSession(new Bundle(), VoiceInteractionSession.SHOW_WITH_ASSIST);
		}

		@Override
		public void onError() {
			//Log.ii(TAG, "onError");
		}

		@Override
		public void onRecognitionPaused() {
			//Log.ii(TAG, "onRecognitionPaused");
		}

		@Override
		public void onRecognitionResumed() {
			//Log.ii(TAG, "onRecognitionResumed");
		}
	};

	private AlwaysOnHotwordDetector mHotwordDetector;

	@Override
	public IBinder onBind(final Intent intent) {
		System.out.println("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");

		return super.onBind(intent);
	}

	@Override
	public void onReady() {
		super.onReady();
		System.out.println("NNNNNNNNNNNNNNNNNNNNNNNNNNN");
		//Log.ii(TAG, "Creating " + this);
		//Log.ii(TAG, "Keyphrase enrollment error? " + getKeyphraseEnrollmentInfo().getParseError());
		////Log.ii(TAG, "Keyphrase enrollment meta-data: " + getKeyphraseEnrollmentInfo().listKeyphraseMetadata().toArray().toString());

		mHotwordDetector = createAlwaysOnHotwordDetector("Ok Google", Locale.forLanguageTag("en-US"), mHotwordCallback);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		System.out.println("HHHHHHHHHHHHHHH");
		Intent args = new Intent(this, TestInteractionActivity.class);
		args.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(args);
		stopSelf(startId);
		return START_STICKY;
	}

	private void hotwordAvailabilityChangeHelper(int availability) {
		//Log.ii(TAG, "Hotword availability = " + availability);
		switch (availability) {
			case AlwaysOnHotwordDetector.STATE_HARDWARE_UNAVAILABLE:
				//Log.ii(TAG, "STATE_HARDWARE_UNAVAILABLE");
				break;
			case AlwaysOnHotwordDetector.STATE_KEYPHRASE_UNSUPPORTED:
				//Log.ii(TAG, "STATE_KEYPHRASE_UNSUPPORTED");
				break;
			case AlwaysOnHotwordDetector.STATE_KEYPHRASE_UNENROLLED:
				//Log.ii(TAG, "STATE_KEYPHRASE_UNENROLLED");
				Intent enroll = mHotwordDetector.createEnrollIntent();
				//Log.ii(TAG, "Need to enroll with " + enroll);
				break;
			case AlwaysOnHotwordDetector.STATE_KEYPHRASE_ENROLLED:
				//Log.ii(TAG, "STATE_KEYPHRASE_ENROLLED - starting recognition");
				if (mHotwordDetector.startRecognition(AlwaysOnHotwordDetector.RECOGNITION_FLAG_ALLOW_MULTIPLE_TRIGGERS)) {
					//Log.ii(TAG, "startRecognition succeeded");
				} else {
					//Log.ii(TAG, "startRecognition failed");
				}
				break;
		}
	}
}
