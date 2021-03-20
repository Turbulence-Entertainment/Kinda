package com.qboxus.musictok.ActivitesFragment.Accounts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.chaos.view.PinView;
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

public class Phone_Otp_F extends Fragment implements View.OnClickListener {
    View view;

    TextView tv1,tv2,request_call,resend_code,edit_num , text_label;

    ImageView back;
    RelativeLayout rl1;
    SharedPreferences sharedPreferences;
    String phone_num;
    User_Model user_model;
    Button send_otp_btn;
    PinView et_code;
    public Phone_Otp_F() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_otp, container, false);

        Bundle bundle = getArguments();
        phone_num = bundle.getString("phone_number");
        user_model = (User_Model) bundle.getSerializable("user_data");
        sharedPreferences=Functions.getSharedPreference(getContext());

        METHOD_findviewbyid();
        METHOD_onclicklistner();
        METHOD_oneminutetimer();


        et_code.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                String txtName = et_code.getText().toString();
                if (txtName.length() == 4) {
                    send_otp_btn.setEnabled(true);
                    send_otp_btn.setClickable(true);
                } else {
                    send_otp_btn.setEnabled(false);
                    send_otp_btn.setClickable(false);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
    }

    public void METHOD_findviewbyid(){
        tv1 = (TextView) view.findViewById(R.id.tv1_id);
        tv2 = (TextView) view.findViewById(R.id.tv2_id);
        request_call = (TextView) view.findViewById(R.id.request_call);
        resend_code = (TextView) view.findViewById(R.id.resend_code);
        edit_num = (TextView) view.findViewById(R.id.edit_num_id);
        edit_num.setText(phone_num);

        back = view.findViewById(R.id.Goback);
        rl1 = view.findViewById(R.id.rl1_id);
        send_otp_btn = view.findViewById(R.id.send_otp_btn);
        et_code = view.findViewById(R.id.et_code);

    }

    public void METHOD_onclicklistner(){
        back.setOnClickListener(this);
        request_call.setOnClickListener(this);
        resend_code.setOnClickListener(this);
        edit_num.setOnClickListener(this);
        send_otp_btn.setOnClickListener(this);
    }

    public void METHOD_oneminutetimer(){
        rl1.setVisibility(View.VISIBLE);

        new CountDownTimer(60000,1000){
            @Override
            public void onTick(long l) {
                tv1.setText("Resend code 00:"+ l/1000);
                tv2.setText("Call Me 00:"+ l/1000);
            }

            @Override
            public void onFinish() {
                rl1.setVisibility(View.GONE);
                resend_code.setVisibility(View.VISIBLE);
            }

        }.start();

    }

    private void call_api_for_code_verification() {
        JSONObject parameters = new JSONObject();
        try {

            parameters.put("phone", phone_num );
            parameters.put("verify","1");
            parameters.put("code", et_code.getText().toString());
        } catch (
                JSONException e) {
            e.printStackTrace();
        }

        Functions.Show_loader(getActivity(),false,false);
        ApiRequest.Call_Api(getActivity(), ApiLinks.verifyPhoneNo, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                Functions.cancel_loader();
                parse_otp_data(resp);

            }
        });
    }

    public void parse_otp_data(String loginData){
        try {
            JSONObject jsonObject=new JSONObject(loginData);
            String code=jsonObject.optString("code");
            if(code.equals("200")){
                call_api_for_phone_registeration();
            }else{
                Toast.makeText(getContext(), jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void call_api_for_phone_registeration() {
        JSONObject parameters = new JSONObject();
        try {

            parameters.put("phone", phone_num );
        }
        catch (
                JSONException e) {
            e.printStackTrace();
        }

        Functions.Show_loader(getActivity(),false,false);
        ApiRequest.Call_Api(getActivity(), ApiLinks.registerUser, parameters, new Callback() {
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
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putString(Variables.u_id,userdata.optString("id"));
                editor.putString(Variables.f_name,userdata.optString("first_name"));
                editor.putString(Variables.l_name,userdata.optString("last_name"));
                editor.putString(Variables.u_name,userdata.optString("username"));
                editor.putString(Variables.gender,userdata.optString("gender"));
                editor.putString(Variables.u_pic,userdata.optString("profile_pic"));
                editor.putString(Variables.auth_token,userdata.optString("auth_token"));
                editor.putBoolean(Variables.islogin,true);
                editor.commit();
                Variables.sharedPreferences=Functions.getSharedPreference(getContext());
                Variables.user_id= Functions.getSharedPreference(getContext()).getString(Variables.u_id,"");

                Variables.Reload_my_videos=true;
                Variables.Reload_my_videos_inner=true;
                Variables.Reload_my_likes_inner=true;
                Variables.Reload_my_notification=true;
                getActivity().finish();
                startActivity(new Intent(getActivity(), MainMenuActivity.class));
            }

            else if(code.equals("201") && !jsonObject.optString("msg").contains("have been blocked")){

                if(user_model.date_of_birth==null) {
                    Functions.Show_Alert(getActivity(), null, "Incorrect login details", "Signup", "Cancel", new Callback() {
                        @Override
                        public void Responce(java.lang.String resp) {
                            if (resp.equalsIgnoreCase("yes")) {
                                Open_DoB_Fragment();
                            }
                        }
                    });
                }else {
                    Open_Username_F();
                }

            }else{
                Toast.makeText(getContext(), jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void Open_Username_F(){
             Create_Username_F signUp_fragment = new Create_Username_F("fromPhone");
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
            Bundle bundle = new Bundle();
            bundle.putSerializable("user_model", user_model);
            signUp_fragment.setArguments(bundle);
            transaction.addToBackStack(null);
            transaction.replace(R.id.dob_fragment, signUp_fragment).commit();

    }

    public void Open_DoB_Fragment(){
        DOB_F DOBF = new DOB_F();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        Bundle bundle = new Bundle();
        user_model.phone_no = phone_num;
        bundle.putSerializable("user_model", user_model);
        bundle.putString("fromWhere","fromPhone");
        DOBF.setArguments(bundle);
        transaction.addToBackStack(null);
        transaction.replace(R.id.sign_up_fragment, DOBF).commit();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.Goback:
                getActivity().onBackPressed();
                break;

            case R.id.resend_code:
                METHOD_oneminutetimer();
                break;


            case R.id.edit_num_id:
                getActivity().onBackPressed();
                break;

            case R.id.send_otp_btn:
                    call_api_for_code_verification();
                    break;

                }
        }

}