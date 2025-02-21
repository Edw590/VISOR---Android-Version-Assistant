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

package com.edw590.visor_c_a.Modules.Speech;

import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.os.Handler;
import android.os.HandlerThread;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.edw590.visor_c_a.GlobalInterfaces.IModuleInst;
import com.edw590.visor_c_a.GlobalUtils.GL_CONSTS;
import com.edw590.visor_c_a.GlobalUtils.ObjectClasses;
import com.edw590.visor_c_a.GlobalUtils.UtilsApp;
import com.edw590.visor_c_a.GlobalUtils.UtilsCheckHardwareFeatures;
import com.edw590.visor_c_a.GlobalUtils.UtilsContext;
import com.edw590.visor_c_a.GlobalUtils.UtilsGeneral;
import com.edw590.visor_c_a.GlobalUtils.UtilsNotifications;
import com.edw590.visor_c_a.ModulesList;
import com.edw590.visor_c_a.Registry.RegistryKeys;
import com.edw590.visor_c_a.Registry.UtilsRegistry;
import com.edw590.visor_c_a.TasksList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;

import GPTComm.GPTComm;
import SpeechQueue.Speech;

/**
 * <p>The 2nd speech module of the assistant (Speech API v2), now based on an instance-internal queue of to-speak
 * speeches instead of on {@link TextToSpeech}'s internal queue.</p>
 * <p>Notifications are also managed here for a case where speech and/or audio output is not available (no hardware
 * support, no TTS engine, or no sound).</p>
 * <p>This module fixes a bug described on a note in the end of {@link Speech#speak(String, int, boolean, Runnable)}
 * and, hopefully, any other bugs related to how {@link TextToSpeech} is implemented in the various devices, because
 * this should now be device/{@link TextToSpeech} implementation independent.</p>
 */
public final class Speech2 implements IModuleInst {

	/*
	Main note of how this module works!!!
	-----

	A speech is requested through a broadcast. This sets a variable that says there's a speech in process, adds the
	speech to a list, and starts processing it.

	While that's happening (from the moment it starts being processed before speaking to before it ends being spoken),
	another speech is requested the same way. As there's already a speech being processed, this new one is added to a
	list and that's it. Nothing more is done to it.

	When the first speech finishes being spoken, it calls a function which removes the speech from the list and
	iterates the list for any other speeches on the line. When it finds one, it starts processing it right away.
	If the speech was interrupted by a higher priority one, the current one will be stopped, and the same function will
	be called, though this time it won't remove the speech from the lists, but will still look for a new speech, always
	from high to low priorities.

	So, this works in one line of flow. Infinite speeches are called, and they're all stacked. Once a speech finishes
	being spoken, it calls the second one on the line. So take things in only on one line of flow (I tried that it is a
	synchronous class on the speech processing part to be easier to think on).
	*/

	// todo With the Ivona TTS, configure it to work normally, then take the voices out of the folder and try to speak -
	// it won't notice it's not speaking. So get it to notice it. Check if the callbacks are called, and if not, put
	// some timer checking that whenever there is a speech taking place (else it would be wasting battery).
	// Also, after getting the voices back on their folder, this still doesn't work and I had to restart the app (not
	// even changing the voices and the engine for it to restart the speech worked - I really had to restart the app).

	private final int element_index = ModulesList.getElementIndex(this.getClass());
	private final HandlerThread main_handlerThread = new HandlerThread((String) ModulesList.getElementValue(element_index,
			ModulesList.ELEMENT_NAME));
	private final Handler main_handler;

	boolean tts_working = false;

	private static final String ACTION_CLEAR_NOTIF_MSGS = "Speech_ACTION_CLEAR_NOTIF_MSGS";
	private NotificationCompat.Builder speeches_notif_builder = null;
	// Leave the lists below static! This way, if the module is restarted, the lists are kept intact.
	static final ArrayList<String> speech_notif_speeches = new ArrayList<>(10);

	TextToSpeech tts = null;
	String current_speech_id = "";
	SpeechQueue.Speech last_speech = new SpeechQueue.Speech();
	@Nullable private AudioFocusRequest audioFocusRequest = null;
	private final VolumeDndState volumeDndState = new VolumeDndState();

	private boolean speeches_on_lists = false;

	// For slow devices, maybe 250 is good?
	// EDIT: I think if we reset and set the volume too quickly, Android will mess up somewhere and one of the changes
	// won't be detected (put a LOW and a CRITICAL one in row and that might happen). The detected change may happen
	// about 500ms after the volume was set. More specifically, in a test, it gave 530ms. For slower devices, I've put
	// 750ms at most. I think this time it should be enough...
	private static final long VOLUME_CHANGE_INTERVAL = 750;

	private static final int OPPOSITE_VOL_DND_OBJ_DEFAULT_VALUE = -VolumeDndState.DEFAULT_VALUE; // Both must be different
	private int stream_active_before_begin_all_speeches = OPPOSITE_VOL_DND_OBJ_DEFAULT_VALUE;
	private long assist_changed_volume_time = Long.MAX_VALUE - VOLUME_CHANGE_INTERVAL;
	private boolean assist_will_change_volume = false;
	private boolean user_changed_volume = false;
	private boolean focus_volume_dnd_done = false;

