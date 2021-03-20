package com.qboxus.musictok.ActivitesFragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

import com.qboxus.musictok.Main_Menu.MainMenuActivity;
import com.qboxus.musictok.R;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONException;
import org.json.JSONObject;

public class Splash_A extends AppCompatActivity {


    CountDownTimer countDownTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);


        Variables.sharedPreferences = getSharedPreferences(Variables.pref_name, MODE_PRIVATE);





        if(Variables.sharedPreferences.getString(Variables.device_id,"0").equals("0")) {
            Call_api_register_device();
        }
        else
            Set_Timer();


    }


    public void Set_Timer(){
        countDownTimer = new CountDownTimer(2500, 500) {

            public void onTick(long millisUntilFinished) {
                // this will call on every 500 ms
            }

            public void onFinish() {

                Intent intent=new Intent(Splash_A.this, MainMenuActivity.class);

                if(getIntent().getExtras()!=null) {
                    intent.putExtras(getIntent().getExtras());
                    setIntent(null);
                }

                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                finish();

            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countDownTimer.cancel();
    }

    public void Call_api_register_device(){
        String androidId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        JSONObject param=new JSONObject();
        try {
            param.put("key",androidId);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(this, ApiLinks.registerDevice, param, new Callback() {
            @Override
            public void Responce(String resp) {

                try {
                    JSONObject jsonObject=new JSONObject(resp);
                    String code=jsonObject.optString("code");
                    if(code.equals("200")){

                        Set_Timer();

                        JSONObject msg=jsonObject.optJSONObject("msg");
                        JSONObject Device=msg.optJSONObject("Device");
                        SharedPreferences.Editor editor2 =  Variables.sharedPreferences.edit();
                        editor2.putString(Variables.device_id, Device.optString("id")).commit();


                    }else {
                        Call_api_show_register_device();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }



            }
        });
       }

    public void Call_api_show_register_device(){
        String androidId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
        JSONObject param=new JSONObject();
        try {
            param.put("key",androidId);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(this, ApiLinks.showDeviceDetail, param, new Callback() {
            @Override
            public void Responce(String resp) {

                try {
                    JSONObject jsonObject=new JSONObject(resp);
                    String code=jsonObject.optString("code");
                    if(code.equals("200")){

                        Set_Timer();

                        JSONObject msg=jsonObject.optJSONObject("msg");
                        JSONObject Device=msg.optJSONObject("Device");
                        SharedPreferences.Editor editor2 =  Variables.sharedPreferences.edit();
                        editor2.putString(Variables.device_id, Device.optString("id")).commit();


                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }



            }
        });
    }

}
