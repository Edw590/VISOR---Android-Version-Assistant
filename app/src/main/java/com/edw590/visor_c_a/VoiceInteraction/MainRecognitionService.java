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

import android.content.Intent;
import android.os.Build;
import android.speech.RecognitionService;

import androidx.annotation.RequiresApi;

/**
 * Stub recognition service needed to be a complete voice interactor.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class MainRecognitionService extends RecognitionService {

    private static final String TAG = "MainRecognitionService";

    @Override
    public void onCreate() {
        super.onCreate();
        //Log.ii(TAG, "onCreate");
    }

    @Override
    protected void onStartListening(Intent recognizerIntent, Callback listener) {
        //Log.id(TAG, "onStartListening");
    }

    @Override
    protected void onCancel(Callback listener) {
        //Log.id(TAG, "onCancel");
    }

    @Override
    protected void onStopListening(Callback listener) {
        //Log.id(TAG, "onStopListening");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.id(TAG, "onDestroy");
    }
}
