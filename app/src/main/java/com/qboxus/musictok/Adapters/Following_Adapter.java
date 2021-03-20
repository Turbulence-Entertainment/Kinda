package com.qboxus.musictok.Adapters;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qboxus.musictok.Models.Following_Get_Set;
import com.qboxus.musictok.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by AQEEL on 3/20/2018.
 */

public class Following_Adapter extends RecyclerView.Adapter<Following_Adapter.CustomViewHolder > {
    public Context context;

    String following_or_fans;

    ArrayList<Following_Get_Set> datalist;
    public interface OnItemClickListener {
        void onItemClick(View view, int postion, Following_Get_Set item);
    }

    public Following_Adapter.OnItemClickListener listener;

    public Following_Adapter(Context context,String following_or_fans ,ArrayList<Following_Get_Set> arrayList, Following_Adapter.OnItemClickListener listener) {
        this.context = context;
        this.following_or_fans=following_or_fans;
        datalist= arrayList;
        this.listener=listener;
    }

    @Override
    public Following_Adapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_following,viewGroup,false);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        Following_Adapter.CustomViewHolder viewHolder = new Following_Adapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
       return datalist.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        ImageView user_image;
        TextView user_name;
        TextView user_id;
        TextView action_txt;
        RelativeLayout mainlayout;

        public CustomViewHolder(View view) {
            super(view);

            mainlayout=view.findViewById(R.id.mainlayout);

            user_image=view.findViewById(R.id.user_image);
            user_name=view.findViewById(R.id.user_name);
            user_id=view.findViewById(R.id.user_id);

            action_txt=view.findViewById(R.id.action_txt);
        }

        public void bind(final int pos , final Following_Get_Set item, final Following_Adapter.OnItemClickListener listener) {



            mainlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v,pos,item);
                }
            });

            action_txt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v,pos,item);
                }
            });


        }


    }

    @Override
    public void onBindViewHolder(final Following_Adapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);

        Following_Get_Set item=datalist.get(i);

        holder.user_name.setText(item.username);

        if(item.profile_pic!=null && !item.profile_pic.equals(""))
        Picasso.get()
                .load(item.profile_pic)
                .placeholder(R.drawable.profile_image_placeholder)
                .into(holder.user_image);


        holder.user_id.setText(item.first_name+" "+item.last_name);
        holder.action_txt.setText(item.follow_status_button);

        if(item.follow_status_button !=null &&
                (item.follow_status_button.equalsIgnoreCase("follow") || item.follow_status_button.equalsIgnoreCase("follow back"))){
            holder.action_txt.setBackground(ContextCompat.getDrawable(context, R.drawable.button_rounded_background));
            holder.action_txt.setTextColor(ContextCompat.getColor(context, R.color.white));

        }


        else if(item.follow_status_button !=null &&
                (item.follow_status_button.equalsIgnoreCase("following")|| item.follow_status_button.equalsIgnoreCase("friends"))){
            holder.action_txt.setBackground(ContextCompat.getDrawable(context, R.drawable.d_gray_border));
            holder.action_txt.setTextColor(ContextCompat.getColor(context, R.color.black));

        }


        else if(item.follow_status_button !=null && item.follow_status_button.equalsIgnoreCase("0")){
            holder.action_txt.setVisibility(View.GONE);
        }



        holder.bind(i,datalist.get(i),listener);

}

}