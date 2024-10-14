/*
 * Copyright 2021-2024 Edw590
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

// Note: this file was modified by me, Edw590, in 2023.

package edu.cmu.pocketsphinx1;

import android.Manifest;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.edw590.visor_c_a.GlobalUtils.UtilsAudio;
import com.edw590.visor_c_a.GlobalUtils.UtilsPermsAuths;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;
import edu.cmu.pocketsphinx.FsgModel;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;

/**
 * <p>Main class to access recognizer functions. After configuration this class
 * starts a listener thread which records the data and recognizes it using
 * Pocketsphinx engine. Recognition events are passed to a client using
 * {@link RecognitionListener}</p>
 * <br>
 * <p><strong>Attention:</strong> same hypothesis won't be sent 2+ times in a row! Even if
 * it's correctly detected the 2+ times, with minutes apart, for example!
 * This is because the Decoder keeps on sending the last hypothesis until it detects a
 * new one, not mattering if we send audio with nothing on it, even all 0s (tested).
 * Though, not detecting the same twice or more in a row is not a problem IN VISOR's CASE
 * because on the first correct detection, the recognition will be stopped. So no
 * problem in not detecting any more correct detections - it will be stopped on the 1st one
 * anyway.</p>
 * <p>The code for this is in end of the while loop on the run() method of the RecognizerThread.</p>
 *
 */
public class SpeechRecognizer {

	final Decoder decoder;

	private final int sampleRate;
	private static final float BUFFER_SIZE_SECONDS = 0.4f;
	int bufferSize;
	@Nullable
	AudioRecord recorder;

	public final int audio_source;

	@Nullable Thread recognizerThread;

	float gain = 3.0f;

	final Handler mainHandler;

	// Race condition. Calling cancel() or stop() interrupts the thread and sets it to null --> doesn't mean the thread
	// stops right away. It may take a bit to actually stop (next while() iteration) - but it's already set as
	// null and so it's "dead" and therefore supposedly decoder.endUtt() was already called --> no (or yes -
	// race condition). So decoder.setSearch() fails because it hasn't ended the utterance yet. This fixes all those
	// problems, by setting it in the beginning of the thread's run() method and resetting it in the end.
	int thread_state = 0;

	final Collection<RecognitionListener> listeners = new HashSet<RecognitionListener>();

	/**
	 * Creates speech recognizer. Recognizer holds the AudioRecord object, so you
	 * need to call {@link release} in order to properly finalize it.
	 *
	 * @param config The configuration object
	 * @throws IOException thrown if audio recorder can not be created for some reason.
	 */
	protected SpeechRecognizer(Config config, @NonNull final Handler main_handler) throws IOException {
		mainHandler = main_handler;

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT ||
				!UtilsPermsAuths.checkSelfPermission(Manifest.permission.CAPTURE_AUDIO_HOTWORD)) {
			audio_source = MediaRecorder.AudioSource.VOICE_RECOGNITION;
		} else {
			audio_source = MediaRecorder.AudioSource.HOTWORD;
		}

		System.out.println("VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV");
		System.out.println(audio_source);

		decoder = new Decoder(config);
		sampleRate = (int) decoder.getConfig().getFloat("-samprate");
		bufferSize = Math.round((float) sampleRate * BUFFER_SIZE_SECONDS);
		recorder = new AudioRecord(audio_source, sampleRate,
				AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSize << 1);

