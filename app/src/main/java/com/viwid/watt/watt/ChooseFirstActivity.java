package com.viwid.watt.watt;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.viwid.watt.watt.Activity.MainAppActivity;
import com.viwid.watt.watt.Adapter.ChooseActivityAdapter;
import com.viwid.watt.watt.Model.ChoooseActivityModel;
import com.viwid.watt.watt.Model.InterestModel;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class ChooseFirstActivity extends AppCompatActivity implements ChooseActivityAdapter.ItemClickListener,View.OnClickListener{

    //Firebase Related Stuff
    private FirebaseAuth mFirebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference,mUsersTableReference,mInterestTableReference;

    private List<InterestModel> interestModelList = new ArrayList<>();

    //Instance variables for layout widgets
    private RecyclerView mRecyclerView;
    private LinearLayout mBottomBar;
    private Button mDone;
    private TextView selected_textview;
    private SearchView mSearchView;

    //Adapter Instance Variable
    private ChooseActivityAdapter mChooseActivityAdapter;

    //Variable to check whether BottomBar is open or not
    private boolean isOpen = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_first);

        //Initialize the firebase related stuff
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("watt-production");
        mUsersTableReference = mDatabaseReference.child("users");
        mInterestTableReference = mDatabaseReference.child("following_data");
        mInterestTableReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i=0;
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                {
                    InterestModel model = dataSnapshot1.child("info").getValue(InterestModel.class);
                    model.setId(i+"");
                    interestModelList.add(model);
                    i++;
                    Log.d("YOGI","user Id :"+model.getCreated_userid()+" Title : "+model.getTitle()+" Desc : "+model.getDescription());
                }


                for (InterestModel model : interestModelList)
                {
                    Log.d("YOGI","Model.getId() "+model.getId()+"");
                }

                mChooseActivityAdapter.setDataList(interestModelList);
                /*fillViewPager(view);*/
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //Initialize the variables for layout widgets
        mRecyclerView = findViewById(R.id.choose_recycler_view);
        mBottomBar = findViewById(R.id.bottomBar);
        mDone = findViewById(R.id.done_button);
        mSearchView = findViewById(R.id.choose_search_view);
        selected_textview = findViewById(R.id.selected_textview);

        //Clearing the focus in Search View
        mSearchView.clearFocus();

        //Close Listener on SearchView
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                return false;
            }
        });

        //Click listerner for Search button
        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchView.clearFocus();
            }
        });

        //Query Text Focus Change Listener for searchView
        mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                //mSearchView.clearFocus();
            }
        });

        //Query Text Change Listener for searchView
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                final List<InterestModel> filterdList = filter(interestModelList,newText);
                mChooseActivityAdapter.setFilter(filterdList);
                return true;
            }
        });

        //Click listener for Done Button
        mDone.setOnClickListener(this);

        //Initializing Adapter and RecylerView
        mChooseActivityAdapter = new ChooseActivityAdapter(this,interestModelList);
        mChooseActivityAdapter.setClickListener(this);
        mChooseActivityAdapter.setCount(0);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,2));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mChooseActivityAdapter);

        //Setting Logged In Value as True in Firebase Database
        mUsersTableReference.child(mFirebaseAuth.getCurrentUser().getUid())
                .child("user_info")
                .child("logged_in").setValue("true");

    }

    private List<InterestModel> filter(List<InterestModel> original,String query)
    {
        query = query.trim().toLowerCase();
        final List<InterestModel> filterModelList = new ArrayList<>();
        for (InterestModel model : original)
        {
            final String text = model.getTitle().trim().toLowerCase();
            if (text.contains(query))
            {
                filterModelList.add(model);
            }
        }
        return filterModelList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.choose_activity_menu_resource,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.action_logout)
        {
            mFirebaseAuth.signOut();

            LoginManager mgr = LoginManager.getInstance();
            Logg.debugMessage(""+mgr);
            mgr.logOut();
            updateUI();
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateUI() {
        Intent mMainIntent = new Intent(this,MainActivity.class);
        startActivity(mMainIntent);
        finish();
    }

    @Override
    public void onItemClick(int count) {

        if(count>0)
        {
            if(isOpen)
            {
                selected_textview.setText(count+" selected");
            }
            else
            {
                isOpen = true;
                mBottomBar.setVisibility(View.VISIBLE);
                mBottomBar.setTranslationY(mBottomBar.getHeight());
                selected_textview.setText(count+" selected");
                //mBottomBar.setAlpha(0.0f);
                //mBottomBar.setAnimation((Animation) getResources().getAnimation(R.anim.slide_down_from_bottom));
                mBottomBar.animate()
                        .translationY(0)
                        .setDuration(200)
                        .setListener(null);
            }
        }
        else
        {
            isOpen = false;
            selected_textview.setText(count+" selected");
            mBottomBar.animate()
                    .translationY(mBottomBar.getHeight())
                    .setDuration(200)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mBottomBar.setVisibility(View.INVISIBLE);
                        }
                    });
        }

    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.done_button)
        {
            Intent MainApp = new Intent(this,MainAppActivity.class);
            startActivity(MainApp);
            finish();
        }
    }
}
