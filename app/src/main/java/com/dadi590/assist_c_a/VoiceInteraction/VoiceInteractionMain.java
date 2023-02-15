package com.dadi590.assist_c_a.VoiceInteraction;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.dadi590.assist_c_a.R;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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
