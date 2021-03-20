package com.qboxus.musictok.ActivitesFragment.Chat.ViewHolders;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.qboxus.musictok.ActivitesFragment.Chat.ChatAdapter;
import com.qboxus.musictok.ActivitesFragment.Chat.Chat_GetSet;
import com.qboxus.musictok.R;

public class  Chatimageviewholder extends RecyclerView.ViewHolder {
    public ImageView chatimage;
    public  TextView datetxt,message_seen;
    public  ProgressBar p_bar;
    public  ImageView not_send_message_icon;
    View view;
    public Chatimageviewholder(View itemView) {
        super(itemView);
        view = itemView;
        this.chatimage = view.findViewById(R.id.chatimage);
        this.datetxt=view.findViewById(R.id.datetxt);
        message_seen=view.findViewById(R.id.message_seen);
        not_send_message_icon=view.findViewById(R.id.not_send_messsage);
        p_bar=view.findViewById(R.id.p_bar);
    }

    public void bind(final Chat_GetSet item, int position , final ChatAdapter.OnItemClickListener listener, final ChatAdapter.OnLongClickListener long_listener) {

        chatimage.setOnClickListener(v ->  {
                listener.onItemClick(item,v,position);

        });

        chatimage.setOnLongClickListener(v ->  {
                long_listener.onLongclick(item,v);
                return false;
        });
    }

}