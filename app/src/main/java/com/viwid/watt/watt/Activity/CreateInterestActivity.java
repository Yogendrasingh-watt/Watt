package com.viwid.watt.watt.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.myhexaville.smartimagepicker.ImagePicker;
import com.myhexaville.smartimagepicker.OnImagePickedListener;
import com.viwid.watt.watt.R;

import java.util.HashMap;

/*
For Creating Interest page
*/
public class CreateInterestActivity extends AppCompatActivity implements View.OnClickListener{

    //Instance variables for layout widgets
    private ImageView closeInterest,addImageView;
    private EditText editTextInterestTitle,editTextDescription;
    private ImageButton addImageButton;
    private Button buttonCreateInterest;
    private TextView addPhototextView;


    private ImagePicker mImagePicker;
    private ProgressDialog loadingBar;
    private Uri currentImageUri;

    //Firebase Stuff
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage mFirebaseStorage;
    private FirebaseUser currentUser;
    private StorageReference mInterestImageStorageReference;
    private DatabaseReference mDatabaseReference,mInterestTableReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_interest);

        //For making the Status bar theme as light
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        //Initialize the firebase related stuff
        mFirebaseAuth = FirebaseAuth.getInstance();
        currentUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("watt-production");
        mInterestTableReference = mDatabaseReference.child("following_data");
        mFirebaseStorage = FirebaseStorage.getInstance();
        mInterestImageStorageReference = mFirebaseStorage.getReference();


        loadingBar = new ProgressDialog(this);

        mImagePicker = new ImagePicker(this,
                null,
                new OnImagePickedListener() {
                    @Override
                    public void onImagePicked(Uri imageUri) {
                        loadingBar.setTitle("Watt-walk the talk");
                        loadingBar.setMessage("Fetching Image");
                        loadingBar.show();
                        loadingBar.setCancelable(false);
                        loadingBar.setCanceledOnTouchOutside(false);
                        addImageButton.setVisibility(View.INVISIBLE);
                        addPhototextView.setVisibility(View.GONE);
                        addImageView.setVisibility(View.VISIBLE);
                        addImageView.setImageURI(imageUri);
                        currentImageUri = imageUri;
                        addImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        loadingBar.dismiss();
                    }
                });

        //Initializing the Layout Widgets
        closeInterest = findViewById(R.id.close_interest);
        editTextInterestTitle = findViewById(R.id.editTextInterestTitle);
        editTextDescription = findViewById(R.id.edittextDescription);
        addImageButton = findViewById(R.id.addImageButton);
        buttonCreateInterest = findViewById(R.id.buttonCreateInterest);
        addPhototextView = findViewById(R.id.addPhotoTextView);
        addImageView = findViewById(R.id.addImageView);

        //Setting onClickListeners
        addImageButton.setOnClickListener(this);
        buttonCreateInterest.setOnClickListener(this);
        closeInterest.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.addImageButton:
                mImagePicker.choosePicture(true);
                break;
            case R.id.buttonCreateInterest:
                String validate = validateForm();
                if(validate!=null)
                {
                    CreateMaterialDialog("Error!","Need valid entries for ",validate);
                }
                else
                {
                    loadingBar.setTitle("Watt-walk the talk");
                    loadingBar.setMessage("Please wait while we are creating the interest page....");
                    loadingBar.show();
                    loadingBar.setCancelable(false);
                    loadingBar.setCanceledOnTouchOutside(false);

                    saveInterestDataInFirebase();
                }
                break;
            case R.id.close_interest:
                onBackPressed();
                break;
        }

    }

    private void saveInterestDataInFirebase() {
        DatabaseReference mCurrentInterestReference = mInterestTableReference.push();
        String key = mCurrentInterestReference.getKey();
        storeImageToFirebaseStorage(key,mCurrentInterestReference);
    }

    //Store Image to Firebase Storage
    private void storeImageToFirebaseStorage(final String key, final DatabaseReference reference) {
        StorageReference filePath = mInterestImageStorageReference.child("InterestImages").child(key+".jpg");

        filePath.putFile(currentImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                        storeDataToFirebaseDatabase(key,downloadUrl);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        CreateMaterialDialog("Interest Didn't get created, Try again");
                        //have to implement the deletion of key from firebase database;
                    }
                });
    }

    //Store Data to Firebase Database
    private void storeDataToFirebaseDatabase(String key,String downloadUrl) {

        DatabaseReference mCurrentInterestReference = mInterestTableReference.child(key);

        HashMap<String,Object> interest_info = new HashMap<>();

        interest_info.put("title",editTextInterestTitle.getText().toString());
        interest_info.put("description",editTextDescription.getText().toString());
        interest_info.put("photoURL",downloadUrl);
        interest_info.put("created_userid",currentUser.getUid());

        mCurrentInterestReference.child("info").setValue(interest_info)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        loadingBar.dismiss();
                        onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        loadingBar.dismiss();
                        CreateMaterialDialog("Interest Didn't get created, Try again");
                    }
                });
    }

    private String validateForm() {
        /*boolean valid = true;*/
        StringBuilder infoBuilder = new StringBuilder("");


        String title = editTextInterestTitle.getText().toString();
        if (TextUtils.isEmpty(title)) {
            /*mFirstName.setError("Required.");
            valid = false;*/
            infoBuilder.append("Interest title, ");
        }

        if(addImageView.getDrawable() == null)
        {
            infoBuilder.append("Interest Image, ");
        }

         /*else {
            mFirstName.setError(null);
        }*/

        String description = editTextDescription.getText().toString();
        if (TextUtils.isEmpty(description)) {
            /*mLastName.setError("Required.");
            valid = false;*/
            infoBuilder.append("Interest description, ");
        } /*else {
            mLastName.setError(null);
        }*/

        if(infoBuilder.toString() == "")
        {
            return null;
        }
        else
        {
            infoBuilder.delete(infoBuilder.length()-2,infoBuilder.length()-1);
            return infoBuilder.toString();
        }
    }

    private void CreateMaterialDialog(String title,String content,String info)
    {
        new MaterialDialog.Builder(this)
                .title(title)
                .titleGravity(GravityEnum.CENTER)
                .content(content+info)
                .contentGravity(GravityEnum.CENTER)
                .positiveText("OK")
                .positiveColorRes(R.color.BrandColor)
                .build()
                .show();
    }

    private void CreateMaterialDialog(String info)
    {
        new MaterialDialog.Builder(this)
                .content(info)
                .contentGravity(GravityEnum.CENTER)
                .positiveText("OK")
                .positiveColorRes(R.color.BrandColor)
                .build()
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mImagePicker.handleActivityResult(resultCode,requestCode,data);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        mImagePicker.handlePermission(requestCode,grantResults);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in,R.anim.slide_down_from_bottom);
    }
}
