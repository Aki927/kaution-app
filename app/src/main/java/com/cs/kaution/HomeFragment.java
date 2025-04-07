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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cs.kaution.model.ReceivedKautionModel;
import com.cs.kaution.model.KautionModel;
import com.cs.kaution.utils.FirebaseUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

// Kaution users can create an incident report by writing a description, taking a photo(optional) and clicking a button to send report
public class HomeFragment extends Fragment {
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int NOTIFICATION_CODE = 101;
    private static final int PIC_ID = 102;
    long sendKautionStart;
    long sendKautionEnd;

    // Get the Firebase Storage instance
    FirebaseStorage storage = FirebaseStorage.getInstance("gs://kaution-backend.firebasestorage.app");

    EditText incidentInput;
    Button sendAlertButton, cameraButton;
    ImageView cameraImage;
    Bitmap photo;
    double longitude, latitude;
    String userInput;
    String imageUrl;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onStart() {
        super.onStart();
        int ALL_PERMISSIONS = 101;

        /*
        final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS};

        ActivityCompat.requestPermissions(this, permissions, ALL_PERMISSIONS);

         */
        // Ask the user for location permission
        if (ActivityCompat.checkSelfPermission(HomeFragment.this.requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HomeFragment.this.requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            // Start the location service
            Intent intent = new Intent(HomeFragment.this.requireContext(), LocationService.class);
            HomeFragment.this.requireContext().startForegroundService(intent); // Start the foreground service
            Log.d("TAG", "################## LOCATION FOREGROUND STARTED ##################");
            updateLocation();
        }

/*
        // Ask user for notification permission
        if (ActivityCompat.checkSelfPermission(HomeFragment.this.requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HomeFragment.this.requireActivity(),
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_CODE);
        }
        Log.d("TAG", "################## CAMERA PERMISSION ##################");
 */

        // Authenticate the user anonymously for Firebase Storage
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // Check if the user is already authenticated
        if (mAuth.getCurrentUser() == null) {
            mAuth.signInAnonymously().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("TAG", "################## AUTHENTICATION SUCCESSFUL ##################");
                } else {
                    Log.e("Auth", "Anonymous authentication failed", task.getException());
                    Toast.makeText(HomeFragment.this.requireContext(), "Authentication failed. Please restart the app.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Since AsyncTask is deprecated, we use this interface to retrieve user location asynchronously
    private interface LocationCallback {
        void onLocationFetched(double latitude, double longitude);
    }

    // Gets the users most recent location
    private void getLocation(LocationCallback callback) {
        Log.d("TAG", "Inside getLocation()");
        FusedLocationProviderClient locationProvider = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Ask user for location permission
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TAG", "Location permissions not granted.");
            return;
        }

        locationProvider.getLastLocation().addOnSuccessListener(loc -> {
            if (loc != null) {
                double fetchedLatitude = loc.getLatitude(); // User's current longitude and latitude
                double fetchedLongitude = loc.getLongitude();
                Log.d("TAG", "Lat/Lon fetched: " + fetchedLatitude + "/" + fetchedLongitude);

                // This passed a fetched location of the user and sends it to the callback
                callback.onLocationFetched(fetchedLatitude, fetchedLongitude);
            } else {
                Log.d("TAG", "Failed to fetch location.");
            }
        }).addOnFailureListener(e -> Log.e("TAG", "Error fetching location: " + e.getMessage(), e));
    }

    private void updateLocation() {
        FusedLocationProviderClient locationProvider = LocationServices.getFusedLocationProviderClient(requireActivity());
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationProvider.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                // Update user's location in Firestore
                String userId = FirebaseAuth.getInstance().getUid();
                if (userId != null) {
                    FirebaseFirestore.getInstance()
                            .collection("USERS")
                            .document(userId)
                            .update("latitude", latitude, "longitude", longitude)
                            .addOnSuccessListener(aVoid -> Log.d("LocationUpdate", "Location updated for user: " + userId))
                            .addOnFailureListener(e -> Log.e("LocationUpdate", "Error updating location", e));
                }
            }
        });
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        incidentInput = view.findViewById(R.id.incident_input);
        sendAlertButton = view.findViewById(R.id.send_alert_button);
        cameraButton = view.findViewById(R.id.camera_button);
        cameraImage = view.findViewById(R.id.camera_image);

        sendKautionStart = System.nanoTime();

        // Opens users camera and waits for the user to take a picture, then loads the picture in an ImageView
        cameraButton.setOnClickListener(view2 -> {
            if (ActivityCompat.checkSelfPermission(HomeFragment.this.requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(HomeFragment.this.requireActivity(),
                        new String[]{Manifest.permission.CAMERA}, PIC_ID);
                Log.d("TAG", "################## CAMERA PERMISSION ##################");
            } else {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, PIC_ID);
            }
        });

        // After user enters a description and an optional image, they can send an incident report by clicking this send alert button
        sendAlertButton.setOnClickListener(view1 -> {
            userInput = incidentInput.getText().toString();

            // User must enter a description at least
            if (userInput.isEmpty()) {
                Toast.makeText(HomeFragment.this.requireContext(), "Please enter incident details!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Gets the user's current location
            getLocation((fetchedLatitude, fetchedLongitude) -> {
                // Ensure the location is fetched before creating the KautionModel
                latitude = fetchedLatitude;
                longitude = fetchedLongitude;

                /*
                Checks to see if a photo was taken since photos are optional. Then it converts the picture into a byte array, then
                creates a file path for storing the picture with metadata (user input caption for picture), then uploads the picture
                to Firebase Storage where it is managed.
                 */
                if (photo != null) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    photo.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    byte[] byteData = outputStream.toByteArray();
                    String path = "kaution_pics/" + UUID.randomUUID() + ".png";
                    StorageReference kautionRef = storage.getReference(path);
                    StorageMetadata metadata = new StorageMetadata.Builder().setCustomMetadata("caption", userInput).build();
                    UploadTask uploadTask = kautionRef.putBytes(byteData, metadata);

                    // Ensures the image is uploaded before to Firebase Storage before the instance is created on Firebase Database and before locating nearby users.
                    uploadTask.addOnSuccessListener(taskSnapshot -> kautionRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        imageUrl = uri.toString();
                        KautionModel kautionModel = new KautionModel(
                                userInput,
                                imageUrl,
                                Timestamp.now(),
                                latitude,
                                longitude,
                                FirebaseUtil.currentUserId()
                        );

                        // Add the incident report along with the image, description, timestamp, location, and user ID as primary key
                        FirebaseUtil.allKautionCollectionReference().add(kautionModel).addOnSuccessListener(task -> {
                            Toast.makeText(requireContext(), "Kaution saved successfully!", Toast.LENGTH_SHORT).show();
                            cameraImage.setImageDrawable(null);
                            incidentInput.setText("");
                            photo = null;

                            // Find nearby users within a 50 meter radius. Note: 50 metres is an arbitrary distance chosen for this app.
                            findNearbyUsers(fetchedLatitude, fetchedLongitude, 50);

                        }).addOnFailureListener(error -> {
                            Toast.makeText(requireContext(), "Failed to save Kaution.", Toast.LENGTH_SHORT).show();
                            Log.e("TAG", "Error saving Kaution", error);
                        });
                    })).addOnFailureListener(error -> {
                        Toast.makeText(requireContext(), "Image upload failed!", Toast.LENGTH_SHORT).show();
                        Log.e("TAG", "Error uploading image", error);
                    });
                } else {
                    // This performs the same operation as above except without a picture taken.
                    KautionModel kautionModel = new KautionModel(
                            userInput,
                            Timestamp.now(),
                            latitude,
                            longitude,
                            FirebaseUtil.currentUserId()
                    );
                    FirebaseUtil.allKautionCollectionReference().add(kautionModel).addOnSuccessListener(task -> {
                        Toast.makeText(requireContext(), "Kaution saved successfully!", Toast.LENGTH_SHORT).show();
                        incidentInput.setText("");

                        findNearbyUsers(fetchedLatitude, fetchedLongitude, 50);

                    }).addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to save Kaution.", Toast.LENGTH_SHORT).show();
                        Log.e("TAG", "Error saving Kaution", e);
                    });
                }
                sendKautionEnd = System.nanoTime();
                long sendTaskTime = sendKautionEnd - sendKautionStart;
                double seconds = (double) sendTaskTime / 1_000_000_000.0;
                Log.i("TASK_SEND_KAUTION", "<<< SENDING KAUTION TASK COMPLETED IN " + seconds + " SECONDS >>>");
            });
        });
        return view;
    }

    // Finds nearby users within a 50 metre radius from the sender and sends a push notification if they are within proximity
    public void findNearbyUsers(double userLat, double userLon, double radius) {
        FirebaseFirestore.getInstance().collection("USERS").get()
                .addOnSuccessListener(sn -> {

                    // Fetch user and their location who are within proximity.
                    for (DocumentSnapshot d : sn.getDocuments()) {
                        String input = userInput.toString();
                        double latitude = d.getDouble("latitude");
                        double longitude = d.getDouble("longitude");

                        // If they have a latitude/longitude record, their distance is calculated relative to the sender
                        if (latitude != 0.0 && longitude != 0.0) {
                            double distance = calculateDistance(latitude, longitude, userLat, userLon);
                            Log.i("DISTANCE", "findNearbyUsers >>> DISTANCE BETWEEN SENDER/RECEIVER: " + distance);

                            // If the distance is within 50 metres proximity, a ReceivedKautionModel is created along with
                            // sent user IDs, image, description, and timestamp
                            if (distance <= radius) {
                                sendNotificationToUser(d.getId()); // Sends a push notification to the nearby user.

                                ReceivedKautionModel im = new ReceivedKautionModel(
                                        d.getId(),
                                        userInput,
                                        imageUrl,
                                        Timestamp.now()
                                );
                                FirebaseUtil.allReceivedKautionCollectionReference().add(im).addOnSuccessListener(task -> {
                                    Toast.makeText(HomeFragment.this.requireContext(), "A Kaution user has been notified!", Toast.LENGTH_SHORT).show();
                                    Log.d("TAG", "HomeFragment >>> USER WAS WITHIN PROXIMITY AND RECEIVED KAUTION.");
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(HomeFragment.this.requireContext(), "Error: Kaution failed to send.", Toast.LENGTH_SHORT).show();
                                    Log.e("TAG", "HomeFragment >>> Error finding nearby users", e);
                                });
                            }
                        }
                    }
                }).addOnFailureListener(e -> Log.e("Firestore", "HOME_FRAG >>> FIND_NEARBY_USERS >>> Firestore call failed", e));
    }

    // Uses the Halversine formula for calclating distances between users as single points on a sphere which mimics
    // distances between users in the world.
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_METRE = 6371000;
        double d_lat = Math.toRadians(lat2-lat1);
        double d_lon = Math.toRadians(lon2-lon1);
        double a = Math.sin(d_lat / 2) * Math.sin(d_lat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(d_lon / 2) * Math.sin(d_lon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS_METRE * c;
        Log.d("DISTANCE", "FROM User_1 to User_2: " + distance);
        return distance;
    }

    /*
    Sends a push notification to the nearby user using their user ID and fcmToken. It gets the Firebase Cloud Messaging (FCM)
    Token of this user a creates a JSON payload as the notification that contains an alert header and the description the sender wrote as the body.
    Then it sends it to FCM service. Recipients of the notification can click the push notification on their device which automatically
    opens the Kaution app for them to view the Kaution incident report.
     */
    private void sendNotificationToUser(String userId) {
        FirebaseFirestore.getInstance().collection("USERS")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    // Gets the unique FCM Token from USERS relation in Firebase
                    String fcmToken = document.getString("fcmToken");
                    Log.d("FCM Token", "Token: " + fcmToken);

                    // Check to see if there is an FCM Token and Create a JSON object if that token exists
                    if (fcmToken != null) {
                        JSONObject payload = new JSONObject();
                        try {
                            JSONObject msg = new JSONObject();
                            msg.put("token",fcmToken);

                            // Formats the notification message using the sender's description as the body.
                            JSONObject notification = new JSONObject();
                            notification.put("title", "Alert Nearby");
                            notification.put("body", userInput);
                            msg.put("notification",notification);
                            payload.put("message",msg);

                            sendFcmNotification(payload); // Finally, send the notification with the payload
                        } catch (JSONException e) {
                            Log.e("TAG", "Error building notification payload", e);
                        }
                    }
                }).addOnFailureListener(e -> Log.e("Firestore", "HOME_FRAG >>> SEND_NOTIFICATION_TO_USER >>> Firestore call failed", e));
    }

    // Sends a push notification to the FCM which runs on a separate thread asynchronously
    private void sendFcmNotification(JSONObject payload) {
        new Thread(() -> {
            try {
                // Get the OAuth 2.0 access token using FirebaseAuthHelper class
                String accessToken = FirebaseAuthHelper.getAccessToken(requireContext());
                Log.i("ACCESS_TOKEN", "HOME_FRAG >>> ACCESS_TOKEN: " + accessToken);

                // This connects to the FCM HTTP v1 API
                HttpURLConnection connection = getHttpURLConnection(accessToken);
                Log.d("JSONPay", "JSONPay: " + payload.toString());
                Log.d("HTTP_URL_CONN", "HttpURLConnection: " + connection);

                // Use an output stream to send the JSON payload in a byte array
                try(OutputStream os = connection.getOutputStream()) {
                    byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // Then it reads the reply from the server
                try(BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    Log.d("Response", "Response: " + response);
                    // System.out.println(response); testing purposes only
                }

                // We will know if it was a success based on the response code: 200 = ok, anything 4XX = bad.
                int responseCode = connection.getResponseCode();
                Log.d("FCM Response", "Response Code: " + responseCode);

            } catch (Exception e) {
                Log.e("FCM Error", "Error sending FCM notification", e);
            }
        }).start();
    }

    /*
    This creates an HTTP connection for sending an FCM notification
     */
    private static @NonNull HttpURLConnection getHttpURLConnection(String accessToken) throws IOException {
        URL url = new URL("https://fcm.googleapis.com/v1/projects/kaution-backend/messages:send");
        // OLD version: https://fcm.googleapis.com/fcm/send

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);
        return connection;
    }

    // User takes a picture and is displayed onto the ImageView in the HomeFragment
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PIC_ID) {
            photo = (Bitmap) data.getExtras().get("data");
            if (photo != null)
                cameraImage.setImageBitmap(photo);
            else
                Toast.makeText(requireContext(), "Failed to capture image", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("TAG", "Location permission granted.");

            // Start location service
            requireContext().startForegroundService(new Intent(requireContext(), LocationService.class));

            // Immediately update user location
            updateLocation();
        } else {
            Log.d("TAG", "Location permission denied.");
            Toast.makeText(requireContext(), "Location permission is required for Kaution reports.", Toast.LENGTH_LONG).show();
        }
    }
     */

}