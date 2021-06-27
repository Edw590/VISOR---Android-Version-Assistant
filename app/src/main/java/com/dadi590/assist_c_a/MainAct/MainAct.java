package com.dadi590.assist_c_a.MainAct;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dadi590.assist_c_a.GlobalUtils.UtilsGeneral;
import com.dadi590.assist_c_a.MainSrv;
import com.dadi590.assist_c_a.Modules.Speech.Speech2;
import com.dadi590.assist_c_a.R;
import com.dadi590.assist_c_a.GlobalUtils.UtilsServices;

import java.util.Locale;

/**
 * The main {@link Activity} of the application - MainActivity.
 */
public class MainAct extends AppCompatActivity {

	@Override
	protected final void onStart() {
		super.onStart();

		// Do this below every time the activity is started/resumed/whatever

		UtilsServices.startMainSrv(UtilsGeneral.getMainAppContext());

		final int perms_left = UtilsPermsRequests.wrapperRequestPerms(null, UtilsGeneral.getMainAppContext());
		UtilsPermsRequests.warnPermissions(perms_left, false);
	}

	@Override
	protected final void onCreate(@Nullable final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_act);

		// Do this only once, when the activity is created and while it's not destroyed

		UtilsServices.startMainSrv(UtilsGeneral.getMainAppContext());

		setButtonsClickListeners();

		// To request focus to the EditText that sends text to the assistant
		final EditText editText = findViewById(R.id.txt_to_send);
		editText.requestFocus();
	}


	/**
	 * Sets all the listeners for buttons of the activity.
	 */
	private void setButtonsClickListeners() {
		final EditText txt_to_speech = findViewById(R.id.txt_to_speech);
		final EditText txt_to_send = findViewById(R.id.txt_to_send);

		findViewById(R.id.btn_tests).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				System.out.println(getOpPackageName());
				// todo Try the above on Lollipop as a curiosity and then document that in the package-info of
				//  HiddenMethods
			}
		});
		findViewById(R.id.btn_perms).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					final NotificationManager mNotificationManager = (NotificationManager)
							getSystemService(Context.NOTIFICATION_SERVICE);

					// Check if the notification policy access has been granted for the app.
					if (!mNotificationManager.isNotificationPolicyAccessGranted()) {
						final Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
						startActivity(intent);
					}
				}

				final int perms_left = UtilsPermsRequests.wrapperRequestPerms(MainAct.this, UtilsGeneral.getMainAppContext());
				UtilsPermsRequests.warnPermissions(perms_left, true);

				//MainActTests.for_tests(getApplicationContext());
			}
		});
		findViewById(R.id.btn_device_admin).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				startActivity(new Intent().setComponent(new ComponentName("com.android.settings",
						"com.android.settings.DeviceAdminSettings")));
				// Didn't find any constants for these 2 strings above
			}
		});
		findViewById(R.id.btn_speak_min).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final String speak = txt_to_speech.getText().toString();
				if (MainSrv.getSpeech2() != null) {
					MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_LOW, null);
				}
			}
		});
		findViewById(R.id.btn_speak_high).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				final String speak = txt_to_speech.getText().toString();
				if (MainSrv.getSpeech2() != null) {
					MainSrv.getSpeech2().speak(speak, Speech2.NO_ADDITIONAL_COMMANDS, Speech2.PRIORITY_HIGH, null);
				}
				// Leave PRIORITY_HIGH there because CRITICAL will get the volume in the maximum, and this is probably
				// just to test if the priority implementation is working.
			}
		});
		findViewById(R.id.btn_send_text).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				if ("start".equals(txt_to_send.getText().toString().toLowerCase(Locale.ENGLISH))) {
					if (MainSrv.getAudioRecorder() != null) {
						MainSrv.getAudioRecorder().record(true,
								MediaRecorder.AudioSource.MIC);
					}
				} else if ("stop".equals(txt_to_send.getText().toString().toLowerCase(Locale.ENGLISH))) {
					if (MainSrv.getAudioRecorder() != null) {
						MainSrv.getAudioRecorder().record(false, -1);
					}
				}

				/*final Intent broadcast_intent = new Intent(BroadcastReceivers_com_registo.ENVIAR_TAREFA);
				broadcast_intent.putExtra("extras_frase_str", txt_to_send.getText().toString());
				broadcast_intent.putExtra("extras_resultados_parciais", false);
				sendBroadcast(broadcast_intent, GL_CONSTS.ASSIST_C_A_RECV_PERM);*/
			}
		});
		findViewById(R.id.btn_skip_speech).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				if (MainSrv.getSpeech2() != null) {
					MainSrv.getSpeech2().skipCurrentSpeech();
				}
			}
		});
	}
}
