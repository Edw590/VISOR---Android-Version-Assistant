/*
 * Copyright 2021 DADi590
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.dadi590.assist_c_a.Modules.SpeechRecognition;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalUtils.GL_CONSTS;
import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Locale;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

/**
 * <p>This class activates PocketSphinx's speech recognition and automatically starts Google's if the assistant's name
 * is spoken.</p>
 * <p>NOTE: the class is public but it's NOT to be used outside its package! It's only public for the service to be
 * instantiated (meaning if it would be put package-private now, no error would appear on the entire project).</p>
 */
public class PocketSphinxRecognition extends Service implements RecognitionListener {

	@Nullable private SpeechRecognizer recognizer = null;

	private static final String KEYWORD_WAKEUP = "WAKEUP";

	private static final String KEYPHRASE = GL_CONSTS.ASSISTANT_NAME_WO_DOTS.toLowerCase(Locale.ENGLISH); // "visor"

	@Override
	public final int onStartCommand(@Nullable final Intent intent, final int flags, final int startId) {
        /*
		If the service was killed by its PID and the system restarted it, this might appear in the logs:

        Scheduling restart of crashed service com.dadi590.assist_c_a/.SpeechRecognizer.Google in 1000ms
        Scheduling restart of crashed service com.dadi590.assist_c_a/.SpeechRecognizer.PocketSphinx in 11000ms
        Start proc 1090:com.dadi590.assist_c_a:null/u0a95 for service com.dadi590.assist_c_a/.SpeechRecognizer.Google

        This below is supposed to fix that - if there's not EXTRA_TIME_START on the intent with a time that is 1 second
        or less ago relative to the current time, the service will be stopped immediately.
        */
		final boolean stop_now;
		if (intent != null && intent.hasExtra(CONSTS_SpeechRecog.EXTRA_TIME_START)) {
			// Must have been called 1 second ago at most - else it was the system restarting it or something.
			stop_now = intent.getLongExtra(CONSTS_SpeechRecog.EXTRA_TIME_START, 0L) + 1000L < System.currentTimeMillis();
		} else {
			stop_now = true;
		}
		if (stop_now) {
			System.out.println("2GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG2");
			stopSelf();
			UtilsSpeechRecognizers.terminateSpeechRecognizers();

			return START_NOT_STICKY;
		}

		final Intent intent1 = new Intent(CONSTS_BC_SpeechRecog.ACTION_POCKETSPHINX_RECOG_STARTED);
		UtilsApp.sendInternalBroadcast(intent1);

		new SetupTask_PocketSphinx(this).execute();


		// todo Try to disable logcat for PocketSphinx. It prints too much. Could make the app slower.


		System.out.println("RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");

		return START_NOT_STICKY;
	}

	/**
	 * <p>The AsyncTask that will take care of the recognition.</p>
	 */
	private static class SetupTask_PocketSphinx extends AsyncTask<Void, Void, Exception> {
		WeakReference<PocketSphinxRecognition> serviceReference;

		/**
		 * <p>Main class constructor.</p>
		 *
		 * @param serviceReference the service object ({@code this})
		 */
		SetupTask_PocketSphinx(final PocketSphinxRecognition serviceReference) {
			this.serviceReference = new WeakReference<>(serviceReference);
		}

		@Nullable
		@Override
		protected final Exception doInBackground(final Void... params) {
			try {
				final Assets assets = new Assets(serviceReference.get());
				final File assetDir = assets.syncAssets();
				serviceReference.get().setupRecognizer(assetDir);
			} catch (final IOException e) {
				return e;
			}
			return null;
		}
		@Override
		protected final void onPostExecute(final Exception e) {
			if (e == null) {
				serviceReference.get().switchSearch(KEYWORD_WAKEUP);
			}
		}
	}

	@Override
	public final void onDestroy() {
		super.onDestroy();

		stopRecognizer();
		stopSelf();
		UtilsSpeechRecognizers.terminateSpeechRecognizers();
	}

	/**
	 * In partial result we get quick updates about current hypothesis. In
	 * keyword spotting mode we can react here, in other modes we need to wait
	 * for final result in onResult.
	 */
	@Override
	public final void onPartialResult(@Nullable final Hypothesis hypothesis) {
		if (hypothesis == null) {
			return;
		}

		final String text = hypothesis.getHypstr();
		if (text.equals(KEYPHRASE)) {
			System.out.println("UUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU");
			stopRecognizer();
			stopSelf();
			UtilsSpeechRecognizers.startGoogleRecognition();
		}
	}

	/**
	 * This callback is called when we stop the recognizer.
	 */
	@Override
	public final void onResult(@Nullable final Hypothesis hypothesis) {
		if (hypothesis == null) {
			return;
		}

		final String text = hypothesis.getHypstr();
		if (text.equals(KEYPHRASE)) {
			System.out.println("PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP");
			stopRecognizer();
			stopSelf();
			UtilsSpeechRecognizers.startGoogleRecognition();
		}
	}

	@Override
	public void onBeginningOfSpeech() {
		// No need to implement.
	}

	/**
	 * We stop recognizer here to get a final result.
	 */
	@Override
	public final void onEndOfSpeech() {
		if (recognizer != null) { // It has arrived null here once
			final String search_name = recognizer.getSearchName();
			if (null == search_name) {
				switchSearch(KEYWORD_WAKEUP);
			} else {
				// And again NPE, but KWS_SEARCH can't be because it's declared as String before this. The recognizer is
				// not either, or it wouldn't get here. So it's the return of the getSearchName() sometimes.
				if (!KEYWORD_WAKEUP.equals(search_name)) {
					switchSearch(KEYWORD_WAKEUP);
				}
			}
		}
	}

	void switchSearch(@NonNull final String searchName) {
		assert recognizer != null; // It's never null when it gets here. It's just to remove the warning.
		recognizer.stop();

		// If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
		if (searchName.equals(KEYWORD_WAKEUP)) {
			recognizer.startListening(searchName);
		} else {
			recognizer.startListening(searchName, 10000);
		}
	}

	void setupRecognizer(@NonNull final File assetsDir) throws IOException {
		// The recognizer can be configured to perform multiple searches
		// of different kind and switch between them

		recognizer = SpeechRecognizerSetup.defaultSetup()
				.setAcousticModel(new File(assetsDir, "en-us-ptm"))
				.setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
				.setKeywordThreshold(1.0f) // Goes from 1e-45f to 1. Adjust for false positives. The greater, the
				// less false positives - though, more probability to fail to a true match.
				// If the Google Hotword recognizer could be put to work... Maybe it would be better (the normal
				// recognition is very good).
				// But this seems to be enough for now, I guess. 0.25f seems to be enough.

				.getRecognizer();

		recognizer.addListener(this);

		// Create keyword-activation search.
		recognizer.addKeyphraseSearch(KEYWORD_WAKEUP, KEYPHRASE);
	}

	@Override
	public final void onError(@Nullable final Exception e) {
		stopRecognizer();
		stopSelf();
		UtilsSpeechRecognizers.terminateSpeechRecognizers();
	}

	@Override
	public final void onTimeout() {
		switchSearch(KEYWORD_WAKEUP);
	}

	/**
	 * <p>Stops and destroys the {@link SpeechRecognizer} instance if it's not stopped and destroyed already.</p>
	 */
	final void stopRecognizer() {
		if (recognizer != null) {
			recognizer.cancel();
			recognizer.shutdown();
			recognizer = null;
		}
	}

	@Override
	@Nullable
	public final IBinder onBind(@Nullable final Intent intent) {
		return null;
	}
}
