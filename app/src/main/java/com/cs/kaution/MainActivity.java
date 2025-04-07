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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.Manifest;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cs.kaution.model.UserModel;
import com.cs.kaution.utils.FirebaseUtil;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;

// MainActivity nests the HomeFragment, MyKautionFragment, SettingsFragment and changes views according to user bottom navigation interaction
public class MainActivity extends AppCompatActivity {
    private final int NOTIFICATION_ID = 104;

    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment;
    MyKautionFragment myKautionFragment;
    SettingsFragment settingsFragment;
    TextView actionBarText;
    UserModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TIRAMISU and newer require permission for notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission();
        } else {
            // Initialize FCM if older than TIRAMISU
            initializeFCM();
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        createNotificationChannel(); // Notification channel

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        actionBarText = findViewById(R.id.action_bar_text);
        String welcome = "Welcome to Kaution, %s!";
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            model = task.getResult().toObject(UserModel.class);
            actionBarText.setText(String.format(welcome, model.getUserName()));
        });
        homeFragment = new HomeFragment();
        myKautionFragment = new MyKautionFragment();
        settingsFragment = new SettingsFragment();

        // Changes views based on user clicking the bottom navigation options: Home, MyKautions, Settings
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_home) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, homeFragment).commit();
            }
            if (item.getItemId() == R.id.menu_mykautions) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, myKautionFragment).commit();
            }
            if (item.getItemId() == R.id.menu_settings) {
                getSupportFragmentManager().beginTransaction().replace(R.id.main_frame_layout, settingsFragment).commit();
            }
            return true;
        });
        bottomNavigationView.setSelectedItemId(R.id.menu_mykautions); // Finally, set the view

        // Check if the activity was opened from a notification
        Intent intent = getIntent();
        boolean fromNotification = intent.getBooleanExtra("FROM_NOTIFICATION", false);
        long receivedStartTime = intent.getLongExtra("RECEIVED_START_TIME", -1);

        /*
        if (fromNotification && receivedStartTime != -1) {
            Log.d("TAG", "App opened from notification!");
            long receivedEndTime = System.nanoTime();
            long receiveTaskTime = receivedEndTime - receivedStartTime;
            double seconds = (double) receiveTaskTime / 1_000_000_000.0;
            Log.i("TASK_RECEIVE_KAUTION", "RECEIVE A KAUTION TASK COMPLETED IN " + seconds + " seconds");
        }
         */

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Get user permission for notifications
    private void requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_ID);
        } else {
            initializeFCM();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == NOTIFICATION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeFCM();
            } else {
                Log.e("TAG", "Notification permission denied.");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // Gets the instance of FCM, specifically a user's fcm Token which is required for sending push notifications among users
    private void initializeFCM() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    FirebaseUtil.currentUserDetails().update("fcmToken", token);
                    Log.d("TAG", "FCM Token: " + token);
                });
    }

    // Notification channel used when sender sends an incident report and nearby users receive the push notifications
    private void createNotificationChannel() {
        String channelId = "location_channel_id";
        String channelName = "Location Updates";
        String channelDescription = "This channel is used for location updates in the background.";
        int importance = NotificationManager.IMPORTANCE_LOW;

        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setDescription(channelDescription);

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }


}
