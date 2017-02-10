package com.tcs.innovations.omnia.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.http.SslError;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity implements DiscreteSeekBar.OnProgressChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private String clientId = "omnia_controller";

    private String topic_control = "omnia/control";
    private String topic_text = "omnia/text";
    private String topic_expression = "omnia/expression";

    private int qos = 2;
    private String broker = "";
    private String url = "";
//    private String url = "https://google.com";
    MqttAndroidClient client;

    private ImageView btnUp;
    private ImageView btnLeft;
    private ImageView btnCenter;
    private ImageView btnRight;
    private ImageView btnDown;

    private EditText mEditText;
    private ImageView sendButton;
    private DiscreteSeekBar throttle;
    private WebView webView;

    private ImageView.OnClickListener sendListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String data = mEditText.getText().toString();

            if (!data.equalsIgnoreCase("")){
                publishMessage(data, topic_text);
            }

            mEditText.setText("");

        }
    };

    private EditText.OnKeyListener keyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {

            // If the event is a key-down event on the "enter" button
            if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                    (keyCode == KeyEvent.KEYCODE_ENTER)) {
                // Perform action on key press

                EditText editText = (EditText) v;

                if(v == mEditText)
                {
                    String data = editText.getText().toString();
                    publishMessage(data, topic_text);
                }

                mEditText.setText("");

                return true;
            }
            return false;

        }
    };

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            String message = "";

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                // Pressed
                switch (view.getId()){
                    case R.id.button_up:
                        message = "forward_press*";
                        break;

                    case R.id.button_down:
                        message = "reverse_press*";
                        break;

                    case R.id.button_left:
                        message = "left_press*";
                        break;

                    case R.id.button_right:
                        message = "right_press*";
                        break;

                    case R.id.button_center:
                        message = "center_press*";
                        break;
                }

            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                // Released

                switch (view.getId()){
                    case R.id.button_up:
                        message = "forward_release*";
                        break;

                    case R.id.button_down:
                        message = "reverse_release*";
                        break;

                    case R.id.button_left:
                        message = "left_release*";
                        break;

                    case R.id.button_right:
                        message = "right_release*";
                        break;

                    case R.id.button_center:
                        message = "center_release*";
                        break;
                }
            }

            publishMessage(message, topic_control);

            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditText = (EditText) findViewById(R.id.chat_edit_text);
        sendButton = (ImageView) findViewById(R.id.send_image);
        sendButton.setOnClickListener(sendListener);
        sendButton.setOnKeyListener(keyListener);

        throttle = (DiscreteSeekBar) findViewById(R.id.throttle);
        throttle.setOnProgressChangeListener(this);

        btnUp = (ImageView)findViewById(R.id.button_up);
        btnLeft = (ImageView)findViewById(R.id.button_left);
        btnCenter = (ImageView)findViewById(R.id.button_center);
        btnRight = (ImageView)findViewById(R.id.button_right);
        btnDown = (ImageView)findViewById(R.id.button_down);

        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getApplicationContext().getString(R.string.shared_prefs), Context.MODE_PRIVATE);
        String ip = sharedPref.getString(getApplicationContext().getString(R.string.ip_address), "");
        url = "https://" + ip + ":8080/";

        webView = (WebView) findViewById(R.id.web_view);
        webView.setWebViewClient(new MyBrowser());
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    request.grant(request.getResources());
                }
            }
        });
        webView.loadUrl(url);

        btnUp.setOnTouchListener(touchListener);
        btnDown.setOnTouchListener(touchListener);
        btnLeft.setOnTouchListener(touchListener);
        btnRight.setOnTouchListener(touchListener);
        btnCenter.setOnTouchListener(touchListener);

        connectPaho();
    }

    private void connectPaho(){
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getApplicationContext().getString(R.string.shared_prefs), Context.MODE_PRIVATE);
        String ip = sharedPref.getString(getApplicationContext().getString(R.string.ip_address), "");
        broker = "tcp://" + ip + ":1883";

        client =
                new MqttAndroidClient(getApplicationContext(), broker,
                        clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                    btnUp.setEnabled(true);
                    btnDown.setEnabled(true);
                    btnLeft.setEnabled(true);
                    btnRight.setEnabled(true);
                    btnCenter.setEnabled(true);

                    Toast.makeText(getApplicationContext(), "MQTT Connection Successful", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");
                    btnUp.setEnabled(false);
                    btnDown.setEnabled(false);
                    btnLeft.setEnabled(false);
                    btnRight.setEnabled(false);
                    btnCenter.setEnabled(false);

                    Toast.makeText(getApplicationContext(), "MQTT Connection Failed", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void publishMessage(String payload, String topic){
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
            Log.i(TAG, "Message published: " + message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        String message = "pwm" + value + "*";
        Log.i(TAG, message);
        publishMessage(message, topic_control);
    }

    @Override
    public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }

    }
}
