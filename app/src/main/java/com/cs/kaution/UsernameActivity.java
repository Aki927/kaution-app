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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cs.kaution.model.UserModel;
import com.cs.kaution.utils.FirebaseUtil;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

public class UsernameActivity extends AppCompatActivity {
    EditText usernameInput;
    Button startButton;
    ProgressBar progressBar;
    String phoneNumber;
    String fcmToken;
    UserModel userModel;
    private static final int LOCATION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_username);

        // Get the phoneNumber from the intent passed from OneTimePassActivity
        phoneNumber = getIntent().getExtras().getString("PHONE_NUMBER");
        Log.d("TAG", "UsernameActivity getIntent PHONE_NUMBER >>> " + phoneNumber);

        usernameInput = findViewById(R.id.usernameInput);
        startButton = findViewById(R.id.startButton);
        progressBar = findViewById(R.id.progressBar);

        // If a user already exists, their name is populated in usernameInput EditText
        getUsername();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        fcmToken = task.getResult();
                        Log.d("FCM Token", "Retrieved token: " + fcmToken);
                        saveTokenToFirestore(fcmToken);
                    } else {
                        Log.e("FCM Token", "Fetching FCM token failed", task.getException());
                    }
                });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /*
    Start button is temporarily hidden for a second when the user clicks the start button. This prevents
    multiple intentional or non-intentional clicks since this UsernameActivity is followed by the MainActivity
     */
    public void setInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            startButton.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            startButton.setVisibility(View.VISIBLE);
        }
    }

    /*
    If a user already exists, their name is populated in usernameInput EditText.
    To get username, FirebaseUtil.currentUserDetails() is called. This method returns a DocumentReference of
    the user if the instance exists in the Firestore Database. If it exists, then the username is loaded onto
    usernameInput.
     */
    public void getUsername() {
        setInProgress(true);
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            setInProgress(false);
            if (task.isSuccessful()) {
                userModel = task.getResult().toObject(UserModel.class);
                if (userModel != null)
                    usernameInput.setText(userModel.getUserName());
            }
        });
    }

    private void saveTokenToFirestore(String token) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            FirebaseFirestore.getInstance()
                    .collection("USERS")
                    .document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> Log.d("Firestore", "FCM Token saved successfully"))
                    .addOnFailureListener(e -> Log.e("Firestore", "USERNAME_ACTIVITY >>> SAVE_TOKEN_TO_FIRESTORE >>> Firestore call failed", e));
        }
        Log.i("TOKEN", "USERNAME_ACTIVITY >>> SAVE_TOKE_TO_FIRESTORE: " + token + " (SUCCESS)");
    }

    /*
    User enters their desired name to be used in the application and must be 3 characters minimum,
    otherwise a toast message warns the user.
     */
    public void startButtonClicked(View view) {
        setUsername();
    }

    public void setUsername() {
        String name = usernameInput.getText().toString();
        int minLength = 3;
        if (name.length() < minLength) {
            Toast.makeText(this, "Name must be at least 3 characters long!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        setInProgress(true);
        /*
        If a user already exists, the user's username is set to whatever is in the EditText. Otherwise,
        a new UserModel is created with the current timestamp, phone number, user ID, and username.
        a new UserModel is created with the current timestamp, phone number, user ID, and username.
        An intent is used to connect the user to the MainActivity.
         */
        if (userModel != null) {
            userModel.setUserName(name);
            FirebaseUtil.currentUserDetails().get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Double lat = documentSnapshot.getDouble("latitude");
                    Double lon = documentSnapshot.getDouble("longitude");

                    if (lat != null && lon != null) {
                        userModel.setLatitude(lat);
                        userModel.setLongitude(lon);
                    }
                }
            }).addOnFailureListener(e -> {
                Log.e("Firebase", "Error fetching location", e);
            });
        } else {
            userModel = new UserModel(
                    Timestamp.now(),
                    0.0,
                    0.0,
                    phoneNumber,
                    name,
                    FirebaseUtil.currentUserId(),
                    fcmToken);
        }

        FirebaseUtil.currentUserDetails().set(userModel).addOnCompleteListener(task -> {
            Intent intent = new Intent(UsernameActivity.this, MainActivity.class);
            /*
            Reset the navigation stack so other activities are removed and new task is created
            // for MainActivity
             */
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        SharedPreferences sharedPreferences = getSharedPreferences("TASK_PREF", Context.MODE_PRIVATE);
        long authStartTime = sharedPreferences.getLong("AUTH_START_TIME", -1);
        long authEndTime = System.nanoTime();
        long authTaskTime = authEndTime - authStartTime;
        double seconds = (double)authTaskTime / 1_000_000_000.0;
        Log.i("TASK_AUTH", "<<< AUTHENTICATION COMPLETED IN " + seconds + " SECONDS >>>");
    }

}