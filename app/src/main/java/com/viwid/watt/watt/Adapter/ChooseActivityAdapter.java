package com.viwid.watt.watt.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.viwid.watt.watt.Model.InterestModel;
import com.viwid.watt.watt.R;
import com.viwid.watt.watt.Model.ChoooseActivityModel;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bolts.Bolts;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by YOGI on 14-08-2018.
 */


/*
Adapter for ChooseFirstActivity
*/
public class ChooseActivityAdapter extends RecyclerView.Adapter<ChooseActivityAdapter.ViewHolder> {

    private Context mContext;
    private LayoutInflater inflater;
    private ItemClickListener mItemClickListener;
    private List<InterestModel> dataList;
    private static int count = 0;
    //Vaiable for Stroinng the CheckBoxState in the ChooseFirst Activity
    private Map<String, Boolean> checkBoxState = new HashMap<>();


    public ChooseActivityAdapter(Context context, List<InterestModel> dataList)
    {
        mContext = context;
        inflater = LayoutInflater.from(context);
        this.dataList = dataList;
        for (InterestModel model : dataList)
        {
            checkBoxState.put(model.getId(),false);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.choose_card_view,parent,false);
        return new ViewHolder(view);
    }

    public void setDataList(List<InterestModel> list)
    {
        dataList = list;
        for (InterestModel model : dataList)
        {
            checkBoxState.put(model.getId(),false);
        }
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InterestModel model = dataList.get(position);

        holder.name.setText(model.getTitle());
        holder.title.setText(model.getDescription());

        Glide.with(mContext)
                .load(model.getPhotoURL())
                .apply(new RequestOptions()
                        .centerCrop())
                .into(holder.circleImageView);

        if(checkBoxState.get(model.getId()))
        {
            holder.select.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.check_layer_list));
            holder.circleImageView.setBorderColor(Color.parseColor("#67d2ff"));
        }
        else
        {
            holder.select.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.add_button_layer_list));
            holder.circleImageView.setBorderColor(Color.parseColor("#979899"));
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void setCount(int i) {
        count = i;
    }

    public void setFilter(List<InterestModel> filterList)
    {
        dataList = new ArrayList<>();
        dataList.addAll(filterList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private ImageView select;
        private TextView name, title;
        private CircleImageView circleImageView;
        private CardView mCardView;

        public ViewHolder(View itemView) {
            super(itemView);

            circleImageView = itemView.findViewById(R.id.choose_circle_image_view);
            select = itemView.findViewById(R.id.select);
            name = itemView.findViewById(R.id.name_interest);
            title = itemView.findViewById(R.id.title_interest);
            mCardView = itemView.findViewById(R.id.choose_card_view);
            //select.setOnClickListener(this);
            mCardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.choose_card_view)
            {
                if(checkBoxState.get(dataList.get(getLayoutPosition()).getId()))
                {
                    count--;
                    checkBoxState.put(dataList.get(getLayoutPosition()).getId(),false);
                    select.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.add_button_layer_list));
                    circleImageView.setBorderColor(Color.parseColor("#979899"));
                }
                else
                {
                    count++;
                    checkBoxState.put(dataList.get(getLayoutPosition()).getId(),true);
                    select.setImageDrawable(ContextCompat.getDrawable(mContext,R.drawable.check_layer_list));
                    circleImageView.setBorderColor(Color.parseColor("#67d2ff"));
                }

                mItemClickListener.onItemClick(count);
            }
        }
    }

    public void setClickListener(ItemClickListener itemClickListener)
    {
        mItemClickListener = itemClickListener;
    }

    public interface ItemClickListener
    {
        void onItemClick(int count);
    }
}