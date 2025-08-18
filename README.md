
<img width="237" height="348" alt="Screenshot 2025-08-17 at 8 27 39 AM" src="https://github.com/user-attachments/assets/7c532903-94ad-4827-a3ed-d65b3e0daa25" />

Kaution is a safety app that enables users to share and receive location-based hazard alerts, featuring OTP verification, real-time notifications, and a secure login system powered by Firebase.


Why?

Other hazard awareness apps bombard you with irrelevant alerts causing unecessary panic. Imagine walking out your home and seeing this on your screen:

<p align="center">
  <img src="https://github.com/user-attachments/assets/5861f77f-a381-449c-b286-ecacdc8404c0" alt="Kaution Demo Screenshot" width="300"/>
</p>

Kaution ensures you only see verified, relevant alerts in your immediate area so you can respond quickly without the noise of false alarms or distant hazards.


## 🎥 Click Below for a Demo

[![Watch the demo](./images/play_demo.png)](https://www.youtube.com/watch?v=0YsQDXU2pEI)


## Core Features

**Secure Authentication** via Firebase (Email + OTP)
**Real-Time Location** updates via background service
**Instant Alerts** using Firebase Cloud Messaging (FCM)
One-Time Password (OTP) system with countdown reset
Built-in **Bug Report** module for user feedback
MVP backend support for future Spring Boot integration


## Tech Stack

| Layer         | Tech                          |
|---------------|-------------------------------|
| Frontend      | Android (Java, XML)           |
| Authentication| Firebase Auth, OTP logic      |
| Database      | Firestore                     |
| Notifications | Firebase Cloud Messaging (FCM)|
| Location      | Android LocationManager API   |


## Firebase Auth Helper

Manages authentication via email/password and sends verification emails.

```java
public void login(String email, String password, AuthCallback callback) {
    mAuth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess(mAuth.getCurrentUser());
            } else {
                callback.onFailure(task.getException());
            }
        });
}
```


## One Time Pass

Handles OTP verification and countdown reset.

```java
countDownTimer = new CountDownTimer(60000, 1000) {
    public void onTick(long millisUntilFinished) {
        countdownText.setText("Expires in: " + millisUntilFinished / 1000 + "s");
    }
    public void onFinish() {
        generateAndSendOTP(); // Auto-reset OTP after 60s
    }
}.start();
```


## Location Service

Starts a foreground service to track and log real-time user location.

```java
locationManager.requestLocationUpdates(
    LocationManager.GPS_PROVIDER, 
    30000, // every 30 seconds
    10,    // every 10 meters
    locationListener
);
```


## Firebase Messaging Service

Handles incoming FCM messages and generates notifications.

```java
@Override
public void onMessageReceived(RemoteMessage remoteMessage) {
    String message = remoteMessage.getNotification().getBody();
    sendNotification(message);
}
```


## Bug Report

Collects feedback and bug reports from users.

```java
submitButton.setOnClickListener(v -> {
    String bugDetails = bugReportInput.getText().toString().trim();
    db.collection("bug_reports")
        .add(new BugReport(userId, bugDetails, Timestamp.now()))
        .addOnSuccessListener(...)
});


```


## 👨‍💻 Author

**Jerome Laranang**

* [LinkedIn](https://linkedin.com/in/jerome-laranang)
* [GitHub](https://github.com/Aki927)
