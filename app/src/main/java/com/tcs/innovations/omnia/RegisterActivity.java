package com.tcs.innovations.omnia;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Button start;

    private EditText ipEditText, portEditText;

    private final int REQUEST_CODE_SOME_FEATURES_PERMISSIONS = 0;
    List<String> permissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_ip);

        if (checkPermission()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                requestPermissions( permissions.toArray( new String[permissions.size()] ), REQUEST_CODE_SOME_FEATURES_PERMISSIONS );
            }
        }


        start = (Button) findViewById(R.id.start_service);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(view);
            }
        });

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.shared_prefs), Context.MODE_PRIVATE);
        String ip = sharedPref.getString(getString(R.string.ip_address), "");
        String port = sharedPref.getString(getString(R.string.port), "");

        ipEditText = (EditText) findViewById(R.id.ip_address_et);
        portEditText = (EditText) findViewById(R.id.port_et);

        ipEditText.setText(ip);
        portEditText.setText(port);

    }

    public void startService(View view) {
        String ip = ipEditText.getText().toString();
        String port = portEditText.getText().toString();

        if (!ip.equalsIgnoreCase("") && !port.equalsIgnoreCase("")){
            SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.shared_prefs), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.ip_address), ip);
            editor.putString(getString(R.string.port), port);
            editor.commit();

            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
        }else {
            Toast.makeText(getApplicationContext(), "Please provide IP and port numbers", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.i(TAG, "onRequestPermissions");
        switch ( requestCode ) {
            case REQUEST_CODE_SOME_FEATURES_PERMISSIONS: {
                Log.i(TAG, "permission granted");
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    private boolean checkPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int hasReadPhoneState = checkSelfPermission(Manifest.permission.READ_PHONE_STATE);
            int hasCallPhone = checkSelfPermission(Manifest.permission.CALL_PHONE);
            permissions = new ArrayList<String>();
            if (hasReadPhoneState != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_PHONE_STATE);
            }

            if (hasCallPhone != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CALL_PHONE);
            }

            return !permissions.isEmpty();

        }

        return true;
    }
}
