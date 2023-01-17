/*
 * Copyright 2023 DADi590
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

package com.dadi590.assist_c_a.BroadcastRecvs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.Nullable;

import com.dadi590.assist_c_a.GlobalUtils.UtilsApp;
import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.Modules.Speech.UtilsSpeech2BC;

/**
 * <p>The class to be used to decide what to do with any broadcasts that need registering on the main app process.</p>
 * <p>This is used even with broadcasts sent to static receivers, because the assistant (at least currently) won't
 * process broadcasts sent when it's not running (as a start because it's supposed to be always running). So static
 * receivers are to wake the assistant in case something shut it down (like the system, for memory optimization, or some
 * error), and registered receivers handle the broadcasts normally.</p>
 */
public final class MainRegRecv {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private MainRegRecv() {
	}

	/**
	 * <p>Registers all the receivers in the class.</p>
	 *
	 */
	public static void registerReceivers() {
		// Note: don't put as a constructor, since the receivers must be registered only after TTS is ready.

		final IntentFilter intentFilter = new IntentFilter();

		// Shutdown and reboot
		intentFilter.addAction(Intent.ACTION_SHUTDOWN);
		intentFilter.addAction(Intent.ACTION_REBOOT);
		intentFilter.addAction(CONSTS.ACTION_HTC_QCK_POFF);
		intentFilter.addAction(CONSTS.ACTION_ANDR_QCK_POFF);

		try {
			UtilsGeneral.getContext().registerReceiver(mainRegBroadcastReceiver, intentFilter);
		} catch (final IllegalArgumentException ignored) {
			// Then it's already registered - imagine this is called again because the speech module was restarted.
		}
	}

	private static final BroadcastReceiver mainRegBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
			System.out.println("PPPPPPPPPPPPPPPPPP-MainRegRcv - " + (null != intent ? intent.getAction() : null));

			if (intent == null || intent.getAction() == null) {
				return;
			}

			switch (intent.getAction()) {
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////

				/////////////////////////////////////
				// Shutdown and reboot
				case (CONSTS.ACTION_HTC_QCK_POFF):
				case (CONSTS.ACTION_ANDR_QCK_POFF):
				case (Intent.ACTION_SHUTDOWN): {
					UtilsApp.prepareShutdown();

					if (intent.getBooleanExtra(Intent.EXTRA_SHUTDOWN_USERSPACE_ONLY, false)) {
						final String speak = "Fast shut down detected.";
						UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);
					} else {
						final String speak = "Shut down detected.";
						UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);
					}
					// Note: must be very small speeches, since the phone will shut down fast.

					break;
				}
				case (Intent.ACTION_REBOOT): {
					UtilsApp.prepareShutdown();

					// No idea if this is supposed detected at all (might be stopped before it gets here by the system as
					// soon as it detects it or something).
					final String speak = "Reboot detected.";
					UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, null);
					// Note: must be a very small speech, since the phone will shut down fast.


					break;
				}
			}

			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
		}
	};
}
