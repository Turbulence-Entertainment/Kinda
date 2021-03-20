package com.qboxus.musictok.ActivitesFragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.qboxus.musictok.Adapters.Discover_Adapter;
import com.qboxus.musictok.Adapters.Sliding_Adapter;
import com.qboxus.musictok.Models.Discover_Get_Set;
import com.qboxus.musictok.Models.Home_Get_Set;
import com.qboxus.musictok.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.qboxus.musictok.Models.SliderModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.ActivitesFragment.Search.Search_Main_F;
import com.qboxus.musictok.Interfaces.Adapter_Click_Listener;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.TouchSafeSwipeRefreshLayout;
import com.rd.PageIndicatorView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class Discover_F extends RootFragment implements View.OnClickListener {

    View view;
    Context context;

    RecyclerView recyclerView;
    EditText search_edit;


    TouchSafeSwipeRefreshLayout swiperefresh;

    public Discover_F() {
        // Required empty public constructor
    }

    ArrayList<Discover_Get_Set> datalist;
    Discover_Adapter adapter;


    PageIndicatorView pageIndicatorView;
    ViewPager viewPager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_discover, container, false);
        context = getContext();

       // Functions.white_status_bar(getActivity());

        datalist = new ArrayList<>();


        recyclerView = (RecyclerView) view.findViewById(R.id.recylerview);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        adapter = new Discover_Adapter(context, datalist, new Discover_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(ArrayList<Home_Get_Set> datalist, int postion) {

                OpenWatchVideo(postion, datalist);

            }
        });
        recyclerView.setAdapter(adapter);


        search_edit = view.findViewById(R.id.search_edit);
        search_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                // this method call before text change

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String query = search_edit.getText().toString();
                if (adapter != null)
                    adapter.getFilter().filter(query);

            }

            @Override
            public void afterTextChanged(Editable s) {
                // this method call after text change
            }
        });


        viewPager = view.findViewById(R.id.viewPager);
        pageIndicatorView = view.findViewById(R.id.pageIndicatorView);
        swiperefresh = view.findViewById(R.id.swiperefresh);
        swiperefresh.setColorSchemeResources(R.color.black);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                Call_api_for_get_slider();
                Call_Api_For_get_Allvideos();
            }
        });

        view.findViewById(R.id.search_layout).setOnClickListener(this);
        view.findViewById(R.id.search_edit).setOnClickListener(this);

        Call_Api_For_get_Allvideos();
        Call_api_for_get_slider();
        return view;
    }


    private void Call_api_for_get_slider() {

        ApiRequest.Call_Api(getActivity(), ApiLinks.showAppSlider, new JSONObject(), new Callback() {
            @Override
            public void Responce(String resp) {
                Parse_Slider_data(resp);
            }
        });

    }


    private void enableDisableSwipeRefresh(boolean enable) {
        if (swiperefresh != null) {
            swiperefresh.setEnabled(enable);
        }
    }

    ArrayList<SliderModel> slider_list = new ArrayList<>();

    public void Parse_Slider_data(String resp) {
        try {
            JSONObject jsonObject = new JSONObject(resp);

            String code = jsonObject.optString("code");
            if (code.equals("200")) {

                slider_list.clear();

                JSONArray msg = jsonObject.optJSONArray("msg");
                for (int i = 0; i < msg.length(); i++) {
                    JSONObject object = msg.optJSONObject(i);
                    JSONObject AppSlider = object.optJSONObject("AppSlider");

                    SliderModel sliderModel = new SliderModel();
                    sliderModel.id = AppSlider.optString("id");
                    sliderModel.image = AppSlider.optString("image");
                    sliderModel.url = AppSlider.optString("url");

                    slider_list.add(sliderModel);
                }

                Set_slider_adapter();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void Set_slider_adapter() {

        pageIndicatorView.setCount(slider_list.size());
        pageIndicatorView.setSelection(0);

        viewPager.setAdapter(new Sliding_Adapter(getActivity(), slider_list, new Adapter_Click_Listener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                String slider_url = slider_list.get(pos).url;
                if (slider_url != null && !slider_url.equals("")) {
                    Webview_F webview_f = new Webview_F();
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
                    Bundle bundle = new Bundle();
                    bundle.putString("url", slider_url);
                    bundle.putString("title", "Link");
                    webview_f.setArguments(bundle);
                    transaction.addToBackStack(null);
                    transaction.replace(R.id.discovery_container, webview_f).commit();
                }
            }
        }));


        pageIndicatorView.setViewPager(viewPager);


    }

    // Bottom two function will get the Discover videos
    // from api and parse the json data which is shown in Discover tab

    private void Call_Api_For_get_Allvideos() {

        JSONObject parameters = new JSONObject();

        ApiRequest.Call_Api(getActivity(), ApiLinks.showDiscoverySections, parameters, new Callback() {
            @Override
            public void Responce(String resp) {

                Parse_data(resp);
                swiperefresh.setRefreshing(false);


            }
        });


    }


    public void Parse_data(String responce) {

        datalist.clear();

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");
                for (int d = 0; d < msgArray.length(); d++) {

                    JSONObject discover_object = msgArray.optJSONObject(d);
                    JSONObject Hashtag = discover_object.optJSONObject("Hashtag");

                    Discover_Get_Set discover_get_set = new Discover_Get_Set();
                    discover_get_set.title = Hashtag.optString("name");
                    discover_get_set.views = Hashtag.optString("views");
                    discover_get_set.videos_count=Hashtag.optString("videos_count");

                    JSONArray video_array = Hashtag.optJSONArray("Videos");

                    ArrayList<Home_Get_Set> video_list = new ArrayList<>();
                    for (int i = 0; i < video_array.length(); i++) {
                        JSONObject itemdata = video_array.optJSONObject(i);

                        JSONObject Video = itemdata.optJSONObject("Video");
                        JSONObject User = Video.optJSONObject("User");
                        JSONObject Sound = Video.optJSONObject("Sound");
                        JSONObject UserPrivacy = User.optJSONObject("PrivacySetting");
                        JSONObject UserPushNotification = User.optJSONObject("PushNotification");

                        Home_Get_Set item = Functions.Parse_video_data(User, Sound, Video, UserPrivacy, UserPushNotification);


                        video_list.add(item);
                    }

                    discover_get_set.arrayList = video_list;

                    datalist.add(discover_get_set);

                }

                adapter.notifyDataSetChanged();

            } else {
                Functions.show_toast(getActivity(), jsonObject.optString("msg"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    // When you click on any Video a new activity is open which will play the Clicked video
    private void OpenWatchVideo(int postion, ArrayList<Home_Get_Set> data_list) {

        Intent intent = new Intent(getActivity(), WatchVideos_F.class);
        intent.putExtra("arraylist", data_list);
        intent.putExtra("position", postion);
        startActivity(intent);

    }


    public void Open_search() {
        Search_Main_F search_main_f = new Search_Main_F();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, search_main_f).commit();

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_layout:
                Open_search();
                break;
            case R.id.search_edit:
                Open_search();
                break;
            default:
                return;

        }
    }

}
