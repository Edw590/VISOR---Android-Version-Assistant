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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import GMan.GMan;

class GManUtils {

	@NonNull
	static String getTasksList(@NonNull final String[] tasks_ids, @NonNull final String cmd_variant) {
		String speak = "";
		SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

		for (final String task_id : tasks_ids) {
			ModsFileInfo.GTask task = GMan.getTask(task_id);
			if (task == null) {
				continue;
			}

			boolean add_task = false;
			try {
				Date task_date = date_format.parse(task.getDate());

				Calendar task_calendar = Calendar.getInstance();
				task_calendar.setTime(task_date);

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
			} catch (final ParseException ignored) {
				// Meaning, the date is empty
			}

			if (add_task || task.getDate().isEmpty()) {
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
		SimpleDateFormat date_time_format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

		for (final String event_id : events_ids) {
			ModsFileInfo.GEvent event = GMan.getEvent(event_id);
			if (event == null) {
				continue;
			}

			Date event_date_time;
			try {
				event_date_time = date_time_format.parse(event.getStart_time());
			} catch (final ParseException ignored) {
				continue;
			}

			Calendar event_calendar = Calendar.getInstance();
			event_calendar.setTime(event_date_time);

			boolean add_event = false;
			Calendar now = Calendar.getInstance();
			switch (cmd_variant) {
				case CmdsList.CmdRetIds.RET_31_TODAY:
					if (event_calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
						add_event = true;
					}
					break;
				case CmdsList.CmdRetIds.RET_31_TOMORROW:
					now.add(Calendar.DAY_OF_YEAR, 1);
					if (event_calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
						add_event = true;
					}
					break;
				case CmdsList.CmdRetIds.RET_31_THIS_WEEK:
					if (event_calendar.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR)) {
						add_event = true;
					}
					break;
				case CmdsList.CmdRetIds.RET_31_NEXT_WEEK:
					int days_until_next_monday = (8 - now.get(Calendar.DAY_OF_WEEK)) % 7;
					if (days_until_next_monday == 0) {
						days_until_next_monday = 7;
					}
					now.add(Calendar.DAY_OF_YEAR, days_until_next_monday);
					Calendar next_monday = (Calendar) now.clone();
					next_monday.add(Calendar.DAY_OF_YEAR, 7);
					if (event_calendar.after(now) && event_calendar.before(next_monday)) {
						add_event = true;
					}
					break;
			}
			if (add_event) {
				String event_on = "";
				if (cmd_variant.equals(CmdsList.CmdRetIds.RET_31_THIS_WEEK) ||
						cmd_variant.equals(CmdsList.CmdRetIds.RET_31_NEXT_WEEK)) {
					event_on = " on " + event_calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US);
				}
				speak += event.getSummary() +
						event_on +
						" at "+ new SimpleDateFormat("HH:mm", Locale.US).format(event_date_time) +
						" for " + getEventDuration(event.getDuration_min()) + "; ";
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

	@NonNull
	private static String getEventDuration(final long min) {
		if (min >= 60) {
			if (min >= 24 * 60) {
				if (min >= 7 * 24 * 60) {
					long weeks = min / (7 * 24 * 60);
					long days = (min % (7 * 24 * 60)) / (24 * 60);
					String week_weeks = "weeks";
					if (weeks == 1) {
						week_weeks = "week";
					}
					String day_days = "days";
					if (days == 1) {
						day_days = "day";
					}
					if (days > 0) {
						return String.format(Locale.US, "%d %s and %d %s", weeks, week_weeks, days, day_days);
					}
					return String.format(Locale.US, "%d %s", weeks, week_weeks);
				}
				long days = min / (24 * 60);
				long hours = (min % (24 * 60)) / 60;
				String day_days = "days";
				if (days == 1) {
					day_days = "day";
				}
				String hour_hours = "hours";
				if (hours == 1) {
					hour_hours = "hour";
				}
				if (hours > 0) {
					return String.format(Locale.US, "%d %s and %d %s", days, day_days, hours, hour_hours);
				}
				return String.format(Locale.US, "%d %s", days, day_days);
			}
			long hours = min / 60;
			long minutes = min % 60;
			String hour_hours = "hours";
			if (hours == 1) {
				hour_hours = "hour";
			}
			String minute_minutes = "minutes";
			if (minutes == 1) {
				minute_minutes = "minute";
			}
			if (minutes > 0) {
				return String.format(Locale.US, "%d %s and %d %s", hours, hour_hours, minutes, minute_minutes);
			}
			return String.format(Locale.US, "%d %s", hours, hour_hours);
		}

		String minute_minutes = "minutes";
		if (min == 1) {
			minute_minutes = "minute";
		}

		return String.format(Locale.US, "%d %s", min, minute_minutes);
	}
}
