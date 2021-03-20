package com.qboxus.musictok.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.hendraanggrian.appcompat.widget.SocialView;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.Models.Comment_Get_Set;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by AQEEL on 3/20/2018.
 */

public class Comments_Adapter extends RecyclerView.Adapter<Comments_Adapter.CustomViewHolder> {

    public Context context;
    public Comments_Adapter.OnItemClickListener listener;
    public Comments_Adapter.onRelyItemCLickListener onRelyItemCLickListener;
    LinkClickListener linkClickListener;
    private ArrayList<Comment_Get_Set> dataList;
    public Comments_Reply_Adapter commentsReplyAdapter;



    // meker the onitemclick listener interface and this interface is impliment in Chatinbox activity
    // for to do action when user click on item

    public interface LinkClickListener {

        void onLinkClicked(SocialView view, String matchedText);
    }


    public interface OnItemClickListener {
        void onItemClick(int positon, Comment_Get_Set item, View view);
    }

    public interface onRelyItemCLickListener {
        void onItemClick(ArrayList<Comment_Get_Set> arrayList, int postion, View view);
    }

    public void notifyy() {
        commentsReplyAdapter.notifyDataSetChanged();
    }


    public void updateReceiptsList(Comment_Get_Set item) {

        commentsReplyAdapter.notifyDataSetChanged();
    }

    public Comments_Adapter(Context context, ArrayList<Comment_Get_Set> dataList, Comments_Adapter.OnItemClickListener listener, Comments_Adapter.onRelyItemCLickListener onRelyItemCLickListener, LinkClickListener linkClickListener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;
        this.linkClickListener = linkClickListener;
        this.onRelyItemCLickListener = onRelyItemCLickListener;

    }

