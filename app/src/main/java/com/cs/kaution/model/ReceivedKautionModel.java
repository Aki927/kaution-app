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

// Received Kaution object with image that is created when a sender sends a Kaution report and another user is withing 50 metres.
public class ReceivedKautionModel {
    String imageUrl;
    String description;
    String userId;
    Timestamp kautionTimestamp;

    public ReceivedKautionModel() {}

    public ReceivedKautionModel(String userId, String description, String imageUrl, Timestamp kautionTimestamp) {
        this.userId = userId;
        this.description = description;
        this.imageUrl = imageUrl;
        this.kautionTimestamp = kautionTimestamp;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) { this.userId = userId; }

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

    public void setKautionTimestamp(Timestamp kautionTimestamp) {
        this.kautionTimestamp = kautionTimestamp;
    }
}
