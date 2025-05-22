package com.edw590.visor_c_a.AccessibilityService;

import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.RequiresApi;

import com.edw590.visor_c_a.GlobalUtils.UtilsApp;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {

	public static final String ACTION_NEW_NOTIFICATION = "AccessSrvc_ACTION_NEW_NOTIFICATION";

	private boolean initialized = false;

	@Override
	@RequiresApi(api = Build.VERSION_CODES.KITKAT)
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (!initialized) {
			return;
		}

		if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
			Parcelable parcelable = event.getParcelableData();
			if (parcelable instanceof Notification) {
				Bundle notif_extras = ((Notification) parcelable).extras;
				String title = "";
				CharSequence title_chars = notif_extras.getCharSequence(Notification.EXTRA_TITLE);
				if (title_chars != null) {
					title = title_chars.toString();
				}
				String text = "";
				CharSequence text_chars = notif_extras.getCharSequence(Notification.EXTRA_TEXT);
				if (text_chars != null) {
					text = text_chars.toString();
				}
				String text2 = "";
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					CharSequence text2_chars = notif_extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
					if (text2_chars != null) {
						text2 = text2_chars.toString();
					}
				}
				String package_name = event.getPackageName().toString();

				Intent broadcast_intent = new Intent(ACTION_NEW_NOTIFICATION);
				broadcast_intent.putExtra("pkg_name", package_name);
				broadcast_intent.putExtra("title", title);
				broadcast_intent.putExtra("txt", text);
				broadcast_intent.putExtra("txt_big", text2);
				UtilsApp.sendInternalBroadcast(broadcast_intent);
			}
		}
	}

	@Override
	protected void onServiceConnected() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			initialized = true;
		}
	}

	@Override
	public void onInterrupt() {
		initialized = false;
	}
}
