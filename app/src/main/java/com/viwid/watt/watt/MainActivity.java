package com.viwid.watt.watt;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.viwid.watt.watt.Activity.MainAppActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/*
Main Activity that gets lauched first*/
public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    // Request Code for asking permissions
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 123;

    //Instance variables for layout widgets
    private Button fbLoginButton,signUpButton;
    private TextView tAndC;
    private TextView loginHere;

    //For Facebook Login
    private CallbackManager mCallbackManager;

    //For Showing the ProgressBar
    private ProgressDialog loadingBar;

    //Firebase Stuff
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference,mUsersTableReference;

    //For fetching the Device Id
    private TelephonyManager tm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Logg.debugMessage("Main Activity Created");

        /*
        Initialize the telephony manager instance to fetch the device Id
        Note : We need to ask user to grant READ_PHONE_STATE permission
        to fetch the device Id. If not granted, device id will not be fetched.
        * */
        tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

        //Initialize the CallbackManager Instance
        mCallbackManager = CallbackManager.Factory.create();

        loadingBar = new ProgressDialog(this);

        //Initialize the firebase related stuff
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("watt-production");
        mUsersTableReference = mDatabaseReference.child("users");

        //Initialize the variables for layout widgets
        fbLoginButton = findViewById(R.id.fb_login);
        signUpButton = findViewById(R.id.signup);
        loginHere = findViewById(R.id.login_here_text);
        tAndC = findViewById(R.id.t_and_c);

        //set the onclick Listener for widgets
        fbLoginButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);
        loginHere.setOnClickListener(this);
        tAndC.setOnClickListener(this);

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

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.fb_login)
        {
            //On Click of Facebook login button, we will check for permissions and then allow the facebook login
            if(checkAndRequestPermissions())
            {
                loginToFacebook();
            }
            /*else if(!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_PHONE_STATE))
            {
                loginToFacebook();
            }*/
        }

        //On Click of Sign Up Button
        else if(v.getId()==R.id.signup)
        {
            Logg.debugMessage("SignUp Button Clicked");
            Intent intent = new Intent(this,SignUpActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up_from_bottom,R.anim.fade_out);
        }

        //On Click of Login Here Button
        else if(v.getId()==R.id.login_here_text)
        {
            Logg.debugMessage("Login Here Clicked");
            Intent intent = new Intent(this,LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_up_from_bottom,R.anim.fade_out);
        }

        //On Click of Terms & Conditions
        else if(v.getId()==R.id.t_and_c)
        {

        }
    }

    private void loginToFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile","email","user_gender"));

        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                CreateMaterialDialog("Some Error has occured, May be Network issue. Try Again "+error.toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();

        //User is Not null and Facebook token is also active
        if (AccessToken.isCurrentAccessTokenActive() && currentUser!=null)
        {
            //Toast.makeText(this,"Tocken is active and user is not null",Toast.LENGTH_LONG).show();
            updateUI();
        }
        //User is Not null and Email is also verified
        else if (currentUser!=null && currentUser.isEmailVerified())
        {
            //Toast.makeText(this,"Email Verified and user is not null",Toast.LENGTH_LONG).show();
            updateUI();
        }
        else if(!AccessToken.isCurrentAccessTokenActive() && currentUser==null)
        {
            //Toast.makeText(this,"Tocken is not active and user is null",Toast.LENGTH_LONG).show();
        }
        else if(AccessToken.isCurrentAccessTokenActive() && currentUser==null)
        {
            //Toast.makeText(this,"Tocken is active and user is null",Toast.LENGTH_LONG).show();
        }
    }

    //For Creating the Material Dialog Box
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

    //For Creating the Material Dialog Box
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

    //
    private void handleFacebookAccessToken(final AccessToken token) {
        Logg.debugMessage("handleFacebookAccessToken:" + token);

        loadingBar.setTitle("Watt-walk the talk");
        loadingBar.setMessage("Please Wait while we are opening the application");
        loadingBar.show();
        loadingBar.setCancelable(false);
        loadingBar.setCanceledOnTouchOutside(false);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Logg.debugMessage("signInWithCredential:success");
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            //loadingBar.dismiss();
                            //updateUI();
                            saveInfoInDatabse(AccessToken.getCurrentAccessToken(),user);
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
                                CreateMaterialDialog("Email Already Exists ");
                                // TODO: Take your action
                            }
                            catch (Exception e)
                            {
                                    /*Toast.makeText(SignUpActivity.this,""+e,Toast.LENGTH_LONG).show();*/
                                CreateMaterialDialog("Some Error has Occured, May be Network issue.Try Again");
                            }
                            finally {
                                LoginManager.getInstance().logOut();
                            }
                        }

                        // ...
                    }
                });
    }

    private void saveInfoInDatabse(final AccessToken token, final FirebaseUser user) {
        GraphRequest request = GraphRequest.newMeRequest(
                token,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {

                        Set<String> Permissions = token.getPermissions();
                        Set<String> deniedPermissions = token.getDeclinedPermissions();
                        Logg.debugMessage("Current Permissions = "+Permissions.toString());
                        Logg.debugMessage("Denied Permission = "+deniedPermissions.toString());

                        DatabaseReference mCurrentUserReference = mUsersTableReference.child(""+user.getUid());

                        HashMap<String,String> user_info = new HashMap<>();

                        try {
                            Logg.debugMessage(""+object.toString());
                            user_info.put("facebook_userId",object.getString("id"));
                            user_info.put("name",object.getString("name"));
                            user_info.put("first_name",object.getString("first_name"));
                            user_info.put("last_name",object.getString("last_name"));
                            if (deniedPermissions.contains("email"))
                            {
                                user_info.put("email","Permission Denied By Facebook User");
                            }
                            else
                            {
                                user_info.put("email",object.getString("email"));
                            }
                            user_info.put("photoURL",object.getJSONObject("picture").getJSONObject("data").getString("url"));
                            if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED)
                            {
                                user_info.put("device_id","Permission Denied");
                            }
                            else
                            {
                                user_info.put("device_id",""+tm.getDeviceId());
                            }
                            user_info.put("created_date",""+System.currentTimeMillis());
                            //user_info.put("isActive","true");

                            Logg.debugMessage("Hash Map : "+user_info.toString());

                            mCurrentUserReference.child("user_info").setValue(user_info)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            loadingBar.dismiss();
                                            updateUI();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            CreateMaterialDialog("Account didn't get created, Try Again!");
                                            LoginManager.getInstance().logOut();
                                            user.delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Logg.debugMessage("User Deleted after Firabase Storage Failure, Current User : ");
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Logg.debugMessage("User Did not get Deleted after Firabase Storage Failure, Exception : ");
                                                        }
                                                    });
                                        }
                                    });
                        } catch (JSONException e) {
                            Logg.debugMessage("Exception  : "+e.getMessage().toString());
                        }

                    }
                }
        );

        Bundle parameters = new Bundle();
        parameters.putString("fields","id,email,name,picture,first_name,last_name");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

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
                    loginToFacebook();
                }
                else
                {
                    loginToFacebook();
                }
            }
        }
    }

    private void updateUI() {

        //Calling the Interest Chooser Activity
        Intent mSuccessIntent = new Intent(this,ChooseFirstActivity.class);
        startActivity(mSuccessIntent);
        finish();

        /*Intent MainApp = new Intent(this,MainAppActivity.class);
        startActivity(MainApp);
        finish();*/
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
