package com.qboxus.musictok.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.qboxus.musictok.Models.Users_Model;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.Adapter_Click_Listener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.qboxus.musictok.SimpleClasses.Functions;

import java.util.ArrayList;

/**
 * Created by AQEEL on 3/19/2019.
 */


public class Users_Adapter extends RecyclerView.Adapter<Users_Adapter.CustomViewHolder > {
    public Context context;

    ArrayList<Users_Model> datalist;
    Adapter_Click_Listener adapter_click_listener;
    public Users_Adapter(Context context, ArrayList<Users_Model> arrayList, Adapter_Click_Listener adapter_click_listener) {
        this.context = context;
        datalist= arrayList;
        this.adapter_click_listener=adapter_click_listener;
    }

    @Override
    public Users_Adapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_users_list2,viewGroup,false);
        Users_Adapter.CustomViewHolder viewHolder = new Users_Adapter.CustomViewHolder(view);
        return viewHolder;
    }


    @Override
    public int getItemCount() {
        return datalist.size();
    }



    @Override
    public void onBindViewHolder(final Users_Adapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);
        Users_Model item=(Users_Model) datalist.get(i);

        if(item.profile_pic!=null && !item.profile_pic.equals("")) {
            Uri uri = Uri.parse(item.profile_pic);
            holder.image.setImageURI(uri);
        }

        holder.username_txt.setText(item.username);

        if(!item.first_name.equals(""))
        holder.name_txt.setText(item.first_name+" "+item.last_name);
        else
            holder.name_txt.setVisibility(View.GONE);


        holder.follower_video_txt.setText(Functions.GetSuffix(item.followers_count)+ " Followers  "+ item.videos +" Videos");
        holder.bind(i,item,adapter_click_listener);

    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView image;
        TextView username_txt,name_txt,follower_video_txt;

        public CustomViewHolder(View view) {
            super(view);

            image=view.findViewById(R.id.image);
            username_txt=view.findViewById(R.id.username_txt);
            follower_video_txt=view.findViewById(R.id.follower_video_txt);
            name_txt=view.findViewById(R.id.name_txt);


        }

        public void bind(final int pos , final Object item, final Adapter_Click_Listener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v,pos,item);
                }
            });


        }

    }



}

