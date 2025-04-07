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

import android.content.Context;

import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

// Retrieves an OAuth 2.0 access token that is required for Firebase Chat Messaging HTTP v1 authentication
public class FirebaseAuthHelper {

    // Reads a JSON file from the assets directory to generate credentials and access token used for Firebase API requests
    public static String getAccessToken(Context context) throws IOException {
        // Load account credentials
        InputStream serviceAccount = context.getAssets().open("kaution-backend-firebase-adminsdk-1v91b-ab78357a5f.json");

        // Google credentials object created with the required Firebase scope
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount)
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/firebase.messaging"));

        credentials.refreshIfExpired(); // Tokens expire time to time to this refreshes it
        return credentials.getAccessToken().getTokenValue();
    }
}
