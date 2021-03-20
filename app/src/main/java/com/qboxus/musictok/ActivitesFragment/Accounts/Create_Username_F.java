package com.qboxus.musictok.ActivitesFragment.Accounts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.qboxus.musictok.Models.User_Model;
import com.qboxus.musictok.Main_Menu.MainMenuActivity;
import com.qboxus.musictok.R;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class Create_Username_F extends Fragment {
    View view;
    EditText username_edit;
    Button sign_up_btn;
    User_Model user_model;
    SharedPreferences sharedPreferences;
    String fromWhere;

    public Create_Username_F(String fromWhere) {
        this.fromWhere = fromWhere;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_user_name_f, container, false);

        Bundle bundle = getArguments();
        user_model = (User_Model) bundle.getSerializable("user_model");
        view.findViewById(R.id.Goback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        sharedPreferences = Functions.getSharedPreference(getContext());
        username_edit = view.findViewById(R.id.username_edit);
        sign_up_btn = view.findViewById(R.id.btn_sign_up);

        username_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                String txtName = username_edit.getText().toString();
                if (txtName.length() > 0) {
                    sign_up_btn.setEnabled(true);
                    sign_up_btn.setClickable(true);
                } else {
                    sign_up_btn.setEnabled(false);
                    sign_up_btn.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        sign_up_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Check_Validation()) {
                    call_api_for_sigup();
                }
            }
        });
        return view;
    }

    private void call_api_for_sigup() {

        JSONObject parameters = new JSONObject();
        try {

            parameters.put("dob", "" + user_model.date_of_birth);
            parameters.put("username", "" + username_edit.getText().toString());

            if (fromWhere.equals("fromEmail")) {
                parameters.put("email", "" + user_model.email);
                parameters.put("password", user_model.password);
            } else if (fromWhere.equals("fromPhone")) {
                parameters.put("phone", "" + user_model.phone_no);
            } else if (fromWhere.equals("social")) {
                parameters.put("email", "" + user_model.email);
                parameters.put("social_id", "" + user_model.socail_id);
                parameters.put("social", "" + user_model.socail_type);
                parameters.put("first_name", "" + user_model.fname);
                parameters.put("last_name", "" + user_model.lname);
                parameters.put("auth_token", "" + user_model.auth_tokon);
                parameters.put("device_token", Variables.device);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.Show_loader(getActivity(), false, false);
        ApiRequest.Call_Api(getActivity(), ApiLinks.registerUser, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                Functions.cancel_loader();
                Parse_signup_data(resp);

            }
        });
    }


    // if the signup successfull then this method will call and it store the user info in local
    public void Parse_signup_data(String loginData) {
        try {
            JSONObject jsonObject = new JSONObject(loginData);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONObject jsonArray = jsonObject.getJSONObject("msg");
                JSONObject userdata = jsonArray.getJSONObject("User");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Variables.u_id, userdata.optString("id"));
                editor.putString(Variables.f_name, userdata.optString("first_name"));
                editor.putString(Variables.l_name, userdata.optString("last_name"));
                editor.putString(Variables.u_name, userdata.optString("username"));
                editor.putString(Variables.gender, userdata.optString("gender"));
                editor.putString(Variables.u_pic, userdata.optString("profile_pic"));

                if (fromWhere.equals("social")) {
                    editor.putString(Variables.auth_token, user_model.auth_tokon);
                } else {
                    editor.putString(Variables.auth_token, userdata.optString("auth_token"));
                }

                editor.putBoolean(Variables.islogin, true);
                editor.commit();

                getActivity().sendBroadcast(new Intent("newVideo"));

                Variables.user_id = Functions.getSharedPreference(getContext()).getString(Variables.u_id, "");

                Variables.Reload_my_videos = true;
                Variables.Reload_my_videos_inner = true;
                Variables.Reload_my_likes_inner = true;
                Variables.Reload_my_notification = true;

                getActivity().finish();
                startActivity(new Intent(getActivity(), MainMenuActivity.class));
            } else {
                Toast.makeText(getActivity(), "" + jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public boolean Check_Validation() {

        String uname = username_edit.getText().toString();

        if (TextUtils.isEmpty(uname)) {
            username_edit.setError("Username can't be empty");
            return false;
        }
        if (uname.length() < 4 || uname.length() > 14) {
            username_edit.setError("Username Length should be between 4 and 14");
            return false;
        }

        return true;
    }


}