package com.viwid.watt.watt.Fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.myhexaville.smartimagepicker.ImagePicker;
import com.myhexaville.smartimagepicker.OnImagePickedListener;
import com.viwid.watt.watt.Adapter.MainRecyclerAdapter;
import com.viwid.watt.watt.Adapter.ProfileRecyclerAdapter;
import com.viwid.watt.watt.Logg;
import com.viwid.watt.watt.R;
import com.viwid.watt.watt.Util.WattUtils;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;

/*
Fragment for Showing the profile of the User
*/
public class Profile_Fragment extends Fragment implements View.OnClickListener{

    ////constants for certain actions
    public static final int EDIT_PROFILE = 0;
    public static final int INVITE_CONTACT = 1;
    public static final int SETTINGS = 2;
    public static final int REPORT = 3;

    public static final String TAG = "profile_fragment";

    //Firebase Stuff
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage mFirebaseStorage;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabaseReference,mUsersTableReference;
    private StorageReference mUserImageStorageReference;

    private ImagePicker mImagePicker;
    private ProgressDialog loadingBar;
    private String currentProfileImageUri,currentPickedImageUri;
    private Bitmap currentImageResource;

    //Instance variables for layout widgets
    private TextView userName,userTitle,userLocation,userDob;
    private CircleImageView profileCircleImageView;
    private ImageView blurredAlbumArt;
    private EditText editText_userName,editText_userTitle,editText_userLocation,editText_userDob;
    private Button saveProile;
    private RecyclerView profile_recyclerView,main_recyclerView;
    private ImageView downArrow,Close;
    private FrameLayout mEditFrame,mDisplayFrame,frame_profileCircleImageView;

    //Adapters
    private ProfileRecyclerAdapter mProfileRecyclerAdapter;
    private MainRecyclerAdapter mainRecyclerAdapter;





    //private Button trotButton;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private AppBarLayout mAppBarLayout;
    private AppBarLayout.LayoutParams params;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public Profile_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Profile_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Profile_Fragment newInstance(String param1, String param2) {
        Profile_Fragment fragment = new Profile_Fragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        currentPickedImageUri = null;
        currentProfileImageUri = null;


        //Fragment Stuff
        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("watt-production");
        mUsersTableReference = mDatabaseReference.child("users");
        mFirebaseStorage = FirebaseStorage.getInstance();
        mUserImageStorageReference = mFirebaseStorage.getReference();


        loadingBar = new ProgressDialog(getActivity());

        mImagePicker = new ImagePicker(getActivity(),
                this,
                new OnImagePickedListener() {
                    @Override
                    public void onImagePicked(Uri imageUri) {
                        loadingBar.setTitle("Watt-walk the talk");
                        loadingBar.setMessage("Fetching Image");
                        loadingBar.show();
                        loadingBar.setCancelable(false);
                        loadingBar.setCanceledOnTouchOutside(false);
                        currentPickedImageUri = imageUri.toString();
                        profileCircleImageView.setImageURI(imageUri);
                        /*Glide.with(getContext())
                                .load(imageUri)
                                .apply(new RequestOptions()
                                        .centerCrop()
                                        .placeholder(R.drawable.bnv_profile)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                                .into(profileCircleImageView);*/
                        loadingBar.dismiss();
                        Logg.debugMessage("Image Picked");
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        //Initializing all the widgets and Views
        profile_recyclerView = view.findViewById(R.id.profile_recycler_action);
        main_recyclerView = view.findViewById(R.id.recycler_view_secondary);

        profile_recyclerView.setHasFixedSize(true);
        profile_recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        mProfileRecyclerAdapter = new ProfileRecyclerAdapter(getContext());
        profile_recyclerView.setAdapter(mProfileRecyclerAdapter);

        main_recyclerView.setHasFixedSize(true);
        main_recyclerView.setLayoutManager(new CustomLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        //main_recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        main_recyclerView.setAdapter(mProfileRecyclerAdapter);

        downArrow = view.findViewById(R.id.expand_more);
        Close = view.findViewById(R.id.close_more);

        mEditFrame = view.findViewById(R.id.edit_frame);
        mDisplayFrame = view.findViewById(R.id.display_frame);

        profileCircleImageView = view.findViewById(R.id.profile_circle_imageView);
        blurredAlbumArt = view.findViewById(R.id.blur_ImageView);
        frame_profileCircleImageView = view.findViewById(R.id.frame_profile_circle_imageView);

        userName = view.findViewById(R.id.textView_Username);
        userTitle = view.findViewById(R.id.textView_Title);
        userLocation = view.findViewById(R.id.textView_Location);
        userDob = view.findViewById(R.id.textView_Dob);

        editText_userName = view.findViewById(R.id.editText_userName);
        editText_userTitle = view.findViewById(R.id.editText_title);
        editText_userLocation = view.findViewById(R.id.editText_location);
        editText_userDob = view.findViewById(R.id.editText_dob);
        saveProile = view.findViewById(R.id.save_profile);

        downArrow.setOnClickListener(this);
        Close.setOnClickListener(this);
        saveProile.setOnClickListener(this);

        /*trotButton = view.findViewById(R.id.trot_button);
        trotButton.setOnClickListener(this);*/
        mCollapsingToolbarLayout = view.findViewById(R.id.profile_collapse_toolbar_layout);
        mAppBarLayout = view.findViewById(R.id.profile_appbar_layout);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                    // If collapsed, then do this
                    profile_recyclerView.setVisibility(View.GONE);
                    downArrow.setVisibility(View.GONE);
                    userLocation.setVisibility(View.GONE);
                    userDob.setVisibility(View.GONE);
                } else if (verticalOffset == 0) {
                    // If expanded, then do this
                    profile_recyclerView.setVisibility(View.VISIBLE);
                    downArrow.setVisibility(View.VISIBLE);
                    userLocation.setVisibility(View.VISIBLE);
                    userDob.setVisibility(View.VISIBLE);
                } else {
                    profile_recyclerView.setVisibility(View.GONE);
                    downArrow.setVisibility(View.GONE);
                    userLocation.setVisibility(View.GONE);
                    userDob.setVisibility(View.GONE);
                    // Somewhere in between
                    // Do according to your requirement
                }
            }
        });
        params = (AppBarLayout.LayoutParams) mCollapsingToolbarLayout.getLayoutParams();

        loadDataFromFirebase();
        return view;
    }


