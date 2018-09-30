package com.viwid.watt.watt.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.viwid.watt.watt.Fragment.ActionDialogFragment;
import com.viwid.watt.watt.Fragment.InterestListFragment;
import com.viwid.watt.watt.Fragment.ProfileBottomSheetFragment;
import com.viwid.watt.watt.Fragment.Profile_Fragment;
import com.viwid.watt.watt.Helper.BottomNavigationHelper;
import com.viwid.watt.watt.Logg;
import com.viwid.watt.watt.MainActivity;
import com.viwid.watt.watt.R;
import com.viwid.watt.watt.ToBeContinue;

import java.util.List;

import static com.viwid.watt.watt.Fragment.Profile_Fragment.EDIT_PROFILE;
import static com.viwid.watt.watt.Fragment.Profile_Fragment.INVITE_CONTACT;
import static com.viwid.watt.watt.Fragment.Profile_Fragment.REPORT;
import static com.viwid.watt.watt.Fragment.Profile_Fragment.SETTINGS;


/*
Main Activity Containing all Navigational Options in the BottomnavigationView*/
public class MainAppActivity extends AppCompatActivity
        implements Profile_Fragment.OnFragmentInteractionListener
        ,ProfileBottomSheetFragment.ButtonClickListerner
        ,ActionDialogFragment.ActionClickListerner{


    //constants for certain actions
    public static final int SCHEDULE_COTROT = 1;
    public static final int CREATE_CHALLENGE = 2;
    public static final int CREATE_ACTIVITY_CARD = 3;
    public static final int CREATE_INTEREST_PAGE = 4;
    public static final int CREATE_TROT = 5;

    //Firebase stuff
    private FirebaseAuth mFirebaseAuth;

    //Instance variables for layout widgets
    private BottomNavigationView mBottomNavigationView;
    private Toolbar mToolbar;

    //for alert dialog
    private AlertDialog.Builder dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_app);

        //For making the Status bar theme as light
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        //Initialize the firebase related stuff
        mFirebaseAuth = FirebaseAuth.getInstance();

        //Initialize the AlertDialog Box
        dialog = new AlertDialog.Builder(this)
                        .setTitle("Are you want to log out")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mFirebaseAuth.signOut();

                                LoginManager mgr = LoginManager.getInstance();
                                Logg.debugMessage(""+mgr);
                                mgr.logOut();
                                updateUI();
                                dialogInterface.cancel();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });


        //Initializing the Layout Widgets
        mToolbar = findViewById(R.id.mainAppToolbar);
        mBottomNavigationView = findViewById(R.id.mainAppBottomNavView);

        mBottomNavigationView.setItemIconTintList(null);

        //Using BottomNavigationHelper for removing the shift Mode in BottomNavigationView
        BottomNavigationHelper.removeShiftMode(mBottomNavigationView);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Start Trotting");

        mBottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.dest_activity:
                        item.setChecked(true);
                        break;
                    case R.id.dest_interests:
                        Fragment fragment1 = new InterestListFragment();
                        FragmentManager fragmentManager1 = getSupportFragmentManager();
                        fragmentManager1.beginTransaction().replace(R.id.fragment_Container,fragment1,InterestListFragment.TAG).commit();
                        item.setChecked(true);
                        break;
                    case R.id.dest_add:
                        item.setChecked(true);
                        ActionDialogFragment actionDialogFragment = new ActionDialogFragment();
                        actionDialogFragment.show(getSupportFragmentManager(),ActionDialogFragment.TAG);

                        break;
                    case R.id.dest_notifications:
                        dialog.show();
                        item.setChecked(true);
                        break;

                    case R.id.dest_profile:
                        item.setChecked(true);
                        Fragment fragment = new Profile_Fragment();
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.fragment_Container,fragment,Profile_Fragment.TAG).commit();
                        break;
                }
                return false;
            }
        });
    }


    private void updateUI() {
        Intent mMainIntent = new Intent(this,MainActivity.class);
        startActivity(mMainIntent);
        finish();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments!=null)
        {
            for (Fragment f : fragments)
            {
                if(f instanceof Profile_Fragment)
                {
                    f.onActivityResult(requestCode,resultCode,data);
                }
            }
        }
    }

    //Method Implementation for ProfileBottomSheet Fragment for Fragment Communnication Between
    //ProfileBottomSheetFragment and Profile_Fragment
    @Override
    public void onButtonClicked(int clickCode) {
        switch (clickCode)
        {
            case EDIT_PROFILE:
                Logg.debugMessage("Edit_profile Clicked 2");
                Profile_Fragment fragment1 = (Profile_Fragment) getSupportFragmentManager().findFragmentByTag(Profile_Fragment.TAG);
                fragment1.updateFragment(EDIT_PROFILE);
                break;
            case INVITE_CONTACT:
                Logg.debugMessage("Invite Clicked 2");
                break;
            case SETTINGS:
                Logg.debugMessage("Settings Clicked 2");
                break;
            case REPORT:
                Logg.debugMessage("Report Clicked 2");
                break;
        }
    }

    //Method Implementation for ActionDialogFragment
    @Override
    public void onActionClicked(int clickCode) {
        switch (clickCode)
        {
            case SCHEDULE_COTROT:
                Intent scheduleCotrot = new Intent(MainAppActivity.this,ScheduleCoTrotActivity.class);
                startActivity(scheduleCotrot);
                overridePendingTransition(R.anim.slide_up_from_bottom,R.anim.fade_out);
                break;
            case CREATE_CHALLENGE:
                Intent createChallenge = new Intent(MainAppActivity.this,CreateChallengeActivity.class);
                startActivity(createChallenge);
                overridePendingTransition(R.anim.slide_up_from_bottom,R.anim.fade_out);
                break;
            case CREATE_INTEREST_PAGE:
                Intent createInterest = new Intent(MainAppActivity.this,CreateInterestActivity.class);
                startActivity(createInterest);
                overridePendingTransition(R.anim.slide_up_from_bottom,R.anim.fade_out);
                break;
            case CREATE_ACTIVITY_CARD:
                Intent createCard = new Intent(MainAppActivity.this,CreateCardActivity.class);
                startActivity(createCard);
                overridePendingTransition(R.anim.slide_up_from_bottom,R.anim.fade_out);
                break;
            case CREATE_TROT:
                Intent MainApp = new Intent(MainAppActivity.this,ToBeContinue.class);
                        startActivity(MainApp);
                break;
        }
    }
}
