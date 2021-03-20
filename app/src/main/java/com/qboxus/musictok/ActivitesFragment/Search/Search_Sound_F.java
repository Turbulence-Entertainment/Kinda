package com.qboxus.musictok.ActivitesFragment.Search;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.qboxus.musictok.Adapters.SoundList_Adapter;
import com.qboxus.musictok.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.Adapter_Click_Listener;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.qboxus.musictok.Models.Sounds_GetSet;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.downloader.request.DownloadRequest;
import com.facebook.shimmer.ShimmerFrameLayout;
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

import static com.qboxus.musictok.ActivitesFragment.Search.Search_Main_F.search_edit;

/**
 * A simple {@link Fragment} subclass.
 */
public class Search_Sound_F extends RootFragment implements Player.EventListener {

    View view;
    Context context;
    String type;
    ShimmerFrameLayout shimmerFrameLayout;
    ArrayList<Object> data_list;
    LinearLayoutManager linearLayoutManager;
    RecyclerView recyclerView;
    SoundList_Adapter adapter;
    static boolean active = false;
    RelativeLayout no_data_layout;
    DownloadRequest prDownloader;

    int page_count = 0;
    boolean ispost_finsh;
    ProgressBar load_more_progress;


    public static String running_sound_id;

    public Search_Sound_F(String type) {
        this.type = type;
    }

    public Search_Sound_F() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_sound_list, container, false);
        context = getContext();

        shimmerFrameLayout = view.findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();

        recyclerView = view.findViewById(R.id.recylerview);
        no_data_layout = view.findViewById(R.id.no_data_layout);
        linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        data_list = new ArrayList<>();
        adapter = new SoundList_Adapter(context, data_list, new Adapter_Click_Listener() {
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
                if (userScrolled && (scrollOutitems == data_list.size() - 1)) {
                    userScrolled = false;

                    if (load_more_progress.getVisibility() != View.VISIBLE && !ispost_finsh) {
                        load_more_progress.setVisibility(View.VISIBLE);
                        page_count = page_count + 1;
                        Call_Api();
                    }
                }


            }
        });


        load_more_progress = view.findViewById(R.id.load_more_progress);
        page_count = 0;

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if ((view != null && isVisibleToUser) && data_list.isEmpty()) {
            Call_Api();
        }
    }

    public void Call_Api() {

        JSONObject params = new JSONObject();
        try {
            if (Functions.getSharedPreference(context).getString(Variables.u_id, null) != null) {
                params.put("user_id", Functions.getSharedPreference(context).getString(Variables.u_id, "0"));
            }

            params.put("type", type);
            params.put("keyword", search_edit.getText().toString());
            params.put("starting_point", "" + page_count);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(getActivity(), ApiLinks.search, params, new Callback() {
            @Override
            public void Responce(String resp) {
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);

                if (type.equals("sound"))
                    Parse_sounds(resp);
            }
        });

    }


    public void Parse_sounds(String responce) {


        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {

                JSONArray msgArray = jsonObject.getJSONArray("msg");
                ArrayList<Object> temp_list = new ArrayList<>();
                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);

                    JSONObject Sound = itemdata.optJSONObject("Sound");

                    Sounds_GetSet item = new Sounds_GetSet();

                    item.id = Sound.optString("id");

                    item.acc_path = Sound.optString("audio");


                    item.sound_name = Sound.optString("name");
                    item.description = Sound.optString("description");
                    item.section = Sound.optString("section");

                    String thum_image = Sound.optString("thum", "");

                    if (thum_image != null && thum_image.contains("http"))
                        item.thum = Sound.optString("thum");
                    else
                        item.thum = ApiLinks.BASE_URL + itemdata.optString("thum");

                    item.duration = Sound.optString("duration");
                    item.date_created = Sound.optString("created");
                    item.fav = Sound.optString("favourite");

                    temp_list.add(item);
                }


                if (page_count == 0) {

                    data_list.addAll(temp_list);

                    if (data_list.isEmpty()) {
                        no_data_layout.setVisibility(View.VISIBLE);
                    } else {
                        no_data_layout.setVisibility(View.GONE);

                        recyclerView.setAdapter(adapter);
                    }
                }
                else {

                    if (temp_list.isEmpty())
                        ispost_finsh = true;
                    else {
                        data_list.addAll(temp_list);
                        adapter.notifyDataSetChanged();
                    }

                }


            } else {
                if (data_list.isEmpty())
                    no_data_layout.setVisibility(View.VISIBLE);
            }

        } catch (JSONException e) {

            e.printStackTrace();
        } finally {
            load_more_progress.setVisibility(View.GONE);
        }
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

                data_list.remove(item);
                data_list.add(pos, item);
                adapter.notifyDataSetChanged();

                adapter.notifyDataSetChanged();

            }
        });

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


    public void Down_load_mp3(final String id, final String sound_name, String url) {

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Please Wait...");
        progressDialog.show();

        prDownloader = PRDownloader.download(url, Variables.app_hided_folder, sound_name + id)
                .build();

        prDownloader.start(new OnDownloadListener() {
            @Override
            public void onDownloadComplete() {
                progressDialog.dismiss();
                Functions.show_toast(context, "audio saved into your phone");
            }

            @Override
            public void onError(Error error) {
                progressDialog.dismiss();
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
