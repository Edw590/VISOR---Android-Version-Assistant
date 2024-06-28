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

package com.edw590.visor_c_a.Modules.PreferencesManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.Nullable;

import com.edw590.visor_c_a.GlobalInterfaces.IModuleInst;
import com.edw590.visor_c_a.GlobalUtils.GPath;
import com.edw590.visor_c_a.GlobalUtils.UtilsContext;
import com.edw590.visor_c_a.GlobalUtils.UtilsFilesDirs;
import com.edw590.visor_c_a.GlobalUtils.UtilsGeneral;
import com.edw590.visor_c_a.GlobalUtils.UtilsShell;
import com.edw590.visor_c_a.Modules.Speech.Speech2;
import com.edw590.visor_c_a.Modules.Speech.UtilsSpeech2BC;
import com.edw590.visor_c_a.ModulesList;

/**
 * <p>The module that keep storing the app preferences on the files.</p>
 */
public final class PreferencesManager implements IModuleInst {

	private final int element_index = ModulesList.getElementIndex(this.getClass());
	private final HandlerThread main_handlerThread = new HandlerThread((String) ModulesList.getElementValue(element_index,
			ModulesList.ELEMENT_NAME));
	private final Handler main_handler;

	private static final long WAIT_TIME = 30_000;

	long last_save_time = 0;
	boolean save_failed = false;

	///////////////////////////////////////////////////////////////
	// IModuleInst stuff
	private boolean is_module_destroyed = false;
	@Override
	public boolean isFullyWorking() {
		if (is_module_destroyed) {
			return false;
		}

		return UtilsGeneral.isThreadWorking(main_handlerThread);
	}
	@Override
	public void destroy() {
		try {
			UtilsContext.getContext().unregisterReceiver(broadcastReceiver);
		} catch (final IllegalArgumentException ignored) {
		}
		UtilsGeneral.quitHandlerThread(main_handlerThread);

		is_module_destroyed = true;
	}
	@Override
	public int wrongIsSupported() {return 0;}
	/**.
	 * @return read all here {@link IModuleInst#wrongIsSupported()} */
	public static boolean isSupported() {
		return true;
	}
	// IModuleInst stuff
	///////////////////////////////////////////////////////////////

	/**
	 * <p>Main class constructor.</p>
	 */
	public PreferencesManager() {
		main_handlerThread.start();
		main_handler = new Handler(main_handlerThread.getLooper());

		try {
			final IntentFilter intentFilter = new IntentFilter();

			intentFilter.addAction(CONSTS_BC_PreferencesManager.ACTION_REQUEST_SAVE_PREFS);

			UtilsContext.getContext().registerReceiver(broadcastReceiver, intentFilter, null, main_handler);
		} catch (final IllegalArgumentException ignored) {
		}

		infinity_thread.start();
	}

	final Thread infinity_thread = new Thread(new Runnable() {
		@Override
		public void run() {
			while (true) {
				if (save_failed) {
					final String to_write = StaticPreferences.getPreferences();
					if (to_write != null) {
						if (StaticPreferences.writePrefsFile(to_write)) {
							last_save_time = System.currentTimeMillis();
							final String speak = "The preferences file has now been saved successfully.";
							UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, 0, null);
							save_failed = false;
						}
					}
				}

				if (StaticPreferences.getPreferences() == null) {
					final String preferences = StaticPreferences.readPrefsFile();
					if (preferences != null) {
						StaticPreferences.updatePreferences(preferences);
					} else {
						if (!UtilsShell.noErr(UtilsFilesDirs.checkPathExists(
								new GPath(false, StaticPreferences.PREFS_FILE_PATH)))) {
							// todo You were here
						}
					}
				}

				try {
					Thread.sleep(WAIT_TIME);
				} catch (final InterruptedException ignored) {
					return;
				}
			}
		}
	});

	public final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(@Nullable final Context context, @Nullable final Intent intent) {
			if (intent == null || intent.getAction() == null) {
				return;
			}

			System.out.println("PPPPPPPPPPPPPPPPPP-PreferencesSaver - " + intent.getAction());

			switch (intent.getAction()) {
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
				////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////

				case (CONSTS_BC_PreferencesManager.ACTION_REQUEST_SAVE_PREFS): {
					if (System.currentTimeMillis() < last_save_time + WAIT_TIME) {
						break;
					}

					final String to_write = StaticPreferences.getPreferences();
					if (to_write != null) {
						if (StaticPreferences.writePrefsFile(to_write)) {
							last_save_time = System.currentTimeMillis();
							save_failed = false;
						} else {
							save_failed = true;
							final String speak = "Warning - the preferences file could not be saved!";
							UtilsSpeech2BC.speak(speak, Speech2.PRIORITY_HIGH, 0, null);
						}
					}

					break;
				}
			}

			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
			////////////////// ADD THE ACTIONS TO THE RECEIVER!!!!! //////////////////
		}
	};
}
