package com.qboxus.musictok.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.qboxus.musictok.Models.HashTagModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.Adapter_Click_Listener;
import com.qboxus.musictok.SimpleClasses.Functions;

import java.util.ArrayList;

/**
 * Created by AQEEL on 3/19/2019.
 */


public class HashTag_Adapter extends RecyclerView.Adapter<HashTag_Adapter.CustomViewHolder > {
    public Context context;

    ArrayList<HashTagModel> datalist;
    Adapter_Click_Listener adapter_click_listener;

    public HashTag_Adapter(Context context, ArrayList<HashTagModel> arrayList, Adapter_Click_Listener adapter_click_listener) {
        this.context = context;
        datalist= arrayList;
        this.adapter_click_listener=adapter_click_listener;
    }

    @Override
    public HashTag_Adapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_hashtag_list,viewGroup,false);
        HashTag_Adapter.CustomViewHolder viewHolder = new HashTag_Adapter.CustomViewHolder(view);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return datalist.size();
    }



    @Override
    public void onBindViewHolder(final HashTag_Adapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);
        HashTagModel item= datalist.get(i);
        holder.name_txt.setText(item.name);

        holder.views_txt.setText(Functions.GetSuffix(item.videos_count));

        if(item.fav!=null && item.fav.equalsIgnoreCase("1")){
            holder.fav_btn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_my_favourite));
        }
        else {
            holder.fav_btn.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_my_un_favourite));
        }

        holder.bind(i,item,adapter_click_listener);

    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        TextView name_txt,views_txt;
        ImageButton fav_btn;

        public CustomViewHolder(View view) {
            super(view);

            name_txt=view.findViewById(R.id.name_txt);
            views_txt=view.findViewById(R.id.views_txt);
            fav_btn=view.findViewById(R.id.fav_btn);
        }

        public void bind(final int pos , final Object item, final Adapter_Click_Listener listener) {


            fav_btn.setOnClickListener( v -> {
                listener.onItemClick(v,pos,item);
            });

            itemView.setOnClickListener(v -> {

                listener.onItemClick(v,pos,item);

            });
        }


    }



}

