package com.viwid.watt.watt.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.viwid.watt.watt.Fragment.InterestFragment;
import com.viwid.watt.watt.Model.InterestModel;

import java.util.List;

/**
 * Created by YOGI on 13-09-2018.
 */

/*
Adapter for Draggable Interest Page
*/
public class InterestPagerAdapter extends FragmentStatePagerAdapter {

    private List<InterestModel> interestModelList;
    private List<InterestFragment> fragmentList;

    public InterestPagerAdapter(FragmentManager fm,List<InterestModel> interestModelList,List<InterestFragment> fragmentList) {
        super(fm);
        this.interestModelList = interestModelList;
        this.fragmentList = fragmentList;
    }

    @Override
    public Fragment getItem(int position) {
        InterestFragment interestFragment = fragmentList.get(position%5);
        interestFragment.bindData(interestModelList.get(position));
        return interestFragment;
    }

    @Override
    public int getCount() {
        Log.d("YOGI",interestModelList.size()+", fragment sie : "+fragmentList.size());
        return interestModelList.size();
    }

}
