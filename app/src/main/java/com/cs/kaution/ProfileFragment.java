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

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cs.kaution.model.UserModel;
import com.cs.kaution.utils.FirebaseUtil;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

/*
This Fragment allows the user to enter their desired new username in a textbox and is dynamically updated in the
Firestore database
 */
public class ProfileFragment extends Fragment {
    Button changeButton;
    EditText editText;
    UserModel model;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        editText = view.findViewById(R.id.username_input);
        changeButton = view.findViewById(R.id.change_button);

        changeButton.setOnClickListener(view1 -> {
            String name = editText.getText().toString();
            int midLength = 3;
            if (name.length() < 3) {
                Toast.makeText(ProfileFragment.this.getActivity(), "Name must be at least 3 characters long!",
                        Toast.LENGTH_SHORT).show();
            }
            model.setUserName(name);
            FirebaseUtil.currentUserDetails().set(model).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ProfileFragment.this.getActivity(), "Username change complete!", Toast.LENGTH_SHORT).show();
                    Log.d("TAG", "ProfileFragment changeButtonClicked >>> name change to: " + name);
                }
            });
        });
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            model = task.getResult().toObject(UserModel.class);
            editText.setText(model.getUserName());
        });
        return view;
    }
}