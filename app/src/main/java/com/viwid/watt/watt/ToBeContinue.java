package com.viwid.watt.watt;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.constraint.Group;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.contrarywind.adapter.WheelAdapter;
import com.contrarywind.listener.OnItemSelectedListener;
import com.contrarywind.view.WheelView;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class ToBeContinue extends AppCompatActivity implements SensorEventListener,View.OnClickListener{



    private static final String PREF = "mySharedPreferences";
    private static final String TIMELEFT = "timeleft";
    private static final String ENDTIME = "endtime";
    private static final String IS_RUNNING = "is_running";
    private static final String START_SENSOR_VALUE = "start_sensor_value";

    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference,mUsersTableReference;
    private WheelView hours,minutes;
    private ImageView rightButton;
    private TextView startTrotting,countDownTextView,trotCountView;
    private Group group1,group2;
    private long mTimeLeftInMillisec,endTimeInMillisec;
    private int hour=0,minute=1;

    private static boolean running  = false;

    private static final int REQUEST_OAUTH_REQUEST_CODE = 1;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    private OnDataPointListener mListener;
    private boolean isTrotting = false;
    private int sensorStartValue;
    private CountDownTimer mCountDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_be_continue);

        //logout = findViewById(R.id.logout);
        mFirebaseAuth = FirebaseAuth.getInstance();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("watt-production");
        mUsersTableReference = mDatabaseReference.child("users");

        //mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        hours = findViewById(R.id.hours_wheelview);
        minutes = findViewById(R.id.minutes_wheelview);

        rightButton = findViewById(R.id.right);
        startTrotting = findViewById(R.id.startTorttingTextView);
        countDownTextView = findViewById(R.id.countDownTimer);
        trotCountView = findViewById(R.id.trotCountView);

        rightButton.setOnClickListener(this);
        startTrotting.setOnClickListener(this);

        group1 = findViewById(R.id.group1);
        group2 = findViewById(R.id.group2);

        hours.setCyclic(false);
        minutes.setCyclic(true);

        final List<String> hoursItems = new ArrayList<>();
        for (int i =0;i<24;i++) {
            hoursItems.add("" + i);
        }
        final List<String> minutesItems = new ArrayList<>();
        for (int i =0;i<=59;i++) {
            minutesItems.add("" + i);
        }

        hours.setAdapter(new WheelAdapter() {
            @Override
            public int getItemsCount() {
                return hoursItems.size();
            }

            @Override
            public Object getItem(int index) {
                return hoursItems.get(index);
            }

            @Override
            public int indexOf(Object o) {
                return hoursItems.indexOf(o);
            }
        });

        hours.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                hour = Integer.parseInt(hoursItems.get(index));
                Logg.debugMessage("Hour Selected : "+hour);
            }
        });

        minutes.setAdapter(new WheelAdapter() {
            @Override
            public int getItemsCount() {
                return minutesItems.size();
            }

            @Override
            public Object getItem(int index) {
                return minutesItems.get(index);
            }

            @Override
            public int indexOf(Object o) {
                return minutesItems.indexOf(o);
            }
        });

        minutes.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(int index) {
                minute = Integer.parseInt(minutesItems.get(index));
                Logg.debugMessage("Minute Selected : "+minute);
            }
        });
        /*logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUsersTableReference.child(mFirebaseAuth.getCurrentUser().getUid())
                        .child("user_info")
                        .child("logged_in")
                        .setValue("false");
                mFirebaseAuth.signOut();

                LoginManager mgr = LoginManager.getInstance();
                mgr.logOut();
                Logg.debugMessage(""+mgr);
                updateUI();
            }
        });*/
        /*mStopTrot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //cancelSubscription();
                unregisterFitnessDataListener();
                mCountDownTimer.cancel();
            }
        });

        mStartTrot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (hasOAuthPermission()) {
                    //Subscription();
                    findFitnessDataSources();
                } else {
                    requestOAuthPermission();
                }
            }
        });*/
    }


    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.right:
            case R.id.startTorttingTextView:
                if (hasOAuthPermission()) {
                    //Subscription();
                    findFitnessDataSources();
                } else {
                    requestOAuthPermission();
                }
                break;
        }
    }

    private void CreateCountDownTimer(long timeleft) {
        mCountDownTimer = new CountDownTimer(timeleft,1000){

            @Override
            public void onTick(long l) {
                mTimeLeftInMillisec = l;
                Logg.debugMessage("onTick has been called"+l);
                long timeInSecs = l/1000;
                long hrs = timeInSecs/3600;
                long remainingSecs = timeInSecs%3600;
                long mins = remainingSecs/60;
                long secs = remainingSecs%60;
                //mins = mins%60;
                String timeFormat = String.format("%02d:%02d:%02d",hrs,mins,secs);
                countDownTextView.setText(timeFormat);
            }

            @Override
            public void onFinish() {
                Logg.debugMessage("count Down timer has been Finished");
                /*mStopTrot.performClick();*/
                unregisterFitnessDataListener();
                mCountDownTimer.cancel();
            }
        };
        mCountDownTimer.start();
    }

    private void findFitnessDataSources() {

        Fitness.getSensorsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .findDataSources(
                        new DataSourcesRequest.Builder()
                                .setDataTypes(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                                .setDataSourceTypes(DataSource.TYPE_RAW)
                                .build())
                .addOnSuccessListener(
                        new OnSuccessListener<List<DataSource>>() {
                            @Override
                            public void onSuccess(List<DataSource> dataSources) {
                                for (DataSource dataSource : dataSources) {
                                    // Let's register a listener to receive Activity data!
                                    if (dataSource.getDataType().equals(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                                            && mListener == null) {
                                        Logg.debugMessage( "Data source for STEP COUNT CUMULATIVE found!  Registering.");
                                        registerFitnessDataListener(dataSource, DataType.TYPE_STEP_COUNT_CUMULATIVE);
                                    }
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Logg.debugMessage( "failed", e);
                            }
                        });
    }

    private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {
        mListener =
                new OnDataPointListener() {
                    @Override
                    public void onDataPoint(DataPoint dataPoint) {
                        for (Field field : dataPoint.getDataType().getFields()) {
                            Value val = dataPoint.getValue(field);
                            /*Logg.debugMessage("Detected DataPoint field: " + field.getName());
                            Logg.debugMessage("Detected DataPoint value: " + val + " val format : "+val.getFormat());*/
                            if(isTrotting)
                            {
                                trotCountView.setText((val.asInt()-sensorStartValue)+" trots");
                            }
                            else
                            {
                                group1.setVisibility(View.GONE);
                                group2.setVisibility(View.VISIBLE);
                                mTimeLeftInMillisec = ((hour*60)+minute)*60*1000L;
                                endTimeInMillisec = System.currentTimeMillis()+mTimeLeftInMillisec;
                                CreateCountDownTimer(mTimeLeftInMillisec);
                                sensorStartValue = val.asInt();
                                trotCountView.setText((val.asInt()-sensorStartValue)+" trots");
                                isTrotting = true;
                                Logg.debugMessage("Now isTrotting is True, CreateCountDownTimer has been Called");
                            }
                        }
                    }
                };

        Fitness.getSensorsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .add(
                        new SensorRequest.Builder()
                                .setDataSource(dataSource) // Optional but recommended for custom data sets.
                                .setDataType(dataType) // Can't be omitted.
                                .setSamplingRate(10, TimeUnit.SECONDS)
                                .build(),
                        mListener)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Logg.debugMessage("Listener registered!");
                                } else {
                                    Logg.debugMessage("Listener not registered.", task.getException());
                                }
                            }
                        });
    }

    private void unregisterFitnessDataListener() {
        if (mListener == null) {
            // This code only activates one listener at a time.  If there's no listener, there's
            // nothing to unregister.
            return;
        }

        // [START unregister_data_listener]
        // Waiting isn't actually necessary as the unregister call will complete regardless,
        // even if called from within onStop, but a callback can still be added in order to
        // inspect the results.
        Fitness.getSensorsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .remove(mListener)
                .addOnCompleteListener(
                        new OnCompleteListener<Boolean>() {
                            @Override
                            public void onComplete(@NonNull Task<Boolean> task) {
                                if (task.isSuccessful() && task.getResult()) {
                                    Logg.debugMessage("Listener was removed!");
                                    mListener = null;
                                    isTrotting = false;
                                    trotCountView.setText(trotCountView.getText()+" trots");
                                } else {
                                    Logg.debugMessage("Listener was not removed.");
                                }
                            }
                        });
        // [END unregister_data_listener]
    }

    private FitnessOptions getFitnessSignInOptions() {
        return FitnessOptions.builder().addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE).build();
    }

    private boolean hasOAuthPermission() {
        FitnessOptions fitnessOptions = getFitnessSignInOptions();
        return GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions);
    }

    private void requestOAuthPermission() {
        FitnessOptions fitnessOptions = getFitnessSignInOptions();
        GoogleSignIn.requestPermissions(
                this,
                REQUEST_OAUTH_REQUEST_CODE,
                GoogleSignIn.getLastSignedInAccount(this),
                fitnessOptions);
    }

    private void Subscription() {

        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Logg.debugMessage("Successfully subscribed!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Logg.debugMessage("There was a problem subscribing.");
                    }
                });

    }

    private void cancelSubscription() {
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .unsubscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Logg.debugMessage("Successfully unsubscribed for data type: ");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Subscription not removed
                        Logg.debugMessage("Failed to unsubscribe for data type: ");
                    }
                });
        // [END unsubscribe_from_datatype]
    }


    private void updateUI() {
        Intent mMainIntent = new Intent(this,MainActivity.class);
        startActivity(mMainIntent);
        finish();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(running)
        {
            //mTrotCount.setText(String.valueOf(sensorEvent.values[0]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onStart() {
        super.onStart();

        SharedPreferences preferences = getSharedPreferences(PREF,MODE_PRIVATE);
        mTimeLeftInMillisec = preferences.getLong(TIMELEFT,0);
        isTrotting = preferences.getBoolean(IS_RUNNING,false);

        if(isTrotting)
        {
            endTimeInMillisec = preferences.getLong(ENDTIME,0);
            mTimeLeftInMillisec = endTimeInMillisec-System.currentTimeMillis();
            sensorStartValue = preferences.getInt(START_SENSOR_VALUE,0);

            if(mTimeLeftInMillisec<0)
            {
                mTimeLeftInMillisec = 0L;
                isTrotting = false;
            }
            else
            {
                group1.setVisibility(View.GONE);
                group2.setVisibility(View.VISIBLE);
                CreateCountDownTimer(mTimeLeftInMillisec);
                findFitnessDataSources();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //running = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences preferences = getSharedPreferences(PREF,MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putLong(TIMELEFT,mTimeLeftInMillisec);
        editor.putBoolean(IS_RUNNING,isTrotting);
        editor.putLong(ENDTIME,endTimeInMillisec);
        editor.putInt(START_SENSOR_VALUE,sensorStartValue);

        editor.apply();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH_REQUEST_CODE) {
                //Subscription();
                findFitnessDataSources();
            }
        }

    }
}