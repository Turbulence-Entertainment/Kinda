package com.qboxus.musictok.Adapters;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.qboxus.musictok.Models.Home_Get_Set;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.Adapter_Click_Listener;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import java.util.ArrayList;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by AQEEL on 3/20/2018.
 */

public class MyVideos_Adapter extends RecyclerView.Adapter<MyVideos_Adapter.CustomViewHolder > {

    public Context context;
    private ArrayList<Home_Get_Set> dataList;

    Adapter_Click_Listener adapter_click_listener;
    public MyVideos_Adapter(Context context, ArrayList<Home_Get_Set> dataList, Adapter_Click_Listener adapter_click_listener) {
        this.context = context;
        this.dataList = dataList;
        this.adapter_click_listener = adapter_click_listener;

    }

    @Override
    public MyVideos_Adapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_myvideo_layout,null);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        MyVideos_Adapter.CustomViewHolder viewHolder = new MyVideos_Adapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
       return dataList.size();
    }



    class CustomViewHolder extends RecyclerView.ViewHolder {


        ImageView thumb_image;

        TextView view_txt;

        public CustomViewHolder(View view) {
            super(view);

            thumb_image=view.findViewById(R.id.thumb_image);
            view_txt=view.findViewById(R.id.view_txt);

        }

        public void bind(final int position,final Home_Get_Set item, final Adapter_Click_Listener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter_click_listener.onItemClick(v,position,item);
                }
            });

        }

    }




    @Override
    public void onBindViewHolder(final MyVideos_Adapter.CustomViewHolder holder, final int i) {
        final Home_Get_Set item= dataList.get(i);
        holder.setIsRecyclable(false);


        try {

            if(Variables.is_show_gif) {
                Glide.with(context)
                        .asGif()
                        .load(item.gif)
                        .skipMemoryCache(true)
                        .thumbnail(new RequestBuilder[]{Glide
                                .with(context)
                                .load(item.thum)})
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE)
                                .placeholder(context.getResources().getDrawable(R.drawable.image_placeholder)).centerCrop())
                        .into(holder.thumb_image);
            }
            else {
                if(item.thum!=null && !item.thum.equals("")) {
                    Uri uri = Uri.parse(item.thum);
                    holder.thumb_image.setImageURI(uri);
                }
            }
        }
        catch (Exception e){
            Log.d(Variables.tag,e.toString());
        }



        holder.view_txt.setText(item.views);
        holder.view_txt.setText(Functions.GetSuffix(item.views));



        holder.bind(i,item,adapter_click_listener);

   }

}