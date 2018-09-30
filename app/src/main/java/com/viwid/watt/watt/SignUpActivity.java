package com.viwid.watt.watt;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.myhexaville.smartimagepicker.ImagePicker;
import com.myhexaville.smartimagepicker.OnImagePickedListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/*
Activity for performing SignUp
*/
public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{


    // Request Code for asking permissions
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 123;

    //Instance variables for layout widgets
    private EditText mFirstName,mLastName,mEmailAddress,mPassword,mMobileNumber;
    private TextView mMale,mFemale;
    private Button mSignUp;
    private CircleImageView mProfilePic;
    private ImageView mCloseSignUp;

    //For Showing the ProgressBar
    private ProgressDialog loadingBar;

    //SmartImagePicker
    private ImagePicker mImagePicker;

    //Firebase Stuff
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage mFirebaseStorage;
    private FirebaseUser currentUser;
    private StorageReference mUserImageStorageReference;
    private DatabaseReference mDatabaseReference,mUsersTableReference;

    private Uri currentImageUri;

    //For fetching the Device Id
    private TelephonyManager tm;
    private String Gender = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Logg.debugMessage("SignUpActivity Created Thread : "+Thread.currentThread());

        /*
        Initialize the telephony manager instance to fetch the device Id
        Note : We need to ask user to grant READ_PHONE_STATE permission
        to fetch the device Id. If not granted, device id will not be fetched.
        * */
        tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);


        //Initialize the firebase related stuff
        mFirebaseAuth = FirebaseAuth.getInstance();
        currentUser = mFirebaseAuth.getCurrentUser();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("watt-production");
        mUsersTableReference = mDatabaseReference.child("users");
        mFirebaseStorage = FirebaseStorage.getInstance();
        mUserImageStorageReference = mFirebaseStorage.getReference();


        loadingBar = new ProgressDialog(this);

        //Initializing the Image Picker
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
                        currentImageUri = imageUri;
                        mProfilePic.setImageURI(imageUri);
                        loadingBar.dismiss();
                    }
                });

        //Initialize the variables for layout widgets
        mProfilePic = findViewById(R.id.profile_image);
        mFirstName = findViewById(R.id.firstname);
        mLastName = findViewById(R.id.lastname);
        mEmailAddress = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mMobileNumber = findViewById(R.id.mobile_number);
        mMale = findViewById(R.id.male_button);
        mFemale = findViewById(R.id.female_button);
        mSignUp = findViewById(R.id.signup_button);
        mCloseSignUp = findViewById(R.id.close_signup);

        //set the onclick Listener for widgets
        mCloseSignUp.setOnClickListener(this);
        mProfilePic.setOnClickListener(this);
        mSignUp.setOnClickListener(this);
        mMale.setOnClickListener(this);
        mFemale.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.close_signup)
        {
            //On Click of close button
            onBackPressed();
        }
        else if(v.getId() == R.id.signup_button)
        {
            //On Click of SignUp Button
            //Validating the Form
            String validate= validateForm();
            if(validate!=null)
            {
                CreateMaterialDialog("Need more information","Please provide following info : ",validate);
            }
            else
            {
                if(checkAndRequestPermissions())
                {
                    createAccount(mEmailAddress.getText().toString(),mPassword.getText().toString());
                }
            }
        }
        else  if(v.getId() == R.id.profile_image)
        {
            //signUpImagePickerDialogFragment();
            mImagePicker.choosePicture(true);
        }


        if(v.getId() == R.id.male_button)
        {
            Logg.debugMessage("male clicked");
            Gender = "male";
            mMale.setTextColor(Color.parseColor("#67d2ff"));
            mFemale.setTextColor(Color.parseColor("#979899"));
        }
        else if(v.getId() == R.id.female_button)
        {
            Logg.debugMessage("female clicked");
            Gender = "female";
            mFemale.setTextColor(Color.parseColor("#67d2ff"));
            mMale.setTextColor(Color.parseColor("#979899"));
        }
    }
    private boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionReadPhoneState = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

            List<String> listPermissionNeeded = new ArrayList<>();

            if (permissionReadPhoneState != PackageManager.PERMISSION_GRANTED) {
                listPermissionNeeded.add(Manifest.permission.READ_PHONE_STATE);
            }

            if (!listPermissionNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, listPermissionNeeded.toArray(new String[listPermissionNeeded.size()]), MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                return false;
            } else {
                return true;
            }
        }
        return true;
    }


    private void createAccount(String email, String password)
    {
            loadingBar.setTitle("Watt-walk the talk");
            loadingBar.setMessage("Please wait while we are creating the account & sending the verification link");
            loadingBar.show();
            loadingBar.setCancelable(false);
            loadingBar.setCanceledOnTouchOutside(false);

            mFirebaseAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful())
                            {
                                Logg.debugMessage("SignUp Task Successfull Thread : "+Thread.currentThread());
                                currentUser = task.getResult().getUser();
                                storeImageToFirebaseStorage(task);
                            }
                            else
                            {
                                loadingBar.dismiss();
                                try

                                {
                                    throw task.getException();
                                }
                                // if user enters wrong email.
                                catch (FirebaseAuthWeakPasswordException weakPassword)
                                {
                                    /*Toast.makeText(SignUpActivity.this,"Weak Password",Toast.LENGTH_LONG).show();*/
                                    CreateMaterialDialog("Weak Password");

                                    // TODO: take your actions!
                                }
                                // if user enters wrong password.
                                catch (FirebaseAuthInvalidCredentialsException malformedEmail)
                                {
                                    /*Toast.makeText(SignUpActivity.this,"invalid Email",Toast.LENGTH_LONG).show();*/
                                    CreateMaterialDialog("invalid Email");

                                    // TODO: Take your action
                                }
                                catch (FirebaseAuthUserCollisionException existEmail)
                                {
                                    /*Toast.makeText(SignUpActivity.this,"Email Already Exists",Toast.LENGTH_LONG).show();*/
                                    CreateMaterialDialog("Email Already Exists");
                                    // TODO: Take your action
                                }
                                catch (Exception e)
                                {
                                    /*Toast.makeText(SignUpActivity.this,""+e,Toast.LENGTH_LONG).show();*/
                                    CreateMaterialDialog("Some Error has Occured, May be Network issue.Try Again");
                                }
                            }
                        }
                    });
    }

    //Method to store the Image in the Firebase Storage
    private void storeImageToFirebaseStorage(@NonNull final Task<AuthResult> task) {

        StorageReference filePath = mUserImageStorageReference.child("UserProfileImages").child(currentUser.getUid()+".jpg");

        filePath.putFile(currentImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Logg.debugMessage("Image Stored in Firebase Storage Thread : "+Thread.currentThread()+" Current User :"+mFirebaseAuth.getCurrentUser());
                        String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                        saveInfoInDatabase(downloadUrl,task);
                        //loadingBar.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Logg.debugMessage("Firebase Storage Exception : "+e.getMessage().toString());
                        loadingBar.dismiss();
                        CreateMaterialDialog("Account didn't get created, Try Again!");
                        currentUser.delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Logg.debugMessage("User Deleted after Firabase Storage Failure, Current User : "+currentUser);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Logg.debugMessage("User Did not get Deleted after Firabase Storage Failure, Exception : "+e.getMessage().toString());
                                        }
                                    });
                    }
                });
    }

    //Method to store the User Info in to firebase Database
    private void saveInfoInDatabase(String downloadUrl,@NonNull final Task<AuthResult> task) {

        DatabaseReference mCurrentUserReference = mUsersTableReference.child(""+currentUser.getUid());

        HashMap<String,Object> user_info = new HashMap<>();

        user_info.put("name",mFirstName.getText().toString()+" "+mLastName.getText().toString());
        user_info.put("first_name",mFirstName.getText().toString());
        user_info.put("last_name",mLastName.getText().toString());
        user_info.put("email",mEmailAddress.getText().toString());
        user_info.put("photoURL",downloadUrl);
        user_info.put("gender",Gender);
        user_info.put("phone_no",mMobileNumber.getText().toString());
        if(ContextCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED)
        {
            user_info.put("device_id","Permission Denied");
        }
        else
        {
            user_info.put("device_id",""+tm.getDeviceId());
        }
        user_info.put("created_date",""+System.currentTimeMillis());
        mCurrentUserReference.child("user_info").setValue(user_info)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Logg.debugMessage("Database Updated");
                        loadingBar.dismiss();
                        task.getResult().getUser().sendEmailVerification()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Logg.debugMessage("SignUp Verification Sent Thread : "+Thread.currentThread());
                                        CreateMaterialDialog("Verify Email",
                                                "We have sent a verification link to your email "+
                                                        mFirebaseAuth.getCurrentUser().getEmail()+
                                                        ". You would be able to login to your account after verification. Thank You","");

                                        mFirebaseAuth.signOut();
                                        ClearFields();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Logg.debugMessage("Firebase Database Exception : "+e.getMessage().toString());
                        loadingBar.dismiss();
                        CreateMaterialDialog("Account didn't get created, Try Again!");
                        StorageReference filePath = mUserImageStorageReference.child("UserProfileImages").child(currentUser.getUid()+".jpg");
                        filePath.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Logg.debugMessage("Image Deleted From Firebase Storage after Firabase Database Failure ");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Logg.debugMessage("Image Did not get Deleted From Firebase Storage after Firabase Database Failure, Exception : "+e.getMessage().toString());
                            }
                        });

                        currentUser.delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Logg.debugMessage("User Deleted after Firabase Database Failure, Current user : "+mFirebaseAuth.getCurrentUser());
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Logg.debugMessage("User Did not get Deleted after Firabase Database Failure, Exception : "+e.getMessage().toString());
                                    }
                                });
                    }
                });
    }

    //Method to clear Fields
    private void ClearFields() {
        mFirstName.setText("");
        mLastName.setText("");
        mEmailAddress.setText("");
        mPassword.setText("");
        mMobileNumber.setText("");
        mMale.setTextColor(Color.parseColor("#979899"));
        mFemale.setTextColor(Color.parseColor("#979899"));
        Gender = "";
        mProfilePic.setImageDrawable(null);
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

    private String validateForm() {
        /*boolean valid = true;*/
        StringBuilder infoBuilder = new StringBuilder("");

        if(mProfilePic.getDrawable() == null)
        {
            infoBuilder.append("Profile Image, ");
        }

        String firstName = mFirstName.getText().toString();
        if (TextUtils.isEmpty(firstName)) {
            /*mFirstName.setError("Required.");
            valid = false;*/
            infoBuilder.append("First name, ");
        } /*else {
            mFirstName.setError(null);
        }*/

        String lastName = mLastName.getText().toString();
        if (TextUtils.isEmpty(lastName)) {
            /*mLastName.setError("Required.");
            valid = false;*/
            infoBuilder.append("Last name, ");
        } /*else {
            mLastName.setError(null);
        }*/

        String email = mEmailAddress.getText().toString();
        if (TextUtils.isEmpty(email)) {
            /*mEmailAddress.setError("Required.");
            valid = false;*/
            infoBuilder.append("Email, ");
        } /*else {
            mEmailAddress.setError(null);
        }*/

        String password = mPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            /*mPassword.setError("Required.");
            valid = false;*/
            infoBuilder.append("Password, ");
        }
        else if(password.length()<=6)
        {
            /*mPassword.setError("Password is Too Small (<=6 Char)");
            valid = false;*/
            infoBuilder.append("Weak Password, ");
        }
        /*else {
            mPassword.setError(null);
        }*/

        String mobileNumber = mMobileNumber.getText().toString();
        if (TextUtils.isEmpty(mobileNumber)) {
            /*mMobileNumber.setError("Required.");
            valid = false;*/
            infoBuilder.append("Mobile Number, ");
        }
        else if(mobileNumber.length()!=10)
        {
            /*mMobileNumber.setError("Wrong Mobile Number.");*/
            infoBuilder.append("Wrong Mobile Number, ");
        }
        /*else {
            mMobileNumber.setError(null);
        }*/

        if(Gender=="")
        {
            /*valid = false;
            Toast.makeText(this,"Select the Gender",Toast.LENGTH_LONG).show();*/
            infoBuilder.append("Gender, ");
        }

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in,R.anim.slide_down_from_bottom);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //SmartImagePicker
        mImagePicker.handleActivityResult(resultCode,requestCode,data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mImagePicker.handlePermission(requestCode,grantResults);

        switch (requestCode)
        {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                Map<String, Integer> perms = new HashMap<>();

                perms.put(Manifest.permission.READ_PHONE_STATE, PackageManager.PERMISSION_GRANTED);

                for (int i = 0; i < permissions.length; i++) {
                    perms.put(permissions[i], grantResults[i]);
                }

                if (perms.get(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
                {
                    createAccount(mEmailAddress.getText().toString(),mPassword.getText().toString());
                }
                else
                {
                    createAccount(mEmailAddress.getText().toString(),mPassword.getText().toString());
                }
            }
        }
    }
}
