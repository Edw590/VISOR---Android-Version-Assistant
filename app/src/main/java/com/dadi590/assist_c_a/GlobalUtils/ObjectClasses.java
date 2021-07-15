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

package com.dadi590.assist_c_a.GlobalUtils;

import android.app.PendingIntent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Method;

/**
 * <p>Classes to instantiate to use with utility methods.</p>
 */
public class ObjectClasses {

	/**
	 * <p>Class to instantiate to give notification information to
	 * {@link UtilsServices#getNotification(NotificationInfo)}.</p>
	 */
	public static class NotificationInfo {

		final String channel_id;
		final String channel_name;
		final String channel_description;
		final int notification_type;
		final String notification_title;
		final String notification_content;
		final PendingIntent notif_content_intent;

		/**
		 * <p>Main class constructor.</p>
		 * <br>
		 * <p><u>---CONSTANTS---</u></p>
		 * <p>- {@link UtilsServices#TYPE_FOREGROUND} --> for {@code notification_type}: a notification for a foreground
		 * service</p>
		 * <p><u>---CONSTANTS---</u></p>
		 *
		 * @param channel_id the ID of the channel the notification is in
		 * @param channel_name the name of the channel the notification is in
		 * @param channel_description the description of the channel the notification is in
		 * @param notification_type one of the constants
		 * @param notification_title the title of the notification
		 * @param notification_content the content of the notification
		 * @param notif_content_intent the intent to use for when the user clicks on the notification
		 */
		public NotificationInfo(@NonNull final String channel_id, @NonNull final String channel_name,
								@NonNull final String channel_description, final int notification_type,
								@NonNull final String notification_title, @NonNull final String notification_content,
								@Nullable final PendingIntent notif_content_intent) {
			this.channel_id = channel_id;
			this.channel_name = channel_name;
			this.channel_description = channel_description;
			this.notification_type = notification_type;
			this.notification_title = notification_title;
			this.notification_content = notification_content;
			this.notif_content_intent = notif_content_intent;
		}
	}

	/**
	 * <p>Class to use as return value of {@link UtilsGeneral#getMethodClass(String)}.</p>
	 */
	public static class GetMethodClassObj {

		public final Method getMethod;
		public final Class<?> hidden_class;

		/**
		 * <p>Main class constructor.</p>
		 *
		 * @param getMethod the instance of {@link Class#getMethod(String, Class[])} to return
		 * @param hidden_class the hidden class that wants to be gotten
		 */
		GetMethodClassObj(@Nullable final Method getMethod, @Nullable final Class<?> hidden_class) {
			this.getMethod = getMethod;
			this.hidden_class = hidden_class;
		}
	}
}