    @Override
    public Comments_Adapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_comment_layout, null);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        Comments_Adapter.CustomViewHolder viewHolder = new Comments_Adapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    @Override
    public void onBindViewHolder(final Comments_Adapter.CustomViewHolder holder, final int i) {
        final Comment_Get_Set item = dataList.get(i);

        holder.setIsRecyclable(false);
        holder.username.setText(item.user_name);

        try {
            Picasso.get().load(item.profile_pic)
                    .resize(50, 50)
                    .placeholder(context.getResources().getDrawable(R.drawable.profile_image_placeholder))
                    .into(holder.user_pic);
        } catch (Exception e) {

        }

        if (item.liked != null && !item.equals("")) {
            if (item.liked.equals("1")) {
                holder.like_image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_like_fill));
            } else {
                holder.like_image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_heart_gray_out));
            }
        }

        holder.like_txt.setText(Functions.GetSuffix(item.like_count));

        holder.message.setText(item.comments);
        if (item.isExpand) {
            holder.less_layout.setVisibility(View.VISIBLE);
            holder.reply_count.setVisibility(View.GONE);
        }

        if (item.arrayList != null && item.arrayList.size() > 0) {
            holder.reply_count.setText("view replies (" + item.arrayList.size() + ")");
        } else {
            holder.reply_count.setVisibility(View.GONE);
        }


        holder.reply_count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.isExpand = true;
                holder.less_layout.setVisibility(View.VISIBLE);
                holder.reply_count.setVisibility(View.GONE);
            }
        });

        holder.show_less_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                item.isExpand = false;
                holder.less_layout.setVisibility(View.GONE);
                holder.reply_count.setVisibility(View.VISIBLE);
            }
        });

        commentsReplyAdapter = new Comments_Reply_Adapter(context, item.arrayList);
        holder.reply_recycler_view.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        holder.reply_recycler_view.setAdapter(commentsReplyAdapter);
        holder.reply_recycler_view.setHasFixedSize(false);
        holder.bind(i, item, listener);

    }


    class CustomViewHolder extends RecyclerView.ViewHolder {

        TextView username, message, reply_count, like_txt, show_less_txt;
        ImageView user_pic, like_image;
        LinearLayout message_layout, less_layout , like_layout;
        RecyclerView reply_recycler_view;

        public CustomViewHolder(View view) {
            super(view);

            username = view.findViewById(R.id.username);
            user_pic = view.findViewById(R.id.user_pic);
            message = view.findViewById(R.id.message);
            reply_count = view.findViewById(R.id.reply_count);
            like_image = view.findViewById(R.id.like_image);
            message_layout = view.findViewById(R.id.message_layout);
            like_txt = view.findViewById(R.id.like_txt);
            reply_recycler_view = view.findViewById(R.id.reply_recycler_view);
            less_layout = view.findViewById(R.id.less_layout);
            show_less_txt = view.findViewById(R.id.show_less_txt);
            like_layout = view.findViewById(R.id.like_layout);
        }

        public void bind(final int postion, final Comment_Get_Set item, final Comments_Adapter.OnItemClickListener listener) {

            itemView.setOnClickListener(v -> {
                listener.onItemClick(postion, item, v);
            });

            message_layout.setOnClickListener(v -> {
                listener.onItemClick(postion, item, v);
            });

            like_layout.setOnClickListener(v -> {
                listener.onItemClick(postion, item, v);
            });

        }


    }


    public class Comments_Reply_Adapter extends RecyclerView.Adapter<Comments_Reply_Adapter.CustomViewHolder> {

        public Context context;
        private ArrayList<Comment_Get_Set> dataList;

        public Comments_Reply_Adapter(Context context, ArrayList<Comment_Get_Set> dataList) {
            this.context = context;
            this.dataList = dataList;

        }

        @Override
        public Comments_Reply_Adapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_comment_reply_layout, null);
            view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            Comments_Reply_Adapter.CustomViewHolder viewHolder = new Comments_Reply_Adapter.CustomViewHolder(view);
            return viewHolder;
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }


        @Override
        public void onBindViewHolder(final Comments_Reply_Adapter.CustomViewHolder holder, final int i) {
            final Comment_Get_Set item = dataList.get(i);
            holder.setIsRecyclable(false);
            holder.username.setText(item.replay_user_name);

            try {
                Picasso.get().load(ApiLinks.BASE_URL + item.replay_user_url)
                        .resize(50, 50)
                        .placeholder(context.getResources().getDrawable(R.drawable.profile_image_placeholder))
                        .into(holder.user_pic);
            } catch (Exception e) {

            }

            holder.message.setText(item.comment_reply);


            Log.d("tictic_logged", "itemlike" + item.comment_reply_liked);
            if (item.comment_reply_liked != null && !item.comment_reply_liked.equals("")) {
                if (item.comment_reply_liked.equals("1")) {
                    holder.reply_like_image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_like_fill));
                } else {
                    holder.reply_like_image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_heart_gray_out));
                }
            }

            holder.like_txt.setText(Functions.GetSuffix(item.reply_liked_count));

            holder.message.setOnMentionClickListener(new SocialView.OnClickListener() {
                @Override
                public void onClick(@NonNull SocialView view, @NonNull CharSequence text) {
                    linkClickListener.onLinkClicked(view, text.toString());
                }
            });


            holder.bind(i, dataList, onRelyItemCLickListener);

        }


        class CustomViewHolder extends RecyclerView.ViewHolder {

            TextView username, like_txt;
            SocialTextView message;
            ImageView user_pic, reply_like_image;
            LinearLayout reply_layout ,like_layout;

            public CustomViewHolder(View view) {
                super(view);

                username = view.findViewById(R.id.username);
                user_pic = view.findViewById(R.id.user_pic);
                message = view.findViewById(R.id.message);
                reply_layout = view.findViewById(R.id.reply_layout);
                reply_like_image = view.findViewById(R.id.reply_like_image);
                like_layout = view.findViewById(R.id.like_layout);
                like_txt = view.findViewById(R.id.like_txt);
            }

            public void bind(final int postion, ArrayList<Comment_Get_Set> datalist, final Comments_Adapter.onRelyItemCLickListener listener) {

                itemView.setOnClickListener(v -> {
                    Comments_Adapter.this.onRelyItemCLickListener.onItemClick(datalist, postion, v);
                });

                reply_layout.setOnClickListener(v -> {
                    Comments_Adapter.this.onRelyItemCLickListener.onItemClick(datalist, postion, v);
                });

                like_layout.setOnClickListener(v -> {
                    Comments_Adapter.this.onRelyItemCLickListener.onItemClick(datalist, postion, v);
                });
            }
        }
    }

    public void updateReceiptsList() {
        this.notifyDataSetChanged();
    }

}