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

package com.edw590.visor_c_a.Modules.CmdsExecutor;

import androidx.annotation.NonNull;

import com.edw590.visor_c_a.Modules.CmdsExecutor.CmdsList.CmdsList;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import GMan.GMan;
import UtilsSWA.UtilsSWA;

class GManUtils {

	@NonNull
	static String getTasksList(@NonNull final String[] tasks_ids, @NonNull final String cmd_variant) {
		String speak = "";
		for (final String task_id : tasks_ids) {
			ModsFileInfo.GTask task = GMan.getTask(task_id);
			if (task == null) {
				continue;
			}

			boolean add_task = false;
			if (task.getDate_s() == 0) {
				// If the task has no date, we add it to the list (it's to be done every day)
				add_task = true;
			} else {
				// Else we check the date
				Calendar task_calendar = Calendar.getInstance();
				task_calendar.setTimeInMillis(task.getDate_s() * 1000);

				Calendar now = Calendar.getInstance();
				switch (cmd_variant) {
					case CmdsList.CmdRetIds.RET_31_TODAY:
						if (task_calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
							add_task = true;
						}
						break;
					case CmdsList.CmdRetIds.RET_31_TOMORROW:
						now.add(Calendar.DAY_OF_YEAR, 1);
						if (task_calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
							add_task = true;
						}
						break;
				}
			}

			if (add_task) {
				speak += task.getTitle() + "; ";
			}
		}

		String when;
		if (cmd_variant.equals(CmdsList.CmdRetIds.RET_31_TODAY)) {
			when = "today";
		} else if (cmd_variant.equals(CmdsList.CmdRetIds.RET_31_TOMORROW)) {
			when = "tomorrow";
		} else {
			when = "";
		}
		if (speak.isEmpty()) {
			speak = "You have no tasks found for " + when + ".";
		} else {
			speak = "Your list of tasks for " + when + ": " + speak + ".";
		}

		return speak;
	}

	@NonNull
	static String getEventsList(@NonNull final String[] events_ids, @NonNull final String cmd_variant) {
		String speak = "";

		for (final String event_id : events_ids) {
			ModsFileInfo.GEvent event = GMan.getEvent(event_id);
			if (event == null) {
				continue;
			}

			long curr_s = System.currentTimeMillis() / 1000;

			long event_end_time_s = event.getStart_time_s() + event.getDuration_min() * 60;
			if (event_end_time_s < curr_s) {
				// Event already ended
				continue;
			}

			long start_of_day_s = UtilsSWA.getStartOfDayDATETIME(curr_s);
			long end_of_day_s = start_of_day_s + 86400 - 1; // 86400 seconds in a day (24*60*60)

			long start_of_next_day_s = start_of_day_s + 86400;
			long end_of_next_day_s = start_of_next_day_s + 86400 - 1;

			long start_of_week_s =
					UtilsSWA.getStartOfDayDATETIME(curr_s - (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1)) * 86400;
			long end_of_week_s = start_of_week_s + 7 * 86400 - 1;

			long start_of_next_week_s = start_of_week_s + 7 * 86400;
			long end_of_next_week_s = start_of_next_week_s + 7 * 86400 - 1;

			boolean add_event = false;
			switch (cmd_variant) {
				case CmdsList.CmdRetIds.RET_31_TODAY:
					if ((event.getStart_time_s() >= start_of_day_s && event.getStart_time_s() <= end_of_day_s) ||
							(event_end_time_s >= start_of_day_s && event_end_time_s <= end_of_day_s) ||
							(start_of_day_s >= event.getStart_time_s() && end_of_day_s <= event_end_time_s)) {
						add_event = true;
					}
					break;
				case CmdsList.CmdRetIds.RET_31_TOMORROW:
					if ((event.getStart_time_s() >= start_of_next_day_s && event.getStart_time_s() <= end_of_next_day_s) ||
							(event_end_time_s >= start_of_next_day_s && event_end_time_s <= end_of_next_day_s) ||
							(start_of_next_day_s >= event.getStart_time_s() && end_of_next_day_s <= event_end_time_s)) {
						add_event = true;
					}
					break;
				case CmdsList.CmdRetIds.RET_31_THIS_WEEK:
					if ((event.getStart_time_s() >= start_of_week_s && event.getStart_time_s() <= end_of_week_s) ||
							(event_end_time_s >= start_of_week_s && event_end_time_s <= end_of_week_s) ||
							(start_of_week_s >= event.getStart_time_s() && end_of_week_s <= event_end_time_s)) {
						add_event = true;
					}
					break;
				case CmdsList.CmdRetIds.RET_31_NEXT_WEEK:
					if ((event.getStart_time_s() >= start_of_next_week_s && event.getStart_time_s() <= end_of_next_week_s) ||
							(event_end_time_s >= start_of_next_week_s && event_end_time_s <= end_of_next_week_s) ||
							(start_of_next_week_s >= event.getStart_time_s() && end_of_next_week_s <= event_end_time_s)) {
						add_event = true;
					}
					break;
			}
			if (add_event) {
				Calendar event_calendar = Calendar.getInstance();
				event_calendar.setTimeInMillis(event.getStart_time_s() * 1000);

				String event_on = "";
				if (cmd_variant.equals(CmdsList.CmdRetIds.RET_31_THIS_WEEK) ||
						cmd_variant.equals(CmdsList.CmdRetIds.RET_31_NEXT_WEEK)) {
					event_on = " on " + event_calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US);
				}

				boolean event_began_today = event.getStart_time_s() >= start_of_day_s &&
						event.getStart_time_s() <= end_of_day_s;
				String event_at = "";
				if (event_began_today) {
					SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
					sdf.setTimeZone(event_calendar.getTimeZone());
					event_at = "at " + sdf.format(event_calendar.getTime());
				}

				long curr_duration = event.getStart_time_s()/60 + event.getDuration_min() -
						System.currentTimeMillis()/1000/60;

				speak += event.getSummary() + event_on + " " + event_at + " for " +
						UtilsSWA.toReadableDurationDATETIME(curr_duration) + "; ";
			}
		}

		String when;
		switch (cmd_variant) {
			case CmdsList.CmdRetIds.RET_31_TODAY:
				when = "today";
				break;
			case CmdsList.CmdRetIds.RET_31_TOMORROW:
				when = "tomorrow";
				break;
			case CmdsList.CmdRetIds.RET_31_THIS_WEEK:
				when = "this week";
				break;
			case CmdsList.CmdRetIds.RET_31_NEXT_WEEK:
				when = "next week";
				break;
			default:
				when = "";
		}
		if (speak.isEmpty()) {
			speak = "You have no events found for " + when + ".";
		} else {
			speak = "Your list of events for " + when + ": " + speak + ".";
		}

		return speak;
	}
}
