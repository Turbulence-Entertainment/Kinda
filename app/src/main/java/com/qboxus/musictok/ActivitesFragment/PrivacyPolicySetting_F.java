package com.qboxus.musictok.ActivitesFragment;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.qboxus.musictok.Models.PrivacyPolicySettingModel;
import com.qboxus.musictok.Models.PushNotificationSettingModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONException;
import org.json.JSONObject;

import io.paperdb.Paper;


public class PrivacyPolicySetting_F extends RootFragment implements View.OnClickListener{

    View view;
    TextView allow_download_txt,allow_commenet_txt,allow_direct_mesg_txt,allow_duet_txt,
            allow_view_likevid_txt;

    String cancel="Cancel";

    PrivacyPolicySettingModel privacyPolicySetting_model;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_privacy_policy_setting_, container, false);
        InitControl();
        return view;
    }


    private void InitControl() {
        privacyPolicySetting_model= (PrivacyPolicySettingModel) Paper.book("Setting").read("PrivacySettingModel");
        allow_download_txt= view.findViewById(R.id.allow_download_txt);
        allow_commenet_txt=view.findViewById(R.id.allow_commenet_txt);
        allow_direct_mesg_txt=   view.findViewById(R.id.allow_direct_mesg_txt);
        allow_duet_txt=view.findViewById(R.id.allow_duet_txt);
        allow_view_likevid_txt= view.findViewById(R.id.allow_view_likevid_txt);

        SetUpScreenData();

        view.findViewById(R.id.PrivacyPolicySetting_F_img_back).setOnClickListener(this);
        view.findViewById(R.id.clickLess).setOnClickListener(this);

        view.findViewById(R.id.allow_download_layout).setOnClickListener(this);
        view.findViewById(R.id.allow_commenet_layout).setOnClickListener(this);
        view.findViewById(R.id.allow_dmesges_layout).setOnClickListener(this);
        view.findViewById(R.id.allow_duet_layout).setOnClickListener(this);
        view.findViewById(R.id.allow_view_likevid_layout).setOnClickListener(this);
    }

    private void SetUpScreenData() {
      try {
          if (privacyPolicySetting_model.getVideos_download().equals("1"))
          {
              allow_download_txt.setText(Functions.StringParseFromServerRestriction("On"));
          }
          else
          {
              allow_download_txt.setText(Functions.StringParseFromServerRestriction("Off"));
          }
          allow_commenet_txt.setText(Functions.StringParseFromServerRestriction(privacyPolicySetting_model.getVideo_comment()));
          allow_direct_mesg_txt.setText(Functions.StringParseFromServerRestriction(privacyPolicySetting_model.getDirect_message()));
          allow_duet_txt.setText(Functions.StringParseFromServerRestriction(privacyPolicySetting_model.getDuet()));
          allow_view_likevid_txt.setText(Functions.StringParseFromServerRestriction(privacyPolicySetting_model.getLiked_videos()));
      }
      catch (Exception e)
      {
          e.getStackTrace();
      }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.clickLess:
                break;
            case R.id.PrivacyPolicySetting_F_img_back:
                getActivity().onBackPressed();
                break;
            case R.id.allow_download_layout:
                final CharSequence[] options = { "On","Off","Cancel" };
                selectImage("Select download option",options,(TextView)view.findViewById(R.id.allow_download_txt),1);
                break;
            case R.id.allow_commenet_layout:
                final CharSequence[] Commentoptions = { "Everyone","Friend","No One","Cancel" };
                selectImage("Select Comment option",Commentoptions,(TextView) view.findViewById(R.id.allow_commenet_txt),2);
                break;
            case R.id.allow_dmesges_layout:
                final CharSequence[] messgeoptions = { "Everyone","Friend","No One","Cancel" };
                selectImage("Select message option",messgeoptions,allow_direct_mesg_txt,3);
                break;
            case R.id.allow_duet_layout:
                final CharSequence[] duetoption = { "Everyone","Friend","No One","Cancel" };
                selectImage("Select duet option",duetoption,allow_duet_txt,4);
                break;
            case R.id.allow_view_likevid_layout:
                final CharSequence[] likevidoption = { "Everyone","Only Me","Cancel" };
                selectImage("Select like video option",likevidoption,allow_view_likevid_txt,5);
                break;

        }
    }
    private void selectImage(String title, final CharSequence[] options, final TextView textView, final int Selected_box) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(),R.style.AlertDialogCustom);
        builder.setTitle(title);
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item){
                CharSequence op = options[item];
                if (!op.equals(cancel)) {
                    textView.setText((""+options[item]).toUpperCase());

                    switch (Selected_box)
                    {
                        case 1:
                            if (op.toString().equalsIgnoreCase("On"))
                            {
                                str_video_download="1";
                            }
                            else
                            {
                                str_video_download="0";
                            }
                            break;
                        case 2:
                            if (op.toString().equalsIgnoreCase("Everyone"))
                            {
                                str_video_comment=Functions.StringParseIntoServerRestriction("Everyone");
                            }
                            else
                            if (op.toString().equalsIgnoreCase("Friend"))
                            {
                                str_video_comment=Functions.StringParseIntoServerRestriction("Friend");
                            }
                            else
                            {
                                str_video_comment=Functions.StringParseIntoServerRestriction("No One");
                            }
                            break;
                        case 3:
                            if (op.toString().equalsIgnoreCase("Everyone"))
                            {
                                str_direct_message=Functions.StringParseIntoServerRestriction("Everyone");
                            }
                            else
                            if (op.toString().equalsIgnoreCase("Friend"))
                            {
                                str_direct_message=Functions.StringParseIntoServerRestriction("Friend");
                            }
                            else
                            {
                                str_direct_message=Functions.StringParseIntoServerRestriction("No One");
                            }
                            break;
                        case 4:
                            if (op.toString().equalsIgnoreCase("Everyone"))
                            {
                                str_duet=Functions.StringParseIntoServerRestriction("Everyone");
                            }
                            else
                            if (op.toString().equalsIgnoreCase("Friend"))
                            {
                                str_duet=Functions.StringParseIntoServerRestriction("Friend");
                            }
                            else
                            {
                                str_duet=Functions.StringParseIntoServerRestriction("No One");
                            }
                            break;
                        case 5:
                            if (op.toString().equalsIgnoreCase("Everyone"))
                            {
                                str_liked_video=Functions.StringParseIntoServerRestriction("Everyone");
                            }
                            else
                            {
                                str_liked_video=Functions.StringParseIntoServerRestriction("Only Me");
                            }

                            break;

                    }
                    Call_Api();
                }
            }
        });
        builder.show();
    }


    String str_video_download,str_direct_message,str_duet,str_liked_video,str_video_comment;
    public void Call_Api(){

        JSONObject params=new JSONObject();
        try {
            params.put("videos_download",str_video_download);
            params.put("direct_message",str_direct_message);
            params.put("duet",str_duet);
            params.put("liked_videos",str_liked_video);
            params.put("video_comment",str_video_comment);
            params.put("user_id", Variables.sharedPreferences.getString(Variables.u_id,""));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(getActivity(), ApiLinks.addPolicySetting, params, new Callback() {
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
                JSONObject privacy_policy_setting=msg.optJSONObject("PrivacySetting");
                privacyPolicySetting_model=new PrivacyPolicySettingModel();
                privacyPolicySetting_model.setVideos_download(""+privacy_policy_setting.optString("videos_download"));
                privacyPolicySetting_model.setDirect_message(""+privacy_policy_setting.optString("direct_message"));
                privacyPolicySetting_model.setDuet(""+privacy_policy_setting.optString("duet"));
                privacyPolicySetting_model.setLiked_videos(""+privacy_policy_setting.optString("liked_videos"));
                privacyPolicySetting_model.setVideo_comment(""+privacy_policy_setting.optString("video_comment"));
                Paper.book("Setting").write("PrivacySettingModel",privacyPolicySetting_model);

                Functions.show_toast(view.getContext(),"Privacy Setting Updated");
            }
            else {
                Functions.show_toast(getActivity(),jsonObject.optString("msg"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



}