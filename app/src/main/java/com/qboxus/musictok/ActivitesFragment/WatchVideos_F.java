package com.qboxus.musictok.ActivitesFragment;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.danikula.videocache.HttpProxyCacheServer;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.hendraanggrian.appcompat.widget.SocialTextView;
import com.hendraanggrian.appcompat.widget.SocialView;
import com.qboxus.musictok.ActivitesFragment.Accounts.Login_A;
import com.qboxus.musictok.Adapters.Watch_Videos_Adapter;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.SimpleClasses.TicTic;
import com.qboxus.musictok.ActivitesFragment.SoundLists.VideoSound_A;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.qboxus.musictok.Models.Home_Get_Set;
import com.qboxus.musictok.Interfaces.KeyboardHeightObserver;
import com.qboxus.musictok.SimpleClasses.KeyboardHeightProvider;
import com.qboxus.musictok.Main_Menu.MainMenuActivity;
import com.qboxus.musictok.Main_Menu.MainMenuFragment;
import com.qboxus.musictok.ActivitesFragment.Profile.Profile_F;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.API_CallBack;
import com.qboxus.musictok.Interfaces.Fragment_Callback;
import com.qboxus.musictok.Interfaces.Fragment_Data_Send;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.OnProgressListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.request.DownloadRequest;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.qboxus.musictok.ActivitesFragment.Video_Recording.Video_Recoder_Duet_A;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */

