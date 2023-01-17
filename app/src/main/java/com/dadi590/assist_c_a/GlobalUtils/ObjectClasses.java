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

package com.dadi590.assist_c_a.GlobalUtils;

import android.app.PendingIntent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Method;

/**
 * <p>Classes to instantiate to use with utility methods.</p>
 */
public final class ObjectClasses {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private ObjectClasses() {
	}

	/**
	 * <p>Class to instantiate to give notification information to
	 * {@link UtilsNotifications#getNotification(NotificationInfo)}.</p>
	 * <p>Check the constructor for more information.</p>
	 */
	public static class NotificationInfo {

		@NonNull final String ch_id;
		@Nullable final String ch_name;
		@Nullable final String ch_description;
		@NonNull final Integer ch_priotiy;
		@NonNull final String notif_title;
		@NonNull final String notif_content;
		@Nullable final PendingIntent notif_content_intent;

		/**
		 * <p>Main class constructor.</p>
		 * <p>If the name or the description are given null values, the channel will NOT be created (meaning it already
		 * exists).</p>
		 * <p>The setPriority() method will be called automatically based on the importance parameter (both are
		 * matched), so no need to set it manually.</p>
		 *
		 * @param ch_id the ID of the channel the notification is in
		 * @param ch_name the name of the channel the notification is in
		 * @param ch_description the description of the channel the notification is in
		 * @param ch_priotiy the priority of the channel the notification is in (don't worry about the new IMPORTANCEs,
		 * as that's automatically addressed based on the PRIORITY)
		 * @param notif_title the title of the notification
		 * @param notif_content the content of the notification
		 * @param notif_content_intent the pending intent to use for when the user clicks on the notification
		 */
		public NotificationInfo(@NonNull final String ch_id, @Nullable final String ch_name,
								@Nullable final String ch_description, @NonNull final Integer ch_priotiy,
								@NonNull final String notif_title,
								@NonNull final String notif_content, @Nullable final PendingIntent notif_content_intent) {
			this.ch_id = ch_id;
			this.ch_name = ch_name;
			this.ch_description = ch_description;
			this.ch_priotiy = ch_priotiy;
			this.notif_title = notif_title;
			this.notif_content = notif_content;
			this.notif_content_intent = notif_content_intent;
		}
	}

	/**
	 * <p>Class to use as return value of {@link UtilsGeneral#getMethodClass(String)}.</p>
	 * <p>Check the constructor for more information.</p>
	 */
	public static class GetMethodClassObj {

		@Nullable public final Method getMethod;
		@Nullable public final Class<?> hidden_class;

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
