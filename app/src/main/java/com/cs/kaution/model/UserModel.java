package com.cs.kaution.model;

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

import com.google.firebase.Timestamp;

/*
UserModel is a user record stored in USERS dataset in Firestore Database. Each UserModel object
is instantiated with a user name, phone number, user ID, recent message, and timestamp when created.
 */

public class UserModel {
    String userName;
    String phoneNumber;
    String userId;
    String recentMessageString;
    String fcmToken;
    double latitude, longitude;
    Timestamp createdTimestamp;

    public UserModel() {
    }

    public UserModel(Timestamp createdTimestamp, String phoneNumber, String userName,
                     String userId, String fcmToken) {
        this.createdTimestamp = createdTimestamp;
        this.phoneNumber = phoneNumber;
        this.userName = userName;
        this.userId = userId;
        this.fcmToken = fcmToken;
    }

    public UserModel(Timestamp createdTimestamp, double latitude, double longitude, String phoneNumber,
                     String userName, String userId, String fcmToken) {
        this.createdTimestamp = createdTimestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phoneNumber = phoneNumber;
        this.userName = userName;
        this.userId = userId;
        this.fcmToken = fcmToken;
    }

    // Getters and Setters
    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRecentMessageString() {
        return recentMessageString;
    }

    public void setRecentMessageString(String recentMessageString) {
        this.recentMessageString = recentMessageString;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
