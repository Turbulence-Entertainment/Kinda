package com.qboxus.musictok.ActivitesFragment.Video_Recording;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coremedia.iso.boxes.Container;
import com.daasuu.gpuv.composer.GPUMp4Composer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.qboxus.musictok.Models.Home_Get_Set;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.ProgressBarListener;
import com.qboxus.musictok.SimpleClasses.SegmentedProgressBar;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.Interfaces.Fragment_Callback;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Video_Recoder_Duet_A extends AppCompatActivity implements View.OnClickListener{


    CameraView cameraView;
    int number=0;

    ArrayList<String> videopaths=new ArrayList<>();

    ImageButton record_image;
    ImageButton done_btn;
    ImageView img_duet_orientation;
    boolean is_recording=false;
    boolean is_flash_on=false;

    ImageButton flash_btn;
    SegmentedProgressBar video_progress;
    LinearLayout camera_options;
    ImageButton rotate_camera,cut_video_btn;


    int sec_passed=0;
    long time_in_milis=0;

    TextView countdown_timer_txt;
    boolean is_recording_timer_enable;
    int recording_time=3;

    Home_Get_Set item;

    boolean duetOrientation=false;
    LinearLayout tab_layout_orientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_recoder_duet);

        cameraView = findViewById(R.id.camera);
        camera_options=findViewById(R.id.camera_options);
        img_duet_orientation=findViewById(R.id.orientation_btn);
        img_duet_orientation.setOnClickListener(this);
        tab_layout_orientation=findViewById(R.id.layout_orientation);


        record_image=findViewById(R.id.record_image);

        cut_video_btn=findViewById(R.id.cut_video_btn);
        cut_video_btn.setVisibility(View.GONE);
        cut_video_btn.setOnClickListener(this);

        done_btn=findViewById(R.id.done);
        done_btn.setEnabled(false);
        done_btn.setOnClickListener(this);


        rotate_camera=findViewById(R.id.rotate_camera);
        rotate_camera.setOnClickListener(this);
        flash_btn=findViewById(R.id.flash_camera);
        flash_btn.setOnClickListener(this);

        findViewById(R.id.Goback).setOnClickListener(this);

        findViewById(R.id.time_btn).setOnClickListener(this);

        Intent intent=getIntent();
        if(intent.hasExtra("data")){
            item=(Home_Get_Set) intent.getSerializableExtra("data");
        }

        Variables.recording_duration=(int) Functions.getfileduration(this,Uri.parse(Variables.app_showing_folder+ item.video_id+".mp4"));

        Set_Player();

        record_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Start_or_Stop_Recording();
            }
        });
        countdown_timer_txt=findViewById(R.id.countdown_timer_txt);


        initlize_Video_progress();


    }


    public void initlize_Video_progress(){
        sec_passed=0;
        video_progress=findViewById(R.id.video_progress);
        video_progress.enableAutoProgressView(Variables.recording_duration);
        video_progress.setDividerColor(Color.WHITE);
        video_progress.setDividerEnabled(true);
        video_progress.setDividerWidth(4);
        video_progress.setShader(new int[]{Color.CYAN, Color.CYAN, Color.CYAN});

        video_progress.SetListener(new ProgressBarListener() {
            @Override
            public void TimeinMill(long mills) {
                time_in_milis=mills;
                sec_passed = (int) (mills/1000);

                if(sec_passed>(Variables.recording_duration/1000)-1){
                    Start_or_Stop_Recording();
                }

                if(is_recording_timer_enable && sec_passed>=recording_time){
                    is_recording_timer_enable=false;
                    Start_or_Stop_Recording();
                }


            }
        });
    }

    // if the Recording is stop then it we start the recording
    // and if the mobile is recording the video then it will stop the recording
    public void Start_or_Stop_Recording(){

        if (!is_recording && sec_passed<(Variables.recording_duration/1000)-1) {
            number=number+1;

            is_recording=true;

            new Thread(new Runnable() {
                @Override
                public void run() {

                    File file = new File(Variables.app_hided_folder +  "myvideo"+(number)+".mp4");
                    videopaths.add(Variables.app_hided_folder +  "myvideo"+(number)+".mp4");
                    cameraView.captureVideo(file);

                    video_progress.resume();

                    video_player.setPlayWhenReady(true);

                }
            }).start();


            done_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_not_done));
            done_btn.setEnabled(false);


            record_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_recoding_yes));

            cut_video_btn.setVisibility(View.GONE);


            camera_options.setVisibility(View.GONE);
            rotate_camera.setVisibility(View.GONE);

        }

        else if (is_recording) {

            is_recording=false;



            new Thread(new Runnable() {
                @Override
                public void run() {

                    video_progress.pause();
                    video_progress.addDivider();

                    video_player.setPlayWhenReady(false);

                    cameraView.stopVideo();

                }
            }).start();



            Check_done_btn_enable();

            cut_video_btn.setVisibility(View.VISIBLE);

            record_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_recoding_no));
            camera_options.setVisibility(View.VISIBLE);

        }

        else if(sec_passed>(Variables.recording_duration/1000)){
            Functions.Show_Alert(this,"Alert","Video only can be a "+(int)Variables.recording_duration/1000+" S");
        }



    }


    SimpleExoPlayer video_player;
    public void Set_Player(){

        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        video_player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "TikTok"));

        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(Variables.app_showing_folder+item.video_id+".mp4"));

        video_player.prepare(videoSource);
        video_player.setRepeatMode(Player.REPEAT_MODE_OFF);

        final PlayerView playerView=findViewById(R.id.playerview);
        playerView.setPlayer(video_player);

        playerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        video_player.setPlayWhenReady(false);


    }


    public void Check_done_btn_enable(){
        if(sec_passed>(Variables.min_time_recording/1000)) {
            done_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_done_red));
            done_btn.setEnabled(true);
        }
        else {
            done_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_not_done));
            done_btn.setEnabled(false);
        }
    }


    // this will apped all the videos parts in one  fullvideo
    private boolean append() {
        final ProgressDialog progressDialog=new ProgressDialog(Video_Recoder_Duet_A.this);
        new Thread(new Runnable() {
            @Override
            public void run() {


                runOnUiThread(new Runnable() {
                    public void run() {

                        progressDialog.setMessage("Please wait..");
                        progressDialog.show();
                    }
                });

                ArrayList<String> video_list=new ArrayList<>();
                for (int i=0;i<videopaths.size();i++){

                    File file=new File(videopaths.get(i));
                    if(file.exists()) {
                        try {
                            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                            retriever.setDataSource(Video_Recoder_Duet_A.this, Uri.fromFile(file));
                            String hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);
                            boolean isVideo = "yes".equals(hasVideo);

                            if (isVideo && file.length() > 3000) {
                                Log.d("resp", videopaths.get(i));
                                video_list.add(videopaths.get(i));
                            }
                        }catch (Exception e){
                            Log.d(Variables.tag,e.toString());
                        }
                    }
                }

                try {

                    Movie[] inMovies = new Movie[video_list.size()];

                    for (int i=0;i<video_list.size();i++){

                        inMovies[i]= MovieCreator.build(video_list.get(i));
                    }


                    List<Track> videoTracks = new LinkedList<Track>();
                    List<Track> audioTracks = new LinkedList<Track>();
                    for (Movie m : inMovies) {
                        for (Track t : m.getTracks()) {
                            if (t.getHandler().equals("soun")) {
                                audioTracks.add(t);
                            }
                            if (t.getHandler().equals("vide")) {
                                videoTracks.add(t);
                            }
                        }
                    }
                    Movie result = new Movie();
                    if (audioTracks.size() > 0) {
                        result.addTrack(new AppendTrack(audioTracks.toArray(new Track[audioTracks.size()])));
                    }
                    if (videoTracks.size() > 0) {
                        result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
                    }

                    Container out = new DefaultMp4Builder().build(result);

                    String outputFilePath=null;
                    outputFilePath=Variables.outputfile2;


                    FileOutputStream fos = new FileOutputStream(new File(outputFilePath));
                    out.writeContainer(fos.getChannel());
                    fos.close();

                    runOnUiThread(new Runnable() {
                        public void run() {

                            progressDialog.dismiss();

                            if(cameraView.isFacingFront()){
                                Change_fliped_video(Variables.outputfile2,Variables.output_filter_file);
                            }
                            else
                            Go_To_post_Activity();

                        }
                    });



                }
                catch (Exception e) {

                }


            }
        }).start();



        return true;
    }


    public void RotateCamera(){

        cameraView.toggleFacing();
    }


    public void Remove_Last_Section(){

        if(videopaths.size()>0){
            File file=new File(videopaths.get(videopaths.size()-1));
            if(file.exists()) {

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(Video_Recoder_Duet_A.this, Uri.fromFile(file));
                String hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);
                String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long timeInMillisec = Long.parseLong(time );
                boolean isVideo = "yes".equals(hasVideo);
                if (isVideo) {
                    time_in_milis=time_in_milis-timeInMillisec;
                    video_progress.removeDivider();
                    videopaths.remove(videopaths.size()-1);
                    video_progress.updateProgress(time_in_milis);
                    video_progress.back_countdown(timeInMillisec);

                    if(video_player!=null) {
                        int audio_backtime = (int) (video_player.getCurrentPosition()- timeInMillisec);
                        if(audio_backtime<0)
                            audio_backtime=0;

                        video_player.seekTo(audio_backtime);
                    }

                    sec_passed = (int) (time_in_milis/1000);

                    Check_done_btn_enable();

                }
            }

            if(videopaths.isEmpty()){

                cut_video_btn.setVisibility(View.GONE);
                rotate_camera.setVisibility(View.VISIBLE);

                initlize_Video_progress();

            }

            file.delete();
        }

    }


    @SuppressLint("WrongConstant")
    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.rotate_camera:
                RotateCamera();
                break;


            case R.id.done:
                append();
                break;
            case R.id.orientation_btn:
                if (duetOrientation)
                {
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) findViewById(R.id.layout_orientation).getLayoutParams();
                    layoutParams.width=FrameLayout.LayoutParams.MATCH_PARENT;
                    layoutParams.height=FrameLayout.LayoutParams.MATCH_PARENT;
                    PlayerView playerView=findViewById(R.id.playerview);
                    playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
                    tab_layout_orientation.setLayoutParams(layoutParams);
                    tab_layout_orientation.setOrientation(LinearLayout.VERTICAL);
                    img_duet_orientation.animate().rotation(0f).setDuration(500).start();

                    duetOrientation=false;
                }

                else
                {
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) findViewById(R.id.layout_orientation).getLayoutParams();
                    layoutParams.width=FrameLayout.LayoutParams.MATCH_PARENT;
                    layoutParams.height=800;
                    PlayerView playerView=findViewById(R.id.playerview);
                    playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                    tab_layout_orientation.setLayoutParams(layoutParams);
                    tab_layout_orientation.setOrientation(LinearLayout.HORIZONTAL);
                    img_duet_orientation.animate().rotation(90f).setDuration(500).start();
                    duetOrientation=true;
                }
                break;

            case R.id.cut_video_btn:

                Functions.Show_Alert(this, null, "Discard the last clip?", "DELETE", "CANCEL", new Callback() {
                    @Override
                    public void Responce(String resp) {
                        if(resp.equalsIgnoreCase("yes")){
                            Remove_Last_Section();
                        }
                    }
                });

                break;

            case R.id.flash_camera:

                if(is_flash_on){
                    is_flash_on=false;
                    cameraView.setFlash(0);
                    flash_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_on));
                }
                else {
                    is_flash_on=true;
                    cameraView.setFlash(CameraKit.Constants.FLASH_TORCH);
                    flash_btn.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_off));
                }

                break;

            case R.id.Goback:
                onBackPressed();
                break;

            case R.id.time_btn:
                if(sec_passed+1<Variables.recording_duration/1000){
                    RecordingTimeRang_F recordingTimeRang_f = new RecordingTimeRang_F(new Fragment_Callback() {
                        @Override
                        public void Responce(Bundle bundle) {
                            if(bundle!=null){
                                is_recording_timer_enable=true;
                                recording_time=bundle.getInt("end_time");
                                countdown_timer_txt.setText("3");
                                countdown_timer_txt.setVisibility(View.VISIBLE);
                                record_image.setClickable(false);
                                final Animation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
                                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                new CountDownTimer(4000,1000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {

                                        countdown_timer_txt.setText(""+(millisUntilFinished/1000));
                                        countdown_timer_txt.setAnimation(scaleAnimation);

                                    }

                                    @Override
                                    public void onFinish() {
                                        record_image.setClickable(true);
                                        countdown_timer_txt.setVisibility(View.GONE);
                                        Start_or_Stop_Recording();
                                    }
                                }.start();

                            }
                        }
                    });
                    Bundle bundle=new Bundle();
                    if(sec_passed<(Variables.recording_duration/1000)-3)
                        bundle.putInt("end_time",(sec_passed+3));
                    else
                        bundle.putInt("end_time",(sec_passed+1));

                    bundle.putInt("total_time",(Variables.recording_duration/1000));
                    recordingTimeRang_f.setArguments(bundle);
                    recordingTimeRang_f.show(getSupportFragmentManager(), "");
                }
                break;


        }


    }



    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }


    @Override
    protected void onDestroy() {
        Release_Resources();
        super.onDestroy();

    }


    public void Release_Resources(){
        try {
            cameraView.stop();

            if(video_player!=null) {
                video_player.setPlayWhenReady(false);
                video_player.release();
            }


        }catch (Exception e){

        }
        DeleteFile();
    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setTitle("Alert")
                .setMessage("Are you Sure? if you Go back you can't undo this action")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        Release_Resources();

                        finish();
                        overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom);

                    }
                }).show();


    }


    public void Go_To_post_Activity(){
        try {
            Functions.copyFile(new File(Variables.outputfile2),
                    new File(Variables.output_filter_file));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Open_post_activity();
    }


    // this function will add the filter to video and save that same video for post the video in post video screen
    public void Change_fliped_video(String srcMp4Path, final String destMp4Path){

        Functions.Show_determinent_loader(this,false,false);
        new GPUMp4Composer(srcMp4Path, destMp4Path)
                .flipHorizontal(true)
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
                                Open_post_activity();


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

                                    Functions.show_toast(Video_Recoder_Duet_A.this,"Try Again");
                                }catch (Exception e){
                                }
                            }
                        });

                    }
                })
                .start();


    }



    public void Open_post_activity(){
        String duet="";
        if (duetOrientation)
            duet="h";
        else
            duet="v";

        Intent intent =new Intent(this,Post_Video_A.class);
        intent.putExtra("duet_video_id",item.video_id);
        intent.putExtra("duet_orientation",duet);
        intent.putExtra("duet_video_username",item.username);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);

    }


    // this will delete all the video parts that is create during priviously created video
    public void DeleteFile(){

        File output = new File(Variables.outputfile);
        File output2 = new File(Variables.outputfile2);

        File gallery_trimed_video = new File(Variables.gallery_trimed_video);
        File gallery_resize_video = new File(Variables.gallery_resize_video);

        if(output.exists()){
            output.delete();
        }

        if(output2.exists()){

            output2.delete();
        }

        if(gallery_trimed_video.exists()){
            gallery_trimed_video.delete();
        }

        if(gallery_resize_video.exists()){
            gallery_resize_video.delete();
        }

        for (int i=0;i<=12;i++) {

            File file = new File(Variables.app_hided_folder + "myvideo" + (i) + ".mp4");
            if (file.exists()) {
                file.delete();
            }

        }

    }


}