		if (recorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
			recorder.release();
			throw new IOException("Failed to initialize recorder. No permission to record audio.");
		}
	}

	/**
	 * Adds listener.
	 */
	public void addListener(RecognitionListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/**
	 * Removes listener.
	 */
	public void removeListener(RecognitionListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/**
	 * Starts recognition. Does nothing if recognition is active.
	 *
	 * @return true if recognition was started or was already active, false if it's still shutting down and was not
	 * started (try again)
	 */
	public boolean startListening(String searchName) {
		if (thread_state == 1) {
			return true;
		} else if (thread_state == 2) {
			return false;
		}

		//Log.ii(TAG, String.format("Start recognition \"%s\"", searchName));
		decoder.setSearch(searchName);
		recognizerThread = new RecognizerThread();
		recognizerThread.start();
		return true;
	}

	/**
	 * Starts recognition. After specified timeout listening stops and the
	 * endOfSpeech signals about that. Does nothing if recognition is active.
	 *
	 * @timeout - timeout in milliseconds to listen.
	 *
	 * @return true if recognition was started or was already active, false if it's still shutting down and was not
	 * started (try again)
	 */
	public boolean startListening(String searchName, int timeout) {
		if (thread_state == 1) {
			return true;
		} else if (thread_state == 2) {
			return false;
		}

		//Log.ii(TAG, String.format("Start recognition \"%s\"", searchName));
		decoder.setSearch(searchName);
		recognizerThread = new RecognizerThread(timeout);
		recognizerThread.start();
		return true;
	}

	private boolean stopRecognizerThread() {
		if (thread_state != 1) {
			return false;
		}

		thread_state = 2;
		recognizerThread.interrupt();
		recognizerThread = null;
		return true;
	}

	/**
	 * Stops recognition. All listeners should receive final result if there is
	 * any. Does nothing if recognition is not active.
	 *
	 * @return true if recognition was stopped, false if it was already stopped
	 */
	public boolean stop() {
		boolean result = stopRecognizerThread();
		if (result) {
			//Log.ii(TAG, "Stop recognition");
			final Hypothesis hypothesis = decoder.hyp();
			mainHandler.post(new ResultEvent(hypothesis, true));
		}
		return result;
	}

	/**
	 * Cancels recognition. Listeners do not receive final result. Does nothing
	 * if recognition is not active.
	 *
	 * @return true if recognition was canceled, false if it was already stopped
	 */
	public boolean cancel() {
		boolean result = stopRecognizerThread();
		if (result) {
			//Log.ii(TAG, "Cancel recognition");
		}

		return result;
	}

	/**
	 * Returns the decoder object for advanced operation (dictionary extension, utterance
	 * data collection, adaptation and so on).
	 *
	 * @return Decoder
	 */
	public Decoder getDecoder() {
		return decoder;
	}

	/**
	 * Shutdown the recognizer and release the recorder
	 */
	public void shutdown() {
		recorder.release();
	}

	/**
	 * Gets name of the currently active search.
	 *
	 * @return active search name or null if no search was started
	 */
	public String getSearchName() {
		return decoder.getSearch();
	}

	public void addFsgSearch(String searchName, FsgModel fsgModel) {
		decoder.setFsg(searchName, fsgModel);
	}

	/**
	 * Adds searches based on JSpeech grammar file.
	 *
	 * @param name
	 *            search name
	 * @param file
	 *            JSGF file
	 */
	public void addGrammarSearch(String name, File file) {
		//Log.ii(TAG, String.format("Load JSGF %s", file));
		decoder.setJsgfFile(name, file.getPath());
	}

	/**
	 * Adds searches based on JSpeech grammar string.
	 *
	 * @param name
	 *            search name
	 * @param file
	 *            JSGF string
	 */
	public void addGrammarSearch(String name, String jsgfString) {
		decoder.setJsgfString(name, jsgfString);
	}

	/**
	 * Adds search based on N-gram language model.
	 *
	 * @param name
	 *            search name
	 * @param file
	 *            N-gram model file
	 */
	public void addNgramSearch(String name, File file) {
		//Log.ii(TAG, String.format("Load N-gram model %s", file));
		decoder.setLmFile(name, file.getPath());
	}

	/**
	 * Adds search based on a single phrase.
	 *
	 * @param name
	 *            search name
	 * @param phrase
	 *            search phrase
	 */
	public void addKeyphraseSearch(String name, String phrase) {
		decoder.setKeyphrase(name, phrase);
	}

	/**
	 * Adds search based on a keyphrase file.
	 *
	 * @param name
	 *            search name
	 * @param phrase
	 *            a file with search phrases, one phrase per line with optional weight in the end, for example
	 *            <br/>
	 *            <code>
	 *            oh mighty computer /1e-20/
	 *            how do you do /1e-10/
	 *            </code>
	 */
	public void addKeywordSearch(String name, File file) {
		decoder.setKws(name, file.getPath());
	}

	/**
	 * Adds a search to look for the phonemes
	 *
	 * @param name
	 *          search name
	 * @param phonetic bigram model
	 *
	 */
	public void addAllphoneSearch(String name, File file) {
		decoder.setAllphoneFile(name, file.getPath());
	}

	private final class RecognizerThread extends Thread {

		private int remainingSamples;
		private int timeoutSamples;
		private final static int NO_TIMEOUT = -1;

		public RecognizerThread(int timeout) {
			if (timeout != NO_TIMEOUT)
				this.timeoutSamples = timeout * sampleRate / 1000;
			else
				this.timeoutSamples = NO_TIMEOUT;
			this.remainingSamples = this.timeoutSamples;
		}

		public RecognizerThread() {
			this(NO_TIMEOUT);
		}

		@Override
		public void run() {

			thread_state = 1;

			recorder.startRecording();
			if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
				final IOException ioe = new IOException(
						"Failed to start recording. Microphone might be already in use.");
				mainHandler.post(new OnErrorEvent(ioe));

				recognizerThread = null;

				thread_state = 0;
				return;
			}

			//Log.id(TAG, "Starting decoding");

			decoder.startUtt();
			final short[] buffer = new short[bufferSize];
			boolean inSpeech = decoder.getInSpeech();
			String last_hypothesis_str = "";

			// Skip the first buffer, usually zeroes
			recorder.read(buffer, 0, buffer.length);

			while (!interrupted() && ((timeoutSamples == NO_TIMEOUT) || (remainingSamples > 0))) {
				final int nread = recorder.read(buffer, 0, bufferSize);
				UtilsAudio.adjustGainBuffer(buffer, gain);

				if (nread < 0) {
					mainHandler.post(new OnErrorEvent(new RuntimeException("error reading audio buffer, nread = " + nread)));

					break; // If an error occurred, leave - else shouldn't throw an error, just some warning
				} else if (nread > 0) {
					decoder.processRaw(buffer, (long) nread, false, false);

					// int max = 0;
					// for (int i = 0; i < nread; i++) {
					//     max = Math.max(max, Math.abs(buffer[i]));
					// }
					// //Log.ie("!!!!!!!!", "Level: " + max);

					if (decoder.getInSpeech() != inSpeech) {
						inSpeech = decoder.getInSpeech();
						mainHandler.post(new InSpeechChangeEvent(inSpeech));
					}

					if (inSpeech)
						remainingSamples = timeoutSamples;

					final Hypothesis hypothesis = decoder.hyp();
					if (hypothesis != null) {
						final String hypothesis_str = hypothesis.getHypstr();
						if (!last_hypothesis_str.equals(hypothesis_str)) {

							// WARNING: this means that the same hypothesis won't be sent 2+ times in a row! Even if
							// it's correctly detected the 2+ times, with minutes apart, for example!
							// This is here because the Decoder keeps on sending the last hypothesis until it detects a
							// new one, not mattering if we send audio with nothing on it, even all 0s (tested).
							// Though, not detecting the same twice or more in a row is not a problem IN VISOR's CASE
							// because on the first correct detection, the recognition will be stopped. So no problem in
							// not detecting any more correct detections - it will be stopped on the 1st one anyway.

							mainHandler.post(new ResultEvent(hypothesis, false));
							last_hypothesis_str = hypothesis_str;
						}
					}
				}

				if (timeoutSamples != NO_TIMEOUT) {
					remainingSamples -= nread;
				}
			}

			try {
				recorder.stop();
			} catch (final IllegalStateException ignored) {
			}
			decoder.endUtt();

			// Remove all pending notifications.
			mainHandler.removeCallbacksAndMessages(null);

			// If we met timeout signal that speech ended
			if (timeoutSamples != NO_TIMEOUT && remainingSamples <= 0) {
				mainHandler.post(new TimeoutEvent());
			}

			thread_state = 0;
		}
	}

	private abstract class RecognitionEvent implements Runnable {
		public void run() {
			RecognitionListener[] emptyArray = new RecognitionListener[0];
			for (RecognitionListener listener : listeners.toArray(emptyArray))
				execute(listener);
		}

		protected abstract void execute(RecognitionListener listener);
	}

	private class InSpeechChangeEvent extends RecognitionEvent {
		private final boolean state;

		InSpeechChangeEvent(boolean state) {
			this.state = state;
		}

		@Override
		protected void execute(RecognitionListener listener) {
			if (state)
				listener.onBeginningOfSpeech();
			else
				listener.onEndOfSpeech();
		}
	}

	private class ResultEvent extends RecognitionEvent {
		protected final Hypothesis hypothesis;
		private final boolean finalResult;

		ResultEvent(Hypothesis hypothesis, boolean finalResult) {
			this.hypothesis = hypothesis;
			this.finalResult = finalResult;
		}

		@Override
		protected void execute(RecognitionListener listener) {
			if (finalResult)
				listener.onResult(hypothesis);
			else
				listener.onPartialResult(hypothesis);
		}
	}

	private class OnErrorEvent extends RecognitionEvent {
		private final Exception exception;

		OnErrorEvent(Exception exception) {
			this.exception = exception;
		}

		@Override
		protected void execute(RecognitionListener listener) {
			listener.onError(exception);
		}
	}

	private class TimeoutEvent extends RecognitionEvent {
		@Override
		protected void execute(RecognitionListener listener) {
			listener.onTimeout();
		}
	}
}
