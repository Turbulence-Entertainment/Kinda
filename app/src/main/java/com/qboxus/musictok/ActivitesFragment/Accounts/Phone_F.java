package com.qboxus.musictok.ActivitesFragment.Accounts;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.qboxus.musictok.Models.User_Model;
import com.qboxus.musictok.R;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;
import com.ybs.countrypicker.CountryPicker;
import com.ybs.countrypicker.CountryPickerListener;

import org.json.JSONException;
import org.json.JSONObject;

import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;

import static android.app.Activity.RESULT_OK;


public class Phone_F extends Fragment implements View.OnClickListener {
    public final static int RESOLVE_HINT = 1011;
    View view;
    TextView tv_country_code;
    RelativeLayout main_rlt;
    FrameLayout container;
    String country_dialing_code = "", countryCodeValue, mPhoneNumber, fromWhere;
    EditText phone_edit;
    User_Model user__model;
    Button btn_send_code;
    TextView login_terms_condition_txt;
    String phoneNo;
    CountryCodePicker ccp;

    public Phone_F(User_Model user__model, String fromWhere) {
        this.user__model = user__model;
        this.fromWhere = fromWhere;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_phone, container, false);
        initView();

        clickListnere();
        TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(getActivity().TELEPHONY_SERVICE);
        if (tm != null) {
            countryCodeValue = tm.getNetworkCountryIso().toUpperCase();
            int cc = PhoneNumberUtil.createInstance(getActivity()).getCountryCodeForRegion(countryCodeValue);
            tv_country_code.setText(countryCodeValue.toUpperCase() + " " + cc);
            country_dialing_code = String.valueOf(cc);
        }

        // getPhone();

        if (fromWhere != null && fromWhere.equals("login")) {

            login_terms_condition_txt.setVisibility(View.GONE);

        }


        return view;
    }

    private void initView() {
        tv_country_code = view.findViewById(R.id.country_code);
        login_terms_condition_txt = view.findViewById(R.id.login_terms_condition_txt);
        container = view.findViewById(R.id.container);
        main_rlt = view.findViewById(R.id.main_rlt);
        phone_edit = view.findViewById(R.id.phone_edit);
        btn_send_code = view.findViewById(R.id.btn_send_code);
        btn_send_code.setOnClickListener(this);


        //     phone_edit.addTextChangedListener(new PhoneNumberFormattingTextWatcher(country_dialing_code));
        phone_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                String txtName = phone_edit.getText().toString();
                if (txtName.length() > 0) {
                    btn_send_code.setEnabled(true);
                    btn_send_code.setClickable(true);
                } else {
                    btn_send_code.setEnabled(false);
                    btn_send_code.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        ccp = view.findViewById(R.id.ccp);

        ccp.registerPhoneNumberTextView(phone_edit);
    }

    private void clickListnere() {
        tv_country_code.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.country_code:
                Opencountry();
                break;
            case R.id.btn_send_code:
                if (Check_Validation()) {

                    if (!ccp.isValid()) {
                        phone_edit.setError("Invalid Phone Number");
                        return;
                    }

                    phoneNo = phone_edit.getText().toString();
                    if (phoneNo.charAt(0) == '0') {
                        phoneNo = phoneNo.substring(1);
                    }
                    phoneNo = ccp.getSelectedCountryCodeWithPlus() + phoneNo;

                    call_api_for_otp();
                }

                break;
        }
    }

    public boolean Check_Validation() {

        final String st_phone = phone_edit.getText().toString();

        if (TextUtils.isEmpty(st_phone)) {
            Functions.show_toast(getActivity(), "Please enter phone number");
            return false;
        }


        return true;
    }

    private void call_api_for_otp() {

        JSONObject parameters = new JSONObject();
        try {

            parameters.put("phone", phoneNo);
            parameters.put("verify", "0");
        } catch (
                JSONException e) {
            e.printStackTrace();
        }

        Functions.Show_loader(getActivity(), false, false);
        ApiRequest.Call_Api(getActivity(), ApiLinks.verifyPhoneNo, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                Functions.cancel_loader();
                parse_login_data(resp);
            }
        });


    }

    public void parse_login_data(String loginData) {
        try {
            JSONObject jsonObject = new JSONObject(loginData);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                Functions.show_toast(getActivity(), jsonObject.optString("msg"));
                Phone_Otp_F phoneOtp_f = new Phone_Otp_F();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
                Bundle bundle = new Bundle();
                String phone_no = phone_edit.getText().toString();
                bundle.putString("phone_number",phoneNo);
                user__model.phone_no = phone_no;
                bundle.putSerializable("user_data", user__model);
                phoneOtp_f.setArguments(bundle);
                transaction.addToBackStack(null);
                transaction.replace(R.id.sign_up_fragment, phoneOtp_f).commit();

            } else {
                Functions.show_toast(getActivity(), jsonObject.optString("msg"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("WrongConstant")
    public void Opencountry() {
        final CountryPicker picker = CountryPicker.newInstance("Select Country");
        picker.setListener(new CountryPickerListener() {
            @Override
            public void onSelectCountry(String name, String code, String dialCode, int flagDrawableResID) {

                country_dialing_code = dialCode;
                tv_country_code.setText(code + " " + dialCode);
                picker.dismiss();

            }
        });
        picker.show(getFragmentManager(), "Select Country");
    }


    private void getPhone() {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Auth.CREDENTIALS_API)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) getActivity())
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) getActivity())
                .build();
        googleApiClient.connect();
        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();
        PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(Login_A.googleApiClient, hintRequest);
        try {
            getActivity().startIntentSenderForResult(intent.getIntentSender(),
                    RESOLVE_HINT, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
            Functions.show_toast(getActivity(), "err");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == RESOLVE_HINT) {
            com.google.android.gms.auth.api.credentials.Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
            if (credential != null) {
                mPhoneNumber = credential.getId();
                if (mPhoneNumber != null) {
                    mPhoneNumber = mPhoneNumber.replace(country_dialing_code, "");
                    if (phone_edit == null)
                        phone_edit = view.findViewById(R.id.phone_edit);

                    phone_edit.setText(mPhoneNumber);
                }
                Log.d(Variables.tag, "mPhoneNumber" + mPhoneNumber);

            } else {
                Functions.show_toast(getActivity(), "err");
            }
        }

    }


}