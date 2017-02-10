package com.tcs.innovations.omnia.controller;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.github.jorgecastillo.FillableLoader;
import com.github.jorgecastillo.State;
import com.github.jorgecastillo.listener.OnStateChangeListener;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashScreen extends AppCompatActivity implements OnStateChangeListener {

    FillableLoader fillableLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        String svg = "M397.241,176.552H276.303L388.414,0H247.172L114.759,264.828h104.166L114.759,512L397.241,176.552z";
        fillableLoader = (FillableLoader) findViewById(R.id.fillableLoader);
        fillableLoader.setSvgPath(Path.bot2);
        fillableLoader.start();

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Intent i = new Intent(SplashScreen.this, LoginActivity.class);
//                startActivity(i);
//                finish();
//            }
//        }, 10000);

        fillableLoader.setOnStateChangeListener(this);

    }

    @Override
    public void onStateChange(int state) {
//        SplashScreen.showStateHint(state);
        Log.i("elecflix", "in onStateChanged");

        switch(state) {
            case State.FILL_STARTED:
                break;
            case State.FINISHED:
                Intent i = new Intent(SplashScreen.this, RegisterActivity.class);
                startActivity(i);
                finish();
                break;
        }
    }
}
