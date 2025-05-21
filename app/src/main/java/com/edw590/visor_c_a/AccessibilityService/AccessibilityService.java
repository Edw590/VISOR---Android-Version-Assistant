package com.edw590.visor_c_a.AccessibilityService;

import android.view.accessibility.AccessibilityEvent;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
		if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
			System.out.println("Package " + event.getPackageName() + " posted a notification: " + event.getText());
		}
	}

	@Override
	public void onInterrupt() {
		// Empty
	}
}