	private static final int AUD_STREAM_PRIORITY_CRITICAL = AudioManager.STREAM_ALARM;
	// SYSTEM_ENFORCED so we can't the change the volume: "Yeah, it will always play at max volume since this stream is
	// intended mainly for camera shutter sounds in markets where they have legal requirements saying that
	// people shouldn't be able to mute/attenuate the shutter sound and go around taking photos of other people
	// without their knowledge." - StackOverflow
	// EDIT: Or not... At least with Lollipop 5.1 and Oreo 8.1. Maybe only in Japanese phones which is where the
	// above is applied (said in another comment).
	// EDIT 2: It's now on ALARM, which I think might be better than SYSTEM_ENFORCED since this one may be
	// system-dependent, and ALARM seems to always have high priority, possibly.
	private static final int AUD_STREAM_PRIORITY_HIGH = AudioManager.STREAM_NOTIFICATION;
	private static final int AUD_STREAM_PRIORITY_OTHERS_SPEAKERS = AudioManager.STREAM_NOTIFICATION;
	// Do not change the HIGH priority to SYSTEM - or it won't play while there's an incoming call.
	// Also don't change to MUSIC, for the same reason.
	// NOTIFICATION doesn't always work. At minimum, on an incoming call, at least sometimes, the volume can't be set.
	// Just let everything on ALARM (except with connected headphones).
	// Easier and always works (alarms have top priority even over Do Not Disturb, at least on Oreo 8.1).
	// EDIT: It's now on RING for us to be able to change the volume easily, as opposite to ALARM (which I will leave
	// with CRITICAL, since it's to be spoken at full volume always.
	// Leave on RING since this way the volume can be set independently of the music playing. On headphones such thing
	// can't be done independently though. Pity.
	// EDIT 2: Changed to NOTIFICATION because of tablets. They might not use RING (miTab Advance doesn't). I can't
	// change it manually, but the app still speaks. With NOTIFICATION, I guess it should work everywhere (there are
	// always notifications). Alarms exist too anyways and the music stream too, of course. So all cool, I think.
	private static final int AUD_STREAM_HEADPHONES = AudioManager.STREAM_MUSIC;
	// With other types, the audio may play on both speakers and headphones, and others, only on speakers. MUSIC plays
	// on either speakers or headphones, depending on if headphones are connected or not, and doesn't play on both.

	AudioAttributes audioAttributes = null; // No problem in being null since it will only be used if TTS is initialized
	                                        // correctly - and if it did, then audioAttributes was initialized decently.

	// The module only runs if this returns non-null. This is just to suppress warnings along the class (even though a
	// warning will appear on isSupported() because "it's never null" --> it's not when the module starts...
	static final NotificationManager notificationManager = UtilsContext.getNotificationManager();
	// The audio support is checked internally. If there's no audio support, the speeches are notified and not attempted
	// to be spoken.
	@NonNull final AudioManager audioManager = (AudioManager) UtilsContext.getSystemService(Context.AUDIO_SERVICE);

	///////////////////////////////////////////////////////////////
	// IModuleInst stuff
	private boolean is_module_destroyed = false;
	@Override
	public boolean isFullyWorking() {
		if (is_module_destroyed) {
			return false;
		}

		// If the speech stops working, it will keep attempting to reconnect it back. This one must ALWAYS be running.
		// It must NEVER be restarted, because it's the only way of communication by the assistant, and also because
		// this module manages notifications as well, for when the speech is not available (so there's always
		// communication, and it's this module that takes care of it).
		// EDIT: and there will be no restarts --> as long as the module is correctly programmed. But it must be on a
		// thread... Can't be all in the same thread! Especially all this stuff to process the speeches.
		return UtilsGeneral.isThreadWorking(main_handlerThread) && UtilsGeneral.isThreadWorking(infinity_thread);
	}
	@Override
	public void destroy() {
		try {
			UtilsContext.getContext().unregisterReceiver(broadcastReceiver);
		} catch (final IllegalArgumentException ignored) {
		}
		UtilsGeneral.quitHandlerThread(main_handlerThread);

		SpeechQueue.SpeechQueue.clearQueue();

		if (tts != null) {
			tts.stop();
			tts.shutdown();
			tts = null;
		}

		is_module_destroyed = true;
	}
	@Override
	public int wrongIsSupported() {return 0;}
	/**.
	 * @return read all here {@link IModuleInst#wrongIsSupported()} */
	public static boolean isSupported() {
		// The module checks internally if there is audio output available or not and if there isn't, it uses
		// notifications instead, so those must always be present (also in case of vibration mode).
		return true;
	}
	// IModuleInst stuff
	///////////////////////////////////////////////////////////////

	/**
	 * <p>Main class constructor.</p>
	 */
	public Speech2() {
		///////////////////////////////////
		// Notification

		final PendingIntent pendingIntent = PendingIntent.getBroadcast(UtilsContext.getContext(), 0,
				new Intent(ACTION_CLEAR_NOTIF_MSGS), PendingIntent.FLAG_CANCEL_CURRENT |
						(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0));
		final ObjectClasses.NotificationInfo notificationInfo = new ObjectClasses.NotificationInfo(
				GL_CONSTS.CH_ID_SPEECHES,
				"Not spoken text",
				"Text skipped in speech for whatever reason, like no device sound",
				NotificationCompat.PRIORITY_MAX,
				"V.I.S.O.R. notifications",
				"",
				pendingIntent
		);
		speeches_notif_builder = UtilsNotifications.getNotification(notificationInfo).
				setVisibility(NotificationCompat.VISIBILITY_PRIVATE).
				setDeleteIntent(pendingIntent).
				setAutoCancel(true);

		///////////////////////////////////
		// TTS

		main_handlerThread.start();
		main_handler = new Handler(main_handlerThread.getLooper());

		// By the way, this must ALWAYS be called here. The entire module expects the tts object to never be null after
		// the constructor is called.
		initializeTts(true);

		infinity_thread.start();
	}

	private final Thread infinity_thread = new Thread(() -> {
		GPTComm.setPreparations(System.currentTimeMillis());
		while (true) {
			// Keep getting the next sentence to speak from the server
			String speak = GPTComm.getNextSpeechSentence();
			if (speak.isEmpty() || GPTComm.END_ENTRY.equals(speak)) {
				try {
					Thread.sleep(1000);
				} catch (final InterruptedException ignored) {
				}

				continue;
			}

			speak(speak, PRIORITY_USER_ACTION, MODE_DEFAULT);
		}
	});

	/**
	 * <p>Initializes the {@link TextToSpeech} object.</p>
	 *
	 * @param from_constructor true if the call to this function was made from the constructor, false otherwise - this
	 *                         way the function can execute tasks that can only be done in the module initialization
	 */
	void initializeTts(final boolean from_constructor) {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
			tts = null;
		}

