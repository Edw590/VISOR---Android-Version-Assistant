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

package com.edw590.visor_c_a.Modules.TasksExecutor;

import android.media.AudioManager;

import androidx.annotation.Nullable;

import com.edw590.visor_c_a.GlobalInterfaces.IModuleInst;
import com.edw590.visor_c_a.GlobalUtils.UtilsAudio;
import com.edw590.visor_c_a.GlobalUtils.UtilsGeneral;
import com.edw590.visor_c_a.Modules.CmdsExecutor.UtilsCmdsExecutorBC;
import com.edw590.visor_c_a.Modules.Speech.Speech2;
import com.edw590.visor_c_a.Modules.Speech.UtilsSpeech2BC;
import com.edw590.visor_c_a.Registry.UtilsRegistry;
import com.edw590.visor_c_a.Registry.ValuesRegistry;

import java.util.Calendar;

public class TasksExecutor implements IModuleInst {

	private static final long CHECK_TIME = 1000;


	///////////////////////////////////////////////////////////////
	// IModuleInst stuff
	private boolean is_module_destroyed = false;

	@Override
	public boolean isFullyWorking() {
		if (is_module_destroyed) {
			return false;
		}

		return UtilsGeneral.isThreadWorking(infinity_thread);
	}

	@Override
	public void destroy() {
		infinity_thread.interrupt();

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
	public TasksExecutor() {
		infinity_thread.start();
	}

	private class Task {
		final String key_to_check;
		@Nullable final Object prev_value;
		@Nullable final Object curr_value;
		final Runnable to_do;

		Task(final String key_to_check, @Nullable final Object prev_value, @Nullable final Object curr_value, final Runnable to_do) {
			this.key_to_check = key_to_check;
			this.prev_value = prev_value;
			this.curr_value = curr_value;
			this.to_do = to_do;
		}

	}

	private int prev_ringer_mode = 0;
	Task[] tasks = {
			new Task(ValuesRegistry.K_IS_USER_SLEEPING, null, true, () -> {
				prev_ringer_mode = UtilsAudio.getAudioModes(-1, true, false);
				UtilsAudio.setAudioModes(-1, -1, AudioManager.RINGER_MODE_VIBRATE, -1);

				UtilsSpeech2BC.speak("Sleep well sir.", Speech2.PRIORITY_MEDIUM, Speech2.MODE_DEFAULT, null);
			}),
			new Task(ValuesRegistry.K_IS_USER_SLEEPING, null, false, () -> {
				final int speech_priority = Speech2.PRIORITY_MEDIUM;
				UtilsAudio.setAudioModes(-1, -1, prev_ringer_mode, -1);

				final int current_hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
				final int current_minute = Calendar.getInstance().get(Calendar.MINUTE);
				final String minute_str = current_minute < 10 ? "0" + current_minute : String.valueOf(current_minute);
				if (current_hour >= 6) {
					if (current_hour <= 11) {
						// Before 6h it's still night and I'll still sleep more
						UtilsSpeech2BC.speak("Good morning. It's " + current_hour + ":" + minute_str,
								speech_priority, Speech2.MODE2_BYPASS_NO_SND, null);
					} else if (current_hour <= 14) {
						UtilsSpeech2BC.speak("Good afternoon. It's " + current_hour + ":" + minute_str,
								speech_priority, Speech2.MODE2_BYPASS_NO_SND, null);
					} else {
						// From 15h onwards it's a nap
						UtilsSpeech2BC.speak("Welcome back. It's " + current_hour + ":" + minute_str,
								speech_priority, Speech2.MODE2_BYPASS_NO_SND, null);

						return;
					}
				} else {
					// From 00h to 5h it's still night
					return;
				}

				//UtilsCmdsExecutorBC.processTask("tell me the weather and the news", false, false, true);
				UtilsCmdsExecutorBC.processTask("tell me the weather", false, false, true);
			}),
	};

	private final Thread infinity_thread = new Thread(() -> {
		try {
			// Wait a bit before checking the values for the first time or for example if the user is detected
			// not to be sleeping, this could think the user just woke up and do the action.
			Thread.sleep(CHECK_TIME + 1000);
		} catch (final InterruptedException ignored) {
			return;
		}

		while (true) {
			for (final Task task : tasks) {
				final Object curr_data = UtilsRegistry.getData(task.key_to_check, true);
				final Object prev_data = UtilsRegistry.getData(task.key_to_check, false);

				if (task.curr_value == null && task.prev_value == null) {
					continue;
				}

				if (task.curr_value == null) {
					if (!task.prev_value.equals(prev_data)) {
						continue;
					}
				} else if (task.prev_value == null) {
					if (!task.curr_value.equals(curr_data)) {
						continue;
					}
				} else {
					if (!task.curr_value.equals(curr_data) || !task.prev_value.equals(prev_data)) {
						continue;
					}
				}

				// Only if the value was just updated
				Registry.Value value = Registry.Registry.getValue(task.key_to_check);
				if (System.currentTimeMillis() - value.getTimeUpdated(true) <= CHECK_TIME) {
					task.to_do.run();
				}
			}

			try {
				Thread.sleep(CHECK_TIME);
			} catch (final InterruptedException ignored) {
				return;
			}
		}
	});
}
