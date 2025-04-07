package com.cs.kaution.adapter;

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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.cs.kaution.ReceivedFragment;
import com.cs.kaution.SentFragment;

// Adapter for managing fragments in ViewPager under MyKautionFragment
public class PagerAdapter extends FragmentPagerAdapter {

    // Constructor that handles fragment transactions
    public PagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    // Use a switch case to return specified position in the adapter
    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: return new SentFragment(); // Sent Kautions
            case 1: return new ReceivedFragment(); // Received Kautions
            default: return new SentFragment();
        }
    }

    @Override
    public int getCount() { return 2; }

    // Use a switch case to return a title for the specified position
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0: return "Sent Kautions";
            case 1: return "Received Kautions";
            default: return null;
        }
    }
}

