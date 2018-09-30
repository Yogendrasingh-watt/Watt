package com.viwid.watt.watt.Fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.viwid.watt.watt.Model.InterestModel;
import com.viwid.watt.watt.R;

import com.bumptech.glide.Glide;
import com.viwid.watt.watt.Helper.DragLayout;

/*
Fragment for showing the Draggable Interest View Pager
*/
public class InterestFragment extends Fragment implements DragLayout.GotoDetailListener{

    //Instance variables for layout widgets
    private ImageView imageView;
    private TextView interestName,by;
    private InterestModel model;

    //Firebase Stuff
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference,mUsersTableReference;
    public InterestFragment() {
        // Required empty public constructor
    }


    public static InterestFragment newInstance() {
        InterestFragment fragment = new InterestFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("watt-production");
        mUsersTableReference = mDatabaseReference.child("users");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_interest, container, false);
        DragLayout dragLayout = view.findViewById(R.id.drag_layout);
        imageView = view.findViewById(R.id.image);
        interestName = view.findViewById(R.id.interestname);
        by = view.findViewById(R.id.by);

        //Using Glide to load the image.
        Glide.with(getActivity())
                .load(model.getPhotoURL())
                .apply(new RequestOptions()
                        .centerCrop())
                .into(imageView);
        interestName.setText(model.getTitle());
        Log.d("YOGI","User Id : "+model.getCreated_userid());
        mUsersTableReference.child(model.getCreated_userid()).child("user_info").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    if (dataSnapshot.child("name").getValue()!=null)
                    {
                        by.setText("by "+dataSnapshot.child("name").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //dragLayout.setGotoDetailListener(this);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void bindData(InterestModel model) {
        this.model = model;
    }


    //For Showing the detail upon complete drag of the CardView
    @Override
    public void gotoDetail() {
        /*Activity activity = (Activity) getContext();
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                new Pair<View, String>(imageView, DetailActivity.IMAGE_TRANSITION_NAME),
                new Pair<View, String>(address1, DetailActivity.ADDRESS1_TRANSITION_NAME),
                new Pair<View, String>(address2, DetailActivity.ADDRESS2_TRANSITION_NAME),
                new Pair<View, String>(address3, DetailActivity.ADDRESS3_TRANSITION_NAME),
                new Pair<View, String>(address4, DetailActivity.ADDRESS4_TRANSITION_NAME),
                new Pair<View, String>(address5, DetailActivity.ADDRESS5_TRANSITION_NAME),
                new Pair<View, String>(ratingBar, DetailActivity.RATINGBAR_TRANSITION_NAME),
                new Pair<View, String>(head1, DetailActivity.HEAD1_TRANSITION_NAME),
                new Pair<View, String>(head2, DetailActivity.HEAD2_TRANSITION_NAME),
                new Pair<View, String>(head3, DetailActivity.HEAD3_TRANSITION_NAME),
                new Pair<View, String>(head4, DetailActivity.HEAD4_TRANSITION_NAME)
        );
        Intent intent = new Intent(activity, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_IMAGE_URL, image_id);
        ActivityCompat.startActivity(activity, intent, options.toBundle());*/
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
