package com.viwid.watt.watt;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.ProviderQueryResult;
import com.viwid.watt.watt.Activity.MainAppActivity;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    //Instance variables for layout widgets
    private EditText mEmail,mPassword;
    private TextView mforgotPassword;
    private Button mLoginButton;
    private ImageView closeLogin;

    //Firebase Stuff
    private FirebaseAuth mFirebaseAuth;

    //For Showing the ProgressBar
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initialize the variables for layout widgets
        mEmail = findViewById(R.id.email_login);
        mPassword = findViewById(R.id.password);
        mforgotPassword = findViewById(R.id.forgot_password);
        mLoginButton = findViewById(R.id.login_button);
        closeLogin = findViewById(R.id.close_login);

        //Initialize the firebase related stuff
        mFirebaseAuth = FirebaseAuth.getInstance();


        loadingBar = new ProgressDialog(this);


        //set the onclick Listener for widgets
        closeLogin.setOnClickListener(this);
        mLoginButton.setOnClickListener(this);
        mforgotPassword.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.close_login)
        {
            //onClick on Close Login button
            onBackPressed();
        }
        else if(v.getId() == R.id.login_button)
        {
            //onClick on Login Button
            signIn(mEmail.getText().toString(),mPassword.getText().toString());
        }
        else if(v.getId() == R.id.forgot_password)
        {
            //onClick on Forgot password
            if (TextUtils.isEmpty(mEmail.getText().toString()))
            {
                CreateMaterialDialog("Please Enter the Email Address to reset the password");
            }
            else
            {
                loadingBar.setTitle("Watt-walk the talk");
                loadingBar.setMessage("Please Wait while we are resetting your password");
                loadingBar.show();
                loadingBar.setCancelable(false);
                loadingBar.setCanceledOnTouchOutside(false);

                mFirebaseAuth.sendPasswordResetEmail(mEmail.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                {
                                    loadingBar.dismiss();
                                    CreateMaterialDialog("A Password Reset link has been sent to your email address");
                                    ClearFields();
                                }
                                else
                                {
                                    loadingBar.dismiss();
                                    try
                                    {
                                        throw task.getException();
                                    }
                                    catch (FirebaseAuthInvalidCredentialsException e)
                                    {
                                        //Toast.makeText(LoginActivity.this,"Invalid Email ",Toast.LENGTH_LONG).show();
                                        CreateMaterialDialog("invalid Email");
                                    }
                                    catch (FirebaseAuthInvalidUserException e)
                                    {
                                        CreateMaterialDialog("Email Doesn't exists");
                                    }
                                    catch (Exception e)
                                    {
                                        CreateMaterialDialog("Some Error has Occured, May be network issue.Try Again");
                                    }
                                }
                            }
                        });
            }
        }
    }

    //Method for signing in
    private void signIn(final String email, String password) {
        String validate = validateForm();
        if(validate!=null)
        {
            CreateMaterialDialog("Need more information","Please provide following info : ",validate);
        }
        else
        {
            loadingBar.setTitle("Watt-walk the talk");
            loadingBar.setMessage("Please Wait while we are logging In");
            loadingBar.show();
            loadingBar.setCancelable(false);
            loadingBar.setCanceledOnTouchOutside(false);

            mFirebaseAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                //loadingBar.dismiss();
                                if(!task.getResult().getUser().isEmailVerified())
                                {
                                    loadingBar.setMessage("It Seems that You have not verified yet, Sending the Verification Link");

                                    task.getResult().getUser().sendEmailVerification()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    loadingBar.dismiss();
                                                    CreateMaterialDialog("Verify Email",
                                                            "We have sent a verification link to your email "+
                                                                    mFirebaseAuth.getCurrentUser().getEmail()+
                                                                    ". You would be able to login to your account after verification. Thank You","");

                                                    mFirebaseAuth.signOut();
                                                    //ClearFields();
                                                }
                                            });


                                }
                                else {
                                    loadingBar.dismiss();
                                    Intent successintent = new Intent(LoginActivity.this,ChooseFirstActivity.class);
                                    startActivity(successintent);
                                    finish();
                                }

                            }
                            else
                            {
                                loadingBar.dismiss();
                                mFirebaseAuth.fetchProvidersForEmail(email)
                                        .addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                                                if(!task.isSuccessful())
                                                {
                                                    try
                                                    {
                                                        throw task.getException();
                                                    }
                                                    catch (FirebaseAuthInvalidCredentialsException e)
                                                    {
                                                        //Toast.makeText(LoginActivity.this,"Invalid Email ",Toast.LENGTH_LONG).show();
                                                        CreateMaterialDialog("invalid Email");
                                                    }
                                                    catch (Exception e)
                                                    {
                                                        //Toast.makeText(LoginActivity.this,""+e,Toast.LENGTH_LONG).show();
                                                        CreateMaterialDialog("Some Error has Occured, May be Network issue.Try Again");
                                                    }
                                                }
                                                else
                                                {
                                                    boolean check = task.getResult().getProviders().isEmpty();
                                                    //throw task.getException();
                                                    if(!check)
                                                    {
                                                        //Toast.makeText(LoginActivity.this,"Wrong Password ",Toast.LENGTH_LONG).show();
                                                        CreateMaterialDialog("Wrong Password ");
                                                    }
                                                    else
                                                    {
                                                        //Toast.makeText(LoginActivity.this,"Email doesn't exists ",Toast.LENGTH_LONG).show();
                                                        CreateMaterialDialog("Email doesn't exists ");
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                    });
        }


    }

    private void ClearFields() {
        mEmail.setText("");
        mPassword.setText("");
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
        //boolean valid = true;
        StringBuilder infoBuilder = new StringBuilder("");
        String email = mEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            infoBuilder.append("Email, ");
        }

        String password = mPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            infoBuilder.append("Password, ");
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
}
