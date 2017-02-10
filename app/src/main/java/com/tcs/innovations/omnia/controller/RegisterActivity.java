package com.tcs.innovations.omnia.controller;

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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class RegisterActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Button start;

    private EditText ipEditText, portEditText;

    private final int REQUEST_CODE_SOME_FEATURES_PERMISSIONS = 0;
    List<String> permissions;

    private RelativeLayout mFormView;
    private ZBarScannerView scannerView;
    private Switch toggleButton;

    // Call back to connect to device and enable bluetooth
    private CompoundButton.OnCheckedChangeListener toggleListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (b){
                // The toggle is enabled
                scannerView.setVisibility(View.VISIBLE);
                mFormView.setVisibility(View.INVISIBLE);
                scannerView.startCamera();
            }else {
                // The toggle is disabled
                scannerView.setVisibility(View.INVISIBLE);
                mFormView.setVisibility(View.VISIBLE);
                scannerView.stopCamera();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_ip);

        mFormView = (RelativeLayout) findViewById(R.id.ip_form_view);
        scannerView = (ZBarScannerView) findViewById(R.id.scanner_view);
        scannerView.setAutoFocus(true);
        scannerView.setResultHandler(this);

        toggleButton = (Switch) findViewById(R.id.toggle_btn);
        toggleButton.setOnCheckedChangeListener(toggleListener);

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

    @Override
    protected void onPause() {
        super.onPause();
        scannerView.stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        scannerView.startCamera();
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

            scannerView.stopCamera();
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
            int hasCameraPermission = checkSelfPermission(Manifest.permission.CAMERA);
            permissions = new ArrayList<String>();
            if (hasReadPhoneState != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_PHONE_STATE);
            }

            if (hasCallPhone != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CALL_PHONE);
            }

            if (hasCameraPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CAMERA);
            }

            return !permissions.isEmpty();

        }

        return true;
    }

    @Override
    public void handleResult(Result result) {

        // Do something with the result here
        Log.v(TAG, result.getContents()); // Prints scan results
        Log.v(TAG, result.getBarcodeFormat().getName()); // Prints the scan format (qrcode, pdf417 etc.)

        String ip = result.getContents();
        String port = "1883";

        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.shared_prefs), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.ip_address), ip);
        editor.putString(getString(R.string.port), port);
        editor.commit();

        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);


    }
}
