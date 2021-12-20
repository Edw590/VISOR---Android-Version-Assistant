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

import android.media.AudioManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;

/**
 * <p>Each speech is an instance of this class.</p>
 */
class SpeechObj {

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

	static final int DEFAULT_AUDIO_STREAM = -3234;

	final String utterance_id;
	final Integer after_speaking_code;
	String txt_to_speak; // Not final because of "As I was saying, " - interrupt the speech and say it again, changed
	final int audio_stream;

	/**
	 * <p>Main class constructor.</p>
	 *
	 * @param utterance_id same as in {@link Speech2#speak(String, int, Integer)}
	 * @param txt_to_speak same as in {@link Speech2#speak(String, int, Integer)}
	 * @param current_speech_obj true if the instance is being created to reset the {@link Speech2#current_speech_obj} -
	 *                           the audio stream will be set to {@link #DEFAULT_AUDIO_STREAM} as a "random" value;
	 *                           false otherwise
	 * @param after_speaking_code same as in {@link Speech2#speak(String, int, Integer)}
	 */
	SpeechObj(final String utterance_id, @NonNull final String txt_to_speak, final boolean current_speech_obj,
			  @Nullable final Integer after_speaking_code) {
		this.utterance_id = utterance_id;
		this.after_speaking_code = after_speaking_code;
		this.txt_to_speak = txt_to_speak;

		if (current_speech_obj) {
			audio_stream = DEFAULT_AUDIO_STREAM;
		} else {
			final int priority = UtilsSpeech2.getSpeechPriority(utterance_id);
			if (priority == Speech2.PRIORITY_CRITICAL) {
				audio_stream = AUD_STREAM_PRIORITY_CRITICAL;
			} else if (priority == Speech2.PRIORITY_HIGH) {
				audio_stream = AUD_STREAM_PRIORITY_HIGH;
			} else {
				if (UtilsGeneral.areExtSpeakersOn()) {
					audio_stream = AUD_STREAM_HEADPHONES;
				} else {
					audio_stream = AUD_STREAM_PRIORITY_OTHERS_SPEAKERS;
				}
			}
		}
	}

	@NonNull
	@Override
	public final String toString() {
		return "[\"" + utterance_id.substring(0, 20) + "...\", " + "\"" + txt_to_speak + "\", "
				+ after_speaking_code + ", " + audio_stream + "]";
	}
}
