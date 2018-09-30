package com.viwid.watt.watt.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.viwid.watt.watt.R;

/**
 * Created by YOGI on 24-08-2018.
 */
/*
Adapter for Action Tabs on Profile Fragment Page.
*/
public class ProfileRecyclerAdapter extends RecyclerView.Adapter<ProfileRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private LayoutInflater inflater;

    private String[] descriptionArray = new String[]{"Description","0","0","0","0","0"};
    private String[] aboutUserArray = new String[]{"About User","Trots","Trotters","Followers","Following","coTrots"};
    private int[] drawableArray = new int[]{R.drawable.watt_description,R.drawable.watt_steps_white,R.drawable.watt_users_white,
                                    R.drawable.watt_followers_white,R.drawable.watt_followers_white,R.drawable.watt_cotrots};
    public ProfileRecyclerAdapter(Context context)
    {
        mContext = context;
        inflater = LayoutInflater.from(mContext);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.profile_action_layout,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.profile_action.setImageDrawable(ContextCompat.getDrawable(mContext,drawableArray[position]));
        holder.description_action.setText(descriptionArray[position]);
        holder.aboutUser_action.setText(aboutUserArray[position]);
    }

    @Override
    public int getItemCount() {
        return descriptionArray.length;
    }
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        public ImageView profile_action;
        public TextView description_action,aboutUser_action;

        public ViewHolder(View itemView) {
            super(itemView);
            profile_action = itemView.findViewById(R.id.profile_action_circle_imageView);
            description_action = itemView.findViewById(R.id.description);
            aboutUser_action = itemView.findViewById(R.id.about_user);
        }

        @Override
        public void onClick(View view) {

        }
    }
}
