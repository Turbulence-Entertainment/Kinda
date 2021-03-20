package com.qboxus.musictok.ActivitesFragment;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.qboxus.musictok.Models.PushNotificationSettingModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONException;
import org.json.JSONObject;

import io.paperdb.Paper;

public class Push_Notification_Setting_F extends RootFragment implements View.OnClickListener{


    View view;
    LinearLayout linearLayout;
    ImageView img_back;
    Switch st_likes,st_comment,st_new_follow,st_mention,st_direct_message,st_video_update;
    String str_likes="1",str_comment="1",str_new_follow="1",str_mention="1",str_direct_message="1",str_video_update="1";

    PushNotificationSettingModel pushNotificationSetting_model;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_push_notification_setting, container, false);
        InitControl();
        return view;
    }


    private void InitControl() {
        pushNotificationSetting_model= (PushNotificationSettingModel) Paper.book("Setting").read("PushSettingModel");
        linearLayout=view.findViewById(R.id.clickLess);
        linearLayout.setOnClickListener(this);
        img_back=view.findViewById(R.id.Push_Notification_Setting_F_img_back);
        img_back.setOnClickListener(this);
        st_likes=view.findViewById(R.id.Push_Notification_Setting_F_st_likes);
        st_likes.setOnClickListener(this);
        st_comment=view.findViewById(R.id.Push_Notification_Setting_F_st_comments);
        st_comment.setOnClickListener(this);
        st_new_follow=view.findViewById(R.id.Push_Notification_Setting_F_st_new_follower);
        st_new_follow.setOnClickListener(this);
        st_mention=view.findViewById(R.id.Push_Notification_Setting_F_st_mention);
        st_mention.setOnClickListener(this);
        st_direct_message=view.findViewById(R.id.Push_Notification_Setting_F_st_direct_message);
        st_direct_message.setOnClickListener(this);
        st_video_update=view.findViewById(R.id.Push_Notification_Setting_F_st_video_update);
        st_video_update.setOnClickListener(this);

        SetUpScreenData();

    }


    private void SetUpScreenData() {
        try {
            str_likes=pushNotificationSetting_model.getLikes();
            st_likes.setChecked(getTrueFalseCondition(str_likes));

            str_video_update=pushNotificationSetting_model.getVideoupdates();
            st_video_update.setChecked(getTrueFalseCondition(str_video_update));

            str_direct_message=pushNotificationSetting_model.getDirectmessage();
            st_direct_message.setChecked(getTrueFalseCondition(str_direct_message));

            str_mention=pushNotificationSetting_model.getMentions();
            st_mention.setChecked(getTrueFalseCondition(str_mention));

            str_new_follow=pushNotificationSetting_model.getNewfollowers();
            st_new_follow.setChecked(getTrueFalseCondition(str_new_follow));

            str_comment=pushNotificationSetting_model.getComments();
            st_comment.setChecked(getTrueFalseCondition(str_comment));
        }
        catch (Exception e)
        {
            e.getStackTrace();
        }
    }


    private boolean getTrueFalseCondition(String str) {
        if (str.equalsIgnoreCase("1"))
            return true;
        else
            return false;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.Push_Notification_Setting_F_img_back:
                getActivity().onBackPressed();
                break;
            case R.id.clickLess:
                break;
            case R.id.Push_Notification_Setting_F_st_likes:
                if (st_likes.isChecked())
                {
                    str_likes="1";
                }
                else
                {
                    str_likes="0";
                }
                Call_Api();
                break;
            case R.id.Push_Notification_Setting_F_st_comments:
                if (st_comment.isChecked())
                {
                    str_comment="1";
                }
                else
                {
                    str_comment="0";
                }
                Call_Api();
                break;
            case R.id.Push_Notification_Setting_F_st_new_follower:
                if (st_new_follow.isChecked())
                {
                    str_new_follow="1";
                }
                else
                {
                    str_new_follow="0";
                }
                Call_Api();
                break;
            case R.id.Push_Notification_Setting_F_st_mention:
                if (st_mention.isChecked())
                {
                    str_mention="1";
                }
                else
                {
                    str_mention="0";
                }
                Call_Api();
                break;
            case R.id.Push_Notification_Setting_F_st_direct_message:
                if (st_direct_message.isChecked())
                {
                    str_direct_message="1";
                }
                else
                {
                    str_direct_message="0";
                }
                Call_Api();
                break;
            case R.id.Push_Notification_Setting_F_st_video_update:
                if (st_video_update.isChecked())
                {
                    str_video_update="1";
                }
                else
                {
                    str_video_update="0";
                }
                Call_Api();
                break;
        }
    }


    public void Call_Api(){

        JSONObject params=new JSONObject();
        try {
            params.put("likes",str_likes);
            params.put("comments",str_comment);
            params.put("new_followers",str_new_follow);
            params.put("mentions",str_mention);
            params.put("video_updates",str_video_update);
            params.put("direct_messages",str_direct_message);
            params.put("user_id", Variables.sharedPreferences.getString(Variables.u_id,""));

        }

        catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(getActivity(), ApiLinks.updatePushNotificationSetting, params, new Callback() {
            @Override
            public void Responce(String resp) {

                Parse_video(resp);


            }
        });

    }


    public void Parse_video(String responce){

        try {
            JSONObject jsonObject=new JSONObject(responce);
            String code=jsonObject.optString("code");

            if(code.equals("200")){

                JSONObject msg=jsonObject.optJSONObject("msg");
                JSONObject push_notification_setting=msg.optJSONObject("PushNotification");
                pushNotificationSetting_model=new PushNotificationSettingModel();
                pushNotificationSetting_model.setComments(""+push_notification_setting.optString("comments"));
                pushNotificationSetting_model.setLikes(""+push_notification_setting.optString("likes"));
                pushNotificationSetting_model.setNewfollowers(""+push_notification_setting.optString("new_followers"));
                pushNotificationSetting_model.setMentions(""+push_notification_setting.optString("mentions"));
                pushNotificationSetting_model.setDirectmessage(""+push_notification_setting.optString("direct_messages"));
                pushNotificationSetting_model.setVideoupdates(""+push_notification_setting.optString("video_updates"));
                Paper.book("Setting").write("PushSettingModel",pushNotificationSetting_model);

                Functions.show_toast(view.getContext(),"Setting Update");
            }

            else {
                Functions.show_toast(getActivity(),jsonObject.optString("msg"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


}