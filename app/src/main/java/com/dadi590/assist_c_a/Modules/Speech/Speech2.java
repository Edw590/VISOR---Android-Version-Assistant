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

package com.dadi590.assist_c_a.Modules.Speech;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalInterfaces.IModule;
import com.dadi590.assist_c_a.GlobalUtils.GL_CONSTS;
import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

/**
 * <p>The 2nd speech module of the assistant (Speech API v2), now based on an instance-internal queue of to-speak
 * speeches.</p>
 * <br>
 * <p>This fixes a bug described on a note in the end of {@link Speech#speak(String, int, boolean, Runnable)} and,
 * hopefully, any other bugs related to how {@link TextToSpeech} is implemented in the various devices.</p>
 */
public class Speech2 implements IModule {

	/*
	Main note of how this module works!!!
	-----

	A speech is requested through a broadcast. This sets a variable that says there's a speech in process, adds the
	speech to a list, and starts processing it.

	While that's happening (from the moment it start being processed before speaking to before it ends being spoken),
	another speech is requested the same way. As there's already a speech being processed, this new one is added to a
	list and that's it. Nothing more is done to it.

	When the first speech finishes being spoken, it calls a function which removes the speech from the list and
	iterates the list for any other speeches on the line. When it finds one, it starts processing it right away.
	If the speech was interrupted by a higher priority one, the current one will be stopped, and the same function will
	be called, though this time it won't remove the speech from the lists, but will still look for a new speech, always
	from high to low priorities.

	So, this works in one line of flow. Infinite speeches are called, and they're all stacked. Once a speech finishes
	being spoken, it calls the second one on the line. So take things in only on one line of flow (I tried that it is a
	synchronous class on the speech processing part to be easy to think on).
	*/

	TextToSpeech tts;
	// If more priorities are ever needed, well, here's a 10 in case I forget to update the number (possible).
	final ArrayList<ArrayList<SpeechObj>> arrays_speech_objs = new ArrayList<>(10);
	SpeechObj current_speech_obj = new SpeechObj("", "", true, null);
	private String last_thing_said = "";
	@Nullable private AudioFocusRequest audioFocusRequest = null;
	private final VolumeDndObj volumeDndObj = new VolumeDndObj();

	private boolean speeches_on_lists = false;

	// For slow devices, maybe 250L is good?
	// EDIT: I think if we reset and set the volume too quickly, Android will mess up somewhere and one of the changes
	// won't be detected (put a LOW and a CRITICAL one in row and that might happen). The detected change may happen
	// about 500ms after the volume was set. More specifically, in a test, it gave 530ms. For slower devices, I've put
	// 750ms at most. I think this time it should be enough...
	private static final long VOLUME_CHANGE_INTERVAL = 750L;

	private static final int OPPOSITE_VOL_DND_OBJ_DEFAULT_VALUE = -VolumeDndObj.DEFAULT_VALUE; // Both must be different
	private int stream_active_before_begin_all_speeches = OPPOSITE_VOL_DND_OBJ_DEFAULT_VALUE;
	private long assist_changed_volume_time = Long.MAX_VALUE - VOLUME_CHANGE_INTERVAL;
	private boolean assist_will_change_volume = false;
	private boolean user_changed_volume = false;
	private boolean is_speaking = false;
	private boolean focus_volume_dnd_done = false;

	AudioAttributes audioAttributes = null; // No problem in being null since it will only be used if TTS is initialized
	                                        // correctly - and if it did, then audioAttributes was initialized decently.

	private boolean is_module_alive = true;
	@Override
	public final boolean isModuleWorkingProperly() {
		if (!is_module_alive) {
			return false;
		}

		return true;
	}
	@Override
	public final void destroyModule() {
		UtilsGeneral.getContext().unregisterReceiver(broadcastReceiver);
		is_module_alive = false;
	}

	/**
	 * <p>Main class constructor.</p>
	 */
	public Speech2() {
		UtilsSpeech2.readyArrayLists(arrays_speech_objs);

		initializeTts(true);
	}

	/**
	 * <p>Initializes the {@link TextToSpeech} object.</p>
	 *
	 * @param from_onCreate true if the call to this function was made from onCreate(), false otherwise - this way the
	 *                      function can execute tasks that can only be done in the module initialization (when
	 *                      onCreate() is called)
	 */
	void initializeTts(final boolean from_onCreate) {
		tts = new TextToSpeech(UtilsGeneral.getContext(), new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(final int status) {
				if (status == TextToSpeech.SUCCESS) {
					tts.setOnUtteranceProgressListener(new TtsUtteranceProgressListener());

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						// Set the TTS voice. With the app as a system app, it seems the app might start even before the
						// system has all voices ready, because the app starts using a Brazilian voice (???). So this fixes
						// it.
						final Set<Voice> voices = tts.getVoices();
						for (final Voice voice : voices) {
							if (GL_CONSTS.PREFERRED_TTS_VOICE.equals(voice.getName())) {
								tts.setVoice(voice);
								break;
							}
						}

						if (from_onCreate) {
							// Set the audio attributes to use
							final AudioAttributes.Builder builder = new AudioAttributes.Builder();
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
								builder.setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_NONE);
							}
							builder.setContentType(AudioAttributes.CONTENT_TYPE_SPEECH);
							builder.setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE); // Kind of
							//builder.setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED); - Don't use: "However, when the
							// track plays it uses the System (Ringer) volume as the master volume control. Not the media
							// volume as I would expect." (this is about setting that flag - changes the audio stream).
							// That's to be changed depending on the speech priority only and the enforcing of the audio is
							// done manually though DND, volume, and the stream. So don't set this flag.
							audioAttributes = builder.build();
							//tts.setAudioAttributes(audioAttributes); - Don't enable this... Makes the app say
							// "Ready[, sir - this part is cut]" if the phone (BV9500) is in Vibrating mode. It starts
							// speaking and it's interrupted - but onDone is never called, only onStop().
							// Which means, if it doesn't work well in one case, don't enable.
						}
					}

