package com.viwid.watt.watt.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.viwid.watt.watt.R;

/*
For Creating Trot Challenge
*/
public class CreateChallengeActivity extends AppCompatActivity implements View.OnClickListener{


    private ImageView closeInterest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_challenge);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        closeInterest = findViewById(R.id.close_interest);
        closeInterest.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in,R.anim.slide_down_from_bottom);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.close_interest:
                onBackPressed();
                break;
        }
    }
}
