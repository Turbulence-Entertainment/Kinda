package com.qboxus.musictok.Adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.hendraanggrian.appcompat.widget.SocialView;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.qboxus.musictok.ActivitesFragment.Accounts.Login_A;
import com.qboxus.musictok.Models.Home_Get_Set;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by AQEEL on 3/20/2018.
 */

public class Home_Adapter extends RecyclerView.Adapter<Home_Adapter.CustomViewHolder> {

    public Context context;
    private Home_Adapter.OnItemClickListener listener;
    private Home_Adapter.LikedClicked liked_listener;
    private ArrayList<Home_Get_Set> dataList;
    Boolean is_loggedIn = false;

    public interface LikedClicked {
        void like_clicked(View view, Home_Get_Set item, int position);
    }


    // meker the onitemclick listener interface and this interface is impliment in Chatinbox activity
    // for to do action when user click on item
    public interface OnItemClickListener {
        void onItemClick(int positon, Home_Get_Set item, View view);
    }


    public Home_Adapter(Context context, ArrayList<Home_Get_Set> dataList, Home_Adapter.OnItemClickListener listener, LikedClicked liked_listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;
        this.liked_listener = liked_listener;

    }

    @Override
    public Home_Adapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_home_layout, null);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));
        Home_Adapter.CustomViewHolder viewHolder = new Home_Adapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    @Override
    public void onBindViewHolder(final Home_Adapter.CustomViewHolder holder, final int i) {
        final Home_Get_Set item = dataList.get(i);
        holder.setIsRecyclable(false);
        try {

            holder.bind(i, item, listener);

            holder.username.setText(Functions.show_username(item.username));


            if ((item.sound_name == null || item.sound_name.equals("") || item.sound_name.equals("null"))) {
                holder.sound_name.setText("original sound - " + item.username);
            } else {
                holder.sound_name.setText(item.sound_name);
            }
            holder.sound_name.setSelected(true);


            holder.desc_txt.setText(item.video_description);

            if (item.profile_pic != null && !item.profile_pic.equals("")) {
                Picasso.get().
                        load(item.profile_pic)
                        .centerCrop()
                        .placeholder(context.getResources().getDrawable(R.drawable.profile_image_placeholder))
                        .resize(100, 100).into(holder.user_pic);
            }


            if ((item.sound_name == null || item.sound_name.equals(""))
                    || item.sound_name.equals("null")) {

                item.sound_pic = item.profile_pic;

            } else if (item.sound_pic != null && !item.sound_pic.equals("")) {
                Picasso.get().
                        load(item.sound_pic).centerCrop()
                        .placeholder(context.getResources().getDrawable(R.drawable.ic_round_music))
                        .resize(100, 100).into(holder.sound_image);
            }


            if (item.liked.equals("1")) {
                holder.like_image.setLikeDrawable(context.getResources().getDrawable(R.drawable.ic_heart_gradient));
                holder.like_image.setLiked(true);
            } else {
                //holder.like_image.animate().cancel();
                holder.like_image.setLikeDrawable(context.getResources().getDrawable(R.drawable.ic_unliked));
                holder.like_image.setLiked(false);
                holder.like_image.animate().cancel();
            }

            if (item.allow_comments != null && item.allow_comments.equalsIgnoreCase("false")) {
                holder.comment_layout.setVisibility(View.GONE);
            } else {
                holder.comment_layout.setVisibility(View.VISIBLE);
            }
            holder.like_txt.setText(Functions.GetSuffix(item.like_count));
            holder.comment_txt.setText(Functions.GetSuffix(item.video_comment_count));


            if (item.verified != null && item.verified.equalsIgnoreCase("1")) {
                holder.varified_btn.setVisibility(View.VISIBLE);
            } else {
                holder.varified_btn.setVisibility(View.GONE);
            }

            if (item.duet_video_id != null && !item.duet_video_id.equals("") && !item.duet_video_id.equals("0")) {
                holder.duet_layout_username.setVisibility(View.VISIBLE);
                holder.duet_username.setText(item.duet_username);
            }

            if (Functions.getSharedPreference(context).getBoolean(Variables.islogin, false)) {
                holder.animate_rlt.setVisibility(View.GONE);
                holder.like_image.setOnLikeListener(new OnLikeListener() {
                    @Override
                    public void liked(LikeButton likeButton) {
                        liked_listener.like_clicked(likeButton, dataList.get(i), i);
                    }

                    @Override
                    public void unLiked(LikeButton likeButton) {
                        liked_listener.like_clicked(likeButton, dataList.get(i), i);
                    }
                });
            }

        } catch (Exception e) {
            Log.d(Variables.tag, e.toString());
        }


    }


    class CustomViewHolder extends RecyclerView.ViewHolder {

        TextView username, desc_txt, sound_name;
        ImageView user_pic, sound_image, varified_btn;
        RelativeLayout duet_layout_username, animate_rlt;
        LinearLayout duet_open_video;
        LinearLayout like_layout, comment_layout, shared_layout, sound_image_layout;
        LikeButton like_image;
        ImageView comment_image;
        TextView like_txt, comment_txt, duet_username;

        public CustomViewHolder(View view) {
            super(view);

            duet_layout_username = view.findViewById(R.id.duet_layout_username);
            duet_username = view.findViewById(R.id.duet_username);
            duet_open_video = view.findViewById(R.id.duet_open_video);
            username = view.findViewById(R.id.username);
            user_pic = view.findViewById(R.id.user_pic);
            sound_name = view.findViewById(R.id.sound_name);
            sound_image = view.findViewById(R.id.sound_image);
            varified_btn = view.findViewById(R.id.varified_btn);
            like_layout = view.findViewById(R.id.like_layout);
            like_image = view.findViewById(R.id.likebtn);
            like_txt = view.findViewById(R.id.like_txt);
            animate_rlt = view.findViewById(R.id.animate_rlt);


            desc_txt = view.findViewById(R.id.desc_txt);

            comment_layout = view.findViewById(R.id.comment_layout);
            comment_image = view.findViewById(R.id.comment_image);
            comment_txt = view.findViewById(R.id.comment_txt);


            sound_image_layout = view.findViewById(R.id.sound_image_layout);
            shared_layout = view.findViewById(R.id.shared_layout);
        }

        public void bind(final int postion, final Home_Get_Set item, final Home_Adapter.OnItemClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(postion, item, v);
                }
            });

            duet_open_video.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    listener.onItemClick(postion, item, v);
                }
            });

            user_pic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    listener.onItemClick(postion, item, v);
                }
            });

            animate_rlt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(postion, item, v);
                }
            });

            username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    listener.onItemClick(postion, item, v);
                }
            });

            username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    listener.onItemClick(postion, item, v);
                }
            });
            comment_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    listener.onItemClick(postion, item, v);
                }
            });

            shared_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    listener.onItemClick(postion, item, v);
                }
            });

            sound_image_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(postion, item, v);
                }
            });


        }


    }


}