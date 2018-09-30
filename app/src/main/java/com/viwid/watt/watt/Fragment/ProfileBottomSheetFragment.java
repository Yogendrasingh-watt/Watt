package com.viwid.watt.watt.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.viwid.watt.watt.Logg;
import com.viwid.watt.watt.R;

/**
 * Created by YOGI on 09-08-2018.
 */
/*
Fragment for showing profile Bottom action sheet.
*/
public class ProfileBottomSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener
{
    public static final String TAG = "profileBottomSheet";

    public TextView editProfile,invitePeople,settings,report,cancel;

    private ButtonClickListerner buttonClickListerner;

    public ProfileBottomSheetFragment()
    {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_bottom_sheet_fragment,container,false);

        editProfile = view.findViewById(R.id.edit_profile);
        invitePeople = view.findViewById(R.id.invite);
        settings = view.findViewById(R.id.settings);
        report = view.findViewById(R.id.report);
        cancel = view.findViewById(R.id.cancel);

        editProfile.setOnClickListener(this);
        invitePeople.setOnClickListener(this);
        settings.setOnClickListener(this);
        report.setOnClickListener(this);
        cancel.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.edit_profile:
                Logg.debugMessage("Edit_profile Clicked 1");
                buttonClickListerner.onButtonClicked(Profile_Fragment.EDIT_PROFILE);
                dismiss();
                break;
            case R.id.invite:
                Logg.debugMessage("Invite Clicked 1");
                buttonClickListerner.onButtonClicked(Profile_Fragment.INVITE_CONTACT);
                dismiss();
                break;
            case R.id.settings:
                Logg.debugMessage("Settings Clicked 1");
                buttonClickListerner.onButtonClicked(Profile_Fragment.SETTINGS);
                dismiss();
                break;
            case R.id.report:
                Logg.debugMessage("Report Clicked 1");
                buttonClickListerner.onButtonClicked(Profile_Fragment.REPORT);
                dismiss();
                break;
            case R.id.cancel:
                Logg.debugMessage("Cancel Clicked 1");
                dismiss();
                break;
        }
    }

    public interface ButtonClickListerner
    {
        void onButtonClicked(int clickCode);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try
        {
            buttonClickListerner = (ButtonClickListerner) context;
            Logg.debugMessage("Context Connected in BottomSheetFragment");
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(context.toString()+" must implement BottomSheetListener");
        }
    }
}