    //Method for Loading the Data from firebase
    private void loadDataFromFirebase() {
        DatabaseReference mCurrentUserReference = mUsersTableReference.child(mCurrentUser.getUid()).child("user_info");

        mCurrentUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    if (dataSnapshot.child("name").getValue()!=null)
                    {
                        userName.setText(dataSnapshot.child("name").getValue().toString());
                    }
                    if (dataSnapshot.child("title").getValue()!=null)
                    {
                        userTitle.setText(dataSnapshot.child("title").getValue().toString());
                    }
                    if (dataSnapshot.child("location").getValue()!=null)
                    {
                        userLocation.setText(dataSnapshot.child("location").getValue().toString());
                    }
                    if (dataSnapshot.child("dob").getValue()!=null)
                    {
                        userDob.setText(dataSnapshot.child("dob").getValue().toString());
                    }
                    //Logg.debugMessage(dataSnapshot.child("name").getValue().toString()+" and "+dataSnapshot.child("name").getValue());

                    if (dataSnapshot.child("photoURL").getValue()!=null)
                    {
                        Logg.debugMessage(dataSnapshot.child("photoURL").getValue()+"");
                        /*.asDrawable()
                                .transition(DrawableTransitionOptions.withCrossFade(200))*/
                        final String imageURI = dataSnapshot.child("photoURL").getValue().toString();

                        Glide.with(getContext())
                                .asBitmap()
                                .load(imageURI)
                                .apply(new RequestOptions()
                                        .centerCrop()
                                        .placeholder(R.drawable.bnv_profile)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                                .listener(new RequestListener<Bitmap>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                        Log.d("YOGI","glide_load_failed_now_playing_fragment");
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                        Log.d("YOGI","glide_on_Resource_Ready_now_playing_fragment");

                                        currentProfileImageUri = currentPickedImageUri = imageURI;
                                        currentImageResource = resource;
                                        Logg.debugMessage(currentImageResource+"");
                                        doAlbumArtStuff(resource);
                                        return false;
                                    }
                                })
                                .into(profileCircleImageView);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.expand_more:
                profileBottomSheetDialogFragment();
                break;
            case R.id.close_more:
                mEditFrame.setVisibility(View.GONE);
                mDisplayFrame.setVisibility(View.VISIBLE);
                frame_profileCircleImageView.setForeground(null);
                ((CustomLayoutManager)(main_recyclerView.getLayoutManager())).setScrollEnabled(true);
                params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                                        |AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED
                                        |AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
                mCollapsingToolbarLayout.setLayoutParams(params);
                populateWithCurrentUserInfo(false);
                profileCircleImageView.setOnClickListener(null);
                if (isProfileImageUpdated())
                {
                    Glide.with(getContext())
                            .asBitmap()
                            .load(Uri.parse(currentProfileImageUri))
                            .apply(new RequestOptions()
                                    .centerCrop()
                                    .placeholder(R.drawable.bnv_profile)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL))
                            .into(profileCircleImageView);

                    currentPickedImageUri = currentProfileImageUri;
                }
                break;
            case R.id.profile_circle_imageView:
                mImagePicker.choosePicture(true);
                break;
            case R.id.save_profile:
                if(isDataChanged())
                {
                    loadingBar.setTitle("Watt-walk the talk");
                    loadingBar.setMessage("Updating profile...");
                    loadingBar.show();
                    loadingBar.setCancelable(false);
                    loadingBar.setCanceledOnTouchOutside(false);
                    saveDataInFirebaseDatabase();
                }
                else
                {
                    Close.performClick();
                }
                break;
        }
    }


    //Method to open Profile Bottom Sheet Fragment
    public void profileBottomSheetDialogFragment() {
        ProfileBottomSheetFragment profileBottomSheetFragment = new ProfileBottomSheetFragment();
        profileBottomSheetFragment.show(getActivity().getSupportFragmentManager(),ProfileBottomSheetFragment.TAG);
    }

    //Methods to Save Data in Firebase
    private void saveDataInFirebaseDatabase() {

        final DatabaseReference mCurrentUserReference = mUsersTableReference.child(""+mCurrentUser.getUid());
        final HashMap<String,Object> user_info = new HashMap<>();

        StorageReference filePath = mUserImageStorageReference.child("UserProfileImages").child(mCurrentUser.getUid()+".jpg");

        if(isProfileImageUpdated())
        {
            filePath.putFile(Uri.parse(currentPickedImageUri))
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Logg.debugMessage("Image Upload SUccessfull");
                            //user_info.put("photoURL",taskSnapshot.getDownloadUrl().toString());
                            mCurrentUserReference.child("user_info").child("photoURL").setValue(taskSnapshot.getDownloadUrl().toString());
                            loadingBar.dismiss();
                            Close.performClick();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            CreateMaterialDialog("Profile Image did not get updated, Try Again...");
                            Close.performClick();
                        }
                    });
        }
        if(!(editText_userName.getText().toString().equals(userName.getText().toString())))
        {
            user_info.put("name",editText_userName.getText().toString());
        }
        if(!(editText_userTitle.getText().toString().equals(userTitle.getText().toString())))
        {
            user_info.put("title",editText_userTitle.getText().toString());
        }
        if(!(editText_userLocation.getText().toString().equals(userLocation.getText().toString())))
        {
            user_info.put("location",editText_userLocation.getText().toString());
        }
        if(!(editText_userDob.getText().toString().equals(userDob.getText().toString())))
        {
            user_info.put("dob",editText_userDob.getText().toString());
        }

        Logg.debugMessage("Yahan tak aa gya");
        if(!user_info.isEmpty())
        {
            for (String key : user_info.keySet())
            {
                Logg.debugMessage("Data is being updated for key : "+key);
                mCurrentUserReference.child("user_info").child(key).setValue(user_info.get(key));
            }
            Logg.debugMessage("Loading Bar Dismissed for upload");
            if (!isProfileImageUpdated())
            {
                loadingBar.dismiss();
                Close.performClick();
            }
            /*mCurrentUserReference.child("user_info").updateChildren(user_info)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            loadingBar.dismiss();
                            CreateMaterialDialog("Profile Updation Error, Check Network connection, Try again");
                        }
                    });*/
        }
    }

    private void CreateMaterialDialog(String info)
    {
        new MaterialDialog.Builder(getActivity())
                .content(info)
                .contentGravity(GravityEnum.CENTER)
                .positiveText("OK")
                .positiveColorRes(R.color.BrandColor)
                .build()
                .show();
    }

    //Method to check, if the user tried to change the data and is it actually getting changed
    private boolean isDataChanged() {
        if(!(editText_userName.getText().toString().equals(userName.getText().toString())) ||
                !(editText_userTitle.getText().toString().equals(userTitle.getText().toString())) ||
                !(editText_userLocation.getText().toString().equals(userLocation.getText().toString())) ||
                !(editText_userDob.getText().toString().equals(userDob.getText().toString())) ||
                isProfileImageUpdated())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //Method to check, if the user has tried to change the profile image
    private boolean isProfileImageUpdated() {
        if(!currentProfileImageUri.equals(currentPickedImageUri))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //Method to populate the user info in the profile fragment fields
    private void populateWithCurrentUserInfo(boolean b) {
        if (b)
        {
            editText_userName.setText(userName.getText().toString());
            editText_userTitle.setText(userTitle.getText().toString());
            editText_userLocation.setText(userLocation.getText().toString());
            editText_userDob.setText(userDob.getText().toString());
        }
        else {
            editText_userName.setText("");
            editText_userTitle.setText("");
            editText_userLocation.setText("");
            editText_userDob.setText("");
        }
    }

    //Method for Updating the Fragment upon certain action being taken from ProfileBottomSheetFragment
    public void updateFragment(int clickCode) {
        switch (clickCode)
        {
            case EDIT_PROFILE:
                Logg.debugMessage("Edit_profile Clicked 3");
                mDisplayFrame.setVisibility(View.GONE);
                mEditFrame.setVisibility(View.VISIBLE);
                frame_profileCircleImageView.setForeground(ContextCompat.getDrawable(getContext(),R.drawable.bg_circle_color_opaque));
                ((CustomLayoutManager)(main_recyclerView.getLayoutManager())).setScrollEnabled(false);
                params.setScrollFlags(0);
                mCollapsingToolbarLayout.setLayoutParams(params);
                populateWithCurrentUserInfo(true);
                profileCircleImageView.setOnClickListener(this);
                break;
            case INVITE_CONTACT:
                Logg.debugMessage("Invite Clicked 3");
                break;
            case SETTINGS:
                Logg.debugMessage("Settings Clicked 3");
                break;
            case REPORT:
                Logg.debugMessage("Report Clicked 3");
                break;
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    //Method to create the blurred Image to put up in the Profile Fragment
    private void doAlbumArtStuff(Bitmap resource) {
        SetBlurredAlbumArt blurredAlbumArt = new SetBlurredAlbumArt();
        blurredAlbumArt.execute(resource);
    }

    //AsyncTask for creating the blurr image
    private class SetBlurredAlbumArt extends AsyncTask<Bitmap, Void, Drawable>
    {

        @Override
        protected Drawable doInBackground(Bitmap... bitmaps) {
            Drawable drawable = null;
            try
            {
                drawable = WattUtils.createBlurredImageFromBitmap(bitmaps[0],getContext(),1);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return drawable;
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            super.onPostExecute(drawable);
            if(drawable!=null)
            {
                if(blurredAlbumArt.getDrawable()!=null)
                {
                    final TransitionDrawable td = new TransitionDrawable(new Drawable[]{
                            blurredAlbumArt.getDrawable(),
                            drawable
                    });

                    blurredAlbumArt.setImageDrawable(td);
                    td.startTransition(150);
                }
                else {
                    blurredAlbumArt.setImageDrawable(drawable);
                }
                Logg.debugMessage("blur done");
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    //Custom layoutmanager for customizing the scrolling behaviour of the recyclerView
    public class CustomLayoutManager extends LinearLayoutManager
    {
        private boolean isScrollEnabled = true;

        public CustomLayoutManager(Context context,int orientation,boolean reverselayout) {
            super(context,orientation,reverselayout);
        }

        public void setScrollEnabled(boolean flag)
        {
            this.isScrollEnabled = flag;
        }

        @Override
        public boolean canScrollVertically() {
            return isScrollEnabled && super.canScrollVertically();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mImagePicker.handleActivityResult(resultCode, requestCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mImagePicker.handlePermission(requestCode, grantResults);
    }
}
