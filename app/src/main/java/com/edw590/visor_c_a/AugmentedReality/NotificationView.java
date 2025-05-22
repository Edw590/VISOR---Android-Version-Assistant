package com.edw590.visor_c_a.AugmentedReality;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class NotificationView extends FrameLayout {

	public NotificationView(@NonNull final Context context, @NonNull final String package_name_str, @NonNull final String message) {
		super(context);

		// Create rounded rectangle background
		GradientDrawable bg = new GradientDrawable();
		bg.setColor(0xCC222222); // semi-transparent dark
		bg.setCornerRadius(dpToPx(5));
		setBackground(bg);

		DisplayMetrics display_metrics = context.getResources().getDisplayMetrics();
		FrameLayout.LayoutParams layout_params = new FrameLayout.LayoutParams(
				(int) (0.3 * display_metrics.widthPixels),
				(int) (0.15 * display_metrics.heightPixels)
		);
		layout_params.leftMargin = (int) (0.65 * display_metrics.widthPixels);
		layout_params.topMargin = (int) (0.1 * display_metrics.heightPixels);
		setLayoutParams(layout_params);

		// Container for texts
		LinearLayout container = new LinearLayout(context);
		container.setOrientation(LinearLayout.VERTICAL);
		container.setPadding(dpToPx(5), dpToPx(5), dpToPx(5), dpToPx(5));
		container.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		// Package name
		TextView package_name = new TextView(context);
		package_name.setText(package_name_str);
		package_name.setTextColor(Color.WHITE);
		package_name.setTextSize(10);
		package_name.setTypeface(package_name.getTypeface(), android.graphics.Typeface.BOLD);

		// Message
		TextView notification_text = new TextView(context);
		notification_text.setText(message);
		notification_text.setTextColor(Color.WHITE);
		notification_text.setTextSize(7);
		LinearLayout.LayoutParams msgParams = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		msgParams.topMargin = dpToPx(3);
		notification_text.setLayoutParams(msgParams);

		// Add views
		container.addView(package_name);
		container.addView(notification_text);
		addView(container);
	}

	private int dpToPx(int dp) {
		float density = getResources().getDisplayMetrics().density;
		return Math.round(dp * density);
	}

	/**
	 * <p>Show notification in parent and auto-remove after delay.</p>
	 *
	 * @param parent the parent view to add the notification to
	 * @param duration_ms the duration in milliseconds to show the notification
	 */
	public void showIn(@NonNull final FrameLayout parent, int duration_ms) {
		parent.addView(this);
		new Handler().postDelayed(() -> {
			parent.removeView(this);
		}, duration_ms);
	}
}
