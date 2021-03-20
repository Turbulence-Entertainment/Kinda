package com.qboxus.musictok.ActivitesFragment;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.qboxus.musictok.Adapters.Report_Type_Adapter;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.Interfaces.Fragment_Callback;
import com.qboxus.musictok.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.qboxus.musictok.Models.Report_Type_Get_Set;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class Report_Type_F extends RootFragment implements View.OnClickListener {


    View view;
    RecyclerView recyclerview;
    Report_Type_Adapter adapter;
    Boolean is_from_register;

    private Fragment_Callback fragment_callback;
    String video_id, user_id;


    ArrayList<Report_Type_Get_Set> data_list = new ArrayList<>();

    public Report_Type_F(boolean isfrom, Fragment_Callback callBack) {
        this.is_from_register = isfrom;
        this.fragment_callback = callBack;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        view = inflater.inflate(R.layout.fragment_report_type, container, false);
        view.findViewById(R.id.back_btn).setOnClickListener(this);
        recyclerview = view.findViewById(R.id.recylerview);

        Bundle bundle = getArguments();
        if (bundle != null) {
            video_id = bundle.getString("video_id");
            user_id = bundle.getString("user_id");
        }
        call_api_for_get_report_type();
        return view;
    }

    private void call_api_for_get_report_type() {

        Functions.Show_loader(getActivity(), false, false);
        JSONObject parameters = new JSONObject();
        ApiRequest.Call_Api(getActivity(), ApiLinks.showReportReasons, parameters, new Callback() {
            @Override
            public void Responce(String resp) {

                Log.d(Variables.tag, "resp at report : " + resp);
                Functions.cancel_loader();
                Parse_data(resp);
            }
        });

    }

    private void Parse_data(String resp) {
        data_list.clear();
        try {
            JSONObject jsonObject = new JSONObject(resp);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {
                JSONArray msgArray = jsonObject.getJSONArray("msg");

                for (int i = 0; i < msgArray.length(); i++) {
                    JSONObject itemdata = msgArray.optJSONObject(i);
                    JSONObject ReportReason = itemdata.optJSONObject("ReportReason");

                    Report_Type_Get_Set item = new Report_Type_Get_Set();
                    item.id = ReportReason.optString("id");
                    item.title = ReportReason.optString("title");
                    data_list.add(item);
                }
                set_adapter();
                Functions.cancel_loader();

            } else {
                Functions.cancel_loader();
                Functions.show_toast(getActivity(), jsonObject.optString("msg"));
            }

        } catch (JSONException e) {
            Functions.cancel_loader();
            e.printStackTrace();
        }

    }


    private void set_adapter() {
        adapter = new Report_Type_Adapter(getActivity(), data_list, new Report_Type_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(int positon, Report_Type_Get_Set item, View view) {
                switch (view.getId()) {
                    case R.id.rlt_report:
                        if (is_from_register) {
                            Log.d(Variables.tag, item.title);
                            sendDataBack(item.title);
                        } else {
                            Submit_Report_F submit_report_f = new Submit_Report_F(new Fragment_Callback() {
                                @Override
                                public void Responce(Bundle bundle) {
                                    getActivity().onBackPressed();
                                }
                            });
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
                            transaction.addToBackStack(null);
                            Bundle args = new Bundle();
                            args.putString("report_id", item.id);
                            args.putString("report_type", item.title);
                            args.putString("video_id", video_id);
                            args.putString("user_id", user_id);
                            submit_report_f.setArguments(args);
                            transaction.replace(R.id.fragment_select_report, submit_report_f).commit();
                        }

                        break;

                }
            }
        });

        adapter.setHasStableIds(true);
        recyclerview.setHasFixedSize(true);
        recyclerview.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerview.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private void sendDataBack(String reason) {

        Bundle bundle = new Bundle();
        if (is_from_register) {
            bundle.putString("reason", reason);
            fragment_callback.Responce(bundle);
            getFragmentManager().popBackStackImmediate();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                getActivity().onBackPressed();
                break;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();


        if (!is_from_register) {
            if (fragment_callback != null)
                fragment_callback.Responce(new Bundle());
        }

    }


}