		tts = new TextToSpeech(UtilsContext.getContext(), status -> {
			success_if:
			if (status == TextToSpeech.SUCCESS) {
				tts.setOnUtteranceProgressListener(new TtsUtteranceProgressListener());

				if (!isTtsAvailable()) {
					if (from_constructor || tts_working) {
						// This won't speak - will show a notification instead, so no problem in calling
						// from here. Also, it will speak only the first time the problem arises, not every
						// time until it's fixed.
						speak("ATTENTION - TTS IS NOT AVAILABLE. The Speech module will keep retrying until a " +
								"TTS voice is detected.", PRIORITY_CRITICAL, 0);

						// todo This is not getting here when I load a wrong folder to disable all voices on Ivona TTS...

					}

					tts_working = false;

					break success_if;
				}

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					// Set the audio attributes to use
					final AudioAttributes.Builder builder = new AudioAttributes.Builder();
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
						builder.setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_NONE);
					}
					builder.setContentType(AudioAttributes.CONTENT_TYPE_SPEECH);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
						builder.setUsage(AudioAttributes.USAGE_ASSISTANT);
					} else {
						builder.setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE); // Kind of
					}
					//builder.setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED); - Don't use: "However, when the
					// track plays it uses the System (Ringer) volume as the master volume control. Not the media
					// volume as I would expect." (this is about setting that flag - changes the audio stream).
					// That's to be changed depending on the speech priority only and the enforcing of the audio
					// is done manually though DND, volume, and the stream. So don't set this flag.
					audioAttributes = builder.build();
					//tts.setAudioAttributes(audioAttributes); - Don't enable this... Makes the app say
					// "Ready[, sir - this part is cut]" if the phone (BV9500) is in Vibrating mode. It starts
					// speaking and it's interrupted - but onDone() is never called, only onStop().
					// Which means, if it doesn't work well in at least one case, don't enable.
				}

				tts_working = true;
			} else {
				tts_working = false;
			}

			// Broadcast the ready action and register the receiver after everything is ready. If TTS is not
			// available, at least there's the notification and activity showing the history.
			registerRecvBcastReady();
		});
	}

	/**
	 * <p>Sets {@link #user_changed_volume} to true in case the audio stream of the changed volume equals the audio
	 * stream currently being used to speak.</p>
	 * <p>It's also a bit smarter than that, as the assistant itself changes the volume sometimes to speak. This function
	 * attempts to differentiate both volume changes (user vs assistant).</p>
	 *
	 * @param audio_stream the audio stream of which the volume changed
	 */
	void setUserChangedVolumeTrue(final int audio_stream) {
		// Detect user changes only after some time after the assistant changed the volume to speak, since the first
		// volume change to be detected would be the assistant himself changing the volume - in case he changed the
		// volume (otherwise the first and next changes will be user changes). This way we wait until the broadcasts for
		// the assistant volume change are received and only after those we start detecting actual user changes of volume
		// (pity there's no way of getting the package which changed the volume or something... - only complicates things).
		// Also, detect only if the assistant is speaking, of course.
		SpeechQueue.Speech curr_speech = SpeechQueue.SpeechQueue.getSpeech(current_speech_id);

		if (curr_speech == null) {
			return;
		}

		// If the assistant is speaking, check the audio stream always. Though...
		if (assist_will_change_volume) {
			if (System.currentTimeMillis() <= assist_changed_volume_time + VOLUME_CHANGE_INTERVAL) {
				if ((volumeDndState.audio_stream != VolumeDndState.DEFAULT_VALUE && audio_stream == volumeDndState.audio_stream)
						|| (audio_stream == curr_speech.getAudioStream())) {
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

		if ((volumeDndState.audio_stream != VolumeDndState.DEFAULT_VALUE && audio_stream == volumeDndState.audio_stream)
				|| (audio_stream == curr_speech.getAudioStream())) {
			// As soon as a user volume change is detected, set the variable to true to indicate the user changed
			// the volume.
			// Also reset only if the stream is the correct one, because the system broadcasts multiple streams at
			// once, and only one of them may be the one we're looking for.
			user_changed_volume = true;
		}
	}

	/**
	 * <p>Adds a speech text to the speeches notification and notifies it.</p>
	 *
	 * @param txt_to_speak the text of the speech
	 */
	private void addSpeechToNotif(final String txt_to_speak) {
		speech_notif_speeches.add(txt_to_speak);

		final int notif_speeches_size = speech_notif_speeches.size();
		if (notif_speeches_size == 1) {
			speeches_notif_builder.setStyle(null);
			speeches_notif_builder.setContentText(speech_notif_speeches.get(0));
		} else {
			final NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
			for (int i = notif_speeches_size - 1; i >= 0; --i) {
				inboxStyle.addLine(speech_notif_speeches.get(i));
			}
			speeches_notif_builder.setStyle(inboxStyle);
			speeches_notif_builder.setContentText(notif_speeches_size + " unread notifications");
		}
		speeches_notif_builder.setWhen(System.currentTimeMillis());

		notificationManager.notify(GL_CONSTS.NOTIF_ID_SPEECHES, speeches_notif_builder.build());
	}

	/**
	 * <p>Checks if the TTS engine is ready to speak.</p>
	 *
	 * @return true if yes, false otherwise
	 */
	boolean isTtsAvailable() {
		 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			final Set<Voice> voices = tts.getVoices();
			if (voices == null || voices.isEmpty() || tts.getVoice() == null || tts.getVoice().getLocale() == null) {
				return false;
			}
		} else {
			if (tts.getLanguage() == null) {
				return false;
			}
		}

		return true;
	}

	/**
	 * <p>Skips the currently speaking speech.</p>
	 *
	 * @return same as in {@link #ttsStop(boolean)}}
	 */
	int skipCurrentSpeech() {
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
	public static final int PRIORITY_LOW = SpeechQueue.SpeechQueue.PRIORITY_LOW;
	public static final int PRIORITY_MEDIUM = SpeechQueue.SpeechQueue.PRIORITY_MEDIUM;
	public static final int PRIORITY_USER_ACTION = SpeechQueue.SpeechQueue.PRIORITY_USER_ACTION;
	public static final int PRIORITY_HIGH = SpeechQueue.SpeechQueue.PRIORITY_HIGH;
	public static final int PRIORITY_CRITICAL = SpeechQueue.SpeechQueue.PRIORITY_CRITICAL;
	// NEVER TEST IF A VARIABLE HAS THE MODEx_DEFAULT BITS SET!!! That way we can set 0 as the all-default instead of
	// having to put MODE1_DEFAULT | MODE2_DEFAULT.
	public static final int MODE_DEFAULT = SpeechQueue.SpeechQueue.MODE_DEFAULT;
	public static final int MODE1_NO_NOTIF = SpeechQueue.SpeechQueue.MODE1_NO_NOTIF;
	public static final int MODE1_ALWAYS_NOTIFY = SpeechQueue.SpeechQueue.MODE1_ALWAYS_NOTIFY;
	public static final int MODE2_BYPASS_NO_SND = SpeechQueue.SpeechQueue.MODE2_BYPASS_NO_SND;
	/**
	 * <p>Speaks the given text.</p>
	 * <br>
	 * <p>When called multiple times, the speeches of the same priority will be spoken in the order the function was
	 * called. With different priorities, higher priority speeches will be spoken first and only then will the lower
	 * priorities speak.</p>
	 * <br>
	 * <p>From the first to the last speech, normally the assistant will try to request audio focus and set the
	 * volume of the stream to use to a predetermined level. After all speeches from a same audio stream are finished,
	 * what the assistant changed before starting to speak will be undone and all will be left as it was (unless the user
	 * changed one of the changed things, and in that case, the changed thing(s) will remain changed). If the
	 * priority is {@link #PRIORITY_CRITICAL}, before both things I said, the assistant will also attempt to disable any
	 * Do Not Disturb setting. The volume will also be set to the maximum the chosen audio stream can handle (which will
	 * be {@link #AUD_STREAM_PRIORITY_CRITICAL}).</p>
	 * <br>
	 * <p>If a speech is requested, this speech module had already been initialized, and the previously selected TTS
	 * engine was uninstalled or there was some error attempting to send text to the engine, this module will restart
	 * internally and another TTS engine will be used to speak (in case there's no engine available after restart, it
	 * will continue to restart until it finds an available engine to speak again). It will only come back to the
	 * preferred engine again if the module is restarted manually (for example by a force stop of the app).</p>
	 * <br>
	 * <p>If {@code AudioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL} right before speaking, the speech
	 * will be aborted and removed from the internal queues. Though {@code task_id} will still be executed.
	 * <br>
	 * <p>All the {@code PRIORITY_} constants except {@link #PRIORITY_USER_ACTION} are to be used only for speeches that
	 * are not a response to a user action - for example automated tasks or broadcast receivers.</p>
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
	 * <p>- {@link #MODE_DEFAULT} --> for {@code mode}: all default modes</p>
	 * <p>- {@link #MODE1_NO_NOTIF} --> for {@code mode}: don't notify even if he can't speak</p>
	 * <p>- {@link #MODE1_ALWAYS_NOTIFY} --> for {@code mode}: always notify, even if he can speak</p>
	 * <p>- {@link #MODE2_BYPASS_NO_SND} --> for {@code mode}: bypass the ringer mode in case it's not set to normal</p>
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
	 * @param mode an OR operation of the constants - no mode must be ORed with another one of the same name number (the
	 * constants with value 0 are optional). 0 can be used to set operation mode as default for everything. NOTE: the
	 * {@code speech_priority} overrides this parameter (for example if 0 is chosen here and CRITICAL on priority, the
	 * sound will be bypassed)
	 *
	 * @return same as in {@link TextToSpeech#speak(CharSequence, int, Bundle, String)} or
	 * {@link TextToSpeech#speak(String, int, HashMap)} (depending on the device API level) in case the speech began
	 * being spoken immediately; one of the constants otherwise.
	 */
	int speak(@NonNull final String txt_to_speak, final int speech_priority, final int mode) {
		return speakInternal(txt_to_speak, speech_priority, mode, null, -1);
	}

	/**
	 * <p>Same as in {@link #speak(String, int, int)}, but with additional parameters to be used only
	 * internally to the class - this is the main speak() method.</p>
	 *
	 * @param utterance_id the utterance ID to be used to register or re-register the given speech in case it's already
	 * in the lists, or null if it's not already in the lists
	 * @param task_id same as in {@link TasksList#removeTask(int)}
	 */
	int speakInternal(final String txt_to_speak, final int speech_priority, final int mode,
					  @Nullable final String utterance_id, final int task_id) {

		// todo Make a way of getting him not to listen what he himself is saying... Or he'll hear himself and process
		// that, which is stupid. For example by cancelling the recognition when he's speaking or, or removing what he
		// said from the string of what he listened or something (2nd one preferable - else we can't tell him to shut
		// up while he's speaking, because he disabled the recognition).
		// When this is implemented, don't forget to check if he's speaking on the speakers or on headphones. If
		// it's on headphones, no need to cancel the recognition. If it's on speakers, not sure. If the volume is
		// high enough, he wouldn't hear us anyways. If we lower the volume, he could hear us.

		// The utteranceIDs (their indexes in the array) are used by me to identify the corresponding Runnable and speech.

		int actual_mode = mode;
		if ((boolean) UtilsRegistry.getData(RegistryKeys.K_SPEECH_ALWAYS_NOTIFY, true)) {
			actual_mode |= MODE1_ALWAYS_NOTIFY;
		}

		if (!UtilsCheckHardwareFeatures.isAudioOutputSupported() && (actual_mode & MODE1_NO_NOTIF) == 0) {
			// If there's no audio output support, just put a notification and that's it.
			addSpeechToNotif(txt_to_speak);

			return TextToSpeech.SUCCESS;
		}
		// Else, if it's supported, let it check by itself if the TTS is ready or not. If it's not... (just below).

		// Reload the TTS instance in case the default voice and/or engine have changed (also if it was uninstalled or
		// disabled or something else that would prevent VISOR from speaking).
		checkReloadTts();

		String utterance_id_to_use = "";
		if (utterance_id != null) {
			utterance_id_to_use = utterance_id;
		}
		if (SpeechQueue.SpeechQueue.getSpeech(utterance_id_to_use) == null) {
			// If it's a new speech, add to the lists.
			final int audio_stream;
			if (speech_priority == Speech2.PRIORITY_CRITICAL) {
				audio_stream = AUD_STREAM_PRIORITY_CRITICAL;
			} else if (speech_priority == Speech2.PRIORITY_HIGH) {
				audio_stream = AUD_STREAM_PRIORITY_HIGH;
			} else {
				if (UtilsGeneral.areExtSpeakersOn()) {
					audio_stream = AUD_STREAM_HEADPHONES;
				} else {
					audio_stream = AUD_STREAM_PRIORITY_OTHERS_SPEAKERS;
				}
			}
			utterance_id_to_use = SpeechQueue.SpeechQueue.addSpeech(txt_to_speak, utterance_id_to_use,
					System.currentTimeMillis(), speech_priority, actual_mode, audio_stream, task_id);
		}

		if (current_speech_id.isEmpty()) {
			// The function only gets here if there was no speech already taking place.

			// Set the current_speech_id to the speech getting ready to be spoken immediately, so that if any other
			// speech is requested goes at the same time, goes directly to the lists (race condition, and this is
			// supposed to fix it).
			current_speech_id = utterance_id_to_use;
			SpeechQueue.Speech curr_speech = SpeechQueue.SpeechQueue.getSpeech(current_speech_id);

			final int tts_error_code = sendTtsSpeak(curr_speech.getText(), curr_speech.getID(),
					curr_speech.getAudioStream());
			if (tts_error_code != TextToSpeech.SUCCESS) {
				initializeTts(false); // Restart the TTS instance if it's not working.

				if ((curr_speech.getMode() & MODE1_NO_NOTIF) == 0) {
					// In case some error occurred (for example, the engine was uninstalled), notify the speech, "call
					// skipCurrentSpeech()", and reinitialize the TTS object.
					addSpeechToNotif(curr_speech.getText());
				}

				// Custom implementation of skipCurrentSpeech(). The difference here is that as tts.stop() will return
				// ERROR, this will ignore it (it's ERROR not because it couldn't stop it because it was running - no,
				// in this case it's because TTS is not even working properly, which cannot be predicted with just
				// ERROR and hence here must be a difference implementation knowing that here specifically ERROR means
				// TTS not working and not failure in stopping a speaking speech).
				current_speech_id = "";
				onStop(utterance_id_to_use, true);
			}

			return tts_error_code;
		} else {
			speeches_on_lists = true;

			// If there's a speech already being spoken, the new one is just added to the list (when the current one
			// stops, it will take care of starting the next ones on the queues).
			// Except if the new speech has a higher priority than the current one. In that case, the current one
			// stops temporarily to give place to the new one.
			if (speech_priority > SpeechQueue.SpeechQueue.getSpeech(current_speech_id).getPriority()) {
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
	 * <p>Attention: not to be called except from inside {@link #speak(String, int, boolean, boolean, Integer)}.</p>
	 *
	 * @param txt_to_speak same as in {@link #speak(String, int, boolean, boolean, Integer)}
	 * @param utterance_id the utterance ID to register the speech
	 * @param audio_stream the audio stream to be used to speak the speech
	 *
	 * @return same as in {@link TextToSpeech}'s speak() methods
	 */
	int sendTtsSpeak(@NonNull final String txt_to_speak, @NonNull final String utterance_id,
						   final int audio_stream) {
		if (!tts_working || !isTtsAvailable()) {
			return TextToSpeech.ERROR;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Bundle bundle = new Bundle(1);
			bundle.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, audio_stream);

			return tts.speak(txt_to_speak, TextToSpeech.QUEUE_ADD, bundle, utterance_id);
		} else {
			HashMap<String, String> hashmap = new LinkedHashMap<>(2);
			hashmap.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(audio_stream));
			hashmap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utterance_id);

			return tts.speak(txt_to_speak, TextToSpeech.QUEUE_ADD, hashmap);
		}
	}

	/**
	 * <p>This checks if the default voice and engine was changed and in that case, changes the assistant voice and
	 * engine to the new default.</p>
	 * <p>Useful at minimum when the phone starts and third-party engines are not ready yet when the app starts.</p>
	 */
	private void checkReloadTts() {
		boolean reload_tts = false;
		final String current_engine = tts.getCurrentEngine();
		if (current_engine == null) {
			reload_tts = true;
		} else {
			reload_tts = !current_engine.equals(tts.getDefaultEngine());
		}
		if (!reload_tts) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				final Voice voice = tts.getVoice();
				if (voice == null || !voice.equals(tts.getDefaultVoice())) {
					reload_tts = true;
				}
			} else {
				final Locale language = tts.getLanguage();
				if (language == null || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 &&
								!language.equals(tts.getDefaultLanguage()))) {
					reload_tts = true;
				}
			}
		}
		if (reload_tts) {
			initializeTts(false);
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
		// The current speech must be cleared so onDone() (in case it's called) knows the speech was force-stopped since
		// the current speech can never be empty when onDone is called (how was it called if there's no current speech)
		// - except if the speech was force stopped. That has a different implementation and must be processed
		// separately. This is a way for onDone to do nothing, since the custom onStop will - which will also make the
		// the program flow continue to be only one (onDone() is called in another thread but nothing is done, so we
		// continue here to onStop()). Read also above the if statement on onDone().
		final String old_speech_id = current_speech_id;
		current_speech_id = "";
		if (tts.stop() == TextToSpeech.ERROR) {
			current_speech_id = old_speech_id;

			return TextToSpeech.ERROR;
		}

		onStop(old_speech_id, skip_speech);

		return TextToSpeech.SUCCESS;
	}

	/**
	 * <p>Sets changes needed to be done for the assistant to speak.</p>
	 * <p>List:</p>
	 * <p>- set a new volume of the used audio stream, in case it needs to be changed</p>
	 * <p>- request the audio focus</p>
	 * <p>- set a new Interruption Filter, in case it needs to be changed</p>
	 * <p>- set a new ringer mode, in case it needs to be changed</p>
	 */
	private void setToSpeakChanges(@NonNull final String utterance_id) {
		SpeechQueue.Speech curr_speech = SpeechQueue.SpeechQueue.getSpeech(utterance_id);

		if (curr_speech.getPriority() == PRIORITY_CRITICAL) {
			audioFocus(true);

			// Set Do Not Disturb to ALARMS (emergency speech)
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (notificationManager.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_ALARMS) {
					volumeDndState.old_interruption_filter = notificationManager.getCurrentInterruptionFilter();
					volumeDndState.new_interruption_filter = NotificationManager.INTERRUPTION_FILTER_ALARMS;

					notificationManager.setInterruptionFilter(volumeDndState.new_interruption_filter);
				}
			}

			// Set the volume
			final int current_volume = audioManager.getStreamVolume(curr_speech.getAudioStream());
			int new_volume = (int) UtilsRegistry.getData(RegistryKeys.K_SPEECH_CRITICAL_VOL, true);
			int max_volume = audioManager.getStreamMaxVolume(curr_speech.getAudioStream());
			int actual_new_volume = new_volume * max_volume / 100;
			if (current_volume < actual_new_volume) {
				volumeDndState.audio_stream = curr_speech.getAudioStream();
				volumeDndState.old_volume = audioManager.getStreamVolume(curr_speech.getAudioStream());

				setResetWillChangeVolume(true);

				audioManager.setStreamVolume(curr_speech.getAudioStream(), actual_new_volume,
						AudioManager.FLAG_FIXED_VOLUME | AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE | AudioManager.FLAG_SHOW_UI);
			}
		} else {
			if ((curr_speech.getMode() & MODE2_BYPASS_NO_SND) != 0) {
				volumeDndState.old_ringer_mode = audioManager.getRingerMode();
				audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

				// Set Do Not Disturb to ALL (normal speech)
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					if (notificationManager.getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_ALL) {
						volumeDndState.old_interruption_filter = notificationManager.getCurrentInterruptionFilter();
						volumeDndState.new_interruption_filter = NotificationManager.INTERRUPTION_FILTER_ALL;

						notificationManager.setInterruptionFilter(volumeDndState.new_interruption_filter);
					}
				}
			}

			if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
				audioFocus(true);

				// Set the volume
				// Put the volume in medium before speaking in case the ringer is NORMAL and the stream is not
				// RING or NOTIFICATION (for example with STREAM_MUSIC).
				final int current_volume = audioManager.getStreamVolume(curr_speech.getAudioStream());
				final int max_volume = audioManager.getStreamMaxVolume(curr_speech.getAudioStream());
				final int new_volume = (int) UtilsRegistry.getData(RegistryKeys.K_SPEECH_NORMAL_VOL, true);
				int actual_new_volume = new_volume * max_volume / 100;
				if (current_volume < actual_new_volume) {
					volumeDndState.audio_stream = curr_speech.getAudioStream();
					volumeDndState.old_volume = current_volume;

					setResetWillChangeVolume(true);

					audioManager.setStreamVolume(curr_speech.getAudioStream(), actual_new_volume,
							AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE | AudioManager.FLAG_SHOW_UI);
				}
			}
		}

		focus_volume_dnd_done = true;
	}

	/**
	 * <p>Resets changes made for the assistant to speak.</p>
	 * <p>List:</p>
	 * <p>- reset the old volume of the used audio stream, in case it had to be changed</p>
	 * <p>- reset the audio focus</p>
	 * <p>- reset the old Interruption Filter, in case it had to be changed</p>
	 * <p>- reset the old ringer mode, in case it had to be changed</p>
	 *
	 *
	 * <p>Sets changes needed to be done for the assistant to speak.</p>
	 * <p>List:</p>
	 * <p>- set a new volume of the used audio stream, in case it needs to be changed</p>
	 * <p>- request the audio focus</p>
	 * <p>- set a new Interruption Filter, in case it needs to be changed</p>
	 * <p>- set a new ringer mode, in case it needs to be changed</p>
	 */
	void resetToSpeakChanges() {
		setResetWillChangeVolume(false);

		// Reset the volume
		if (volumeDndState.old_volume != VolumeDndState.DEFAULT_VALUE) {
			boolean carry_on = false;
			if (stream_active_before_begin_all_speeches == volumeDndState.audio_stream) {
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
				if (audioManager.getStreamVolume(volumeDndState.audio_stream) != volumeDndState.old_volume) {
					try {
						audioManager.setStreamVolume(volumeDndState.audio_stream, volumeDndState.old_volume,
								AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE | AudioManager.FLAG_SHOW_UI);
					} catch (final SecurityException ignored) {
						// Toggles DND, so I guess that would be because the volume is 0, maybe. If I'm right, then
						// I just need to increase 1 to put it one level above, which would be out of DND.
						audioManager.setStreamVolume(volumeDndState.audio_stream, volumeDndState.old_volume + 1,
								AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE | AudioManager.FLAG_SHOW_UI);
					}
				}
			}
		}

		// Reset the ringer mode
		if (volumeDndState.old_ringer_mode != VolumeDndState.DEFAULT_VALUE) {
			audioManager.setRingerMode(volumeDndState.old_ringer_mode);
		}

		// Reset Do Not Disturb
		if (volumeDndState.old_interruption_filter != VolumeDndState.DEFAULT_VALUE) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (notificationManager.isNotificationPolicyAccessGranted()) {
					// Only reset if the interruption mode was not changed while the assistant was speaking.
					if (notificationManager.getCurrentInterruptionFilter() == volumeDndState.new_interruption_filter) {
						notificationManager.setInterruptionFilter(volumeDndState.old_interruption_filter);
					}
				}
			}
		}

		// Reset the audio focus
		audioFocus(false);

		volumeDndState.setDefaultValues();

		focus_volume_dnd_done = false;
	}

	/**
	 * <p>Requests audio focus depending on the speech priority.</p>
	 * <br>
	 * <p><em>Call only AFTER having {@link #current_speech_id} updated, as it's used here!</em></p>
	 *
	 * @param request true to request the audio focus, false to abandon the audio focus
	 */
	void audioFocus(final boolean request) {
		SpeechQueue.Speech curr_speech = SpeechQueue.SpeechQueue.getSpeech(current_speech_id);
		if (request) {
			final int duration_hint;
			if (curr_speech.getPriority() == PRIORITY_CRITICAL) {
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
				audioManager.requestAudioFocus(onAudioFocusChangeListener, curr_speech.getAudioStream(),
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
	final AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener =
			focusChange -> {
				// No need to implement
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
	 * {@link UtteranceProgressListener} (means for a speech) to be executed in that case - unless there's a request to
	 * bypass a no-sound setting, like Do Not Disturb or Vibrating mode, for example.</p>
	 */
	void rightBeforeSpeaking(@NonNull final String utterance_id) {
		SpeechQueue.Speech curr_speech = SpeechQueue.SpeechQueue.getSpeech(utterance_id);

		if (curr_speech == null) {
			// If the speech was removed from the lists before it could be spoken, skip it and return (already happened,
			// for some reason).
			skipCurrentSpeech();

			return;
		}

		boolean skip_speaking = false;

		// Check the ringer mode, which must be NORMAL, otherwise the assistant will not speak - unless the speech is a
		// CRITICAL speech (except if it's to bypass a no-sound mode).
		if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			skip_speaking = (curr_speech.getPriority() != PRIORITY_CRITICAL &&
					(curr_speech.getMode() & MODE2_BYPASS_NO_SND) == 0);
		}

		if (skip_speaking) {
			new Thread(TasksList.removeTask(curr_speech.getTaskID()).runnable).start();
			UtilsApp.sendInternalBroadcast(new Intent(CONSTS_BC_Speech.ACTION_AFTER_SPEAK_ID).
					putExtra(CONSTS_BC_Speech.EXTRA_AFTER_SPEAK_ID_1, curr_speech.getID()));

			if ((curr_speech.getMode() & MODE1_NO_NOTIF) == 0) {
				// Notify the speech before skipping it (don't skip the notification).
				addSpeechToNotif(curr_speech.getText());
			}

			skipCurrentSpeech();
		} else {
			// If it's to speak, prepare the app to speak.
			if (!focus_volume_dnd_done) {
				setToSpeakChanges(utterance_id);
				if (!speeches_on_lists) {
					if (AudioSystem.isStreamActive(curr_speech.getAudioStream(), 0)) { // 0 == Now
						stream_active_before_begin_all_speeches = volumeDndState.audio_stream;
					}
				}
			}

			if ((curr_speech.getMode() & MODE1_ALWAYS_NOTIFY) != 0) {
				addSpeechToNotif(curr_speech.getText());
			}
		}
	}

	/**
	 * <p>The {@link UtteranceProgressListener} to be used for the speech.</p>
	 */
	final class TtsUtteranceProgressListener extends UtteranceProgressListener {

		@Override
		public void onStart(final String utteranceId) {
			System.out.println("^/^/^/^/^/^/^/^/^/^/^/^/^/^/^");
			rightBeforeSpeaking(utteranceId);
			System.out.println("^/^/^/^/^/^/^/^/^/^/^/^/^/^/^");
		}

		@Override
		public void onDone(final String utteranceId) {
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
			System.out.println(utteranceId);
			System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");

			// If the utterance ID is empty, then it means it was force stopped and it's to be done nothing.
			// When tts.stop() is called, I think onDone was supposed to be called (I think it's supposed to be always
			// called?). Though, on Lollipop 5.1 it is called, but not on Oreo 8.1. So as the custom onStop will be
			// called first, it will put empty the utterance ID of the current speech and tell onDone that what was to
			// be done, was already done by onStop(). This in case onDone is called. If it's not, no matter - onStop()
			// already did onDone()'s job as a failsafe measure.
			if (!current_speech_id.isEmpty()) {
				current_speech_id = "";
				speechTreatment(utteranceId);
			}
		}

		// Up to API 20
		@Override
		public void onError(final String utteranceId) {
			System.out.println("^-^-^-^-^-^-^-^-^-^-^-^-^-^-^");
			System.out.println(utteranceId);
			System.out.println("^-^-^-^-^-^-^-^-^-^-^-^-^-^-^");

			// The if statement below has the same reason as the reason on onDone for the same if statement. It's a
			// precaution, since I don't know when there's an error, which of onDone and onError will be called, and if
			// both are called, which one is called first. So in any case, the first to be called will stop the other
			// one from being called this way.
			if (!current_speech_id.isEmpty()) {
				current_speech_id = "";
				speechTreatment(utteranceId);
			}
		}

		// As of API 21
		@Override
		public void onError(final String utteranceId, final int errorCode) {
			super.onError(utteranceId, errorCode);
			System.out.println("^*^*^*^*^*^*^*^*^*^*^*^*^*^*^");
			System.out.println(errorCode);
			System.out.println("^*^*^*^*^*^*^*^*^*^*^*^*^*^*^");

			// The super call up here just calls the other onError() method. So don't do anything here (except printing
			// the error).
		}

		// As of API 23
		@Override
		public void onStop(final String utteranceId, final boolean interrupted) {
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
		System.out.println(utteranceId);
		System.out.println("^+^+^+^+^+^+^+^+^+^+^+^+^+^+^");

		// current_speech_id is already null here - this onStop() is only called from ttsStop(), which empties
		// current_speech_id by itself. Use utteranceId to get info about the speech that was stopped.
		if (skip_speech) {
			// If it's to skip the speech, just stop the current speech with tts.stop(), which will delete all the
			// speeches on its list (which are none, according with the Speech2 implementation - nothing is ever on the
			// TextToSpeech list except the current speech, and all speeches are always on the Speech2 lists).
			speechTreatment(utteranceId);
		} else {
			SpeechQueue.SpeechQueue.getSpeech(utteranceId).rephraseInterrSpeech();
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
	 *                    {@link #onStop(String, boolean)}), or an empty string to signal the function not to remove any
	 *                    speech from the queues and only start the next one
	 */
	void speechTreatment(@NonNull final String utteranceId) {
		// Main note: everything that calls this function empties current_speech_id first.

		// Why is this check here and not just the removal? Refer to the custom onStop().
		if (!utteranceId.isEmpty()) {
			// Won't happen, except from the custom onStop() - or the speech wouldn't have taken place.
			last_speech = SpeechQueue.SpeechQueue.removeSpeech(utteranceId);
			// todo It's getting null here on API 15 and 19 at least.... (on Oreo it doesn't)
			//  EDIT: not on 15 anymore... hmm...
			//  Synchronize the class... ('synchronize' keyword)
			//  It's null and the error is only on the .txt_to_speak usage. So inside the removeSpeechById all went
			//  fine, which means the speech was really not on the lists anymore --> thread mess?
			// After this, check it again on API 15 and 19. On 19 now the speech is not working well. These errors
			// might be the reason. VISOR won't speak nor throw a notification. Not cool.

			// todo Remake this module from scratch.... And organize it this time

			// If there's an ID of a Task to run after the speech is finished, run it in a new thread.
			new Thread(TasksList.removeTask(last_speech.getTaskID()).runnable).start();

			UtilsApp.sendInternalBroadcast(new Intent(CONSTS_BC_Speech.ACTION_AFTER_SPEAK_ID).
					putExtra(CONSTS_BC_Speech.EXTRA_AFTER_SPEAK_ID_1, last_speech.getID()));
		}

		SpeechQueue.Speech next_speech = SpeechQueue.SpeechQueue.getNextSpeech(-1);
		if (next_speech != null) {
			// If there are more speeches and they use the same audio stream, don't reset the volume and abandon
			// the audio focus. Do that only if the stream to be used next is different (reset the previous one).
			// Also, check if the assistant changed the volume at all. If it didn't, don't reset anything (that's
			// checked by verifying if the audio stream is the DEFAULT_VALUE or not).
			if (volumeDndState.audio_stream != VolumeDndState.DEFAULT_VALUE &&
					volumeDndState.audio_stream != next_speech.getAudioStream()) {
				if (focus_volume_dnd_done) {
					resetToSpeakChanges();
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

			speakInternal(next_speech.getText(), next_speech.getPriority(), next_speech.getMode(), next_speech.getID(),
					next_speech.getTaskID());

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
				Thread.sleep(500);
			} catch (final InterruptedException ignored) {
			}

			return;
		}

		allSpeechesFinished();
	}

	/**
	 * <p>Things to be done when there all speeches were taken care of (spoken or skipped) and there are no more in the
	 * lists.</p>
	 */
	private void allSpeechesFinished() {
		speeches_on_lists = false;

		// Since there are no more speeches, reset the stream volume and abandon the audio focus of the last used audio
		// stream.
		if (focus_volume_dnd_done) {
			resetToSpeakChanges();
		}

		if (assist_will_change_volume) {
			// Same reason than on speechTreatment().
			setResetWillChangeVolume(false);
		}

		// Doesn't matter if the user changed the volume or not if all the speeches have been finished.
		user_changed_volume = false;
	}




	/**
	 * <p>Register the module's broadcast receiver AND broadcast {@link CONSTS_BC_Speech#ACTION_READY}.</p>
	 * <p>Call how many times needed - the function only executes if it's the first time it's called.</p>
	 */
	void registerRecvBcastReady() {
		final IntentFilter intentFilter = new IntentFilter();

		intentFilter.addAction(AudioManager.VOLUME_CHANGED_ACTION);

		intentFilter.addAction(CONSTS_BC_Speech.ACTION_CALL_SPEAK);
		intentFilter.addAction(CONSTS_BC_Speech.ACTION_SKIP_SPEECH);
		intentFilter.addAction(CONSTS_BC_Speech.ACTION_REMOVE_SPEECH);
		intentFilter.addAction(CONSTS_BC_Speech.ACTION_SAY_AGAIN);

		intentFilter.addAction(ACTION_CLEAR_NOTIF_MSGS);

		try {
			UtilsContext.getContext().registerReceiver(broadcastReceiver, intentFilter, null, main_handler);

			// Don't take this out of here. This way, this function can be called as many times as needed and it will
			// only act if it's the first time - else, the exception will be thrown (the receiver is already registered)
			// and this line below won't get to be executed.
			UtilsApp.sendInternalBroadcast(new Intent(CONSTS_BC_Speech.ACTION_READY));
		} catch (final IllegalArgumentException ignored) {
		}
	}

	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
			if (intent == null || intent.getAction() == null) {
				return;
			}

			System.out.println("PPPPPPPPPPPPPPPPPP-Speech2 - " + intent.getAction());

			switch (intent.getAction()) {
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////

				case (AudioManager.VOLUME_CHANGED_ACTION): {
					setUserChangedVolumeTrue(intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE,
							Speech2.OPPOSITE_VOL_DND_OBJ_DEFAULT_VALUE));

					break;
				}

				case (CONSTS_BC_Speech.ACTION_CALL_SPEAK): {
					@NonNull final String txt_to_speak = intent.getStringExtra(CONSTS_BC_Speech.EXTRA_CALL_SPEAK_1);
					final int mode = intent.getIntExtra(CONSTS_BC_Speech.EXTRA_CALL_SPEAK_2, 0);
					final int speech_priority = intent.getIntExtra(CONSTS_BC_Speech.EXTRA_CALL_SPEAK_3, -1);
					final int task_id = intent.getIntExtra(CONSTS_BC_Speech.EXTRA_CALL_SPEAK_4, -1);
					final String utterance_id = intent.getStringExtra(CONSTS_BC_Speech.EXTRA_CALL_SPEAK_5);

					speakInternal(txt_to_speak, speech_priority, mode, utterance_id, task_id);

					break;
				}
				case (CONSTS_BC_Speech.ACTION_SKIP_SPEECH): {
					skipCurrentSpeech();

					break;
				}
				case (CONSTS_BC_Speech.ACTION_REMOVE_SPEECH): {
					final String speech_id = intent.getStringExtra(CONSTS_BC_Speech.EXTRA_CALL_SPEAK_1);

					SpeechQueue.SpeechQueue.removeSpeech(speech_id);

					break;
				}
				case (CONSTS_BC_Speech.ACTION_SAY_AGAIN): {
					if (last_speech.getTime() > System.currentTimeMillis() + 120*1000) {
						// 1.5 minutes at most until he forgets what he said (seems a good number)
						speak("I haven't said anything.", PRIORITY_USER_ACTION, MODE2_BYPASS_NO_SND);
					} else {
						speak("I said: " + last_speech, PRIORITY_USER_ACTION, MODE2_BYPASS_NO_SND);
					}

					break;
				}
				case (ACTION_CLEAR_NOTIF_MSGS): {
					speech_notif_speeches.clear();

					break;
				}
			}

			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
		}
	};
}
