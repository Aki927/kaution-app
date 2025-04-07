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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cs.kaution.model.ReportBugModel;
import com.cs.kaution.utils.FirebaseUtil;
import com.google.firebase.Timestamp;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BugReportFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BugReportFragment extends Fragment {
    Button submit;
    EditText input;
    ReportBugModel bugModel;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public BugReportFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BugReportFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BugReportFragment newInstance(String param1, String param2) {
        BugReportFragment fragment = new BugReportFragment();
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
        View view = inflater.inflate(R.layout.fragment_bug_report, container, false);
        submit = view.findViewById(R.id.submit_button);
        input = view.findViewById(R.id.feedback_input);

        // When user clicks submit button, the EditText input is saved into a ReportBugModel object along with timestamp and user ID.
        submit.setOnClickListener(view1 -> {
            String strInput = input.getText().toString();
            if (strInput.isEmpty()) {
                Toast.makeText(BugReportFragment.this.getContext(), "Feedback required!", Toast.LENGTH_SHORT).show();
                return;
            }
            bugModel = new ReportBugModel(strInput, Timestamp.now(), FirebaseUtil.currentUserId());
            FirebaseUtil.allBugReportCollectionReference().add(bugModel).addOnSuccessListener(task -> {
                Toast.makeText(BugReportFragment.this.getContext(), "Report sent successfully!", Toast.LENGTH_SHORT).show();
                input.setText("");
            }).addOnFailureListener(e -> {
                Toast.makeText(BugReportFragment.this.getContext(), "Error: Report failed to send.", Toast.LENGTH_SHORT).show();
            });
        });
        return view;
    }
}