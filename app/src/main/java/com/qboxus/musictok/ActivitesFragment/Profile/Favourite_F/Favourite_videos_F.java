package com.qboxus.musictok.ActivitesFragment.Profile.Favourite_F;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.qboxus.musictok.ActivitesFragment.WatchVideos_F;
import com.qboxus.musictok.Adapters.VideosList_Adapter;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Adapter_Click_Listener;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.Models.Home_Get_Set;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class Favourite_videos_F extends Fragment {

    View view;
    Context context;
    ShimmerFrameLayout shimmerFrameLayout;
    RecyclerView recyclerView;

    public Favourite_videos_F() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_favourite_videos, container, false);
        context = getContext();

        shimmerFrameLayout =view.findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();
        recyclerView = view.findViewById(R.id.recylerview);

        Call_Api();

        return view;
    }


    public void Call_Api() {

        JSONObject params = new JSONObject();
        try {
            params.put("user_id", Functions.getSharedPreference(context).getString(Variables.u_id, ""));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(getActivity(), ApiLinks.showFavouriteVideos, params, new Callback() {
            @Override
            public void Responce(String resp) {
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                Parse_video(resp);


            }
        });

    }


    ArrayList<Home_Get_Set> data_list;

    public void Parse_video(String responce) {

        data_list = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");
                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);
                    JSONObject VideoFavourite = itemdata.optJSONObject("VideoFavourite");
                    JSONObject Video = itemdata.optJSONObject("Video");
                    JSONObject Sound = Video.optJSONObject("Sound");
                    JSONObject User = Video.optJSONObject("User");
                    JSONObject UserPrivacy = User.optJSONObject("PrivacySetting");
                    JSONObject UserPushNotification = User.optJSONObject("PushNotification");

                    Home_Get_Set item = Functions.Parse_video_data(User, Sound, Video, UserPrivacy, UserPushNotification);

                    data_list.add(item);
                }

                if (data_list.isEmpty()) {
                    view.findViewById(R.id.no_data_layout).setVisibility(View.VISIBLE);
                } else
                    view.findViewById(R.id.no_data_layout).setVisibility(View.GONE);


                GridLayoutManager linearLayoutManager = new GridLayoutManager(context, 2);
                recyclerView.setLayoutManager(linearLayoutManager);
                VideosList_Adapter adapter = new VideosList_Adapter(context, data_list, new Adapter_Click_Listener() {
                    @Override
                    public void onItemClick(View view, int pos, Object object) {

                        Home_Get_Set item = (Home_Get_Set) object;
                        OpenWatchVideo(item.video_id);


                    }
                });
                recyclerView.setAdapter(adapter);


            } else {
                view.findViewById(R.id.no_data_layout).setVisibility(View.VISIBLE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void OpenWatchVideo(String video_id) {
        Intent intent = new Intent(getActivity(), WatchVideos_F.class);
        intent.putExtra("video_id", video_id);
        startActivity(intent);
    }


}