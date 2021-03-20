package com.qboxus.musictok.Main_Menu;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.InstanceIdResult;
import com.qboxus.musictok.ActivitesFragment.Chat.Chat_Activity;
import com.qboxus.musictok.ActivitesFragment.Home_F;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.R;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Fragment_Callback;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.NetworkUtils;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class MainMenuActivity extends AppCompatActivity {
    public static MainMenuActivity mainMenuActivity;
    private MainMenuFragment mainMenuFragment;
    long mBackPressed;



    public static Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        mainMenuActivity=this;

        intent=getIntent();

        setIntent(null);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        Variables.screen_height= displayMetrics.heightPixels;
        Variables.screen_width= displayMetrics.widthPixels;


        Variables.user_id=Functions.getSharedPreference(this).getString(Variables.u_id,"");
        Variables.user_name=Functions.getSharedPreference(this).getString(Variables.u_name,"");
        Variables.user_pic=Functions.getSharedPreference(this).getString(Variables.u_pic,"");



        if(Functions.getSharedPreference(this).getBoolean(Variables.islogin,false)){
            Get_Public_IP();
        }

        if (savedInstanceState == null) {

            initScreen();

        }

        else {
            mainMenuFragment = (MainMenuFragment) getSupportFragmentManager().getFragments().get(0);
        }



        Functions.make_directry(Variables.app_hided_folder);
        Functions.make_directry(Variables.app_showing_folder);
        Functions.make_directry(Variables.draft_app_folder);

        Log.d(Variables.tag,"Top status bar height:"+getStatusBarHeight(this));
        Log.d(Variables.tag,"Bottom Navigation bar height:"+getNavigation_barHeight(this));


    }


    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        if(intent!=null){
            String type=intent.getStringExtra("type");
            if(type!=null && type.equalsIgnoreCase("message")){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Chat_Activity chat_activity = new Chat_Activity(new Fragment_Callback() {
                            @Override
                            public void Responce(Bundle bundle) {

                            }
                        });
                        FragmentTransaction transaction = MainMenuActivity.mainMenuActivity.getSupportFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);

                        Bundle args = new Bundle();
                        args.putString("user_id", intent.getStringExtra("user_id"));
                        args.putString("user_name", intent.getStringExtra("user_name"));
                        args.putString("user_pic", intent.getStringExtra("user_pic"));

                        chat_activity.setArguments(args);
                        transaction.addToBackStack(null);
                        transaction.replace(R.id.MainMenuFragment, chat_activity).commit();
                    }
                },2000);

            }
        }

    }

    private void initScreen() {
        mainMenuFragment = new MainMenuFragment();
        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, mainMenuFragment)
                .commit();

        findViewById(R.id.container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    public void Get_Public_IP(){
        ApiRequest.Call_Api_GetRequest(this, "https://api.ipify.org/?format=json", new Callback() {
            @Override
            public void Responce(String resp) {
                try {
                    JSONObject responce=new JSONObject(resp);
                    String ip=responce.optString("ip");

                    Add_Firebase_tokon(ip);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void Add_Firebase_tokon(String ip){

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }
                        String token = task.getResult().getToken();
                        Functions.getSharedPreference(MainMenuActivity.this).edit().putString(Variables.device_token,token).commit();

                        JSONObject params=new JSONObject();
                        try {
                            params.put("user_id",Functions.getSharedPreference(MainMenuActivity.this).getString(Variables.u_id,""));
                            params.put("device",getString(R.string.device));
                            params.put("version",getString(R.string.version));
                            params.put("ip",""+ip);
                            params.put("device_token",token);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        ApiRequest.Call_Api(MainMenuActivity.this, ApiLinks.addDeviceData,params,null);

                    }
                });

    }

    @Override
    public void onBackPressed() {
        if (!mainMenuFragment.onBackPressed()) {
            int count = this.getSupportFragmentManager().getBackStackEntryCount();
            if (count == 0) {
                if (mBackPressed + 2000 > System.currentTimeMillis()) {
                    super.onBackPressed();
                    return;
                } else {
                    Functions.show_toast(getBaseContext(),"Tap Again To Exit");
                    mBackPressed = System.currentTimeMillis();

                }
            } else {
                super.onBackPressed();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Functions.deleteCache(this);
    }


    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    public static int getNavigation_barHeight(Context context){
        int navigationBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return navigationBarHeight;
    }


}
