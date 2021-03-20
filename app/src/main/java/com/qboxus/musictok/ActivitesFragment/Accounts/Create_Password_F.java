package com.qboxus.musictok.ActivitesFragment.Accounts;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.qboxus.musictok.Models.User_Model;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;

public class Create_Password_F extends Fragment {
    View view;
    EditText password_edt;
    Button btn_pass;
    User_Model user_model;
    String fromWhere,st_email;
    public Create_Password_F(String fromWhere) {
        this.fromWhere = fromWhere;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_create_password, container, false);
        Bundle bundle = getArguments();
        user_model = (User_Model) bundle.getSerializable("user_model");
        st_email = bundle.getString("email");

        view.findViewById(R.id.Goback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        password_edt = view.findViewById(R.id.password_edt);
        btn_pass = view.findViewById(R.id.btn_pass);
        password_edt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                String txtName = password_edt.getText().toString();
                if (txtName.length() > 0) {
                    btn_pass.setEnabled(true);
                    btn_pass.setClickable(true);
                } else {
                    btn_pass.setEnabled(false);
                    btn_pass.setClickable(false);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        btn_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Check_Validation()){
                    Create_Username_F user_name_f = new Create_Username_F(fromWhere);
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    Bundle bundle = new Bundle();
                    user_model.password = password_edt.getText().toString();
                    bundle.putSerializable("user_model", user_model);
                    user_name_f.setArguments(bundle);
                    transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
                    transaction.addToBackStack(null);
                    transaction.replace(R.id.pass_f, user_name_f).commit();
                }
            }
        });

        return view;
    }
    // this will check the validations like none of the field can be the empty
    public boolean Check_Validation(){

        String password=password_edt.getText().toString();

        if(TextUtils.isEmpty(password) || password.length()<2 ){
            Functions.show_toast(getActivity(),"Please enter password");
            return false;
        }

        return true;
    }


}