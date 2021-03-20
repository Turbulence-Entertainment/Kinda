package com.qboxus.musictok.ActivitesFragment;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qboxus.musictok.Models.Home_Get_Set;
import com.qboxus.musictok.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.qboxus.musictok.Adapters.MyVideos_Adapter;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.Adapter_Click_Listener;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class Taged_Videos_F extends RootFragment implements View.OnClickListener {

    View view;
    Context context;

    NestedScrollView scrollView;
    RelativeLayout recylerview_main_layout;

    RelativeLayout top_layout;

    RecyclerView recyclerView;
    ArrayList<Home_Get_Set> data_list;
    MyVideos_Adapter adapter;

    String tag_id,tag_txt,favourite="0";

    TextView tag_txt_view,tag_title_txt;
    ProgressBar progress_bar;

    ImageButton fav_btn;

    public Taged_Videos_F() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_taged_videos, container, false);
        context=getContext();

        if(Variables.sharedPreferences==null){
            Variables.sharedPreferences=getActivity().getSharedPreferences(Variables.pref_name,Context.MODE_PRIVATE);
        }


        Bundle bundle=getArguments();
        if(bundle!=null){
            tag_txt=bundle.getString("tag");
        }


        tag_txt_view=view.findViewById(R.id.tag_txt_view);
        tag_title_txt=view.findViewById(R.id.tag_title_txt);

        tag_txt_view.setText(tag_txt);
        tag_title_txt.setText(tag_txt);

        fav_btn=view.findViewById(R.id.fav_btn);
        fav_btn.setOnClickListener(this);

        recyclerView=view.findViewById(R.id.recylerview);
        scrollView=view.findViewById(R.id.scrollview);


        top_layout=view.findViewById(R.id.top_layout);
        recylerview_main_layout=view.findViewById(R.id.recylerview_main_layout);




        ViewTreeObserver observer = top_layout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {

                final int height=top_layout.getMeasuredHeight();

                top_layout.getViewTreeObserver().removeGlobalOnLayoutListener(
                        this);

                ViewTreeObserver observer = recylerview_main_layout.getViewTreeObserver();
                observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        // TODO Auto-generated method stub
                        RelativeLayout.LayoutParams params= (RelativeLayout.LayoutParams) recylerview_main_layout.getLayoutParams();
                        params.height= (int) (recylerview_main_layout.getMeasuredHeight()+ height);
                        recylerview_main_layout.setLayoutParams(params);
                        recylerview_main_layout.getViewTreeObserver().removeGlobalOnLayoutListener(
                                this);

                    }
                });

            }
        });





        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                    if (!scrollView.canScrollVertically(1)) {
                        recyclerView.setNestedScrollingEnabled(true);


                    }else {
                        recyclerView.setNestedScrollingEnabled(false);
                    }

                }
            });
        }



        recyclerView=view.findViewById(R.id.recylerview);
        final GridLayoutManager layoutManager = new GridLayoutManager(context,3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            recyclerView.setNestedScrollingEnabled(false);
        }else {
            recyclerView.setNestedScrollingEnabled(true);
        }

        data_list=new ArrayList<>();
        adapter=new MyVideos_Adapter(context, data_list,  new Adapter_Click_Listener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                Home_Get_Set item=(Home_Get_Set) object;
                OpenWatchVideo(pos);
            }
        });

        recyclerView.setAdapter(adapter);



        progress_bar=view.findViewById(R.id.progress_bar);

        view.findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        Call_Api_For_get_Allvideos();




        return view;
    }


    //this will get the all videos data of user and then parse the data
    private void Call_Api_For_get_Allvideos() {
        progress_bar.setVisibility(View.VISIBLE);
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Variables.sharedPreferences.getString(Variables.u_id,""));
            parameters.put("hashtag", tag_txt);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(getActivity(), ApiLinks.showVideosAgainstHashtag, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                progress_bar.setVisibility(View.GONE);
                Parse_data(resp);
            }
        });


    }

    public void Parse_data(String responce){

        data_list.clear();

        try {
            JSONObject jsonObject=new JSONObject(responce);
            String code=jsonObject.optString("code");
            if(code.equals("200")){
                JSONArray msgArray=jsonObject.getJSONArray("msg");



                    for (int i=0;i<msgArray.length();i++) {
                        JSONObject itemdata = msgArray.optJSONObject(i);
                        JSONObject Hashtag=itemdata.optJSONObject("Hashtag");
                        tag_id=Hashtag.optString("id");
                        favourite=Hashtag.optString("favourite");


                        JSONObject Video=itemdata.optJSONObject("Video");
                        JSONObject User=Video.optJSONObject("User");
                        JSONObject Sound=Video.optJSONObject("Sound");
                        JSONObject UserPrivacy=User.optJSONObject("PrivacySetting");
                        JSONObject UserPushNotification=User.optJSONObject("PushNotification");

                        Home_Get_Set item=Functions.Parse_video_data(User,Sound,Video,UserPrivacy,UserPushNotification);

                        data_list.add(item);
                    }


                if(favourite!=null && favourite.equalsIgnoreCase("1")){
                    fav_btn.setBackgroundResource(R.drawable.ic_my_favourite);
                }

                adapter.notifyDataSetChanged();
                progress_bar.setVisibility(View.GONE);

            }else {
                progress_bar.setVisibility(View.GONE);
                Functions.show_toast(getActivity(),jsonObject.optString("msg"));
            }

        } catch (JSONException e) {
            progress_bar.setVisibility(View.GONE);
            e.printStackTrace();
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        Functions.deleteCache(context);
    }

    private void OpenWatchVideo(int postion) {
        Intent intent=new Intent(getActivity(), WatchVideos_F.class);
        intent.putExtra("arraylist", data_list);
        intent.putExtra("position",postion);
        startActivity(intent);
    }


    public void Call_api_fav_hashtag(){

        JSONObject params=new JSONObject();
        try {
            params.put("user_id",Variables.sharedPreferences.getString(Variables.u_id,""));
            params.put("hashtag_id",tag_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.Show_loader(context,false,false);
        ApiRequest.Call_Api(getActivity(), ApiLinks.addHashtagFavourite, params, new Callback() {
            @Override
            public void Responce(String resp) {
                Functions.cancel_loader();

                if(favourite.equalsIgnoreCase("0")){
                    favourite="1";
                    fav_btn.setBackgroundResource(R.drawable.ic_my_favourite);
                }
                else {
                    favourite="0";
                    fav_btn.setBackgroundResource(R.drawable.ic_my_un_favourite);
                }

            }
        });

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.fav_btn:
                Call_api_fav_hashtag();
                break;
        }
    }
}
