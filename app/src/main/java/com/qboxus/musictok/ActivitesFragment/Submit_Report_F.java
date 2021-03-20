package com.qboxus.musictok.ActivitesFragment;

import android.os.Bundle;

import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.qboxus.musictok.ActivitesFragment.Video_Recording.Preview_Video_A;
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


public class Submit_Report_F extends RootFragment implements View.OnClickListener {
    View view;
    TextView report_type_txt;
    EditText report_description_txt;
    String report_id,txt_report_type, video_id,user_id;
    Fragment_Callback fragment_callback;

    public Submit_Report_F() {
    }
    public Submit_Report_F(Fragment_Callback fragment_callback) {
        this.fragment_callback=fragment_callback;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_submit_report, container, false);

        Bundle bundle = getArguments();
        if(bundle!=null) {
            report_id=bundle.getString("report_id");
            txt_report_type = bundle.getString("report_type");
            video_id = bundle.getString("video_id");
            user_id=bundle.getString("user_id");
        }


        report_type_txt = view.findViewById(R.id.report_type);
        report_type_txt.setText(txt_report_type);

        report_description_txt=view.findViewById(R.id.report_description_txt);



        view.findViewById(R.id.back_btn).setOnClickListener(this);
        view.findViewById(R.id.report_reason_layout).setOnClickListener(this);
        view.findViewById(R.id.submit_btn).setOnClickListener(this);

        return view;
    }


    public boolean Check_validation(){

        if(TextUtils.isEmpty(report_type_txt.getText())){
            Functions.show_toast(getContext(),"Please give some reason.");
            return false;
        }

        return true;
    }

    public void Call_Api_report_video(){

        JSONObject params=new JSONObject();
        try {

            params.put("user_id", Variables.sharedPreferences.getString(Variables.u_id,""));
            params.put("video_id",video_id);
            params.put("report_reason_id",report_id);
            params.put("description",report_description_txt.getText());

        } catch (JSONException e) {
            e.printStackTrace();
        }



        Functions.Show_loader(getContext(),false,false);
        ApiRequest.Call_Api(getActivity(), ApiLinks.reportVideo, params, new Callback() {
            @Override
            public void Responce(String resp) {
                Functions.cancel_loader();

                try {
                    JSONObject jsonObject=new JSONObject(resp);
                    String code=jsonObject.optString("code");
                    if(code.equals("200")){
                        Functions.show_toast(getContext(),"Report submitted successfully");
                        if(fragment_callback!=null)
                            fragment_callback.Responce(null);

                        getActivity().onBackPressed();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    public void Call_Api_report_user(){

        JSONObject params=new JSONObject();
        try {

            params.put("user_id", Variables.sharedPreferences.getString(Variables.u_id,""));
            params.put("report_user_id",user_id);
            params.put("report_reason_id",report_id);
            params.put("description",report_description_txt.getText());

        } catch (JSONException e) {
            e.printStackTrace();
        }



        Functions.Show_loader(getContext(),false,false);
        ApiRequest.Call_Api(getActivity(), ApiLinks.reportUser, params, new Callback() {
            @Override
            public void Responce(String resp) {
                Functions.cancel_loader();

                try {
                    JSONObject jsonObject=new JSONObject(resp);
                    String code=jsonObject.optString("code");
                    if(code.equals("200")){
                        Functions.show_toast(getContext(),"Report submitted successfully");
                        if(fragment_callback!=null)
                            fragment_callback.Responce(null);

                        getActivity().onBackPressed();

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.submit_btn:
                if(video_id!=null && Check_validation())
                    Call_Api_report_video();

                else if(user_id!=null && Check_validation())
                    Call_Api_report_user();

                break;

            case R.id.report_reason_layout:
                Report_Type_F _report_typeF = new Report_Type_F(true,new Fragment_Callback() {
                    @Override
                    public void Responce(Bundle resp) {
                        if(resp !=null){
                            String report_reason = resp.getString("reason");
                            report_type_txt.setText(report_reason);
                        }
                    }
                });
                Bundle bundle=new Bundle();
                bundle.putString("video_id",video_id);
                bundle.putString("user_id",user_id);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
                transaction.addToBackStack(null);
                transaction.replace(R.id.submit_f, _report_typeF).commit();
                break;

            case R.id.back_btn:
                getActivity().onBackPressed();
                break;
        }
    }
}