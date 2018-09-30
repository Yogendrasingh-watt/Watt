package com.viwid.watt.watt.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;import android.widget.TextView;

import com.viwid.watt.watt.Activity.MainAppActivity;
import com.viwid.watt.watt.Logg;
import com.viwid.watt.watt.R;

/**
 * Created by YOGI on 07-09-2018.
 */
/*
Action Dialog Fragment
*/
public class ActionDialogFragment extends DialogFragment implements View.OnClickListener{

    public static final String TAG = "actionDialogFragment";
    private TextView mCoTrot,mChallenge,mActivityCard,mInterestPage,mTrot;
    private ActionClickListerner mActionClickListerner;

    public ActionDialogFragment()
    {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.action_dialog_fragment,container,false);

        mCoTrot = view.findViewById(R.id.schedule_coTrot);
        mChallenge = view.findViewById(R.id.create_challenge);
        mActivityCard = view.findViewById(R.id.create_activity_card);
        mInterestPage = view.findViewById(R.id.create_interest_page);
        mTrot = view.findViewById(R.id.create_trot);

        mCoTrot.setOnClickListener(this);
        mChallenge.setOnClickListener(this);
        mActivityCard.setOnClickListener(this);
        mInterestPage.setOnClickListener(this);
        mTrot.setOnClickListener(this);

        return view;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.schedule_coTrot:
                mActionClickListerner.onActionClicked(MainAppActivity.SCHEDULE_COTROT);
                dismiss();
                break;
            case R.id.create_challenge:
                mActionClickListerner.onActionClicked(MainAppActivity.CREATE_CHALLENGE);
                dismiss();
                break;
            case R.id.create_activity_card:
                mActionClickListerner.onActionClicked(MainAppActivity.CREATE_ACTIVITY_CARD);
                dismiss();
                break;
            case R.id.create_interest_page:
                mActionClickListerner.onActionClicked(MainAppActivity.CREATE_INTEREST_PAGE);
                dismiss();
                break;
            case R.id.create_trot:
                mActionClickListerner.onActionClicked(MainAppActivity.CREATE_TROT);
                dismiss();
                break;
        }
    }


    public interface ActionClickListerner
    {
        void onActionClicked(int clickCode);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try
        {
            mActionClickListerner = (ActionDialogFragment.ActionClickListerner) context;
            Logg.debugMessage("Context Connected in ActionDialogfragment");
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(context.toString()+" must implement ActionClickListener");
        }
    }
}
