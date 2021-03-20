package com.qboxus.musictok.ActivitesFragment;


import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qboxus.musictok.ActivitesFragment.Accounts.Request_Varification_F;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.qboxus.musictok.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class Setting_F extends RootFragment implements View.OnClickListener {

    View view;
    Context context;


    public Setting_F() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_setting, container, false);
        context=getContext();

        view.findViewById(R.id.Goback).setOnClickListener(this);
        view.findViewById(R.id.request_verification_txt).setOnClickListener(this);
        view.findViewById(R.id.privacy_policy_txt).setOnClickListener(this);
        view.findViewById(R.id.privacy_setting_txt).setOnClickListener(this);
        view.findViewById(R.id.push_notification_txt).setOnClickListener(this);
        view.findViewById(R.id.change_password_txt).setOnClickListener(this);
        view.findViewById(R.id.terms_condition_txt).setOnClickListener(this::onClick);



        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.Goback:
                getActivity().onBackPressed();
                break;

            case R.id.request_verification_txt:
                Open_request_verification();
                break;

            case R.id.privacy_policy_txt:
                Open_web_url("Privacy Policy", ApiLinks.privacy_policy);
                break;


            case R.id.privacy_setting_txt:
                Open_Privacy_Setting();
                break;

            case R.id.push_notification_txt:
                OpenPushNotificationSetting();
                break;

            case R.id.terms_condition_txt:
                Open_web_url("Terms & Conditions", ApiLinks.terms_conditions);
                break;

            case R.id.change_password_txt:
                Open_Password();
                break;

            default:
                return;
        }

    }


    private void Open_Password() {
        Change_Password_F change_password_f = new Change_Password_F();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        transaction.addToBackStack(null);
        transaction.replace(R.id.Setting_F, change_password_f).commit();
    }


    private void Open_Privacy_Setting() {
        PrivacyPolicySetting_F policy_setting_f = new PrivacyPolicySetting_F();
        Bundle bundle=new Bundle();
        policy_setting_f.setArguments(bundle);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        transaction.addToBackStack(null);
        transaction.replace(R.id.Setting_F, policy_setting_f).commit();
    }

    private void OpenPushNotificationSetting() {
        Push_Notification_Setting_F push_setting_f = new Push_Notification_Setting_F();
        Bundle bundle=new Bundle();
        push_setting_f.setArguments(bundle);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        transaction.addToBackStack(null);
        transaction.replace(R.id.Setting_F, push_setting_f).commit();
    }


    public void Open_request_verification(){
        Request_Varification_F request_varification_f = new Request_Varification_F();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        transaction.addToBackStack(null);
        transaction.replace(R.id.Setting_F, request_varification_f).commit();
    }

    public void Open_web_url(String title,String url){
        Webview_F webview_f = new Webview_F();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        Bundle bundle=new Bundle();
        bundle.putString("url",url);
        bundle.putString("title",title);
        webview_f.setArguments(bundle);
        transaction.addToBackStack(null);
        transaction.replace(R.id.Setting_F, webview_f).commit();
    }



}
