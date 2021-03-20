package com.qboxus.musictok.ActivitesFragment.Accounts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.qboxus.musictok.Models.User_Model;
import com.qboxus.musictok.Main_Menu.MainMenuActivity;
import com.qboxus.musictok.R;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.Services.Upload_Service;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class Email_F extends Fragment {
    View view;
    EditText email_edit,password_edt;
    TextView login_terms_condition_txt,forgot_pass_btn;
    Button next_btn;
    SharedPreferences sharedPreferences;
    String fromWhere;
    User_Model user__model;
    public Email_F(User_Model user__model, String fromWhere) {
        this.user__model = user__model;
        this.fromWhere = fromWhere;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_email_reg, container, false);
        email_edit = view.findViewById(R.id.email_edit);
        password_edt = view.findViewById(R.id.password_edt);
        forgot_pass_btn = view.findViewById(R.id.forgot_pass_btn);
        next_btn = view.findViewById(R.id.btn_next);
        login_terms_condition_txt = view.findViewById(R.id.login_terms_condition_txt);
        sharedPreferences= Functions.getSharedPreference(getContext());
        if(fromWhere != null && fromWhere != null) {
            if (fromWhere.equals("login")) {
                login_terms_condition_txt.setVisibility(View.GONE);
                forgot_pass_btn.setVisibility(View.VISIBLE);
                password_edt.setVisibility(View.VISIBLE);
                next_btn.setText("Log in");
            }else{
                if(user__model != null){
                    email_edit.setText(user__model.email);
                    next_btn.setEnabled(true);
                    next_btn.setClickable(true);
                }
            }
        }




        email_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                String txtName = email_edit.getText().toString();
                if (txtName.length() > 0) {
                    next_btn.setEnabled(true);
                    next_btn.setClickable(true);
                } else {
                    next_btn.setEnabled(false);
                    next_btn.setClickable(false);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Check_Validation()){
                    if(fromWhere.equals("login")){
                        call_api_for_login();
                    }else {
                        Create_Password_F create_password_f = new Create_Password_F("fromEmail");
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
                        Bundle bundle = new Bundle();
                        user__model.email = email_edit.getText().toString();
                        bundle.putSerializable("user_model", user__model);
                        create_password_f.setArguments(bundle);
                        transaction.addToBackStack(null);
                        transaction.replace(R.id.sign_up_fragment, create_password_f).commit();
                    }
                }
            }
        });

        forgot_pass_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),Forgot_Pass_A.class));
            }
        });
        return view;
    }

    private void call_api_for_login() {
        JSONObject parameters = new JSONObject();
        try {

            parameters.put("email", email_edit.getText().toString());
            parameters.put("password",""+password_edt.getText().toString());
            } catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.Show_loader(getActivity(),false,false);
        ApiRequest.Call_Api(getActivity(), ApiLinks.login, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                Functions.cancel_loader();
                parse_login_data(resp);

            }
        });

    }
    public void parse_login_data(String loginData){
        try {
            JSONObject jsonObject=new JSONObject(loginData);
            String code=jsonObject.optString("code");
            if(code.equals("200")){

                JSONObject jsonArray=jsonObject.getJSONObject("msg");
                JSONObject userdata = jsonArray.getJSONObject("User");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Variables.u_id,userdata.optString("id"));
                editor.putString(Variables.f_name,userdata.optString("first_name"));
                editor.putString(Variables.l_name,userdata.optString("last_name"));
                editor.putString(Variables.u_name,userdata.optString("username"));
                editor.putString(Variables.gender,userdata.optString("gender"));
                editor.putString(Variables.u_pic,userdata.optString("profile_pic"));
                editor.putString(Variables.auth_token,userdata.optString("auth_token"));
                editor.putBoolean(Variables.islogin,true);
                editor.commit();
                Variables.user_id= Functions.getSharedPreference(getContext()).getString(Variables.u_id,"");

                Variables.Reload_my_videos=true;
                Variables.Reload_my_videos_inner=true;
                Variables.Reload_my_likes_inner=true;
                Variables.Reload_my_notification=true;
                getActivity().finish();
                startActivity(new Intent(getActivity(), MainMenuActivity.class));

            }else{
                Toast.makeText(getActivity(), jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // this will check the validations like none of the field can be the empty
    public boolean Check_Validation(){

        final String st_email = email_edit.getText().toString();
        final String password = password_edt.getText().toString();

        if(TextUtils.isEmpty(st_email)){
            email_edit.setError("Please enter email");
            return false;
        }
        if(fromWhere.equals("login")){
            if(TextUtils.isEmpty(password)){
                password_edt.setError("Please enter password");
                return false;
            }
        }

        return true;
    }


}