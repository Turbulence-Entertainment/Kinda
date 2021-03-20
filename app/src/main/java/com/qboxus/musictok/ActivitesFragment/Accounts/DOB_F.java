package com.qboxus.musictok.ActivitesFragment.Accounts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.qboxus.musictok.Models.User_Model;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.ycuwq.datepicker.date.DatePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static java.util.Calendar.DATE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

public class DOB_F extends Fragment {
    View view;
    DatePicker datePicker;
    Button btn_dob_next;
    String current_date,st_year;
    Date c;
    ImageView Goback;
    String fromWhere;
    User_Model user__model = new User_Model();
    public DOB_F() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_dob_fragment, container, false);
        c = Calendar.getInstance().getTime();
        datePicker = view.findViewById(R.id.datePicker);
        btn_dob_next = view.findViewById(R.id.btn_dob_next);
        Goback = view.findViewById(R.id.Goback);
        datePicker.setMaxDate(System.currentTimeMillis() - 1000);

        Bundle bundle = getArguments();
        user__model = (User_Model) bundle.getSerializable("user_model");
        fromWhere = bundle.getString("fromWhere");
        datePicker.setOnDateSelectedListener(new DatePicker.OnDateSelectedListener() {
            @Override
            public void onDateSelected(int year, int month, int day) {
                btn_dob_next.setEnabled(true);
                btn_dob_next.setClickable(true);
                st_year = String.valueOf(year);
                current_date = year + "-" + month + "-" + day;
                user__model.date_of_birth = current_date;
            }
        });
        datePicker.getYearPicker().setEndYear(2020);
        view.findViewById(R.id.btn_dob_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SimpleDateFormat df = new SimpleDateFormat("yyy", Locale.getDefault());
                String formattedDate = df.format(c);
                Date dob = null;
                Date currentdate = null;
                try
                {
                    dob = df.parse(formattedDate);
                    currentdate = df.parse(current_date);
                } catch (ParseException e){

                }

                int value = getDiffYears(currentdate,dob);

                if(value > 17){
                    if(fromWhere.equals("signup")){
                        Email_Phone_F emailPhoneF = new Email_Phone_F(fromWhere);
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("user_model", user__model);
                        emailPhoneF.setArguments(bundle);
                        transaction.addToBackStack(null);
                        transaction.replace(R.id.dob_fragment, emailPhoneF).commit();
                    }else if(fromWhere.equals("social") ||fromWhere.equals("fromPhone")){
                        Create_Username_F signUp_fragment = new Create_Username_F(fromWhere);
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("user_model", user__model);
                        signUp_fragment.setArguments(bundle);
                        transaction.addToBackStack(null);
                        transaction.replace(R.id.dob_fragment, signUp_fragment).commit();
                    }
                }else{
                    Toast.makeText(getActivity(), "Age must be 18 or over", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        return view;
    }

    public int getDiffYears(Date first, Date last) {
        Calendar a = getCalendar(first);
        Calendar b = getCalendar(last);
        int diff = b.get(YEAR) - a.get(YEAR);
        if (a.get(MONTH) > b.get(MONTH) ||
                (a.get(MONTH) == b.get(MONTH) && a.get(DATE) > b.get(DATE))) {
            diff--;
        }
        return diff;
    }
    public static Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(date);
        return cal;
    }
}