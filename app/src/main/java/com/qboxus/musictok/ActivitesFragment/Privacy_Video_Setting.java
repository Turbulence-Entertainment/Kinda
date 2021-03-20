package com.qboxus.musictok.ActivitesFragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.Interfaces.Fragment_Callback;
import com.qboxus.musictok.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONException;
import org.json.JSONObject;

public class Privacy_Video_Setting extends Fragment implements View.OnClickListener {

    View view;
    TextView view_video;
    Switch allow_comment_switch, allow_duet_switch;
    String video_id, comment_value, duet_value, privacy_value, duet_video_id;
    RelativeLayout allow_duet_layout;
    Fragment_Callback callback;
    Boolean call_api = false;
    Bundle bundle;

    public Privacy_Video_Setting(Fragment_Callback callback) {
        this.callback = callback;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_privacy_video_setting, container, false);
        view_video = view.findViewById(R.id.view_video);
        allow_duet_layout = view.findViewById(R.id.allow_duet_layout);

        allow_comment_switch = view.findViewById(R.id.allow_comment_switch);
        allow_comment_switch.setOnClickListener(this);

        allow_duet_switch = view.findViewById(R.id.allow_duet_switch);
        allow_duet_switch.setOnClickListener(this);

        view.findViewById(R.id.view_video_layout).setOnClickListener(this);
        view.findViewById(R.id.back_btn).setOnClickListener(this);

        Bundle bundle = getArguments();
        if (bundle != null) {

            video_id = bundle.getString("video_id");
            privacy_value = bundle.getString("privacy_value");
            duet_value = bundle.getString("duet_value");
            comment_value = bundle.getString("comment_value");
            duet_video_id = bundle.getString("duet_video_id");

            view_video.setText(privacy_value);

            allow_comment_switch.setChecked(comment_value(comment_value));

            allow_duet_switch.setChecked(getTrueFalseCondition(duet_value));

            if (Variables.is_enable_duet && (duet_video_id != null && duet_video_id.equalsIgnoreCase("0"))) {
                allow_duet_layout.setVisibility(View.VISIBLE);
            }
        }
        return view;
    }


    private boolean getTrueFalseCondition(String str) {
        if (str.equalsIgnoreCase("1"))
            return true;
        else
            return false;
    }


    private boolean comment_value(String str) {
        if (str.equalsIgnoreCase("true"))
            return true;
        else
            return false;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_video_layout:
                open_dialog_for_privacy(getActivity());
                break;

            case R.id.back_btn:
                getActivity().onBackPressed();
                break;

            case R.id.allow_duet_switch:
                if (allow_duet_switch.isChecked()) {
                    duet_value = "1";
                } else {
                    duet_value = "0";
                }
                Call_Api();
                break;

            case R.id.allow_comment_switch:
                if (allow_comment_switch.isChecked()) {
                    comment_value = "true";
                } else {
                    comment_value = "false";
                }
                Call_Api();
                break;

            default:
                break;


        }

    }


    public void Call_Api() {

        JSONObject params = new JSONObject();
        try {
            params.put("video_id", video_id);
            params.put("allow_comments", comment_value);
            params.put("allow_duet", duet_value);
            params.put("privacy_type", view_video.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(Variables.tag, "params at video_setting: " + params);

        ApiRequest.Call_Api(getActivity(), ApiLinks.updateVideoDetail, params, new Callback() {
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
                Functions.show_toast(view.getContext(),"Setting Update");

                call_api = true;
                bundle = new Bundle();
                bundle.putString("video_id", video_id);
                bundle.putBoolean("call_api", call_api);
            } else {
                Functions.show_toast(getActivity(),jsonObject.optString("msg"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    private void open_dialog_for_privacy(Context context) {
        final CharSequence[] options = {"Public", "Private"};

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);

        builder.setTitle(null);

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int item) {
                view_video.setText(options[item]);
                Call_Api();
                dialog.dismiss();

            }

        });

        builder.show();

    }


    @Override
    public void onDetach() {
        super.onDetach();
        if (callback != null) {
            callback.Responce(bundle);
        }
    }


}