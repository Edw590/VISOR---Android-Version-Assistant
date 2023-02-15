package com.dadi590.assist_c_a.VoiceInteraction;

import android.app.Activity;
import android.app.VoiceInteractor;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.RequiresApi;

import com.dadi590.assist_c_a.R;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class TestInteractionActivity extends Activity implements View.OnClickListener {
    static final String TAG = "TestInteractionActivity";

    VoiceInteractor mInteractor;
    Button mAbortButton;
    Button mCompleteButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.out.println("KKKKKKKKKKKKKKKKKKKK");

        if (!isVoiceInteraction()) {
            //Log.iw(TAG, "Not running as a voice interaction!");
            finish();
            return;
        }

        setContentView(R.layout.aohd_test_interaction);
        mAbortButton = (Button)findViewById(R.id.abort);
        mAbortButton.setOnClickListener(this);
        mCompleteButton = (Button)findViewById(R.id.complete);
        mCompleteButton.setOnClickListener(this);

        // Framework should take care of these.
        getWindow().setGravity(Gravity.TOP);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mInteractor = getVoiceInteractor();
        VoiceInteractor.ConfirmationRequest req = new VoiceInteractor.ConfirmationRequest("This is a confirmation", null) {
            @Override
            public void onCancel() {
                //Log.ii(TAG, "Canceled!");
                getActivity().finish();
            }

            @Override
            public void onConfirmationResult(boolean confirmed, Bundle result) {
                //Log.ii(TAG, "Confirmation result: confirmed=" + confirmed + " result=" + result);
                getActivity().finish();
            }
        };
        mInteractor.submitRequest(req);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        if (v == mAbortButton) {
            VoiceInteractor.AbortVoiceRequest req = new VoiceInteractor.AbortVoiceRequest("Dammit, we suck :(", null) {
                @Override
                public void onCancel() {
                    //Log.ii(TAG, "Canceled!");
                }

                @Override
                public void onAbortResult(Bundle result) {
                    //Log.ii(TAG, "Abort result: result=" + result);
                    getActivity().finish();
                }
            };
            mInteractor.submitRequest(req);
        } else if (v == mCompleteButton) {
            VoiceInteractor.CompleteVoiceRequest req = new VoiceInteractor.CompleteVoiceRequest("Woohoo, completed!", null) {
                @Override
                public void onCancel() {
                    //Log.ii(TAG, "Canceled!");
                }

                @Override
                public void onCompleteResult(Bundle result) {
                    //Log.ii(TAG, "Complete result: result=" + result);
                    getActivity().finish();
                }
            };
            mInteractor.submitRequest(req);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
