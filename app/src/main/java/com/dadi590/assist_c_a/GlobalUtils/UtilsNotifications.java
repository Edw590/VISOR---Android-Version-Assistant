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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.dadi590.assist_c_a.R;

/**
 * <p>Global notification-related utilities.</p>
 */
public final class UtilsNotifications {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsNotifications() {
	}

	/**
	 * <p>Returns a {@link NotificationCompat.Builder} with the given specifications and some default settings (check
	 * the implementation of this function).</p>
	 *
	 * @param notificationInfo an instance of {@link ObjectClasses.NotificationInfo}
	 *
	 * @return the builder
	 */
	@NonNull
	public static NotificationCompat.Builder getNotification(@NonNull final ObjectClasses.NotificationInfo notificationInfo) {
		final Context context = UtilsContext.getContext();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			// Faster than making a map...
			int importance = NotificationManager.IMPORTANCE_UNSPECIFIED;
			switch (notificationInfo.ch_priotiy) {
				case NotificationCompat.PRIORITY_MIN:
					break;
				case NotificationCompat.PRIORITY_LOW:
					importance = NotificationManager.IMPORTANCE_LOW;
					break;
				case NotificationCompat.PRIORITY_DEFAULT:
					importance = NotificationManager.IMPORTANCE_DEFAULT;
					break;
				case NotificationCompat.PRIORITY_HIGH:
					importance = NotificationManager.IMPORTANCE_HIGH;
					break;
				case NotificationCompat.PRIORITY_MAX:
					importance = NotificationManager.IMPORTANCE_MAX;
					break;
			}
			createNotifChannel(notificationInfo.ch_id, notificationInfo.ch_name, notificationInfo.ch_description,
					importance);
		}

		final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationInfo.ch_id).
			setContentTitle(notificationInfo.notif_title).
			setContentText(notificationInfo.notif_content).
			setVisibility(NotificationCompat.VISIBILITY_SECRET).
			setContentIntent(notificationInfo.notif_content_intent).
			setWhen(System.currentTimeMillis()).
			setShowWhen(true).
			setLocalOnly(true).
			setOngoing(false).
			setPriority(notificationInfo.ch_priotiy).
			//setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE);
			setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.app_logo_legacy_only));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			builder.setSmallIcon(R.drawable.app_logo_transparent);
		} else {
			builder.setSmallIcon(R.mipmap.app_logo_legacy_only);
		}

		return builder;
	}

	/**
	 * <p>Creates a channel for notifications, required as of {@link Build.VERSION_CODES#O}.</p>
	 * <p>If the name or the description are given null values, the channel will NOT be created (meaning it already
	 * exists).</p>
	 *  @param id the ID of the channel
	 * @param name the name of the channel
	 * @param description the description of the channel
	 * @param importance the importance of the channel
	 */
	@RequiresApi(Build.VERSION_CODES.O)
	public static void createNotifChannel(@NonNull final String id, @Nullable final String name,
										  @Nullable final String description, @NonNull final Integer importance) {
		if (null == name || null == description) {
			return;
		}

		String chName = name;
		if (chName.isEmpty()) {
			// If it's an empty string, an error will be thrown. A space works.
			chName = " ";
		}

		final NotificationChannel channel = new NotificationChannel(id, chName, importance);
		channel.setDescription(description);

		final NotificationManager notificationManager = (NotificationManager) UtilsContext.getNotificationManager();

		try {
			notificationManager.createNotificationChannel(channel);
		} catch (final IllegalArgumentException ignored) {
			// This might throw an error saying "java.lang.IllegalArgumentException: Invalid importance level".
			// If that happens, the importance goes to MIN - if it's a system app, will remain on MIN; if it's a normal
			// app, will be put in LOW (hopefully - it says "higher")
			channel.setImportance(NotificationManager.IMPORTANCE_MIN);
			notificationManager.createNotificationChannel(channel);
		}
	}

	/**
	 * <p>Same as {@link NotificationManager#cancel(int)}.</p>
	 */
	public static void cancelNotification(final int id) {
		final NotificationManager notificationManager =
				(NotificationManager) UtilsContext.getNotificationManager();
		notificationManager.cancel(id);
	}
}
