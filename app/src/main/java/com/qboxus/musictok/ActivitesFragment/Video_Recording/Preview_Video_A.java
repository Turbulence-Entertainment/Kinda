package com.qboxus.musictok.ActivitesFragment.Video_Recording;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.daasuu.gpuv.composer.GPUMp4Composer;
import com.daasuu.gpuv.egl.filter.GlFilterGroup;
import com.daasuu.gpuv.player.GPUPlayerView;
import com.daasuu.gpuv.player.PlayerScaleType;
import com.qboxus.musictok.SimpleClasses.FilterType;
import com.qboxus.musictok.Adapters.Filter_Adapter;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Preview_Video_A extends AppCompatActivity  implements Player.EventListener {


    String video_url , isSoundSelected;
    GPUPlayerView gpuPlayerView;
    public static int  select_postion=0;
    final List<FilterType> filterTypes = FilterType.createFilterList();
    Filter_Adapter adapter;
    RecyclerView recylerview;

    String draft_file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_video);



        Intent intent=getIntent();
        if(intent!=null){
            String fromWhere =intent.getStringExtra("fromWhere");
            if(fromWhere != null && fromWhere.equals("video_recording")){
                isSoundSelected=intent.getStringExtra("isSoundSelected");
                draft_file=intent.getStringExtra("draft_file");
            }else{
                draft_file=intent.getStringExtra("draft_file");
            }
        }


        select_postion=0;
        video_url= Variables.outputfile2;
        findViewById(R.id.Goback).setOnClickListener(v-> {

                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);

        });


        findViewById(R.id.next_btn).setOnClickListener(v-> {

                if(select_postion==0){

                    try {
                        Functions.copyFile(new File(Variables.outputfile2), new File(Variables.output_filter_file));
                        GotopostScreen();
                    }

                    catch (IOException e) {
                        e.printStackTrace();
                        Log.d(Variables.tag,e.toString());
                        Save_Video(Variables.outputfile2,Variables.output_filter_file);
                    }

                }
                else
                Save_Video(Variables.outputfile2,Variables.output_filter_file);

        });


        Set_Player(video_url);
        if(isSoundSelected != null && isSoundSelected.equals("yes")){
            Log.d("resp","isSoundSelected : "+isSoundSelected);
            PreparedAudio();
        }


        recylerview=findViewById(R.id.recylerview);

        adapter = new Filter_Adapter(this, filterTypes, (view, postion, item) ->  {

                select_postion=postion;
                gpuPlayerView.setGlFilter(FilterType.createGlFilter(filterTypes.get(postion), getApplicationContext()));
                adapter.notifyDataSetChanged();

        });
        recylerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recylerview.setAdapter(adapter);


    }


    MediaPlayer audio;
    public  void PreparedAudio(){
        video_player.setVolume(0);

        File file=new File(Variables.app_hided_folder + Variables.SelectedAudio_AAC);
        if(file.exists()) {
            audio = new MediaPlayer();
            try {
                audio.setDataSource(Variables.app_hided_folder + Variables.SelectedAudio_AAC);
                audio.prepare();
                audio.setLooping(true);


                video_player.seekTo(0);
                video_player.setPlayWhenReady(true);
                audio.start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    // this function will set the player to the current video
    SimpleExoPlayer video_player;
    public void Set_Player(String path){

        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
         video_player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "TikTok"));

        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(path));

        video_player.prepare(videoSource);

        video_player.setRepeatMode(Player.REPEAT_MODE_ALL);
        video_player.addListener(this);


        video_player.setPlayWhenReady(true);


        gpuPlayerView = new GPUPlayerView(this);

        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(path);
        String rotation=metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);

        if(rotation!=null && rotation.equalsIgnoreCase("0")){
            gpuPlayerView.setPlayerScaleType(PlayerScaleType.RESIZE_FIT_WIDTH);
        }
        else
            gpuPlayerView.setPlayerScaleType(PlayerScaleType.RESIZE_NONE);



        gpuPlayerView.setSimpleExoPlayer(video_player);
        gpuPlayerView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        ((MovieWrapperView) findViewById(R.id.layout_movie_wrapper)).addView(gpuPlayerView);

        gpuPlayerView.onResume();

    }


    // this is lifecyle of the Activity which is importent for play,pause video or relaese the player

    @Override
    protected void onRestart() {
        super.onRestart();

        if(video_player!=null){
            video_player.setPlayWhenReady(true);
        }
        if (audio != null) {
            audio.start();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        if(video_player!=null){
            video_player.setPlayWhenReady(true);
        }
    }



    @Override
    public void onStop() {
        super.onStop();
        try{
            if(video_player!=null){
                video_player.setPlayWhenReady(false);
            }
            if(audio!=null){
                audio.pause();
            }
        }catch (Exception e){

        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if(video_player!=null){
            video_player.release();
        }

        if(audio!=null){
            audio.pause();
            audio.release();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        if(video_player !=null){
            video_player.setPlayWhenReady(true);
        }

    }



    // this function will add the filter to video and save that same video for post the video in post video screen
    public void Save_Video(String srcMp4Path, final String destMp4Path){

        Functions.Show_determinent_loader(this,false,false);
        new GPUMp4Composer(srcMp4Path, destMp4Path)
                .filter(new GlFilterGroup(FilterType.createGlFilter(filterTypes.get(select_postion), getApplicationContext())))
                .listener(new GPUMp4Composer.Listener() {
                    @Override
                    public void onProgress(double progress) {

                        Log.d("resp",""+(int) (progress*100));
                        Functions.Show_loading_progress((int)(progress*100));



                    }

                    @Override
                    public void onCompleted() {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Functions.cancel_determinent_loader();

                                GotopostScreen();


                            }
                        });


                    }

                    @Override
                    public void onCanceled() {
                        Log.d("resp", "onCanceled");
                    }

                    @Override
                    public void onFailed(Exception exception) {

                        Log.d("resp",exception.toString());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    Functions.cancel_determinent_loader();

                                    Functions.show_toast(Preview_Video_A.this,"Try Again");
                                }catch (Exception e){

                                }
                            }
                        });

                    }
                })
                .start();


    }



    public void GotopostScreen(){

        if(adapter!=null)
            adapter.recycle_bitmap();

        Intent intent =new Intent(Preview_Video_A.this,Post_Video_A.class);
        intent.putExtra("draft_file",draft_file);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

    }


    @Override
    public void onBackPressed() {

        finish();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);

    }


    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        if(playbackState== Player.STATE_ENDED){

            video_player.seekTo(0);
            video_player.setPlayWhenReady(true);

            if(audio!=null){
                audio.seekTo(0);
                audio.start();
            }
        }

    }


}
