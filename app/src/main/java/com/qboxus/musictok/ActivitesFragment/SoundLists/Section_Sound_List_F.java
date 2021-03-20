package com.qboxus.musictok.ActivitesFragment.SoundLists;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.qboxus.musictok.Adapters.SoundList_Adapter;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Adapter_Click_Listener;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.qboxus.musictok.Models.Sounds_GetSet;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class Section_Sound_List_F extends RootFragment implements Player.EventListener,View.OnClickListener  {



    View view;
    Context context;

    TextView title_txt;
    String id;


    ArrayList<Object> datalist;
    SoundList_Adapter adapter;
    static boolean active = false;
    LinearLayoutManager linearLayoutManager;
    RecyclerView recyclerView;

    DownloadRequest prDownloader;

    public static String running_sound_id;


    ProgressBar pbar;
    SwipeRefreshLayout swiperefresh;
    RelativeLayout no_data_layout;



    int page_count = 0;
    boolean ispost_finsh;
    ProgressBar load_more_progress;

    public Section_Sound_List_F() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_section_sound_list, container, false);
        context=getContext();


        title_txt=view.findViewById(R.id.title_txt);


        Bundle bundle=getArguments();
        if(bundle!=null){

            id=bundle.getString("id");
            title_txt.setText(bundle.getString("name"));
        }


        running_sound_id="none";
        PRDownloader.initialize(context);

        view.findViewById(R.id.back_btn).setOnClickListener(this::onClick);
        pbar=view.findViewById(R.id.pbar);
        load_more_progress=view.findViewById(R.id.load_more_progress);

        no_data_layout =view.findViewById(R.id.no_data_layout);

        recyclerView = view.findViewById(R.id.listview);
        linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);


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



        swiperefresh=view.findViewById(R.id.swiperefresh);
        swiperefresh.setColorSchemeResources(R.color.black);
        swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                page_count=0;

                Call_api();
            }
        });


        Set_adapter();

        Call_api();


        return view;
    }




    public void Set_adapter(){
        datalist=new ArrayList<>();

        adapter = new SoundList_Adapter(context, datalist, new Adapter_Click_Listener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {

                Sounds_GetSet item = (Sounds_GetSet) object;

                if (view.getId() == R.id.done) {
                    StopPlaying();
                    Down_load_mp3(item.id, item.sound_name, item.acc_path);
                } else if (view.getId() == R.id.fav_btn) {
                    Call_Api_For_Fav_sound(pos, item);
                } else {
                    if (thread != null && !thread.isAlive()) {
                        StopPlaying();
                        playaudio(view, item);
                    } else if (thread == null) {
                        StopPlaying();
                        playaudio(view, item);
                    }
                }
            }
        });

        recyclerView.setAdapter(adapter);


    }


    public void Call_api(){

        JSONObject params=new JSONObject();
        try {
            params.put("starting_point",page_count);
            params.put("sound_section_id",id);
            params.put("user_id", Functions.getSharedPreference(context).getString(Variables.u_id,""));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(getActivity(), ApiLinks.showSoundsAgainstSection, params, new Callback() {
            @Override
            public void Responce(String resp) {
                pbar.setVisibility(View.GONE);
                load_more_progress.setVisibility(View.GONE);
                Parse_data(resp);
            }
        });

    }

    public void Parse_data(String responce){


        try {
            JSONObject jsonObject=new JSONObject(responce);
            String code=jsonObject.optString("code");
            if(code.equals("200")){

                JSONArray msgArray=jsonObject.getJSONArray("msg");

                ArrayList<Sounds_GetSet> templist=new ArrayList<>();
                for(int i=0;i<msgArray.length();i++){
                    JSONObject itemdata = msgArray.optJSONObject(i).optJSONObject("Sound");

                    Sounds_GetSet item=new Sounds_GetSet();

                    item.id=itemdata.optString("id");

                    item.acc_path=itemdata.optString("audio");
                    if(!item.acc_path.contains(Variables.http)){
                       item.acc_path=ApiLinks.BASE_URL +item.acc_path;
                    }

                    item.sound_name=itemdata.optString("name");
                    item.description=itemdata.optString("description");
                    item.section=itemdata.optString("section");


                    item.thum=itemdata.optString("thum");
                    if(!item.thum.contains(Variables.http)){
                        item.thum=ApiLinks.BASE_URL +item.thum;
                    }



                    item.duration=itemdata.optString("duration");
                    item.date_created=itemdata.optString("created");
                    item.fav = itemdata.optString("favourite");


                    templist.add(item);
                }




                if (page_count == 0) {

                    datalist.addAll(templist);

                    if (datalist.isEmpty()) {
                        no_data_layout.setVisibility(View.VISIBLE);
                    } else {
                        no_data_layout.setVisibility(View.GONE);

                        recyclerView.setAdapter(adapter);
                    }
                }
                else {

                    if (datalist.isEmpty())
                        ispost_finsh = true;
                    else {
                        datalist.addAll(templist);
                        adapter.notifyDataSetChanged();
                    }

                }


            }

            else {
                no_data_layout.setVisibility(View.VISIBLE);
            }


        } catch (JSONException e) {

            e.printStackTrace();
        }

    }




    View previous_view;
    SimpleExoPlayer player;
    Thread thread;
    String previous_url="none";
    public void playaudio(View view, final Sounds_GetSet item){
        previous_view=view;

        if(previous_url.equals(item.acc_path)){
            previous_url="none";
            running_sound_id="none";
        }else {

            previous_url=item.acc_path;
            running_sound_id=item.id;

            DefaultTrackSelector trackSelector = new DefaultTrackSelector();
            player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);

            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, "TikTok"));

            MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(item.acc_path));

            Log.d(Variables.tag,item.acc_path);

            player.prepare(videoSource);
            player.addListener(this);


            player.setPlayWhenReady(true);

        }

    }


    public void StopPlaying(){
        if(player!=null){
            player.setPlayWhenReady(false);
            player.removeListener(this);
            player.release();
        }

        show_Stop_state();

    }


    @Override
    public void onStart() {
        super.onStart();
        active=true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active=false;

        running_sound_id="null";

        if(player!=null){
            player.setPlayWhenReady(false);
            player.removeListener(this);
            player.release();
        }

        show_Stop_state();

    }


    public void Show_Run_State(){

        if (previous_view != null) {
            previous_view.findViewById(R.id.loading_progress).setVisibility(View.GONE);
            previous_view.findViewById(R.id.pause_btn).setVisibility(View.VISIBLE);
            previous_view.findViewById(R.id.done).setVisibility(View.VISIBLE);
        }

    }


    public void Show_loading_state(){
        previous_view.findViewById(R.id.play_btn).setVisibility(View.GONE);
        previous_view.findViewById(R.id.loading_progress).setVisibility(View.VISIBLE);
    }


    public void show_Stop_state(){

        if (previous_view != null) {
            previous_view.findViewById(R.id.play_btn).setVisibility(View.VISIBLE);
            previous_view.findViewById(R.id.loading_progress).setVisibility(View.GONE);
            previous_view.findViewById(R.id.pause_btn).setVisibility(View.GONE);
            previous_view.findViewById(R.id.done).setVisibility(View.GONE);
        }

        running_sound_id="none";

    }


    public void Down_load_mp3(final String id,final String sound_name, String url){

        final ProgressDialog progressDialog=new ProgressDialog(context);
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        prDownloader= PRDownloader.download(url, Variables.app_hided_folder, Variables.SelectedAudio_AAC)
                .build();

        prDownloader.start(new OnDownloadListener() {
            @Override
            public void onDownloadComplete() {
                progressDialog.dismiss();
                Intent output = new Intent();
                output.putExtra("isSelected","yes");
                output.putExtra("sound_name",sound_name);
                output.putExtra("sound_id",id);
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



    private void Call_Api_For_Fav_sound(final int pos, final Sounds_GetSet item) {

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

                datalist.remove(item);
                datalist.add(pos, item);
                adapter.notifyDataSetChanged();

                adapter.notifyDataSetChanged();

            }
        });

    }



    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        if(playbackState== Player.STATE_BUFFERING){
            Show_loading_state();
        }
        else if(playbackState==Player.STATE_READY){
            Show_Run_State();
        }else if(playbackState==Player.STATE_ENDED){
            show_Stop_state();
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_btn:
                getActivity().onBackPressed();
                break;
        }
    }


}