package com.dadi590.assist_c_a.GlobalUtils;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.dadi590.assist_c_a.MainSrv;
import com.dadi590.assist_c_a.R;

/**
 * <p>Global {@link Service}s-related utilities.</p>
 */
public final class UtilsServices {

	/**
	 * <p>Private empty constructor so the class can't be instantiated (utility class).</p>
	 */
	private UtilsServices() {
	}

	/**
	 * <p>Restarts a service by terminating it's PID (thus forcing the shut down) and starting it normally.</p>
	 *
	 * @param context a context
	 * @param service_class the class of the service to restart
	 */
	public static void forceRestartService(@NonNull final Context context, @NonNull final Class<?> service_class) {
		UtilsProcesses.terminatePID(UtilsProcesses.getRunningServicePID(context, service_class));
		startService(context, service_class);
	}

	/**
	 * <p>Starts a service.</p>
	 *
	 * @param context a context
	 * @param service_class the class of the service to start
	 */
	public static void startService(@NonNull final Context context, @NonNull final Class<?> service_class) {
		final Intent intent = new Intent(context, service_class);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			context.startForegroundService(intent);
		} else {
			context.startService(intent);
		}
	}

	/**
	 * Starts the Main Service if it hasn't already been started.
	 *
	 * @param context a context
	 */
	public static void startMainSrv(@NonNull final Context context) {
		// Check if the Main Service is active or not
		boolean service_active = false;
		final ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		final String main_srv_class = MainSrv.class.toString().split(" ")[1];
		for (final ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (main_srv_class.equals(service.service.getClassName())) {
				service_active = true;
				break;
			}
		}
		// If not active, start it
		if (!service_active) {
			final Intent Main_Service = new Intent(context, MainSrv.class);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				context.startForegroundService(Main_Service);
			} else {
				context.startService(Main_Service);
			}
		}
	}

	public static final int TYPE_FOREGROUND = 0;
	/**
	 * <p>Returns a {@link Notification} with the given title and content text.</p>
	 * <br>
	 * <p><u>---CONSTANTS---</u></p>
	 * <p>- {@link #TYPE_FOREGROUND} --> for {@code notification_type}: a notification for a foreground service</p>
	 * <p><u>---CONSTANTS---</u></p>
	 *
	 * @param context a context
	 * @param notification_type one of the constants
	 * @return the {@link Notification}
	 */
	@NonNull
	public static Notification getNotification(@NonNull final Context context, final int notification_type) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			final String title = "Main notification";
			createNotificationChannel(context, notification_type, GL_CONSTS.CH_ID_MAIN_SRV_FOREGROUND, title, "");
		}
		final NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
				GL_CONSTS.CH_ID_MAIN_SRV_FOREGROUND);
		builder.setContentTitle(GL_CONSTS.ASSISTANT_NAME + " Systems running");
		builder.setContentText("");
		builder.setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE);
		builder.setVisibility(NotificationCompat.VISIBILITY_SECRET);
		//builder.setContentIntent(pendingIntent);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			builder.setSmallIcon(R.drawable.dadi_empresas_inc);
			builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
					R.drawable.ic_stat_dadi_empresas_inc__transparent));
			// The line above wasn't supposed to be needed, but without it, a red icon appears on Lollipop, so let it stay.
		} else {
			builder.setSmallIcon(R.drawable.ic_stat_dadi_empresas_inc__transparent);
			builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
					R.drawable.dadi_empresas_inc));
		}
		if (notification_type == TYPE_FOREGROUND) {
			builder.setOngoing(true);
			builder.setPriority(NotificationCompat.PRIORITY_MIN);
		}
		return builder.build();
	}

	/**
	 * <p>Creates a channel for notifications.</p>
	 *
	 * @param context a context
	 * @param notification_type same as in {@link #getNotification(Context, int)}
	 * @param channel_id the ID of the channel
	 * @param ch_name the name of the channel
	 * @param ch_description the descrption of the channel
	 */
	@RequiresApi(api = Build.VERSION_CODES.O)
	private static void createNotificationChannel(final Context context, final int notification_type,
												  final String channel_id, final String ch_name,
												  final String ch_description) {
		// Create the NotificationChannel, but only on API 26+ because
		// the NotificationChannel class is new and not in the support library
		String chName = ch_name;
		if (chName.isEmpty()) {
			// If it's an empty string, an error will be thrown. A space works.
			chName = " ";
		}
		int importance = NotificationManager.IMPORTANCE_DEFAULT;
		if (notification_type == TYPE_FOREGROUND) {
			importance = NotificationManager.IMPORTANCE_UNSPECIFIED;
		}
		final NotificationChannel channel = new NotificationChannel(channel_id, chName, importance);
		channel.setDescription(ch_description);
		// Register the channel with the system; you can't change the importance
		// or other notification behaviors after this
		final NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
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
}
