package com.qboxus.musictok.ActivitesFragment.Accounts;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.chaos.view.PinView;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Forgot_Pass_A extends AppCompatActivity implements View.OnClickListener {
    ViewFlipper viewFlipper;
    ImageView go_back, go_back2, go_back3;
    EditText recover_email, ed_new_pass, ed_confirm_pass;
    Button btn_next_email, confirm_otp, change_pass;
    String Code, otp, user_id, user_email;
    RelativeLayout rl1;
    TextView tv1,edit_num,resend_code;
    PinView et_code;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);
        viewFlipper = findViewById(R.id.viewflliper);
        go_back = findViewById(R.id.Goback1);
        go_back2 = findViewById(R.id.Goback2);
        go_back3 = findViewById(R.id.Goback3);
        confirm_otp = findViewById(R.id.confirm_otp);
        ed_new_pass = findViewById(R.id.ed_new_pass);
        rl1 =   findViewById(R.id.rl1_id);
        tv1 =   findViewById(R.id.tv1_id);
        recover_email = findViewById(R.id.recover_email);
        btn_next_email = findViewById(R.id.btn_next);
        change_pass = findViewById(R.id.change_password_btn);
        change_pass.setOnClickListener(this);
        confirm_otp.setOnClickListener(this);
        btn_next_email.setOnClickListener(this);
        go_back.setOnClickListener(this);
        go_back2.setOnClickListener(this);
        go_back3.setOnClickListener(this);
        edit_num = findViewById(R.id.edit_email);


        resend_code = findViewById(R.id.resend_code);
        resend_code.setOnClickListener(this);

        et_code = findViewById(R.id.et_code);
        recover_email.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                String txtName = recover_email.getText().toString();
                if (txtName.length() > 0) {
                    btn_next_email.setEnabled(true);
                    btn_next_email.setClickable(true);
                } else {
                    btn_next_email.setEnabled(false);
                    btn_next_email.setClickable(false);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        et_code.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                String txtName = et_code.getText().toString();
                if (txtName.length() == 4) {
                    confirm_otp.setEnabled(true);
                    confirm_otp.setClickable(true);
                } else {
                    confirm_otp.setEnabled(false);
                    confirm_otp.setClickable(false);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });



        ed_new_pass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                String txtName = ed_new_pass.getText().toString();
                if (txtName.length() > 0) {
                    change_pass.setEnabled(true);
                    change_pass.setClickable(true);
                } else {
                    change_pass.setEnabled(false);
                    change_pass.setClickable(false);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }




    public void METHOD_oneminutetimer(){
        rl1.setVisibility(View.VISIBLE);

        new CountDownTimer(60000,1000){
            @Override
            public void onTick(long l) {
                tv1.setText("Resend code 00:"+ l/1000);
            }

            @Override
            public void onFinish() {
                rl1.setVisibility(View.GONE);
            }

        }.start();

    }

    @Override
    public void onClick(View v) {
        if (v == go_back) {
           onBackPressed();
        }
        if (v == go_back2) {
            viewFlipper.showPrevious();
        }
        if (v == go_back3) {
            viewFlipper.showPrevious();
        }
        if (v == btn_next_email) {
            if (validateEmail()) {
                Functions.Show_loader(this, false, false);
                checkEmail(recover_email.getText().toString());
                user_email = recover_email.getText().toString();
            }
        }
        if (v == confirm_otp) {
            otp = et_code.getText().toString();
            if (otp != null && otp.length() == 4) {
                Functions.Show_loader(this, false, false);
                checkOtp(otp, user_email);
            } else {
                Functions.show_toast(this , "Code is Invalid");
            }
        }
        if (v == change_pass) {
            if (validateNewPassword()) {
                Functions.Show_loader(this, false, false);
                forgot_changepassword(ed_new_pass.getText().toString(), user_email);
            }
        }

        if(v == resend_code) {
            checkOtp(otp, user_email);
            METHOD_oneminutetimer();
        }
    }

    private void forgot_changepassword(String newpass, String email) {
        JSONObject params = new JSONObject();
        try {
            params.put("email", email);
            params.put("password", newpass);
        } catch (JSONException e) {
            Functions.cancel_loader();
            e.printStackTrace();
        }


        ApiRequest.Call_Api(this, ApiLinks.changePasswordForgot, params, new Callback() {
            @Override
            public void Responce(String resp) {
                Functions.cancel_loader();
                try {
                    JSONObject response=new JSONObject(resp);
                    String code=response.optString("code");
                    JSONArray msg=response.optJSONArray("msg");
                    if(code.equals("200")) {
                        Functions.cancel_loader();
                        startActivity(new Intent(Forgot_Pass_A.this, Login_A.class));
                        finish();
                    }else {
                        String msg_txt =  response.getString("msg");
                        Functions.show_toast(Forgot_Pass_A.this,msg_txt);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    private void checkOtp(String otp, String email) {
        JSONObject params = new JSONObject();
        try {
            params.put("email", email);
            params.put("code", otp);
        } catch (JSONException e) {
            Functions.cancel_loader();
            e.printStackTrace();
        }

        ApiRequest.Call_Api(this, ApiLinks.verifyforgotPasswordCode, params, new Callback() {
            @Override
            public void Responce(String resp) {
                Functions.cancel_loader();
                try {
                    JSONObject response=new JSONObject(resp);
                    String code=response.optString("code");

                    if(code.equals("200")) {
                        JSONObject json = new JSONObject(response.toString());
                        JSONObject msgObj = json.getJSONObject("msg");
                        JSONObject json1 = new JSONObject(msgObj.toString());
                        JSONObject user_obj = json1.getJSONObject("User");
                        user_id = user_obj.optString("id");
                        viewFlipper.showNext();
                        Functions.cancel_loader();
                    }else {
                        String msg_txt =  response.getString("msg");
                        Toast.makeText(Forgot_Pass_A.this, ""+msg_txt, Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void checkEmail(final String email) {
        JSONObject params = new JSONObject();
        try {
            params.put("email", email);
        } catch (JSONException e) {
            Functions.cancel_loader();
            e.printStackTrace();
        }

        ApiRequest.Call_Api(this, ApiLinks.forgotPassword, params, new Callback() {
            @Override
            public void Responce(String resp) {
                Functions.cancel_loader();

                try {
                    JSONObject response=new JSONObject(resp);
                    String code=response.optString("code");
                    JSONArray msg=response.optJSONArray("msg");
                    if(code.equals("200")) {
                        Functions.cancel_loader();
                        viewFlipper.showNext();
                        edit_num.setText("Your code was emailed to "+recover_email.getText().toString());
                        METHOD_oneminutetimer();
                    }else {
                        String msg_txt =  response.getString("msg");
                        Toast.makeText(Forgot_Pass_A.this, ""+msg_txt, Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }
    public boolean validateEmail() {
        String email = recover_email.getText().toString().trim();
        if (email.isEmpty()) {
            Functions.show_toast(this ,"Please enter valid email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Functions.show_toast(this ,"Please enter valid email");
            return false;
        } else {

            return true;
        }
    }

    @Override
    public void onBackPressed() {
        if (viewFlipper.getDisplayedChild() == 0) {
            finish();
        } else if (viewFlipper.getDisplayedChild() == 1) {
            viewFlipper.showPrevious();
        } else
            viewFlipper.showPrevious();
    }


    public boolean validateNewPassword() {
        String newpass = ed_new_pass.getText().toString();
        if (newpass.isEmpty()) {
            Functions.show_toast(this , "Please enter valid new password");
            return false;
        } if (newpass.length() <= 5 || newpass.length() >= 12) {
            Functions.show_toast(this, "Please enter valid password");
            return false;
        }   else {

            return true;
        }
    }
}