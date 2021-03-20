package com.qboxus.musictok.ActivitesFragment.SoundLists;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.qboxus.musictok.Adapters.Sounds_Adapter;
import com.qboxus.musictok.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.qboxus.musictok.Models.Sound_catagory_Get_Set;
import com.qboxus.musictok.Models.Sounds_GetSet;
import com.qboxus.musictok.R;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.downloader.request.DownloadRequest;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class Discover_SoundList_F extends RootFragment implements Player.EventListener {

    RecyclerView recyclerView;
    Sounds_Adapter adapter;
    ArrayList<Sound_catagory_Get_Set> datalist;
    LinearLayoutManager linearLayoutManager;
    RelativeLayout no_data_layout;
    DownloadRequest prDownloader;
    static boolean active = false;

    View view;
    Context context;

    SwipeRefreshLayout swiperefresh;
    ProgressBar pbar;


    ProgressBar load_more_progress;
    int page_count = 0;
    boolean ispost_finsh;


    public static String running_sound_id;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.activity_sound_list, container, false);
        context = getContext();

        running_sound_id = "none";

        load_more_progress = view.findViewById(R.id.load_more_progress);

        PRDownloader.initialize(context);
        pbar = view.findViewById(R.id.pbar);
        no_data_layout =view.findViewById(R.id.no_data_layout);
        datalist = new ArrayList<>();

        recyclerView = view.findViewById(R.id.listview);
        linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(true);
        recyclerView.getLayoutManager().setMeasurementCacheEnabled(false);

        Set_adapter();

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
                        Call_Api_For_get_allsound();
                    }
                }


            }
        });

        swiperefresh = view.findViewById(R.id.swiperefresh);
        swiperefresh.setColorSchemeResources(R.color.black);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                previous_url = "none";
                StopPlaying();
                Call_Api_For_get_allsound();
            }
        });

        Call_Api_For_get_allsound();

        return view;
    }



    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if((view!=null && visible)){
            Call_Api_For_get_allsound();
        }else{
            StopPlaying();
        }
    }

    public void Set_adapter() {

        adapter = new Sounds_Adapter(context, datalist, (view, postion, item) -> {

            switch (view.getId()){
                case R.id.see_all_btn:
                    Open_Section_list(postion);
                    break;

                case R.id.done:
                    StopPlaying();
                    Down_load_mp3(item.id, item.sound_name, item.acc_path);
                    break;

                case  R.id.fav_btn:
                    Call_Api_For_Fav_sound(postion, item);
                    break;

                default:
                    if (thread != null && !thread.isAlive()) {
                        StopPlaying();
                        playaudio(view, item);
                    }
                    else if (thread == null) {
                        StopPlaying();
                        playaudio(view, item);
                    }
                    break;
            }


        });

        recyclerView.setAdapter(adapter);

    }


    private void Call_Api_For_get_allsound() {

        JSONObject parameters = new JSONObject();
        try {

            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.u_id, null));
            parameters.put("starting_point", "" + page_count);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        ApiRequest.Call_Api(getActivity(), ApiLinks.showSounds, parameters, resp -> {
            swiperefresh.setRefreshing(false);
            pbar.setVisibility(View.GONE);
            Parse_data(resp);

        });


    }

    public void Parse_data(String responce) {
        Log.d("Hashtag", "responce" + responce.toString());
        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {

                JSONArray msg = jsonObject.optJSONArray("msg");

                ArrayList<Sound_catagory_Get_Set> temp_list = new ArrayList<>();

                for (int i=0; i<msg.length(); i++){

                    JSONObject object=msg.optJSONObject(i);
                    JSONObject SoundSection=object.optJSONObject("SoundSection");
                    JSONArray Sound_obj=object.optJSONArray("Sound");

                    ArrayList<Sounds_GetSet> sound_list = new ArrayList<>();
                    for (int j=0; j<Sound_obj.length();j++){
                        JSONObject Sound = Sound_obj.optJSONObject(j);


                        Sounds_GetSet item = new Sounds_GetSet();

                        item.id = Sound.optString("id");

                        String accpath = Sound.optString("audio");
                        if (accpath != null && accpath.contains("http"))
                            item.acc_path = Sound.optString("audio");
                        else
                            item.acc_path = ApiLinks.BASE_URL + Sound.optString("audio");

                        item.sound_name = Sound.optString("name");
                        item.description = Sound.optString("description");
                        item.section = Sound.optString("section");
                        String thum_image = Sound.optString("thum");

                        if (thum_image != null && thum_image.contains("http"))
                            item.thum = Sound.optString("thum");
                        else
                            item.thum = ApiLinks.BASE_URL + Sound.optString("thum");

                        item.duration = Sound.optString("duration");
                        item.date_created = Sound.optString("created");
                        item.fav = Sound.optString("favourite");

                        sound_list.add(item);
                    }

                    Sound_catagory_Get_Set sound_catagory_get_set = new Sound_catagory_Get_Set();
                    sound_catagory_get_set.id=SoundSection.optString("id");
                    sound_catagory_get_set.catagory = SoundSection.optString("name");
                    sound_catagory_get_set.sound_list = sound_list;

                    temp_list.add(sound_catagory_get_set);

                }

                if(page_count==0){
                    datalist.clear();
                }

                datalist.addAll(temp_list);
                adapter.notifyDataSetChanged();

            } else {
                no_data_layout.setVisibility(View.VISIBLE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.d(Variables.tag,e.toString());
        } finally {
            load_more_progress.setVisibility(View.GONE);
        }
    }





    public void Open_Section_list(int pos){

        Sound_catagory_Get_Set item=datalist.get(pos);
        Section_Sound_List_F section_sound_list_f = new Section_Sound_List_F();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("id", item.id);
        args.putString("name",item.catagory);
        section_sound_list_f.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.SoundList_Main_A, section_sound_list_f).commit();

    }





    @Override
    public boolean onBackPressed() {
        getActivity().onBackPressed();
        return super.onBackPressed();
    }


    View previous_view;
    Thread thread;
    SimpleExoPlayer player;
    String previous_url = "none";

    public void playaudio(View view, final Sounds_GetSet item) {
        previous_view = view;

        if (previous_url.equals(item.acc_path)) {

            previous_url = "none";
            running_sound_id = "none";
        } else {
            previous_url = item.acc_path;
            running_sound_id = item.id;

            DefaultTrackSelector trackSelector = new DefaultTrackSelector();
            player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);

            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, "TikTok"));

            MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(item.acc_path));


            player.prepare(videoSource);
            player.addListener(this);


            player.setPlayWhenReady(true);


        }

    }


    public void StopPlaying() {

        if (player != null) {
            player.setPlayWhenReady(false);
            player.removeListener(this);
            player.release();
        }

        show_Stop_state();

    }


    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }


    @Override
    public void onStop() {
        super.onStop();
        active = false;

        running_sound_id = "null";

        if (player != null) {
            player.setPlayWhenReady(false);
            player.removeListener(this);
            player.release();
        }

        show_Stop_state();

    }


    public void Show_Run_State() {

        if (previous_view != null) {
            previous_view.findViewById(R.id.loading_progress).setVisibility(View.GONE);
            previous_view.findViewById(R.id.pause_btn).setVisibility(View.VISIBLE);
            previous_view.findViewById(R.id.done).setVisibility(View.VISIBLE);
        }

    }


    public void Show_loading_state() {
        previous_view.findViewById(R.id.play_btn).setVisibility(View.GONE);
        previous_view.findViewById(R.id.loading_progress).setVisibility(View.VISIBLE);
    }


    public void show_Stop_state() {

        if (previous_view != null) {
            previous_view.findViewById(R.id.play_btn).setVisibility(View.VISIBLE);
            previous_view.findViewById(R.id.loading_progress).setVisibility(View.GONE);
            previous_view.findViewById(R.id.pause_btn).setVisibility(View.GONE);
            previous_view.findViewById(R.id.done).setVisibility(View.GONE);
        }

        running_sound_id = "none";

    }


    ProgressDialog progressDialog;
    public void Down_load_mp3(final String id, final String sound_name, String url) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        progressDialog.show();

        prDownloader = PRDownloader.download(url, Variables.app_hided_folder, Variables.SelectedAudio_AAC)
                .build();

        prDownloader.start(new OnDownloadListener() {
            @Override
            public void onDownloadComplete() {
                progressDialog.dismiss();
                Intent output = new Intent();
                output.putExtra("isSelected", "yes");
                output.putExtra("sound_name", sound_name);
                output.putExtra("sound_id", id);
                getActivity().setResult(RESULT_OK, output);
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom);
            }

            @Override
            public void onError(Error error) {
                progressDialog.dismiss();
            }
        });

    }


    private void Call_Api_For_Fav_sound(int pos, final Sounds_GetSet item) {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.u_id, "0"));
            parameters.put("sound_id", item.id);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.Show_loader(context, false, false);
        ApiRequest.Call_Api(getActivity(), ApiLinks.addSoundFavourite, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                Functions.cancel_loader();

                if (item.fav.equals("1"))
                    item.fav = "0";
                else
                    item.fav = "1";

                for (int i = 0; i < datalist.size(); i++) {
                    Sound_catagory_Get_Set catagory_get_set = datalist.get(i);
                    if (catagory_get_set.sound_list.contains(item)) {
                        int index = catagory_get_set.sound_list.indexOf(item);
                        catagory_get_set.sound_list.remove(item);
                        catagory_get_set.sound_list.add(index, item);
                        break;
                    }
                }

                adapter.notifyDataSetChanged();

            }
        });

    }


    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        if (playbackState == Player.STATE_BUFFERING) {
            Show_loading_state();
        } else if (playbackState == Player.STATE_READY) {
            Show_Run_State();
        } else if (playbackState == Player.STATE_ENDED) {
            show_Stop_state();
        }

    }


}
