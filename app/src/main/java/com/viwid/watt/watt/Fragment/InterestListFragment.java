package com.viwid.watt.watt.Fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.viwid.watt.watt.Adapter.InterestPagerAdapter;
import com.viwid.watt.watt.Adapter.ProfileRecyclerAdapter;
import com.viwid.watt.watt.Helper.CustPagerTransformer;
import com.viwid.watt.watt.Model.InterestModel;
import com.viwid.watt.watt.R;

import java.util.ArrayList;
import java.util.List;
/*
Fragment for Hosting View Pager for Draggable Interests
*/
public class InterestListFragment extends Fragment {

    public static String TAG = "InterestListFragment";

    //Firebase Stuff
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage mFirebaseStorage;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabaseReference,mUsersTableReference,mInterestTableReference;
    private StorageReference mUserImageStorageReference;


    private List<InterestModel> interestModelList = new ArrayList<>();

    //ViewPager and Adapter
    private ViewPager viewPager;
    private InterestPagerAdapter interestPagerAdapter;
    private List<InterestFragment> fragmentList = new ArrayList<>();

    public InterestListFragment() {
        // Required empty public constructor
    }


    public static InterestListFragment newInstance(String param1, String param2) {
        InterestListFragment fragment = new InterestListFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("watt-production");
        mUsersTableReference = mDatabaseReference.child("users");
        mInterestTableReference = mDatabaseReference.child("following_data");
    }

    private void fillViewPager(View view) {
        viewPager = view.findViewById(R.id.viewPagerInterest);

        viewPager.setPageTransformer(false,new CustPagerTransformer(getActivity()));

        for (int i=0;i<5;i++)
        {
            fragmentList.add(new InterestFragment());
        }

        interestPagerAdapter = new InterestPagerAdapter(getChildFragmentManager(),interestModelList,fragmentList);
        /*interestPagerAdapter.setData(interestModelList);
        interestPagerAdapter.setFragment(fragmentList);*/

        viewPager.setAdapter(interestPagerAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view =  inflater.inflate(R.layout.fragment_interest_list, container, false);

        mInterestTableReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                {
                    InterestModel model = dataSnapshot1.child("info").getValue(InterestModel.class);
                    interestModelList.add(model);
                    Log.d("YOGI","user Id :"+model.getCreated_userid()+" Title : "+model.getTitle()+" Desc : "+model.getDescription());
                }
                fillViewPager(view);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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

}
