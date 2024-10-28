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

package com.edw590.visor_c_a.VoiceInteraction;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.voice.VoiceInteractionSession;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.edw590.visor_c_a.Modules.AudioRecorder.UtilsAudioRecorderBC;
import com.edw590.visor_c_a.Registry.UtilsRegistry;
import com.edw590.visor_c_a.Registry.RegistryKeys;
import com.edw590.visor_c_a.Modules.SpeechRecognitionCtrl.UtilsSpeechRecognizersBC;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class MainInteractionSession extends VoiceInteractionSession implements View.OnClickListener {
	static final String TAG = "MainInteractionSession";

	Intent mStartIntent;
	View mContentView;
	TextView mText;
	Button mStartButton;
	Button mConfirmButton;
	Button mCompleteButton;
	Button mAbortButton;

	static final int STATE_IDLE = 0;
	static final int STATE_LAUNCHING = 1;
	static final int STATE_CONFIRM = 2;
	static final int STATE_COMMAND = 3;
	static final int STATE_ABORT_VOICE = 4;
	static final int STATE_COMPLETE_VOICE = 5;

	int mState = STATE_IDLE;
	Request mPendingRequest;

	MainInteractionSession(Context context) {
		super(context);
	}

	@Override
	public void onHandleAssist(final AssistState state) {
		super.onHandleAssist(state);
	}

	@Override
	public void onCreate() {
        /*super.onCreate(args);
        showWindow();
        mStartIntent = args.getParcelable("intent");*/
	}

	@Override
	public void onShow(Bundle args, int showFlags) {
		closeSystemDialogs();
		mStartIntent = args.getParcelable("intent");

		if ((boolean) UtilsRegistry.getData(RegistryKeys.K_IS_RECORDING_AUDIO_INTERNALLY, true)) {
			// If it's recording audio, it must be stopped. So stop and start the hotword recognizer.
			UtilsAudioRecorderBC.recordAudio(false, -1, true);
		} else {
			// If it's not recording audio, start the commands recognizer.
			UtilsSpeechRecognizersBC.startCommandsRecognition();
		}
	}

	void updateState() {
		mStartButton.setEnabled(mState == STATE_IDLE);
		mConfirmButton.setEnabled(mState == STATE_CONFIRM || mState == STATE_COMMAND);
		mAbortButton.setEnabled(mState == STATE_ABORT_VOICE);
		mCompleteButton.setEnabled(mState == STATE_COMPLETE_VOICE);
	}

	public void onClick(View v) {
		if (v == mStartButton) {
			mState = STATE_LAUNCHING;
			updateState();
			//startVoiceActivity(mStartIntent);
		} else if (v == mConfirmButton) {
			if (mState == STATE_CONFIRM) {
				//mPendingRequest.sendConfirmResult(true, null);
			} else {
				//mPendingRequest.sendCommandResult(true, null);
			}
			mPendingRequest = null;
			mState = STATE_IDLE;
			updateState();
		} else if (v == mAbortButton) {
			//mPendingRequest.sendAbortVoiceResult(null);
			mPendingRequest = null;
			mState = STATE_IDLE;
			updateState();
		} else if (v== mCompleteButton) {
			//mPendingRequest.sendCompleteVoiceResult(null);
			mPendingRequest = null;
			mState = STATE_IDLE;
			updateState();
		}
	}

	@Override
	public boolean[] onGetSupportedCommands(String[] commands) {
		return new boolean[commands.length];
	}

	@Override
	public void onRequestConfirmation(ConfirmationRequest request) {
		////Log.ii(TAG, "onConfirm: prompt=" + prompt + " extras=" + extras);
		//Log.ii(TAG, "onConfirm: request=" + request);
		//mText.setText(prompt);
		mText.setText(request.toString());
		mStartButton.setText("Confirm");
		mPendingRequest = request;
		mState = STATE_CONFIRM;
		updateState();
	}

	@Override
	public void onRequestCompleteVoice(CompleteVoiceRequest request) {
		////Log.ii(TAG, "onCompleteVoice: message=" + message + " extras=" + extras);
		//Log.ii(TAG, "onCompleteVoice: request=" + request);
		//mText.setText(message);
		mText.setText(request.toString());
		mPendingRequest = request;
		mState = STATE_COMPLETE_VOICE;
		updateState();
	}

	@Override
	public void onRequestAbortVoice(AbortVoiceRequest request) {
		////Log.ii(TAG, "onAbortVoice: message=" + message + " extras=" + extras);
		//Log.ii(TAG, "onAbortVoice: request=" + request);
		//mText.setText(message);
		mText.setText(request.toString());
		mPendingRequest = request;
		mState = STATE_ABORT_VOICE;
		updateState();
	}

	@Override
	public void onRequestCommand(CommandRequest request) {
		////Log.ii(TAG, "onCommand: command=" + command + " extras=" + extras);
		//Log.ii(TAG, "onRequestCommand: request=" + request);
		//mText.setText("Command: " + command);
		mStartButton.setText("Finish Command");
		mPendingRequest = request;
		mState = STATE_COMMAND;
		updateState();
	}

	@Override
	public void onCancelRequest(Request request) {
		//Log.ii(TAG, "onCancel");
		request.cancel();
	}
}
