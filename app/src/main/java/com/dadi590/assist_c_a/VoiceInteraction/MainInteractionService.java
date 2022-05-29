/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.Arrays;
import java.util.Locale;

@RequiresApi(api = Build.VERSION_CODES.L)
public class MainInteractionService extends VoiceInteractionService {
	static final String TAG = "MainInteractionService";

	private final Callback mHotwordCallback = new Callback() {
		@Override
		public void onAvailabilityChanged(int status) {
			Log.i(TAG, "onAvailabilityChanged(" + status + ")");
			hotwordAvailabilityChangeHelper(status);
		}

		@Override
		public void onDetected(EventPayload eventPayload) {
			Log.i(TAG, "onDetected");
			showSession(new Bundle(), VoiceInteractionSession.SHOW_WITH_ASSIST);
		}

		@Override
		public void onError() {
			Log.i(TAG, "onError");
		}

		@Override
		public void onRecognitionPaused() {
			Log.i(TAG, "onRecognitionPaused");
		}

		@Override
		public void onRecognitionResumed() {
			Log.i(TAG, "onRecognitionResumed");
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
		Log.i(TAG, "Creating " + this);
		Log.i(TAG, "Keyphrase enrollment error? " + getKeyphraseEnrollmentInfo().getParseError());
		//Log.i(TAG, "Keyphrase enrollment meta-data: " + getKeyphraseEnrollmentInfo().listKeyphraseMetadata().toArray().toString());

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
		Log.i(TAG, "Hotword availability = " + availability);
		switch (availability) {
			case AlwaysOnHotwordDetector.STATE_HARDWARE_UNAVAILABLE:
				Log.i(TAG, "STATE_HARDWARE_UNAVAILABLE");
				break;
			case AlwaysOnHotwordDetector.STATE_KEYPHRASE_UNSUPPORTED:
				Log.i(TAG, "STATE_KEYPHRASE_UNSUPPORTED");
				break;
			case AlwaysOnHotwordDetector.STATE_KEYPHRASE_UNENROLLED:
				Log.i(TAG, "STATE_KEYPHRASE_UNENROLLED");
				Intent enroll = mHotwordDetector.createEnrollIntent();
				Log.i(TAG, "Need to enroll with " + enroll);
				break;
			case AlwaysOnHotwordDetector.STATE_KEYPHRASE_ENROLLED:
				Log.i(TAG, "STATE_KEYPHRASE_ENROLLED - starting recognition");
				if (mHotwordDetector.startRecognition(AlwaysOnHotwordDetector.RECOGNITION_FLAG_ALLOW_MULTIPLE_TRIGGERS)) {
					Log.i(TAG, "startRecognition succeeded");
				} else {
					Log.i(TAG, "startRecognition failed");
				}
				break;
		}
	}
}
