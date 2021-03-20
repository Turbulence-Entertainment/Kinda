package com.qboxus.musictok.ActivitesFragment.SoundLists;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.exoplayer2.util.Log;
import com.qboxus.musictok.ActivitesFragment.Accounts.Login_A;
import com.qboxus.musictok.Models.Home_Get_Set;
import com.qboxus.musictok.Adapters.MyVideos_Adapter;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.Adapter_Click_Listener;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.qboxus.musictok.ActivitesFragment.Video_Recording.Video_Recoder_A;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.downloader.request.DownloadRequest;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.qboxus.musictok.ActivitesFragment.WatchVideos_F;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class VideoSound_A extends AppCompatActivity implements View.OnClickListener {

    Home_Get_Set item;
    TextView sound_name, description_txt;
    ImageView sound_image;

    File audio_file;


    RecyclerView recyclerView;
    GridLayoutManager linearLayoutManager;
    ProgressBar load_more_progress;
    int page_count = 0;
    boolean ispost_finsh;
    ArrayList<Home_Get_Set> data_list;
    MyVideos_Adapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_sound);

        Functions.make_directry(Variables.app_hided_folder);
        Functions.make_directry(Variables.app_showing_folder);
        Functions.make_directry(Variables.draft_app_folder);


        Intent intent = getIntent();
        if (intent.hasExtra("data")) {
            item = (Home_Get_Set) intent.getSerializableExtra("data");
        }


        sound_name = findViewById(R.id.sound_name);
        description_txt = findViewById(R.id.description_txt);
        sound_image = findViewById(R.id.sound_image);

        if ((item.sound_name == null || item.sound_name.equals("") || item.sound_name.equals("null"))) {
            sound_name.setText("original sound - " + item.first_name + " " + item.last_name);
        } else {
            sound_name.setText(item.sound_name);
        }
        description_txt.setText(item.video_description);


        findViewById(R.id.back_btn).setOnClickListener(this);

        findViewById(R.id.save_btn).setOnClickListener(this);
        findViewById(R.id.create_btn).setOnClickListener(this);

        findViewById(R.id.play_btn).setOnClickListener(this);
        findViewById(R.id.pause_btn).setOnClickListener(this);


        Uri uri = Uri.parse(item.thum);
        sound_image.setImageURI(uri);

        Log.d(Variables.tag, item.thum);
        Log.d(Variables.tag, item.sound_url_acc);

        Save_Audio();


        recyclerView = findViewById(R.id.recylerview);
        linearLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(linearLayoutManager);

        data_list = new ArrayList<>();

        adapter = new MyVideos_Adapter(this, data_list, new Adapter_Click_Listener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {

                Home_Get_Set item = (Home_Get_Set) object;
                OpenWatchVideo(item.video_id);


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

                android.util.Log.d("resp", "" + scrollOutitems);
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


        load_more_progress = findViewById(R.id.load_more_progress);

        page_count = 0;
        Call_Api();


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.back_btn:
                onBackPressed();
                break;
            case R.id.save_btn:
                if (audio_file != null && audio_file.exists()) {
                    try {
                        Functions.copyFile(audio_file,
                                new File(Variables.app_showing_folder + item.video_id + ".acc"));
                        Functions.Show_Alert(VideoSound_A.this, "Audio Saved", "This sound is saved successfully");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case R.id.create_btn:
                if (Functions.getSharedPreference(this).getBoolean(Variables.islogin, false)) {
                    if (audio_file != null && audio_file.exists()) {
                        StopPlaying();
                        Open_video_recording();
                    }
                } else {
                    Intent intent = new Intent(this, Login_A.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
                }
                break;

            case R.id.play_btn:
                if (audio_file != null && audio_file.exists())
                    playaudio();

                break;

            case R.id.pause_btn:
                StopPlaying();
                break;
        }
    }


    public void Call_Api() {


        JSONObject params = new JSONObject();
        try {

            params.put("sound_id", item.sound_id);
            params.put("starting_point", "" + page_count);
            params.put("device_id", Variables.sharedPreferences.getString(Variables.device_id, ""));


        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(this, ApiLinks.showVideosAgainstSound, params, new Callback() {
            @Override
            public void Responce(String resp) {
                Parse_video(resp);


            }
        });

    }


    public void Parse_video(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");

                ArrayList<Home_Get_Set> temp_list = new ArrayList<>();

                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);

                    JSONObject Video = itemdata.optJSONObject("Video");
                    JSONObject User = itemdata.optJSONObject("User");
                    JSONObject Sound = itemdata.optJSONObject("Sound");

                    Home_Get_Set item = new Home_Get_Set();

                    item.user_id = User.optString("id");
                    item.username = User.optString("username");
                    item.first_name = User.optString("first_name", getResources().getString(R.string.app_name));
                    item.last_name = User.optString("last_name", "User");

                    item.profile_pic = User.optString("profile_pic", "null");
                    if (!item.profile_pic.contains(Variables.http)) {
                        item.profile_pic = ApiLinks.BASE_URL + item.profile_pic;
                    }


                    item.verified = User.optString("verified");

                    if (Sound != null) {
                        item.sound_id = Sound.optString("id");
                        item.sound_name = Sound.optString("sound_name");
                        item.sound_pic = Sound.optString("thum");
                        item.sound_url_mp3 = Sound.optString("audio");
                        item.sound_url_acc = Sound.optString("audio");
                    }


                    item.like_count = "0" + Video.optInt("like_count");
                    item.video_comment_count = Video.optString("comment_count");

                    item.privacy_type = Video.optString("privacy_type");
                    item.allow_comments = Video.optString("allow_comments");
                    item.allow_duet = Video.optString("allow_duet");
                    item.video_id = Video.optString("id");
                    item.liked = Video.optString("like");
                    item.views = Video.optString("view");

                    item.video_url = Video.optString("video", "");
                    if (!item.video_url.contains(Variables.http)) {
                        item.video_url = ApiLinks.BASE_URL + item.video_url;
                    }

                    item.video_description = Video.optString("description");

                    item.thum = Video.optString("thum");
                    item.created_date = Video.optString("created");

                    temp_list.add(item);
                }

                if (temp_list.isEmpty())
                    ispost_finsh = true;
                else {
                    data_list.addAll(temp_list);
                    adapter.notifyDataSetChanged();
                }


            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            load_more_progress.setVisibility(View.GONE);
        }

    }


    private void OpenWatchVideo(String video_id) {
        Intent intent = new Intent(this, WatchVideos_F.class);
        intent.putExtra("video_id", video_id);
        startActivity(intent);
    }


    SimpleExoPlayer player;

    public void playaudio() {

        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "TikTok"));

        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.fromFile(audio_file));


        player.prepare(videoSource);
        player.setPlayWhenReady(true);

        Show_playing_state();
    }


    public void StopPlaying() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
        Show_pause_state();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        StopPlaying();
    }

    @Override
    protected void onStop() {
        super.onStop();
        StopPlaying();
        Log.d(Variables.tag, "onStop");

    }


    public void Show_playing_state() {
        findViewById(R.id.play_btn).setVisibility(View.GONE);
        findViewById(R.id.pause_btn).setVisibility(View.VISIBLE);
    }

    public void Show_pause_state() {
        findViewById(R.id.play_btn).setVisibility(View.VISIBLE);
        findViewById(R.id.pause_btn).setVisibility(View.GONE);
    }

    DownloadRequest prDownloader;
    ProgressDialog progressDialog;

    public void Save_Audio() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        prDownloader = PRDownloader.download(item.sound_url_acc, Variables.app_hided_folder, Variables.SelectedAudio_AAC)
                .build();

        prDownloader.start(new OnDownloadListener() {
            @Override
            public void onDownloadComplete() {
                progressDialog.dismiss();
                audio_file = new File(Variables.app_hided_folder + Variables.SelectedAudio_AAC);
            }

            @Override
            public void onError(Error error) {
                progressDialog.dismiss();
            }
        });


    }


    public void Open_video_recording() {
        Intent intent = new Intent(VideoSound_A.this, Video_Recoder_A.class);
        intent.putExtra("sound_name", sound_name.getText().toString());
        intent.putExtra("sound_id", item.sound_id);
        intent.putExtra("isSelected", "yes");
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);

    }


}
