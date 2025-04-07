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

// This KautionModel object is what is stored in the Firebase database for each sent or received incident report
public class KautionModel {
    String userId;
    String description;
    double latitude, longitude;
    Timestamp kautionTimestamp;
    String imageUrl;

    public KautionModel() {}

    // Received kaution without image
    public KautionModel(String userId, String description, String imageUrl) {
        this.userId=userId;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    // Sent kaution with image
    public KautionModel(String description, String imageUrl, Timestamp kautionTimestamp, double latitude, double longitude, String userId) {
        this.description = description;
        this.imageUrl = imageUrl;
        this.kautionTimestamp = kautionTimestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userId = userId;
    }

    // Sent kaution without image
    public KautionModel(String description, Timestamp kautionTimestamp, double latitude, double longitude, String userId) {
        this.description = description;
        this.kautionTimestamp = kautionTimestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userId = userId;
    }

    // Getters and Setters for eac attribute
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Timestamp getKautionTimestamp() {
        return kautionTimestamp;
    }

    public void setKautionTimestamp(Timestamp kautionTimestamp) { this.kautionTimestamp = kautionTimestamp; }

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
