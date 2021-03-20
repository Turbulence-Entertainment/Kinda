package com.qboxus.musictok.Adapters;

import android.content.Context;
import android.net.Uri;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.viewpager.widget.PagerAdapter;

import com.facebook.drawee.view.SimpleDraweeView;
import com.qboxus.musictok.Models.SliderModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.Adapter_Click_Listener;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.SimpleClasses.Variables;

import java.util.ArrayList;


public class Sliding_Adapter extends PagerAdapter {


    private ArrayList<SliderModel> imageList;
    private LayoutInflater inflater;
    private Context context;

    Adapter_Click_Listener adapter_click_listener;

    public Sliding_Adapter(Context context, ArrayList<SliderModel> IMAGES, Adapter_Click_Listener click_listener) {
        this.context = context;
        this.imageList=IMAGES;
        this.adapter_click_listener=click_listener;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, final int position) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View imageLayout = inflater.inflate(R.layout.item_slider_layout, view, false);

        assert imageLayout != null;

        final SimpleDraweeView imageView = imageLayout.findViewById(R.id.save_image);
        final RelativeLayout slider_rlt = imageLayout.findViewById(R.id.slider_rlt);

        String url = imageList.get(position).image;
        if (url != null) {
            String imageUrl = ApiLinks.BASE_URL + url;
            Uri uri = Uri.parse(imageUrl);
            imageView.setImageURI(uri);

            Log.d(Variables.tag,imageUrl);
        }

        slider_rlt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter_click_listener.onItemClick(v,position,imageList.get(position));
            }
        });

        view.addView(imageLayout, 0);




        return imageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }


}
