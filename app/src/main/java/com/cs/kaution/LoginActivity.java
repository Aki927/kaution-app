package com.cs.kaution;

//*********************************************************************
//	Jerome Laranang
//
//  This Android program is a hazard awareness app where users can send other Kaution
//  app users an incident report by taking a photo and writing a description
//  of hazards or any public safety concern that they may want to warn others about.
//  Push notifications are received in the background and foreground to any
//  user within 50 metres distance from the sender. Firebase Authentication is used
//  to authorize users during login, Firestore Database is used to manage the data,
//  and Firebase Storage is used to manage images.
//
//  This app is not yet available in the Play Store. Users will need an .apk file to run the program.
//*********************************************************************

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.hbb20.CountryCodePicker;

public class LoginActivity extends AppCompatActivity {
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final int PIC_ID = 102;
    private static final int NOTIFICATION_CODE = 101;


    //private final int NOTIFICATION_ID = 104;
    private CountryCodePicker countryCodePicker;
    private EditText phoneNumber;

    private final String PHONE_KEY = "phoneNumberInput";

    private long authStartTime;
    /*
    @Override
    protected void onStart() {
        super.onStart();
        int ALL_PERMISSIONS = 101;

        final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS};

        ActivityCompat.requestPermissions(this, permissions, ALL_PERMISSIONS);
    }
     */

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ask user for notification permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_CODE);
        }
        Log.d("TAG", "################## CAMERA PERMISSION ##################");
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Uses a country code picker. Note: Kaution currently only allows Canada/US number formats +1 ###-###-####
        countryCodePicker = findViewById(R.id.countryCode);
        phoneNumber = findViewById(R.id.phoneNumber);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        // Progress bar is only visible once user clicks verify button
        progressBar.setVisibility(View.GONE);

        // Registers the carrier number country code i.e., +1 for Canada and US.
        countryCodePicker.registerCarrierNumberEditText(phoneNumber);

        authStartTime = System.nanoTime();
        SharedPreferences sharedPreferences = getSharedPreferences("TASK_PREF", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("AUTH_START_TIME", authStartTime);
        editor.apply();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Saves current state of phone number when user re-enters the app or changes screen orientation
        if (savedInstanceState != null) {
            phoneNumber.setText(savedInstanceState.getString(PHONE_KEY, ""));
        }

    }

    /*
    Validates user entry for phone number and shows an error if number is invalid or empty.
    Phone number is put into an intent and is passed with the user to OneTimePassActivity
     */
    public void loginButtonClicked(View view) {
        String phoneNumberText = phoneNumber.getText().toString().trim();
        if (phoneNumberText.isEmpty()) {
            phoneNumber.setError("Number cannot be left blank!");
        } else if (!countryCodePicker.isValidFullNumber()) {
            phoneNumber.setError("Not a valid number");
        } else {
            Intent intent = new Intent(LoginActivity.this, OneTimePassActivity.class);
            intent.putExtra("PHONE_NUMBER", countryCodePicker.getFullNumberWithPlus());
            intent.putExtra("AUTH_START_TIME", authStartTime);
            startActivity(intent);
        }
    }

    // Out-state for saved instances
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(PHONE_KEY, phoneNumber.getText().toString());
    }
}