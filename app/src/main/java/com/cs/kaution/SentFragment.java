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
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cs.kaution.adapter.SentKautionRecyclerAdapter;
import com.cs.kaution.model.KautionModel;
import com.cs.kaution.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

/*
This Fragment shows a list of all the Kaution incident reports that the user has sent in the HomeFragment.
 */
public class SentFragment extends Fragment {
    RecyclerView recyclerView;
    SentKautionRecyclerAdapter adapter;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SentFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SentFragment newInstance(String param1, String param2) {
        SentFragment fragment = new SentFragment();
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
        View view = inflater.inflate(R.layout.fragment_sent, container, false);
        recyclerView = view.findViewById(R.id.sent_kautions_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        setupSentKautionsRecyclerView();

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("TASK_PREF", Context.MODE_PRIVATE);
        return view;
    }

    // Sets up the RecyclerView of queried results from the KAUTIONS dataset in Firebase Database
    // that match the Kaution user's user ID.
    private void setupSentKautionsRecyclerView() {
        Query query = FirebaseUtil.allKautionCollectionReference()
                .whereEqualTo("userId", FirebaseUtil.currentUserId())
                .orderBy("kautionTimestamp", Query.Direction.DESCENDING);
        //.orderBy("kautionTimestamp", Query.Direction.DESCENDING);
        Log.d("SENT_FRAGMENT", "Querying Firestore: " + query);

        query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.e("SENT_FRAGMENT", "Firestore query failed", e);
                return;
            }
            if (snapshots == null || snapshots.isEmpty()) {
                Log.w("SENT_FRAGMENT", "Firestore returned empty snapshot!");
            } else {
                Log.d("SENT_FRAGMENT", "Firestore returned: " + snapshots.size() + " documents");
            }
        });

        // Dynamically update the list when the Firestore Database changes
        FirestoreRecyclerOptions<KautionModel> user = new FirestoreRecyclerOptions.Builder<KautionModel>()
                .setQuery(query, KautionModel.class).build();
        Log.d("SENT_FRAGMENT", "SETUP_KAUTIONS_RECYCLER_VIEW >>> FS_REC_OPTNS >>> " + user);

        adapter = new SentKautionRecyclerAdapter(user, getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        Log.d("SENT_FRAGMENT", "SETUP_KAUTIONS_RECYCLER_VIEW >>> SUCCESSFULLY QUERIED");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null)
            adapter.stopListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter != null)
            adapter.startListening();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }
}