public class WatchVideos_F extends AppCompatActivity implements Player.EventListener,
        KeyboardHeightObserver, View.OnClickListener, Fragment_Data_Send {

    Context context;

    RecyclerView recyclerView;
    ArrayList<Home_Get_Set> data_list;
    int position = 0;
    int currentPage = -1;
    LinearLayoutManager layoutManager;

    Watch_Videos_Adapter adapter;

    ProgressBar p_bar;

    private KeyboardHeightProvider keyboardHeightProvider;

    RelativeLayout write_layout;

    EditText message_edit;
    ImageButton send_btn;
    ProgressBar send_progress;

    Handler handler;
    Runnable runnable;
    Boolean animation_running = false;

    String video_id;
    String link;

    public WatchVideos_F() {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_watchvideo);
        context = this;

        if (Variables.sharedPreferences == null) {
            Variables.sharedPreferences = getSharedPreferences(Variables.pref_name, Context.MODE_PRIVATE);
        }

        p_bar = findViewById(R.id.p_bar);
        handler = new Handler();
        Intent bundle = getIntent();
        if (bundle != null) {

            Uri appLinkData = bundle.getData();
            video_id = bundle.getStringExtra("video_id");

            if (video_id != null) {
                Call_Api_For_Singlevideos(video_id);
            } else if (appLinkData == null) {
                data_list = (ArrayList<Home_Get_Set>) bundle.getSerializableExtra("arraylist");
                position = bundle.getIntExtra("position", 0);
                Set_Adapter();
            } else {
                link = appLinkData.toString();
                String[] parts = link.split("=");
                video_id = parts[1];
                Call_Api_For_Singlevideos(parts[1]);
            }

        }


        findViewById(R.id.Goback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        write_layout = findViewById(R.id.write_layout);
        message_edit = findViewById(R.id.message_edit);
        send_btn = findViewById(R.id.send_btn);
        send_btn.setOnClickListener(this);

        send_progress = findViewById(R.id.send_progress);

        keyboardHeightProvider = new KeyboardHeightProvider(this);


        findViewById(R.id.WatchVideo_F).post(new Runnable() {
            public void run() {
                keyboardHeightProvider.start();
            }
        });


        FragmentManager fragmentManager = this.getSupportFragmentManager();
        fragmentManager.findFragmentById(R.id.WatchVideo_F);
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                int count = getSupportFragmentManager().getBackStackEntryCount();
                if (count == 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Functions.black_status_bar(WatchVideos_F.this);
                   }

                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Functions.white_status_bar(WatchVideos_F.this);
                     }
                }
            }
        });


    }


    @Override
    public void onBackPressed() {

        if (video_id != null && link != null) {
            startActivity(new Intent(this, MainMenuActivity.class));
            finish();
        } else {
            super.onBackPressed();
        }

    }


    private void Call_Api_For_Singlevideos(String video_id) {

        try {
            JSONObject parameters = new JSONObject();
            if (Variables.sharedPreferences.getString(Variables.u_id, null) != null)
                parameters.put("user_id", Variables.sharedPreferences.getString(Variables.u_id, "0"));

            parameters.put("video_id", video_id);


            ApiRequest.Call_Api(this, ApiLinks.showVideoDetail, parameters, new Callback() {
                @Override
                public void Responce(String resp) {

                    Singal_Video_Parse_data(resp);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Log.d(Variables.tag, e.toString());
        }
    }


    public void Singal_Video_Parse_data(String responce) {

        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONObject msg = jsonObject.optJSONObject("msg");

                JSONObject Video = msg.optJSONObject("Video");
                JSONObject User = msg.optJSONObject("User");
                JSONObject Sound = msg.optJSONObject("Sound");
                JSONObject UserPrivacy = User.optJSONObject("PrivacySetting");
                JSONObject UserPushNotification = User.optJSONObject("PushNotification");

                Home_Get_Set item = Functions.Parse_video_data(User, Sound, Video, UserPrivacy, UserPushNotification);


                if (data_list != null && !data_list.isEmpty()) {
                    data_list.remove(currentPage);
                    data_list.add(currentPage, item);
                    adapter.notifyDataSetChanged();
                } else {
                    data_list = new ArrayList<>();
                    data_list.add(item);
                    Set_Adapter();
                }

            } else {
                Functions.show_toast(WatchVideos_F.this,jsonObject.optString("msg"));
            }

        } catch (JSONException e) {

            e.printStackTrace();
        }

    }


    public void Set_Adapter() {
        recyclerView = findViewById(R.id.recylerview);
        layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);

        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);


        adapter = new Watch_Videos_Adapter(context, data_list, new Watch_Videos_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(int postion, final Home_Get_Set item, View view) {

                switch (view.getId()) {

                    case R.id.user_pic:
                        onPause();

                        OpenProfile(item, false);
                        break;
                    case R.id.animate_rlt:
                        if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {

                        } else {

                            Intent intent = new Intent(WatchVideos_F.this, Login_A.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
                        }
                        break;
                    case R.id.comment_layout:
                        OpenComment(item);
                        break;

                    case R.id.shared_layout:

                        final VideoAction_F fragment = new VideoAction_F(item.video_id, new Fragment_Callback() {
                            @Override
                            public void Responce(Bundle bundle) {

                                if (bundle.getString("action").equals("save")) {
                                    Save_Video(item);
                                }

                                else if (bundle.getString("action").equals("duet")) {
                                    Duet_video(item);
                                }

                                else if (bundle.getString("action").equals("privacy")) {
                                    Privacy_Video_Setting privacy_video_setting = new Privacy_Video_Setting(new Fragment_Callback() {
                                        @Override
                                        public void Responce(Bundle bundle) {
                                            if (bundle != null) {
                                                if (bundle.getBoolean("call_api")) {
                                                    Call_Api_For_Singlevideos(bundle.getString("video_id"));
                                                }
                                            }
                                        }
                                    });
                                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                                    transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
                                    Bundle bundle1 = new Bundle();
                                    bundle1.putString("video_id", item.video_id);
                                    bundle1.putString("privacy_value", item.privacy_type);
                                    bundle1.putString("duet_value", item.allow_duet);
                                    bundle1.putString("comment_value", item.allow_comments);
                                    bundle1.putString("duet_video_id", item.duet_video_id);
                                    privacy_video_setting.setArguments(bundle1);
                                    transaction.addToBackStack(null);
                                    transaction.replace(R.id.WatchVideo_F, privacy_video_setting).commit();
                                    onPause();
                                }

                                else if (bundle.getString("action").equals("delete")) {

                                    Functions.Show_loader(WatchVideos_F.this, false, false);
                                    Functions.Call_Api_For_Delete_Video(WatchVideos_F.this, item.video_id, new API_CallBack() {
                                        @Override
                                        public void ArrayData(ArrayList arrayList) {

                                        }

                                        @Override
                                        public void OnSuccess(String responce) {

                                            Functions.cancel_loader();
                                            finish();

                                        }

                                        @Override
                                        public void OnFail(String responce) {

                                        }
                                    });

                                }

                                else if (bundle.getString("action").equals("favourite")) {
                                    Favourite_video(item);
                                }

                                else if (bundle.getString("action").equals("not_intrested")) {
                                    Not_Interest_video(item);
                                }

                                else if (bundle.getString("action").equals("report")) {
                                    Open_video_report(item);
                                }


                            }
                        });

                        Bundle bundle = new Bundle();
                        bundle.putString("video_id", item.video_id);
                        bundle.putString("user_id", item.user_id);
                        bundle.putSerializable("data", item);

                        fragment.setArguments(bundle);

                        fragment.show(getSupportFragmentManager(), "");

                        break;


                    case R.id.sound_image_layout:

                        if (check_permissions()) {
                            Intent intent = new Intent(WatchVideos_F.this, VideoSound_A.class);
                            intent.putExtra("data", item);
                            startActivity(intent);
                        }

                        break;

                    case R.id.duet_open_video:
                        Open_duet_video(item);
                        break;
                }

            }
        }, new Watch_Videos_Adapter.LikedClicked() {
            @Override
            public void like_clicked(View view, Home_Get_Set item, int position) {
                if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {
                    view.animate().start();
                    Like_Video(position, item);
                } else {
                    view.animate().cancel();
                    Intent intent = new Intent(getApplicationContext(), Login_A.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
                }
            }
        });

        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);


        // this is the scroll listener of recycler view which will tell the current item number
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //here we find the current item number
                final int scrollOffset = recyclerView.computeVerticalScrollOffset();
                final int height = recyclerView.getHeight();
                int page_no = scrollOffset / height;

                if (page_no != currentPage) {
                    currentPage = page_no;

                    Privious_Player();
                    Set_Player(currentPage);
                }

            }
        });
        recyclerView.scrollToPosition(position);


    }


    private void Open_duet_video(Home_Get_Set item) {
        Intent intent123 = new Intent(this, WatchVideos_F.class);
        intent123.putExtra("video_id", item.duet_video_id);
        startActivity(intent123);
    }


    @Override
    public void onResume() {
        super.onResume();
        keyboardHeightProvider.setKeyboardHeightObserver(this);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }


    int privious_height = 0;
    @Override
    public void onKeyboardHeightChanged(int height, int orientation) {

        Log.d(Variables.tag, "" + height);
        if (height < 0) {
            privious_height = Math.abs(height);;
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(write_layout.getWidth(), write_layout.getHeight());
        params.bottomMargin = height + privious_height;
        write_layout.setLayoutParams(params);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_btn:
                if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {

                    String comment_txt = message_edit.getText().toString();
                    if (!TextUtils.isEmpty(comment_txt)) {
                        Send_Comments(data_list.get(currentPage).user_id, data_list.get(currentPage).video_id, comment_txt);
                    }

                } else {
                    Intent intent = new Intent(getApplicationContext(), Login_A.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
                }
                break;
        }
    }


    @Override
    public void onDataSent(String yourData) {
        int comment_count = Functions.ParseInterger(yourData);
        Home_Get_Set item = data_list.get(currentPage);
        item.video_comment_count = "" + comment_count;
        data_list.add(currentPage, item);
        adapter.notifyDataSetChanged();
    }


    public void Set_Player(final int currentPage) {

        final Home_Get_Set item = data_list.get(currentPage);

        Call_cache();

        Log.d(Variables.tag, item.video_url);

        HttpProxyCacheServer proxy = TicTic.getProxy(context);
        String proxyUrl = proxy.getProxyUrl(item.video_url);
        LoadControl loadControl = new DefaultLoadControl.Builder()
                .setAllocator(new DefaultAllocator(true, 16))
                .setBufferDurationsMs(1 * 1024, 1 * 1024, 500, 1024)
                .setTargetBufferBytes(-1)
                .setPrioritizeTimeOverSizeThresholds(true)
                .createDefaultLoadControl();

        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        final SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(context, trackSelector, loadControl);

        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, context.getResources().getString(R.string.app_name)));


        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(proxyUrl));


        player.prepare(videoSource);

        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.addListener(this);


        View layout = layoutManager.findViewByPosition(currentPage);
        PlayerView playerView = layout.findViewById(R.id.playerview);
        playerView.setPlayer(player);


        player.setPlayWhenReady(true);
        privious_player = player;


        final RelativeLayout mainlayout = layout.findViewById(R.id.mainlayout);
        playerView.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    super.onFling(e1, e2, velocityX, velocityY);
                    float deltaX = e1.getX() - e2.getX();
                    float deltaXAbs = Math.abs(deltaX);
                    // Only when swipe distance between minimal and maximal distance value then we treat it as effective swipe
                    if ((deltaXAbs > 100) && (deltaXAbs < 1000)) {
                        if (deltaX > 0) {
                            OpenProfile(item, true);
                        }
                    }


                    return true;
                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    super.onSingleTapUp(e);
                    if (!player.getPlayWhenReady()) {
                        privious_player.setPlayWhenReady(true);
                    } else {
                        privious_player.setPlayWhenReady(false);
                    }


                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    Show_video_option(item);

                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {


                    if (!player.getPlayWhenReady()) {
                        privious_player.setPlayWhenReady(true);
                    }

                    if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {

                        if (!animation_running) {
                            if (handler != null && runnable != null) {
                                handler.removeCallbacks(runnable);

                            }
                            runnable = new Runnable() {
                                public void run() {
                                    Show_heart_on_DoubleTap(data_list.get(currentPage), mainlayout, e);
                                    Like_Video(currentPage, data_list.get(currentPage));
                                }
                            };
                            handler.postDelayed(runnable, 500);


                        }

                    } else {

                        Intent intent = new Intent(getApplicationContext(), Login_A.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
                    }
                    return super.onDoubleTap(e);

                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });


        SocialTextView desc_txt = layout.findViewById(R.id.desc_txt);
        desc_txt.setOnHashtagClickListener(new SocialView.OnClickListener() {
            @Override
            public void onClick(@NonNull SocialView view, @NonNull CharSequence text) {
                OpenHashtag(text.toString());

            }
        });

        desc_txt.setOnMentionClickListener(new SocialView.OnClickListener() {
            @Override
            public void onClick(@NonNull SocialView view, @NonNull CharSequence text) {

                OpenProfile_byUsername(text.toString());
                Functions.show_toast(context , "" + text);
            }
        });


        LinearLayout soundimage = (LinearLayout) layout.findViewById(R.id.sound_image_layout);
        Animation aniRotate = AnimationUtils.loadAnimation(context, R.anim.d_clockwise_rotation);
        soundimage.startAnimation(aniRotate);

        if (Variables.sharedPreferences.getBoolean(Variables.islogin, false))
            Functions.Call_Api_For_update_view(WatchVideos_F.this, item.video_id);


        if (item.allow_comments != null && item.allow_comments.equalsIgnoreCase("false")) {
            write_layout.setVisibility(View.INVISIBLE);
        } else {
            write_layout.setVisibility(View.VISIBLE);
        }


        Call_Api_For_Singlevideos(item.video_id);
    }


    SimpleExoPlayer cache_player;

    public void Call_cache() {

        if (currentPage + 1 < data_list.size()) {

            if (cache_player != null)
                cache_player.release();

            HttpProxyCacheServer proxy = TicTic.getProxy(context);
            String proxyUrl = proxy.getProxyUrl((data_list.get(currentPage + 1).video_url));

            LoadControl loadControl = new DefaultLoadControl.Builder()
                    .setAllocator(new DefaultAllocator(true, 16))
                    .setBufferDurationsMs(1 * 1024, 1 * 1024, 500, 1024)
                    .setTargetBufferBytes(-1)
                    .setPrioritizeTimeOverSizeThresholds(true)
                    .createDefaultLoadControl();

            DefaultTrackSelector trackSelector = new DefaultTrackSelector();
            cache_player = ExoPlayerFactory.newSimpleInstance(context, trackSelector, loadControl);
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, context.getResources().getString(R.string.app_name)));
            MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(proxyUrl));
            cache_player.prepare(videoSource);

        }

    }

    // when we swipe for another video this will relaese the privious player
    SimpleExoPlayer privious_player;

    public void Privious_Player() {
        if (privious_player != null) {
            privious_player.removeListener(this);
            privious_player.release();
        }
    }


    private void Show_video_option(final Home_Get_Set item) {

        Functions.Show_video_option(context, item, new Callback() {
            @Override
            public void Responce(String resp) {

                if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {
                    if (resp.equalsIgnoreCase("favourite")) {
                        Favourite_video(item);
                    } else if (resp.equalsIgnoreCase("not_intrested")) {
                        Not_Interest_video(item);
                    } else if (resp.equalsIgnoreCase("report")) {
                        Open_video_report(item);
                    }

                } else {

                    Intent intent = new Intent(getApplicationContext(), Login_A.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
                }

            }
        });
    }


    public void Favourite_video(final Home_Get_Set item) {

        JSONObject params = new JSONObject();
        try {
            params.put("video_id", item.video_id);
            params.put("user_id", Variables.sharedPreferences.getString(Variables.u_id, ""));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.Show_loader(context, false, false);
        ApiRequest.Call_Api(this, ApiLinks.addVideoFavourite, params, new Callback() {
            @Override
            public void Responce(String resp) {
                Functions.cancel_loader();

                try {
                    JSONObject jsonObject = new JSONObject(resp);

                    String code = jsonObject.optString("code");
                    if (code.equals("200")) {

                        if (item.favourite != null && item.favourite.equals("0"))
                            item.favourite = "1";
                        else
                            item.favourite = "0";

                        data_list.set(currentPage, item);
                        adapter.notifyDataSetChanged();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });


    }

    public void Not_Interest_video(final Home_Get_Set item) {

        JSONObject params = new JSONObject();
        try {
            params.put("video_id", item.video_id);
            params.put("user_id", Variables.sharedPreferences.getString(Variables.u_id, ""));

        } catch (JSONException e) {
            e.printStackTrace();
        }


        Functions.Show_loader(context, false, false);
        ApiRequest.Call_Api(this, ApiLinks.NotInterestedVideo, params, new Callback() {
            @Override
            public void Responce(String resp) {
                Functions.cancel_loader();
                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");
                    if (code.equals("200")) {
                        data_list.remove(item);
                        adapter.notifyDataSetChanged();
                        Functions.show_toast(context , "Successfully added action!");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });


    }

    public void Open_video_report(Home_Get_Set home_get_set) {
        Report_Type_F _report_typeF = new Report_Type_F(false, null);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);

        Bundle bundle = new Bundle();
        bundle.putString("video_id", home_get_set.video_id);
        _report_typeF.setArguments(bundle);

        transaction.addToBackStack(null);
        transaction.replace(R.id.WatchVideo_F, _report_typeF).commit();
        onPause();
    }


    public void Show_heart_on_DoubleTap(Home_Get_Set item, final RelativeLayout mainlayout, MotionEvent e) {

        int x = (int) e.getX() - 100;
        int y = (int) e.getY() - 100;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        final ImageView iv = new ImageView(getApplicationContext());
        lp.setMargins(x, y, 0, 0);
        iv.setLayoutParams(lp);
        if (item.liked.equals("1"))
            iv.setImageDrawable(getResources().getDrawable(
                    R.drawable.ic_like));
        else
            iv.setImageDrawable(getResources().getDrawable(
                    R.drawable.ic_like_fill));

        mainlayout.addView(iv);
        Animation fadeoutani = AnimationUtils.loadAnimation(context, R.anim.fade_out);

        fadeoutani.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // this will call when animation start
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mainlayout.removeView(iv);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // this will call when animation start again
            }
        });
        iv.startAnimation(fadeoutani);

    }

    // this function will call for like the video and Call an Api for like the video
    public void Like_Video(final int position, final Home_Get_Set home_get_set) {

        String action = home_get_set.liked;

        if (action.equals("1")) {
            action = "0";
            home_get_set.like_count = "" + (Functions.ParseInterger(home_get_set.like_count) - 1);
        } else {
            action = "1";
            home_get_set.like_count = "" + (Functions.ParseInterger(home_get_set.like_count) + 1);
        }


        data_list.remove(position);
        home_get_set.liked = action;
        data_list.add(position, home_get_set);
        adapter.notifyDataSetChanged();


        Functions.Call_Api_For_like_video(this, home_get_set.video_id, action, null);
    }


    public boolean check_permissions() {

        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
        };

        if (!hasPermissions(context, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 2);
        } else {

            return true;
        }

        return false;
    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    // this will open the comment screen
    public void OpenComment(Home_Get_Set item) {
        int comment_count = Functions.ParseInterger(item.video_comment_count);
        Fragment_Data_Send fragment_data_send = this;

        Comment_F comment_f = new Comment_F(comment_count, fragment_data_send, "WatchVideos_F");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("video_id", item.video_id);
        args.putString("user_id", item.user_id);
        args.putSerializable("data", item);
        comment_f.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.WatchVideo_F, comment_f).commit();

    }


    // this will open the profile of user which have uploaded the currenlty running video
    private void OpenProfile(final Home_Get_Set item, boolean from_right_to_left) {

        if (Variables.sharedPreferences.getString(Variables.u_id, "0").equals(item.user_id)) {

            TabLayout.Tab profile = MainMenuFragment.tabLayout.getTabAt(4);
            profile.select();

        } else {

            Profile_F profile_f = new Profile_F(new Fragment_Callback() {
                @Override
                public void Responce(Bundle bundle) {

                    Call_Api_For_Singlevideos(item.video_id);

                }
            });
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            if (from_right_to_left)
                transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
            else
                transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);

            Bundle args = new Bundle();
            args.putString("user_id", item.user_id);
            args.putString("user_name", item.username);
            args.putString("user_pic", item.profile_pic);
            profile_f.setArguments(args);
            transaction.addToBackStack(null);
            transaction.replace(R.id.WatchVideo_F, profile_f).commit();

        }


    }


    private void OpenProfile_byUsername(String username) {

        if (Variables.sharedPreferences.getString(Variables.u_name, "0").equals(username)) {

            TabLayout.Tab profile = MainMenuFragment.tabLayout.getTabAt(4);
            profile.select();

        } else {

            Profile_F profile_f = new Profile_F();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
            Bundle args = new Bundle();
            args.putString("user_name", username);
            profile_f.setArguments(args);
            transaction.addToBackStack(null);
            transaction.replace(R.id.WatchVideo_F, profile_f).commit();

        }


    }


    public void Send_Comments(final String user_id, String video_id, final String comment) {

        send_progress.setVisibility(View.VISIBLE);
        send_btn.setVisibility(View.GONE);

        Functions.Call_Api_For_Send_Comment(this, video_id, comment, new API_CallBack() {
            @Override
            public void ArrayData(ArrayList arrayList) {

                message_edit.setText(null);
                send_progress.setVisibility(View.GONE);
                send_btn.setVisibility(View.VISIBLE);

                int comment_count = Functions.ParseInterger(data_list.get(currentPage).video_comment_count);
                comment_count++;
                onDataSent("" + comment_count);


            }

            @Override
            public void OnSuccess(String responce) {
                // this method will be call when data return in string form
            }

            @Override
            public void OnFail(String responce) {
                // on faild api this will be call
            }
        });

    }


    public void Duet_video(final Home_Get_Set item) {

        Log.d(Variables.tag, item.video_url);
        if (item.video_url != null) {

            Functions.Show_determinent_loader(context, false, false);
            PRDownloader.initialize(getApplicationContext());
            DownloadRequest prDownloader = PRDownloader.download(item.video_url, Variables.app_showing_folder, item.video_id + ".mp4")
                    .build()

                    .setOnProgressListener(new OnProgressListener() {
                        @Override
                        public void onProgress(Progress progress) {
                            int prog = (int) ((progress.currentBytes * 100) / progress.totalBytes);
                            Functions.Show_loading_progress(prog);

                        }
                    });


            prDownloader.start(new OnDownloadListener() {
                @Override
                public void onDownloadComplete() {
                    Functions.cancel_determinent_loader();
                    Open_duet_Recording(item);
                }

                @Override
                public void onError(Error error) {
                    Functions.show_toast(context , "Error");
                    Functions.cancel_determinent_loader();
                }


            });

        }

    }

    public void Open_duet_Recording(Home_Get_Set item) {
        Intent intent = new Intent(WatchVideos_F.this, Video_Recoder_Duet_A.class);
        intent.putExtra("data", item);
        startActivity(intent);
    }


    // this will open the profile of user which have uploaded the currenlty running video
    private void OpenHashtag(String tag) {

        Taged_Videos_F taged_videos_f = new Taged_Videos_F();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("tag", tag);
        taged_videos_f.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.WatchVideo_F, taged_videos_f).commit();

    }


    public void Save_Video(final Home_Get_Set item) {

        JSONObject params = new JSONObject();
        try {
            params.put("video_id", item.video_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.Show_loader(context, false, false);
        ApiRequest.Call_Api(this, ApiLinks.downloadVideo, params, new Callback() {
            @Override
            public void Responce(String resp) {
                Functions.cancel_loader();
                try {
                    JSONObject responce = new JSONObject(resp);
                    String code = responce.optString("code");
                    if (code.equals("200")) {
                        final String download_url = responce.optString("msg");

                        if (download_url != null) {

                            Functions.Show_determinent_loader(context, false, false);
                            PRDownloader.initialize(getApplicationContext());
                            DownloadRequest prDownloader = PRDownloader.download(ApiLinks.BASE_URL + download_url, Variables.app_showing_folder, item.video_id + ".mp4")
                                    .build()
                                    .setOnProgressListener(new OnProgressListener() {
                                        @Override
                                        public void onProgress(Progress progress) {

                                            int prog = (int) ((progress.currentBytes * 100) / progress.totalBytes);
                                            Functions.Show_loading_progress(prog);

                                        }
                                    });


                            prDownloader.start(new OnDownloadListener() {
                                @Override
                                public void onDownloadComplete() {
                                    Functions.cancel_determinent_loader();
                                    Delete_water_marke_video(download_url);
                                    Scan_file(item);
                                }

                                @Override
                                public void onError(Error error) {
                                    Functions.show_toast(context , "Error");
                                    Functions.cancel_determinent_loader();
                                }


                            });

                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });


    }


    public void Delete_water_marke_video(String video_url) {

        JSONObject params = new JSONObject();
        try {
            params.put("video_url", video_url);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(this, ApiLinks.deleteWaterMarkVideo, params, null);


    }


    public void Scan_file(Home_Get_Set item) {
        MediaScannerConnection.scanFile(WatchVideos_F.this,
                new String[]{Variables.app_showing_folder + item.video_id + ".mp4"},
                null,
                new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri) {

                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }


    // this is lifecyle of the Activity which is importent for play,pause video or relaese the player
    @Override
    public void onPause() {
        super.onPause();
        if (privious_player != null) {
            privious_player.setPlayWhenReady(false);
        }

    }


    @Override
    public void onStop() {
        super.onStop();
        if (privious_player != null) {
            privious_player.setPlayWhenReady(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (privious_player != null) {
            privious_player.release();
        }
        keyboardHeightProvider.setKeyboardHeightObserver(null);
        keyboardHeightProvider.close();
    }


    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        if (playbackState == Player.STATE_BUFFERING) {
            p_bar.setVisibility(View.VISIBLE);
        } else if (playbackState == Player.STATE_READY) {
            p_bar.setVisibility(View.GONE);
        }

    }


}
