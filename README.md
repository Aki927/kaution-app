
<img width="1048" height="325" alt="Screenshot 2025-08-17 at 8 01 53 AM" src="https://github.com/user-attachments/assets/606b2d36-e6fa-42a4-9b4a-2e4406458127" />

Kaution is a mobile safety app that enables users to share and receive location-based hazard alerts, featuring OTP verification, real-time notifications, and a secure login system powered by Firebase.


Why

Other hazard awareness apps bombard you with irrelevant alerts causing unecessary panic. Imagine walking out your home and seeing this on your screen:

<img src="https://github.com/user-attachments/assets/5861f77f-a381-449c-b286-ecacdc8404c0" alt="Kaution Demo Screenshot" width="300"/>

Kaution ensures you only see verified, relevant alerts in your immediate area so you can respond quickly without the noise of false alarms or distant hazards.


## 🎥 Demo

[Watch the demo on YouTube](https://youtu.be/0YsQDXU2pEI)


Core Features

**Secure Authentication** via Firebase (Email + OTP)
**Real-Time Location** updates via background service
**Instant Alerts** using Firebase Cloud Messaging (FCM)
One-Time Password (OTP) system with countdown reset
Built-in **Bug Report** module for user feedback
MVP backend support for future Spring Boot integration


Tech Stack

| Layer         | Tech                          |
|---------------|-------------------------------|
| Frontend      | Android (Java, XML)           |
| Authentication| Firebase Auth, OTP logic      |
| Database      | Firestore                     |
| Notifications | Firebase Cloud Messaging (FCM)|
| Location      | Android LocationManager API   |


FirebaseAuthHelper.java

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


OneTimePassActivity.java

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


LocationService.java

Starts a foreground service to track and log real-time user location.

```java
locationManager.requestLocationUpdates(
    LocationManager.GPS_PROVIDER, 
    30000, // every 30 seconds
    10,    // every 10 meters
    locationListener
);
```


MyFirebaseMessagingService.java

Handles incoming FCM messages and generates notifications.

```java
@Override
public void onMessageReceived(RemoteMessage remoteMessage) {
    String message = remoteMessage.getNotification().getBody();
    sendNotification(message);
}
```


BugReportFragment.java

Collects feedback and bug reports from users.

```java
submitButton.setOnClickListener(v -> {
    String bugDetails = bugReportInput.getText().toString().trim();
    db.collection("bug_reports")
        .add(new BugReport(userId, bugDetails, Timestamp.now()))
        .addOnSuccessListener(...)
});


```
👨‍💻 Author
Jerome Laranang

LinkedIn
GitHub
