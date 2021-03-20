package com.qboxus.musictok.SimpleClasses;

import android.content.SharedPreferences;
import android.os.Environment;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by AQEEL on 2/15/2019.
 */

public  class  Variables {

   public Variables(){
        // default contructor
    }

    public static final String device="android";

    public static int screen_width;
    public static int screen_height;

    public static final String SelectedAudio_AAC ="SelectedAudio.aac";

    public static final String root= Environment.getExternalStorageDirectory().toString();
    public static final String app_hided_folder =root+"/.HidedTicTic/";
    public static final String app_showing_folder =root+"/Kinda/";
    public static final String draft_app_folder= app_hided_folder +"Draft/";


    public static int max_recording_duration=18000;
    public static int recording_duration=18000;
    public static int min_time_recording=5000;
    public static int Char_limit =150;

    public static String output_frontcamera= app_hided_folder + "output_frontcamera.mp4";
    public static String outputfile= app_hided_folder + "output.mp4";
    public static String outputfile2= app_hided_folder + "output2.mp4";
    public static String output_filter_file= app_hided_folder + "output-filtered.mp4";
    public static String gallery_trimed_video= app_hided_folder + "gallery_trimed_video.mp4";
    public static String gallery_resize_video= app_hided_folder + "gallery_resize_video.mp4";



    public static SharedPreferences sharedPreferences;
    public static final String pref_name="pref_name";
    public static final String u_id="u_id";
    public static final String u_name="u_name";
    public static final String u_pic="u_pic";
    public static final String f_name="f_name";
    public static final String l_name="l_name";
    public static final String gender="u_gender";
    public static final String islogin="is_login";
    public static final String device_token="device_token";
    public static final String auth_token ="api_token";
    public static final String device_id="device_id";
    public static final String uploading_video_thumb="uploading_video_thumb";

    public static String user_id;
    public static String user_name;
    public static String user_pic;



    public static String tag="tictic_";

    public static String Selected_sound_id="null";

    public static  boolean Reload_my_videos=false;
    public static  boolean Reload_my_videos_inner=false;
    public static  boolean Reload_my_likes_inner=false;
    public static  boolean Reload_my_notification=false;


    public static final String gif_firstpart="https://media.giphy.com/media/";
    public static final String gif_secondpart="/100w.gif";

    public static final String gif_firstpart_chat="https://media.giphy.com/media/";
    public static final String gif_secondpart_chat="/200w.gif";


    public static final String http="http";

    public static final SimpleDateFormat df =
            new SimpleDateFormat("dd-MM-yyyy HH:mm:ssZZ", Locale.ENGLISH);

    public static final SimpleDateFormat df2 =
            new SimpleDateFormat("dd-MM-yyyy HH:mmZZ", Locale.ENGLISH);


 // if you want a user can't share a video from your app then you have to set this value to true
    public static final boolean is_secure_info=false;

    // if you want a ads did not show into your app then set the belue valuw to true.
    public static final boolean is_remove_ads=false;

    // if you want a video thumnail image show rather then a video gif then set the below value to false.
    public static final boolean is_show_gif=true;


    public static final boolean is_toast_enable=false;


    // if you want to add a limit that a user can watch only 6 video then change the below value to true
    // if you want to change the demo videos limit count then set the count as you want
    public static final boolean is_demo_app=false;
    public static final int demo_app_videos_count=6;


    // if you want to add a duet function into our project you have to set this value to "true"
    // and also get the extended apis
    public static final boolean is_enable_duet=false;


    public final static int permission_camera_code=786;
    public final static int permission_write_data=788;
    public final static int permission_Read_data=789;
    public final static int permission_Recording_audio=790;
    public final static int Pick_video_from_gallery=791;


}
