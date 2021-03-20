package com.qboxus.musictok.ActivitesFragment.Chat.ViewHolders;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.qboxus.musictok.ActivitesFragment.Chat.ChatAdapter;
import com.qboxus.musictok.ActivitesFragment.Chat.Chat_GetSet;
import com.qboxus.musictok.R;

public class Chataudioviewholder extends RecyclerView.ViewHolder{
    public TextView datetxt,message_seen;
    public  ProgressBar p_bar;
    public  ImageView not_send_message_icon;
    public ImageView play_btn;
    public SeekBar seekBar;
    public TextView total_time;
    public LinearLayout audio_bubble;


    View view;
    public Chataudioviewholder(View itemView) {
        super(itemView);
        view = itemView;
        audio_bubble=view.findViewById(R.id.audio_bubble);
        datetxt=view.findViewById(R.id.datetxt);
        message_seen=view.findViewById(R.id.message_seen);
        not_send_message_icon=view.findViewById(R.id.not_send_messsage);
        p_bar=view.findViewById(R.id.p_bar);
        this.play_btn=view.findViewById(R.id.play_btn);
        this.seekBar=(SeekBar) view.findViewById(R.id.seek_bar);
        this.total_time=(TextView)view.findViewById(R.id.total_time);

    }

    public void bind(final Chat_GetSet item, int position , final ChatAdapter.OnItemClickListener listener, final ChatAdapter.OnLongClickListener long_listener) {



        audio_bubble.setOnClickListener( v ->  {
                listener.onItemClick(item,v,position);

        });

        audio_bubble.setOnLongClickListener(v -> {
                long_listener.onLongclick(item,v);
                return false;

        });

        seekBar.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

    }


}
