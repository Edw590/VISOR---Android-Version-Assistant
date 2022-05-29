/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dadi590.assist_c_a.VoiceInteraction;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.dadi590.assist_c_a.R;

@RequiresApi(api = Build.VERSION_CODES.L)
public class VoiceInteractionMain extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.out.println("------------------------");
        System.out.println(getPackageManager().checkPermission("android.permission.PACKAGE_USAGE_STATS","com.dadi590.assist_c_a.VoiceInteraction"));
        System.out.println(getPackageManager().checkPermission("android.permission.RECORD_AUDIO","com.dadi590.assist_c_a.VoiceInteraction"));
        System.out.println(getPackageManager().checkPermission("android.permission.MANAGE_VOICE_KEYPHRASES","com.dadi590.assist_c_a.VoiceInteraction"));
        System.out.println("------------------------");

        // todo VÊ O RESULTADO DO isActiveService() do VoiceInteractionService de alguma forma!!!!!!!!

        System.out.println("JJJJJJJJJJJJJJJJ");
        setContentView(R.layout.aohd_main);
        findViewById(R.id.start).setOnClickListener(mStartListener);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    View.OnClickListener mStartListener = new View.OnClickListener() {
        public void onClick(View v) {
            startService(new Intent(VoiceInteractionMain.this, MainInteractionService.class));
        }
    };
}
