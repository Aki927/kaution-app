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

import static com.cs.kaution.utils.FirebaseUtil.formatTimestamp;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cs.kaution.R;
import com.cs.kaution.model.KautionModel;
import com.cs.kaution.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

// Adapter for received Kautions using a RecyclerView
public class SentKautionRecyclerAdapter extends FirestoreRecyclerAdapter<KautionModel, SentKautionRecyclerAdapter.KautionModelViewHolder> {
    Context context;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public SentKautionRecyclerAdapter(@NonNull FirestoreRecyclerOptions<KautionModel> options, Context c) {
        super(options);
        this.context = c;
    }

    // Inflate each item in the RecyclerView
    @NonNull
    @Override
    public KautionModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.current_kautions_recycler_row, parent, false);
        return new KautionModelViewHolder(view);
    }

    // Bind the data from Firestore model to ViewHolder
    @Override
    protected void onBindViewHolder(@NonNull KautionModelViewHolder holder, int position, @NonNull KautionModel model) {
        holder.kautionDescription.setText(model.getDescription()); // Kaution report description

        // Kaution report image
        if (model.getImageUrl() != null && !model.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(model.getImageUrl())
                    .placeholder(R.drawable.baseline_downloading_24)
                    .error(R.drawable.baseline_error_24)
                    .into(holder.kautionImage);
        } else {
            holder.kautionImage.setImageResource(R.drawable.baseline_image_24);
        }

        // Kaution timestamp when it was taken
        if (model.getKautionTimestamp() != null) {
            com.google.firebase.Timestamp timestamp = model.getKautionTimestamp();
            String formattedDate = FirebaseUtil.formatTimestamp(timestamp);
            holder.timestamp.setText(formattedDate);
        } else {
            holder.timestamp.setText("(date N/A)");
        }
    }

    // This subclass holds a reference to view to the RecyclerViews
    public class KautionModelViewHolder extends RecyclerView.ViewHolder {
        ImageView kautionImage;
        TextView kautionDescription;
        TextView timestamp;

        public KautionModelViewHolder(@NonNull View itemView) {
            super(itemView);
            kautionImage = itemView.findViewById(R.id.kaution_image);
            kautionDescription = itemView.findViewById(R.id.kaution_description);
            timestamp = itemView.findViewById(R.id.kaution_datetime);
        }
    }
}
