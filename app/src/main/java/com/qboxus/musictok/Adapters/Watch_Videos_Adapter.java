package com.qboxus.musictok.Adapters;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.qboxus.musictok.ActivitesFragment.WatchVideos_F;
import com.qboxus.musictok.Models.Home_Get_Set;
import com.qboxus.musictok.R;
import com.google.android.exoplayer2.ui.PlayerView;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by AQEEL on 3/20/2018.
 */

public class Watch_Videos_Adapter extends RecyclerView.Adapter<Watch_Videos_Adapter.CustomViewHolder> {

    public Context context;
    private Watch_Videos_Adapter.OnItemClickListener listener;
    private ArrayList<Home_Get_Set> dataList;
    private Watch_Videos_Adapter.LikedClicked liked_listener;

    // meker the onitemclick listener interface and this interface is impliment in Chatinbox activity
    // for to do action when user click on item
    public interface OnItemClickListener {
        void onItemClick(int positon, Home_Get_Set item, View view);
    }

    public interface LikedClicked {
        void like_clicked(View view , Home_Get_Set item, int position);
    }

    public Watch_Videos_Adapter(Context context, ArrayList<Home_Get_Set> dataList, Watch_Videos_Adapter.OnItemClickListener listener, Watch_Videos_Adapter.LikedClicked liked_listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;
        this.liked_listener = liked_listener;

    }

    @Override
    public Watch_Videos_Adapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_watch_layout, null);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));
        Watch_Videos_Adapter.CustomViewHolder viewHolder = new Watch_Videos_Adapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    @Override
    public void onBindViewHolder(final Watch_Videos_Adapter.CustomViewHolder holder, final int i) {
        final Home_Get_Set item = dataList.get(i);
        try {

            holder.bind(i, item, listener);

            holder.username.setText(Functions.show_username(item.username));

            if ((item.sound_name == null || item.sound_name.equals("") || item.sound_name.equals("null"))) {
                holder.sound_name.setText("original sound - " + item.username);
            } else {
                holder.sound_name.setText(item.sound_name);
            }

            holder.sound_name.setSelected(true);
            holder.desc_txt.setText("" + item.video_description);

            if (item.sound_id != null && (item.sound_id.equals("") && item.sound_id.equals("0"))) {
                holder.sound_image_layout.setVisibility(View.GONE);
            } else {
                holder.sound_image_layout.setVisibility(View.VISIBLE);
                if (item.profile_pic != null && !item.profile_pic.equals(""))
                    Picasso.get().
                            load(item.profile_pic)
                            .placeholder(context.getResources().getDrawable(R.drawable.profile_image_placeholder))
                            .resize(100, 100).into(holder.user_pic);

            }


            if ((item.sound_name == null || item.sound_name.equals(""))
                    || item.sound_name.equals("null")) {

                item.sound_pic = item.profile_pic;

            } else if (item.sound_pic.equals(""))
                item.sound_pic = "Null";


            if (item.sound_pic != null && !item.sound_pic.equals(""))
                Picasso.get().
                        load(item.sound_pic)
                        .placeholder(context.getResources().getDrawable(R.drawable.ic_round_music))
                        .resize(100, 100).into(holder.sound_image);




            if (item.liked.equals("1")) {
                holder.like_image.setLikeDrawable(context.getResources().getDrawable(R.drawable.ic_heart_gradient));
                holder.like_image.setLiked(true);
            } else {
                holder.like_image.setLikeDrawable(context.getResources().getDrawable(R.drawable.ic_unliked));
                holder.like_image.setLiked(false);
            }


            if (item.allow_comments != null && item.allow_comments.equalsIgnoreCase("false"))
                holder.comment_layout.setVisibility(View.GONE);
            else
                holder.comment_layout.setVisibility(View.VISIBLE);


            Log.d(Variables.tag, item.like_count + "----" + item.video_comment_count);
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
            if (item.privacy_type.equals("private")) {
                holder.video_privacy_type.setText(item.privacy_type);
                holder.private_video_layout.setVisibility(View.VISIBLE);
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

        PlayerView playerview;
        TextView username, sound_name , video_privacy_type;
        ImageView user_pic, sound_image, varified_btn;
        RelativeLayout duet_layout_username , animate_rlt;
        LinearLayout like_layout, comment_layout, shared_layout, sound_image_layout;
        LikeButton like_image ;
        ImageView comment_image;
        TextView like_txt, comment_txt, duet_username;
        SocialTextView desc_txt;
        LinearLayout duet_open_video, private_video_layout;


        public CustomViewHolder(View view) {
            super(view);

            playerview = view.findViewById(R.id.playerview);
            duet_open_video = view.findViewById(R.id.duet_open_video);
            username = view.findViewById(R.id.username);
            user_pic = view.findViewById(R.id.user_pic);
            sound_name = view.findViewById(R.id.sound_name);
            sound_image = view.findViewById(R.id.sound_image);
            varified_btn = view.findViewById(R.id.varified_btn);
            duet_username = view.findViewById(R.id.duet_username);
            like_layout = view.findViewById(R.id.like_layout);
            like_image = view.findViewById(R.id.likebtn);
            like_txt = view.findViewById(R.id.like_txt);
            private_video_layout = view.findViewById(R.id.private_video_layout);
            animate_rlt = view.findViewById(R.id.animate_rlt);
            duet_layout_username = view.findViewById(R.id.duet_layout_username);

            comment_layout = view.findViewById(R.id.comment_layout);
            comment_image = view.findViewById(R.id.comment_image);
            comment_txt = view.findViewById(R.id.comment_txt);

            desc_txt = view.findViewById(R.id.desc_txt);
            video_privacy_type = view.findViewById(R.id.video_privacy_type);

            sound_image_layout = view.findViewById(R.id.sound_image_layout);
            shared_layout = view.findViewById(R.id.shared_layout);
        }

        public void bind(final int postion, final Home_Get_Set item, final Watch_Videos_Adapter.OnItemClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
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

            duet_open_video.setOnClickListener(new View.OnClickListener() {
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

            animate_rlt.setOnClickListener(new View.OnClickListener() {
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