package com.qboxus.musictok.ActivitesFragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;

import com.qboxus.musictok.Adapters.Notification_Adapter;
import com.qboxus.musictok.Main_Menu.MainMenuFragment;
import com.qboxus.musictok.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.qboxus.musictok.Models.Notification_Get_Set;
import com.qboxus.musictok.ActivitesFragment.Profile.Profile_F;
import com.qboxus.musictok.R;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Notification_F extends RootFragment implements View.OnClickListener {

    View view;
    Context context;

    Notification_Adapter adapter;
    RecyclerView recyclerView;
    ArrayList<Notification_Get_Set> datalist;
    SwipeRefreshLayout swiperefresh;


    int page_count = 0;
    boolean ispost_finsh;

    ProgressBar load_more_progress;
    LinearLayoutManager linearLayoutManager;

    public Notification_F() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_notification, container, false);
        context = getContext();


        datalist = new ArrayList<>();


        recyclerView = (RecyclerView) view.findViewById(R.id.recylerview);
        linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);


        adapter = new Notification_Adapter(context, datalist, new Notification_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, Notification_Get_Set item) {

                switch (view.getId()) {
                    case R.id.watch_btn:
                        OpenWatchVideo(item);
                        break;
                    default:
                        Open_Profile(item);
                        break;
                }
            }
        });

        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean userScrolled;
            int scrollOutitems;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                scrollOutitems = linearLayoutManager.findLastVisibleItemPosition();

                Log.d("resp", "" + scrollOutitems);
                if (userScrolled && (scrollOutitems == datalist.size() - 1)) {
                    userScrolled = false;

                    if (load_more_progress.getVisibility() != View.VISIBLE && !ispost_finsh) {
                        load_more_progress.setVisibility(View.VISIBLE);
                        page_count = page_count + 1;
                        Call_api();
                    }
                }


            }
        });

        load_more_progress = view.findViewById(R.id.load_more_progress);


        view.findViewById(R.id.inbox_btn).setOnClickListener(this);
        swiperefresh = view.findViewById(R.id.swiperefresh);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page_count = 0;
                Call_api();
            }
        });


        return view;
    }


    AdView adView;

    @Override
    public void onStart() {
        super.onStart();
        adView = view.findViewById(R.id.bannerad);
        if (!Variables.is_remove_ads) {
            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
        } else {
            adView.setVisibility(View.GONE);
        }
    }


    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if ((view != null && (page_count == 0 && visible)) || Variables.Reload_my_notification) {
            Variables.Reload_my_notification = false;

            if (datalist != null && datalist.isEmpty())
                swiperefresh.setRefreshing(true);

            page_count = 0;
            Call_api();
        }
    }


    public void Call_api() {

        if(datalist==null)
            datalist=new ArrayList<>();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("user_id", Variables.user_id);
            jsonObject.put("starting_point", "" + page_count);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(getActivity(), ApiLinks.showAllNotifications, jsonObject, new Callback() {
            @Override
            public void Responce(String resp) {
                swiperefresh.setRefreshing(false);
                parse_data(resp);
            }
        });

    }


    public void parse_data(String resp) {
        try {
            JSONObject jsonObject = new JSONObject(resp);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msg = jsonObject.getJSONArray("msg");
                ArrayList<Notification_Get_Set> temp_list = new ArrayList<>();

                for (int i = 0; i < msg.length(); i++) {
                    JSONObject data = msg.getJSONObject(i);

                    JSONObject Notification = data.optJSONObject("Notification");
                    JSONObject Video = data.optJSONObject("Video");
                    JSONObject Sender = data.optJSONObject("Sender");
                    JSONObject Receiver = data.optJSONObject("Receiver");

                    Notification_Get_Set item = new Notification_Get_Set();

                    item.id = Notification.optString("id");

                    item.user_id = Sender.optString("id");
                    item.username = Sender.optString("username");
                    item.first_name = Sender.optString("first_name");
                    item.last_name = Sender.optString("last_name");

                    item.profile_pic = Sender.optString("profile_pic", "");
                    if (!item.profile_pic.contains(Variables.http)) {
                        item.profile_pic = ApiLinks.BASE_URL + item.profile_pic;
                    }

                    item.effected_fb_id = Receiver.optString("id");

                    item.type = Notification.optString("type");

                    if (item.type.equalsIgnoreCase("video_comment") || item.type.equalsIgnoreCase("video_like")) {

                        item.video_id = Video.optString("id");
                        item.video = Video.optString("video");
                        item.thum = Video.optString("thum");
                        item.gif = Video.optString("gif");

                    }

                    item.string = Notification.optString("string");
                    item.created = Notification.optString("created");

                    temp_list.add(item);


                }

                if (page_count == 0) {
                    datalist.clear();
                    datalist.addAll(temp_list);
                }
                else {
                    datalist.addAll(temp_list);
                }

                adapter.notifyDataSetChanged();

            }

            if (datalist.isEmpty()) {
                view.findViewById(R.id.no_data_layout).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.no_data_layout).setVisibility(View.GONE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            load_more_progress.setVisibility(View.GONE);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.inbox_btn:
                Open_inbox_F();
                break;
        }
    }


    private void Open_inbox_F() {
        Inbox_F inbox_f = new Inbox_F();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, inbox_f).commit();

    }


    private void OpenWatchVideo(Notification_Get_Set item) {
        Intent intent = new Intent(getActivity(), WatchVideos_F.class);
        intent.putExtra("video_id", item.video_id);
        startActivity(intent);
    }


    public void Open_Profile(Notification_Get_Set item) {
        if (Functions.getSharedPreference(context).getString(Variables.u_id, "0").equals(item.user_id)) {

            TabLayout.Tab profile = MainMenuFragment.tabLayout.getTabAt(4);
            profile.select();

        } else {

            Profile_F profile_f = new Profile_F();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
            Bundle args = new Bundle();
            args.putString("user_id", item.user_id);
            args.putString("user_name", item.username);
            args.putString("user_pic", item.profile_pic);
            profile_f.setArguments(args);
            transaction.addToBackStack(null);
            transaction.replace(R.id.MainMenuFragment, profile_f).commit();

        }

    }


}