					if (from_onCreate) {
						registerReceiver();
						UtilsApp.sendInternalBroadcast(new Intent(CONSTS_BC.ACTION_READY));
					} else {
						UtilsApp.sendInternalBroadcast(new Intent(CONSTS_BC.ACTION_READY_AGAIN));
					}
				} else {
					// If he can't talk, won't be too much useful... So exit with an error to indicate something is very
					// wrong and must be fixed as soon as possible.

					// An empty string should be enough to throw an error and crash the application.
					System.out.println("----------- APP DEAD - TTS INITIALIZATION ERROR -----------");
					//speak("", NO_ADDITIONAL_COMMANDS, PRIORITY_LOW, null);
					// todo Send an email about this and put a notification on the phone!!!
				}
			}
		}, GL_CONSTS.PREFERRED_TTS_ENGINE);
	}

	//////////////////////////////////////
	// Getters

	/**.
	 * @return the variable {@link #last_thing_said}
	 */
	@NonNull
	final String getLastThingSaid() {
		return last_thing_said;
	}

	//////////////////////////////////////

	/**
	 * <p>Sets {@link #user_changed_volume} to true in case the audio stream of the changed volume equals the audio
	 * stream currently being used to speak.</p>
	 * <p>It's also a bit smarter than that, as the assistant itself changes the volume sometimes to speak. This function
	 * attempts to differentiate both volume changes (user vs assistant).</p>
	 *
	 * @param audio_stream the audio stream of which the volume changed
	 */
	final void setUserChangedVolumeTrue(final int audio_stream) {
		// Detect user changes only after some time after the assistant changed the volume to speak, since the first
		// volume change to be detected would be the assistant himself changing the volume - in case he changed the
		// volume (otherwise the first and next changes will be user changes). This way we wait until the broadcasts for
		// the assistant volume change are received and only after those we start detecting actual user changes of volume
		// (pity there's no way of getting the package which changed the volume or something... - only complicates things).
		// Also, detect only if the assistant is speaking, of course.

		final boolean carry_on;
		if (is_speaking) {
			carry_on = true;
			// If the assistant is speaking, check the audio stream always. Though...
			if (assist_will_change_volume) {
				if (System.currentTimeMillis() <= assist_changed_volume_time + VOLUME_CHANGE_INTERVAL) {
					if ((volumeDndObj.audio_stream != VolumeDndObj.DEFAULT_VALUE && audio_stream == volumeDndObj.audio_stream)
							|| (audio_stream == current_speech_obj.audio_stream)) {
						// ... if the assistant will change the volume and it's detected here a volume change before
						// the maximum allowed waiting time for the assistant to change the volume, and the audio stream
						// of that change is the audio stream currently being used, reset the will change volume variables.
						setResetWillChangeVolume(false);
					}
					// Else, it was one of the various volume change broadcasts made by the system. Keep waiting for the
					// broadcast with the correct audio stream volume change.

					return;
				} else {
					// Else, if the assistant will change the volume but the first volume change detection was after
					// the maximum allowed waiting period, reset the will change volume variables and check anyways.
					// This, as a start, shouldn't happen. But if it does, assume it's a user change, and assume there
					// was some error and the assistant didn't get to change the volume.
					setResetWillChangeVolume(false);
				}
			}
		} else {
			// If the assistant is not speaking, discard any volume changes.
			carry_on = false;
		}

		if (carry_on) {
			if ((volumeDndObj.audio_stream != VolumeDndObj.DEFAULT_VALUE && audio_stream == volumeDndObj.audio_stream)
					|| (audio_stream == current_speech_obj.audio_stream)) {
				// As soon as a user volume change is detected, set the variable to true to indicate the user changed
				// the volume.
				// Also reset only if the stream is the correct one, because the system broadcasts multiple streams at
				// once, and only one of them may be the one we're looking for.
				user_changed_volume = true;
			}
		}
	}

	/**
	 * <p>Skips the currently speaking speech.</p>
	 *
	 * @return same as in {@link TextToSpeech#stop()}
	 */
	final int skipCurrentSpeech() {
		return ttsStop(true);
	}

	public static final int NO_ADDITIONAL_COMMANDS = 0;
	// Below, high values for return values to not be confused with TextToSpeech.speak() methods' current return values
	// and any other values that might arise.
	public static final int SPEECH_ON_LIST = 3234_0;
	public static final int CUR_SPEECH_STOPPED = 3234_1;
	public static final int CUR_SPEECH_NOT_STOPPED = 3234_2;
	// To add a new priority, create one here, update the doc below, and update the constant NUMBER_OF_PRIORITIES.
	// If you get to 10 or above, see if replacing X by 2 characters works.
	public static final int PRIORITY_LOW = 0;
	public static final int PRIORITY_MEDIUM = 1;
	public static final int PRIORITY_USER_ACTION = 2;
	public static final int PRIORITY_HIGH = 3;
	public static final int PRIORITY_CRITICAL = 4;
	/**
	 * <p>Speaks the given text.</p>
	 * <br>
	 * <p>When called multiple times, the speeches of the same priority will be spoken in the order the function was
	 * called. With different priorities, higher priority speeches will be spoken first and only then will the lower
	 * priorities speak.</p>
	 * <br>
	 * <p>From the first to the last speech, the assistant will try to (in this order) request audio focus and set the
	 * volume of the stream to use to a predetermined level. After all speeches from a same audio stream are finished,
	 * what the assistant changed before starting to speak will be undone and all will be left as was (unless the user
	 * changed one of the changed things, and in that case, the changed thing(s) will remain changed. Also, if the
	 * priority is {@link #PRIORITY_CRITICAL}, before both things I said, the assistant will attempt to disable any
	 * Do Not Disturb setting and only then will proceed to the other 2 things. The volume will also be set to the
	 * maximum the chosen audio stream can handle.</p>
	 * <br>
	 * <p>If a speech is requested, this speech module had already been initialized, and the previously selected TTS
	 * engine was uninstalled or there was some error attempting to send text to the engine, this module will restart
	 * internally and another TTS engine will be used to speak (in case there's no engine available after restart, it
	 * will continue to restart until it finds an available engine to speak again). It will only come back to the
	 * preferred engine again if the module is restarted manually (for example by a force stop of the app).</p>
	 * <br>
	 * <p>If {@code AudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL}, exactly nothing will be done (the
	 * speech will be completely aborted), except broadcast {@code after_speaking_code}.
	 * <br>
	 * <p>All the {@code PRIORITY_} constants except {@link #PRIORITY_USER_ACTION} are to be used only for speeches that
	 * are not a response to a user action, for example automated tasks or broadcast receivers.</p>
	 * <p>The {@link #PRIORITY_USER_ACTION} is the only one to be used for speeches that are a response to a user
	 * action. For example, for audio recording speeches - the assistant will not record audio unless the user asked him
	 * to, either directly or to start recording in a defined time.</p>
	 * <p>The {@link #PRIORITY_CRITICAL} is to be used only for things that need to be said immediately and at maximum
	 * volume. For example, extreme security warnings, like Device Administrator mode successfully revoked.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #PRIORITY_LOW} --> for {@code speech_priority}: low priority speech</p>
	 * <p>- {@link #PRIORITY_MEDIUM} --> for {@code speech_priority}: medium priority speech</p>
	 * <p>- {@link #PRIORITY_USER_ACTION} --> for {@code speech_priority}: medium-high priority speech</p>
	 * <p>- {@link #PRIORITY_HIGH} --> for {@code speech_priority}: high priority speech</p>
	 * <p>- {@link #PRIORITY_CRITICAL} --> for {@code speech_priority}: critical priority speech</p>
	 * <br>
	 * <p>- {@link #SPEECH_ON_LIST} --> for the returning value: if the speech was put on the list of to-speak speeches</p>
	 * <p>- {@link #CUR_SPEECH_STOPPED} --> for the returning value: in case the speech had a higher priority than the
	 * currently speaking speech and this last one was aborted temporarily because of that</p>
	 * <p>- {@link #CUR_SPEECH_NOT_STOPPED} --> for the returning value: in case the speech had a higher priority than
	 * the currently speaking speech, but this last one was not aborted temporarily due to an error</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param txt_to_speak what to speak
	 * @param speech_priority one of the constants (ordered according with their priority from lowest to highest)
	 * @param after_speaking_code a unique reference which will be broadcast as soon as the speech is finished (for
	 *                            example, a unique reference to a {@link Runnable} which is detected by a receiver and
	 *                            which will execute the Runnable that corresponds to the reference); or null, if nothing
	 *                            is required to be done after the speech finishes
	 *
	 * @return same as in {@link TextToSpeech#speak(CharSequence, int, Bundle, String)} or
	 * {@link TextToSpeech#speak(String, int, HashMap)} (depending on the device API level) in case the speech began
	 * being spoken immediately; one of the constants otherwise.
	 */
	final int speak(@NonNull final String txt_to_speak, final int speech_priority,
					@Nullable final Integer after_speaking_code) {
		return speakInternal(txt_to_speak, speech_priority, null, after_speaking_code);
	}

	/**
	 * <p>Same as in {@link #speak(String, int, Integer)}, but with additional parameters to be used only
	 * internally to the class - this is the main speak() method.</p>
	 *
	 * @param txt_to_speak same as in {@link #speak(String, int, Integer)}
	 * @param priority same as in {@link #speak(String, int, Integer)}
	 * @param utterance_id the utterance ID to be used to re-register the given speech in case it's already in the lists,
	 *                     null if it's not already in the lists
	 * @param after_speaking_code same as in {@link #speak(String, int, Integer)}
	 *
	 * @return same as in {@link #speak(String, int, Integer)}
	 */
	private int speakInternal(final String txt_to_speak, final int priority, @Nullable final String utterance_id,
							  @Nullable final Integer after_speaking_code) {

		// todo Make a way of getting him not to listen what he himself is saying... Or he'll hear himself and process
		// that, which is stupid. For example by cancelling the recognition when he's speaking or, or removing what he
		// said from the string of what he listened or something (2nd one preferable - else we can't tell him to shut
		// up while he's speaking, because he disabled the recognition).
		// When this is implemented, don't forget to check if he's speaking on the speakers or on headphones. If
		// it's on headphones, no need to cancel the recognition. If it's on speakers, not sure. If the volume is
		// high enough, he wouldn't hear us anyways. If we lower the volume, he could hear us.

		// The utteranceIDs (their indexes in the array) are used by me to identify the corresponding Runnable and speech.

		SpeechObj new_speech_obj = null;
		String utterance_id_to_use;
		if (utterance_id == null) {
			// A while true to create random utterance IDs and ensure they have not been already used on the lists
			while (true) {
				utterance_id_to_use = UtilsSpeech2.generateUtteranceId(priority);

				boolean match_found = false;
				for (final Iterable<SpeechObj> speech_objs : arrays_speech_objs) {
					for (final SpeechObj speech_obj : speech_objs) {
						if (speech_obj.utterance_id.equals(utterance_id_to_use)) {
							match_found = true;
							break;
						}
					}
					if (match_found) {
						break;
					}
				}
				if (!match_found) {
					break;
				}
			}
			new_speech_obj = new SpeechObj(utterance_id_to_use, txt_to_speak, false, after_speaking_code);
			arrays_speech_objs.get(priority).add(new_speech_obj);
		} else {
			utterance_id_to_use = utterance_id;
		}

		if (current_speech_obj.utterance_id.isEmpty()) {
			// The function only gets here if there was no speech already taking place.

			// Set the current_speech_obj to the speech getting ready to be spoken immediately, so that if any other
			// speech is requested goes at the same time, goes directly to the lists (race condition, and this is
			// supposed to fix it).
			if (new_speech_obj == null) {
				// If it's null above, then the speech was already on the lists. So go look for it.
				// Below will never been null if the implementation of Speech2 is correct.
				current_speech_obj = Objects.requireNonNull(
						UtilsSpeech2.getSpeechObjFromId(utterance_id_to_use, arrays_speech_objs));
			} else {
				// If it's not null, then a new speech was just created and we can use that object instead of going
				// looking for it (optimized).
				current_speech_obj = new_speech_obj;
			}

			// In case there was some error (for example, the engine was uninstalled), reinitialize the TTS object.
			final int tts_error_code = sendTtsSpeak(current_speech_obj.txt_to_speak, current_speech_obj.utterance_id,
					current_speech_obj.audio_stream);
			if (tts_error_code != TextToSpeech.SUCCESS) {
				initializeTts(false);
			}

			return tts_error_code;
		} else {
			speeches_on_lists = true;

			// If there's a speech already being spoken, the new one is just added to the list (when the current one
			// stops, it will take care of starting the next ones on the queues).
			// Except if the new speech has a higher priority than the current one. In that case, the current one
			// stops temporarily to give place to the new one.
			if (priority > UtilsSpeech2.getSpeechPriority(current_speech_obj.utterance_id)) {
				if (ttsStop(false) == TextToSpeech.SUCCESS) {
					return CUR_SPEECH_STOPPED;
				} else {
					return CUR_SPEECH_NOT_STOPPED;
				}
			} else {
				// Even though no speech is taking place, the speech is added to the lists. Why? Because it may be
				// stopped and may need to be spoken later. For example, a higher priority speech is requested. The
				// current one will be temporarily stopped, the higher priority one will be spoken, and then the first
				// one will be spoken again.
				// So all speeches go to the lists always.

				return SPEECH_ON_LIST;
			}
		}
	}

	/**
	 * <p>Sends the specified string to {@link TextToSpeech#speak(CharSequence, int, Bundle, String)} or
	 * {@link TextToSpeech#speak(String, int, HashMap)}.</p>
	 * <br>
	 * <p>Attention: not to be called except from inside {@link #speak(String, int, Integer)}.</p>
	 *
	 * @param txt_to_speak same as in {@link #speak(String, int, Integer)}
	 * @param utterance_id the utterance ID to register the speech
	 * @param audio_stream the audio stream to be used to speak the speech
	 *
	 * @return same as in {@link TextToSpeech}'s speak() methods
	 */
	int sendTtsSpeak(final String txt_to_speak, final String utterance_id, final int audio_stream) {
		final TtsParamsObj tts_params = new TtsParamsObj();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			tts_params.bundle.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, audio_stream);

			return tts.speak(txt_to_speak, TextToSpeech.QUEUE_ADD, tts_params.bundle, utterance_id);
		} else {
			tts_params.hashmap.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(audio_stream));
			tts_params.hashmap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utterance_id);

			return tts.speak(txt_to_speak, TextToSpeech.QUEUE_ADD, tts_params.hashmap);
		}
	}

	/**
	 * <p>An improved/adapted version of {@link TextToSpeech#stop()} which, after executing the mentioned method, calls
	 * {@link #onStop(String, boolean)} if and only if there was no error calling {@link TextToSpeech#stop()}.</p>
	 *
	 * @param skip_speech true if the current speech is to be skipped, false otherwise
	 *
	 * @return same as in {@link TextToSpeech#stop()}
	 */
	private int ttsStop(final boolean skip_speech) {
		// The current speech must be cleared so onDone() (in case it's called) knows the speech was force stopped since
		// the current speech can never be empty when onDone is called (how was it called if there's no current speech)
		// - except if the speech was force stopped. That has a different implementation and must be processed
		// separately. This is a way for onDone to do nothing, since the custom onStop will - which will also make the
		// the program flow continue to be only one (onDone() is called in another thread but nothing is done, so we
		// continue here to onStop()). Read also above the if statement on onDone().
		final SpeechObj old_speech_obj = current_speech_obj;
		current_speech_obj = new SpeechObj("", "", true, null);
		if (tts.stop() == TextToSpeech.ERROR) {
			current_speech_obj = old_speech_obj;

			return TextToSpeech.ERROR;
		}

		onStop(old_speech_obj.utterance_id, skip_speech);

		return TextToSpeech.SUCCESS;
	}

	/**
	 * <p>Sets the volume and Do Not Disturb to specific states for the assistant to speak (depending on the speech
	 * priority), and also requests audio focus (if {@link AudioManager#getRingerMode()} != NORMAL).</p>
	 */
	private void setVolumeDndFocus() {
		final AudioManager audioManager = (AudioManager) UtilsGeneral.getContext()
				.getSystemService(Context.AUDIO_SERVICE);
		if (UtilsSpeech2.getSpeechPriority(current_speech_obj.utterance_id) == PRIORITY_CRITICAL) {
			audioFocus(true);

			// Set Do Not Disturb
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				final NotificationManager notificationManager = (NotificationManager) UtilsGeneral.getContext()
						.getSystemService(Context.NOTIFICATION_SERVICE);
				if (notificationManager.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_ALARMS) {
					volumeDndObj.old_interruption_filter = notificationManager.getCurrentInterruptionFilter();
					volumeDndObj.new_interruption_filter = NotificationManager.INTERRUPTION_FILTER_ALARMS;

					notificationManager.setInterruptionFilter(volumeDndObj.new_interruption_filter);
				}
			}
			// Set the volume
			volumeDndObj.audio_stream = current_speech_obj.audio_stream;
			volumeDndObj.old_volume = audioManager.getStreamVolume(current_speech_obj.audio_stream);
			volumeDndObj.new_volume = audioManager.getStreamMaxVolume(current_speech_obj.audio_stream);

			setResetWillChangeVolume(true);

			audioManager.setStreamVolume(current_speech_obj.audio_stream, volumeDndObj.new_volume,
					AudioManager.FLAG_FIXED_VOLUME | AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE | AudioManager.FLAG_SHOW_UI);
		} else {
			if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
				audioFocus(true);

				// Set the volume
				// Put the volume in medium before speaking in case the ringer is NORMAL and the stream is not
				// RING or NOTIFICATION (for example with STREAM_MUSIC).
				final int current_volume = audioManager.getStreamVolume(current_speech_obj.audio_stream);
				final int max_volume = audioManager.getStreamMaxVolume(current_speech_obj.audio_stream);
				final int new_volume = max_volume / 2;
				if (current_volume < new_volume) {
					volumeDndObj.audio_stream = current_speech_obj.audio_stream;
					volumeDndObj.old_volume = current_volume;
					volumeDndObj.new_volume = new_volume;

					setResetWillChangeVolume(true);

					audioManager.setStreamVolume(current_speech_obj.audio_stream, new_volume,
							AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE | AudioManager.FLAG_SHOW_UI);
				}
			}
		}

		focus_volume_dnd_done = true;
	}

	/**
	 * <p>Resets the old volume of the used stream in case the user didn't change it, the audio focus and Do Not
	 * Disturb (DND).</p>
	 */
	final void resetVolumeDndFocus() {
		setResetWillChangeVolume(false);

		// Reset the audio focus
		audioFocus(false);

		// Reset the volume
		if (volumeDndObj.old_volume != VolumeDndObj.DEFAULT_VALUE) {
			boolean carry_on = false;
			if (stream_active_before_begin_all_speeches == volumeDndObj.audio_stream) {
				stream_active_before_begin_all_speeches = OPPOSITE_VOL_DND_OBJ_DEFAULT_VALUE;
				// If the user changed the volume user while the assistant was speaking, reset only if the stream
				// was already being used before the assistant started to speak (the user raises the volume because it
				// was too low to hear the assistant, and had nothing playing - then the assistant lowers it again after
				// finishing, even with the user having raised it to hear the assistant better at first next time --> ???).
				// This fixes that.
				carry_on = true;
			} else {
				if (!user_changed_volume) {
					carry_on = true;
				}
			}
			if (carry_on) {
				final AudioManager audioManager = (AudioManager) UtilsGeneral.getContext()
						.getSystemService(Context.AUDIO_SERVICE);
				if (audioManager.getStreamVolume(volumeDndObj.audio_stream) != volumeDndObj.old_volume) {
					try {
						audioManager.setStreamVolume(volumeDndObj.audio_stream, volumeDndObj.old_volume,
								AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE | AudioManager.FLAG_SHOW_UI);
					} catch (final SecurityException ignored) {
						// Toggles DND, so I guess that would be because the volume is 0, maybe. If I'm right, then
						// I just need to increase 1 to put it one level above, which would be out of DND.
						audioManager.setStreamVolume(volumeDndObj.audio_stream, volumeDndObj.old_volume + 1,
								AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE | AudioManager.FLAG_SHOW_UI);
					}
				}
			}
		}

		// Reset Do Not Disturb
		if (volumeDndObj.old_interruption_filter != VolumeDndObj.DEFAULT_VALUE) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				final NotificationManager notificationManager = (NotificationManager) UtilsGeneral.getContext()
						.getSystemService(Context.NOTIFICATION_SERVICE);
				if (notificationManager.isNotificationPolicyAccessGranted()) {
					// Only reset if the interruption mode was not changed while the assistant was speaking.
					if (notificationManager.getCurrentInterruptionFilter() == volumeDndObj.new_interruption_filter) {
						notificationManager.setInterruptionFilter(volumeDndObj.old_interruption_filter);
					}
				}
			}
		}

		volumeDndObj.setDefaultValues();

		focus_volume_dnd_done = false;
	}

	/**
	 * <p>Requests audio focus depending on the speech priority.</p>
	 * <br>
	 * <p><em>Call only AFTER having {@link #current_speech_obj} updated, as it's used here!</em></p>
	 *
	 * @param request true to request the audio focus, false to abandon the audio focus
	 */
	final void audioFocus(final boolean request) {
		final AudioManager audioManager = (AudioManager) UtilsGeneral.getContext()
				.getSystemService(Context.AUDIO_SERVICE);
		if (audioManager != null) {
			final int priority = UtilsSpeech2.getSpeechPriority(current_speech_obj.utterance_id);
			if (request) {
				final int duration_hint;
				if (priority == PRIORITY_CRITICAL) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
						// todo Test this here! The comment in the 2nd else statement below
						duration_hint = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE;
					} else {
						duration_hint = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
					}
				} else {
					duration_hint = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
					// Some weird behavior on Lollipop 5.1 prevents it from speaking with TRANSIENT if media is playing,
					// so leave GAIN here only.
					// On Oreo 8.1, without this, other apps won't continue playback --> fix this!!!
				}

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					audioFocusRequest = new AudioFocusRequest.Builder(duration_hint)
							.setAudioAttributes(audioAttributes)
							.build();
					audioManager.requestAudioFocus(audioFocusRequest);
				} else {
					audioManager.requestAudioFocus(onAudioFocusChangeListener, current_speech_obj.audio_stream,
							duration_hint);
				}
			} else {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
					if (audioFocusRequest != null) {
						audioManager.abandonAudioFocusRequest(audioFocusRequest);
						audioFocusRequest = null;
					}
				} else {
					audioManager.abandonAudioFocus(onAudioFocusChangeListener);
				}
			}
		}
	}

	final AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener =
			new AudioManager.OnAudioFocusChangeListener() {
		@Override
		public void onAudioFocusChange(final int focusChange) {
		}
	};

	/**
	 * <p>Sets or resets the variables {@link #assist_changed_volume_time} and {@link #assist_changed_volume_time} to
	 * default values.</p>
	 *
	 * @param set true to set, false to reset
	 */
	private void setResetWillChangeVolume(final boolean set) {
		if (set) {
			assist_changed_volume_time = System.currentTimeMillis();
			assist_will_change_volume = true;
		} else {
			assist_will_change_volume = false;
			assist_changed_volume_time = Long.MAX_VALUE - VOLUME_CHANGE_INTERVAL;
		}
	}

	/**
	 * <p>Things to do exactly before starting to speak, as soon as detection is possible.</p>
	 * <br>
	 * <p>In case the ringer mode is not set to NORMAL (to be detected exactly before starting to speak), no callbacks
	 * are triggered by calling this function, aside from {@link UtteranceProgressListener#onStop(String, boolean)},
	 * which doesn't have an implementation in this app on purpose, so consider this as the last part of
	 * {@link UtteranceProgressListener} (means for a speech) to be executed in that case.</p>
	 */
	final void rightBeforeSpeaking() {
		boolean skip_speech = false;

		// Check the ringer mode, which must be NORMAL, otherwise the assistant will not speak - unless the speech is a
		// CRITICAL speech.
		if (UtilsSpeech2.getSpeechPriority(current_speech_obj.utterance_id) != PRIORITY_CRITICAL) {
			final AudioManager audioManager = (AudioManager) UtilsGeneral.getContext()
					.getSystemService(Context.AUDIO_SERVICE);
			if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
				System.out.println("+++++++++++++++++++++++++++++++++++++++");
				System.out.println(current_speech_obj.utterance_id);
				System.out.println(current_speech_obj.after_speaking_code);
				if (current_speech_obj.after_speaking_code != null) {
					UtilsSpeech2.broadcastAfterSpeakCode(current_speech_obj.after_speaking_code);
				}

				skipCurrentSpeech();
				skip_speech = true;
			}
		}


		if (!skip_speech) {
			// If it's to speak, prepare the app to speak.
			if (!focus_volume_dnd_done) {
				setVolumeDndFocus();
				if (!speeches_on_lists) {
					if (AudioSystem.isStreamActive(current_speech_obj.audio_stream, 0)) { // 0 == Now
						stream_active_before_begin_all_speeches = volumeDndObj.audio_stream;
					}
				}
			}

			is_speaking = true;
		}
	}

	/**
	 * <p>The {@link UtteranceProgressListener} to be used for the speech.</p>
	 */
	class TtsUtteranceProgressListener extends UtteranceProgressListener {

		@Override
		public final void onStart(final String utteranceId) {
			rightBeforeSpeaking();
		}

		@Override
		public final void onDone(final String utteranceId) {
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			System.out.println(arrays_speech_objs);
			System.out.println(utteranceId);
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");

			// If the utterance ID is empty, then it means it was force stopped and it's to be done nothing.
			// When tts.stop() is called, I think onDone was supposed to be called (I think it's supposed to be always
			// called?). Though, on Lollipop 5.1 it is called, but not on Oreo 8.1. So as the custom onStop will be
			// called first, it will put empty the utterance ID of the current speech and tell onDone that what was to
			// be done, was already done by onStop(). This in case onDone is called. If it's not, no matter - onStop()
			// already did onDone()'s job as a failsafe measure.
			if (!current_speech_obj.utterance_id.isEmpty()) {
				current_speech_obj = new SpeechObj("", "", true, null);
				speechTreatment(utteranceId);
			}
		}

		// Up to API 20
		@Override
		public final void onError(final String utteranceId) {
			System.out.println("^-^-^-^-^-^-^-^-^-^-^-^-^-^-^");
			System.out.println(arrays_speech_objs);
			System.out.println(utteranceId);
			System.out.println("^-^-^-^-^-^-^-^-^-^-^-^-^-^-^");

			// The if statement below has the same reason as the reason on onDone for the same if statement. It's a
			// precaution, since I don't know when there's an error, which of onDone and onError will be called, and if
			// both are called, which one is called first. So in any case, the first to be called will stop the other
			// one from being called this way.
			if (!current_speech_obj.utterance_id.isEmpty()) {
				current_speech_obj = new SpeechObj("", "", true, null);
				speechTreatment(utteranceId);
			}
		}

		// As of API 21
		@Override
		public final void onError(final String utteranceId, final int errorCode) {
			super.onError(utteranceId, errorCode);
			System.out.println("^*^*^*^*^*^*^*^*^*^*^*^*^*^*^");
			System.out.println(errorCode);
			System.out.println("^*^*^*^*^*^*^*^*^*^*^*^*^*^*^");

			// The super call up here just calls the other onError() method. So don't do anything here (except printing
			// the error).
		}

		// As of API 23
		@Override
		public final void onStop(final String utteranceId, final boolean interrupted) {
			super.onStop(utteranceId, interrupted);

			// Do nothing here. Why? Read the custom onStop() method's documentation which explains it.
			// Do NOT implement this method. It's here just to indicate that it's not to be implemented.
		}
	}
	/**
	 * <p>A custom "callback" of the {@link #ttsStop(boolean)} method like the original {@link TextToSpeech#stop()}
	 * should have since API 1.</p>
	 *
	 * @param utteranceId the utterance ID of the speech that was taking place when {@link #ttsStop(boolean)} was called
	 * @param skip_speech same as in {@link #ttsStop(boolean)}
	 */
	private void onStop(@NonNull final String utteranceId, final boolean skip_speech) {
		System.out.println("^+^+^+^+^+^+^+^+^+^+^+^+^+^+^");
		System.out.println(arrays_speech_objs);
		System.out.println(utteranceId);
		System.out.println("^+^+^+^+^+^+^+^+^+^+^+^+^+^+^");

		// current_speech_obj is already null here - this onStop() is only called from ttsStop(), which empties
		// current_speech_obj by itself. Use utteranceId to get info about the speech that was stopped.
		if (skip_speech) {
			// If it's to skip the speech, just stop the current speech with tts.stop(), which will delete all the speeches
			// on its list (which are none, according with the Speech2 implementation - nothing is ever on the list except
			// the current speech).
			speechTreatment(utteranceId);
		} else {
			UtilsSpeech2.reSayRephraseSpeech(utteranceId, arrays_speech_objs);
			// In this case, the speech is not to be removed from the list. Only stopped temporarily.
			speechTreatment("");
		}
	}

	/**
	 * <p>Checks if there are speeches yet to be spoken after a speech is finished, and in affirmative case, chooses
	 * what speech is to be spoken next, based on its priority, and tells {@link TextToSpeech} to speak it.</p>
	 * <br>
	 * <p>This method is to be called from inside {@link TtsUtteranceProgressListener} only, except for
	 * {@link #onStop(String, boolean)}.</p>
	 *
	 * @param utteranceId same as in the method of {@link UtteranceProgressListener} that called this function (or
	 * {@link #onStop(String, boolean)}
	 */
	final void speechTreatment(final String utteranceId) {
		// Main note: everything that calls this function empties current_speech_obj first.

		// Why is this check here and not just the removal? Refer to the custom onStop().
		if (!utteranceId.isEmpty()) {
			// Won't happen - or the speech wouldn't have taken place.
			last_thing_said = Objects.requireNonNull(UtilsSpeech2.removeSpeechById(utteranceId, arrays_speech_objs))
					.txt_to_speak;
		}

		// From back to beginning since high priority has greater value than low priority and the first speeches to be
		// put back on track are the higher priority ones.
		for (int priority = arrays_speech_objs.size() - 1; priority >= 0; priority--) {
			if (!arrays_speech_objs.get(priority).isEmpty()) {
				final SpeechObj correct_speech_obj = arrays_speech_objs.get(priority).get(0);

				final int speech_priority = UtilsSpeech2.getSpeechPriority(correct_speech_obj.utterance_id);
				final String utterance_id = correct_speech_obj.utterance_id;
				final String speech = correct_speech_obj.txt_to_speak;
				final int audio_stream = correct_speech_obj.audio_stream;
				final Integer runnable = correct_speech_obj.after_speaking_code;

				// If there are more speeches and they use the same audio stream, don't reset the volume and abandon
				// the audio focus. Do that only if the stream to be used next is different (reset the previous one).
				// Also, check if the assistant changed the volume at all. If it didn't, don't reset anything (that's
				// checked by verifying if the audio stream is the DEFAULT_VALUE or not).
				if (volumeDndObj.audio_stream != VolumeDndObj.DEFAULT_VALUE && volumeDndObj.audio_stream != audio_stream) {
					if (focus_volume_dnd_done) {
						resetVolumeDndFocus();
					}
					if (assist_will_change_volume) {
						// Just to be sure, in case there was an error and the assistant didn't change the volume after
						// saying it would, or in case the volume changed broadcast was not detected - it would still
						// be waiting to detect it. With this here, not anymore.
						setResetWillChangeVolume(false);
					}
					// The audio stream is changing, so if the user had changed the previous stream's volume, they
					// didn't change the new one's.
					user_changed_volume = false;
				}

				speakInternal(speech, speech_priority, utterance_id, runnable);

				try {
					// This being here won't cause any stop in the assistant once this module is a Service in a separate
					// process. It's a break between speeches so they're not all at once without a small break in
					// between (which is awkward, and doesn't help the brain process when one ends and the other one
					// starts). 500 milliseconds should suffice, I guess.

					// Also, do NOT remove this from here. It's here in purpose. Supposed the assistant is saying
					// something when the device is restarted. It must warn at once about that without a small break.
					// Like it must do if it's saying something and a higher priority thing must be said. No one makes
					// a break for that. Instead, stops the speech and says for example, "Attention sir, that task is over.
					// Now carrying on what I was saying, (...)".
					Thread.sleep(500L);
				} catch (final InterruptedException ignored) {
					Thread.currentThread().interrupt();
				}

				return;
			}
		}

		allSpeechesFinished();
	}

	/**
	 * <p>Things to be done when there all speeches were taken care of (spoken or skipped) and there are no more in the
	 * lists.</p>
	 */
	private void allSpeechesFinished() {
		speeches_on_lists = false;
		is_speaking = false;

		// Since there are no more speeches, reset the stream volume and abandon the audio focus of the last used audio
		// stream.
		if (focus_volume_dnd_done) {
			resetVolumeDndFocus();
		}

		if (assist_will_change_volume) {
			// Same reason than on speechTreatment().
			setResetWillChangeVolume(false);
		}

		// Doesn't matter if the user changed the volume or not if all the speeches have been finished.
		user_changed_volume = false;
	}




	/**
	 * <p>Register the module's broadcast receiver.</p>
	 */
	final void registerReceiver() {
		final IntentFilter intentFilter = new IntentFilter();

		intentFilter.addAction(CONSTS_BC.ACTION_READY_AGAIN);

		intentFilter.addAction(CONSTS_BC.ACTION_CALL_SPEAK);
		intentFilter.addAction(CONSTS_BC.ACTION_SKIP_SPEECH);
		intentFilter.addAction(CONSTS_BC.ACTION_REMOVE_SPEECH);
		intentFilter.addAction(AudioManager.VOLUME_CHANGED_ACTION);

		try {
			UtilsGeneral.getContext().registerReceiver(broadcastReceiver, intentFilter);
		} catch (final IllegalArgumentException ignored) {
		}
	}

	public final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
			if (intent == null || intent.getAction() == null) {
				return;
			}

			System.out.println("PPPPPPPPPPPPPPPPPP-Speech2 - " + intent.getAction());

			switch (intent.getAction()) {
				case (CONSTS_BC.ACTION_READY_AGAIN): {
					// todo Warn there was an error with the selected engine and the TTS had to be reinitialized with
					//  another engine
					final int tts_error_code = sendTtsSpeak(current_speech_obj.txt_to_speak,
							current_speech_obj.utterance_id, current_speech_obj.audio_stream);
					System.out.println("IIIIIIIIIIIIIIIIIIIIIIII");
					System.out.println(tts_error_code);
					if (tts_error_code != TextToSpeech.SUCCESS) {
						initializeTts(false);
					}

					break;
				}
				case (CONSTS_BC.ACTION_CALL_SPEAK): {
					final String txt_to_speak = intent.getStringExtra(CONSTS_BC.EXTRA_CALL_SPEAK_1);
					final int additional_command = intent.getIntExtra(CONSTS_BC.EXTRA_CALL_SPEAK_2, NO_ADDITIONAL_COMMANDS);
					final int speech_priority = intent.getIntExtra(CONSTS_BC.EXTRA_CALL_SPEAK_3, -1);
					@Nullable final Integer after_speaking_code;
					if (intent.hasExtra(CONSTS_BC.EXTRA_CALL_SPEAK_4)) {
						// The -1 doesn't matter here, because if the extra doesn't exist, that means after_speaking_code is
						// supposedly null but can't be sent as null because it's an int, so the extra is not sent at
						// all. So in that case, here it's put as null too. If the extra exists, then a value other than
						// null is to be put on after_speaking_code, and so it is. Conclusion: -1 is never used.
						after_speaking_code = intent.getIntExtra(CONSTS_BC.EXTRA_CALL_SPEAK_4, -1);
					} else {
						after_speaking_code = null;
					}

					speak(txt_to_speak, speech_priority, after_speaking_code);

					break;
				}

				case (CONSTS_BC.ACTION_SKIP_SPEECH): {
					skipCurrentSpeech();

					break;
				}

				case (CONSTS_BC.ACTION_REMOVE_SPEECH): {
					final String speech = intent.getStringExtra(CONSTS_BC.EXTRA_CALL_SPEAK_1);
					final int speech_priority = intent.getIntExtra(CONSTS_BC.EXTRA_CALL_SPEAK_2, -1);
					final boolean low_to_high = intent.getBooleanExtra(CONSTS_BC.EXTRA_CALL_SPEAK_3, true);

					final String speech_id = UtilsSpeech2.getSpeechIdBySpeech(speech, speech_priority, low_to_high,
							arrays_speech_objs);
					if (speech_id != null) {
						UtilsSpeech2.removeSpeechById(speech_id, arrays_speech_objs);
					}

					break;
				}

				case (AudioManager.VOLUME_CHANGED_ACTION): {
					setUserChangedVolumeTrue(intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE,
							Speech2.OPPOSITE_VOL_DND_OBJ_DEFAULT_VALUE));

					break;
				}
			}
		}
	};
}
