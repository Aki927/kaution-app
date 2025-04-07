package com.cs.kaution.utils;

//*********************************************************************
//	Jerome Laranang, T00635622
//
//	COMP 3461 Phase 4: High-Fidelity Prototype
//
//  This Android program is a safety app where users can send other Kaution
//  app users an incident report by taking a photo and writing a description
//  such as hazards or public safety concerns that they may want to warn others about.
//  Push notifications are received in the background and foreground to any
//  user within 50 metres distance from the sender. Firebase Authentication is used
//  to authorize users during login, Firestore Database is used to manage the data,
//  and Firebase Storage is used to manage images.
//
//  This app is not yet available in the Play Store. Users will need an .apk file to run the program.
//*********************************************************************

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Locale;

// Helper class to retrieve collections and references from Firestore
public class FirebaseUtil {

    // Returns UID
    public static String currentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }

    // Returns a CollectionReference of all users in the USERS dataset
    public static CollectionReference allUserCollectionReference() {
        return FirebaseFirestore.getInstance().collection("USERS");
    }

    // Returns a DocumentReference of the current user's instance in the USERS dataset
    public static DocumentReference currentUserDetails() {
        return FirebaseFirestore.getInstance().collection("USERS").document(currentUserId());
    }

    // Returns a CollectionReference of existing records in the KAUTIONS dataset
    public static CollectionReference allKautionCollectionReference() {
        return FirebaseFirestore.getInstance().collection("KAUTIONS");
    }

    // Returns a DocumentReference of a the instance of an existing Kaution in the KAUTIONS dataset
    public static DocumentReference currentKautionDetails() {
        return FirebaseFirestore.getInstance().collection("KAUTIONS").document(currentUserId());
    }

    // Returns a CollectionReference of existing records in the RECEIVED_KAUTIONS dataset
    public static CollectionReference allReceivedKautionCollectionReference() {
        return FirebaseFirestore.getInstance().collection("RECEIVED_KAUTIONS");
    }

    // Returns a DocumentReference of a the instance of an existing received Kaution in the RECEIVED_KAUTIONS dataset
    public static DocumentReference currentReceivedKautionDetails() {
        return FirebaseFirestore.getInstance().collection("RECEIVED_KAUTIONS").document(currentUserId());
    }

    // Format the timestamps
    public static String formatTimestamp(Timestamp t) {
        return new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.CANADA).format(t.toDate());
    }

    // Return true if user is already logged in and false otherwise. Used for a case where user should stay logged in.
    public static boolean isLoggedIn() {
        return currentUserId() != null;
    }


    // Returns a CollectionReference of all the instances of the BUG_REPORTS dataset in Firestore Database.
    public static CollectionReference allBugReportCollectionReference() {
        return FirebaseFirestore.getInstance().collection("BUG_REPORTS");
    }

    public static CollectionReference allKautionModelCollectionReference() {
        return FirebaseFirestore.getInstance().collection("KAUTIONS");
    }


}
