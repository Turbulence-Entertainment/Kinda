package com.qboxus.musictok.ActivitesFragment.Video_Recording;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.qboxus.musictok.R;
import com.qboxus.musictok.Adapters.Users_Adapter;
import com.qboxus.musictok.Models.Users_Model;
import com.qboxus.musictok.Interfaces.Adapter_Click_Listener;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.Interfaces.Fragment_Callback;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Friends_F extends Fragment {
    View view;
    Context context;

    String user_id;


    Users_Adapter adapter;
    RecyclerView recyclerView;

    ArrayList<Users_Model> datalist;
    EditText search_edit;

    RelativeLayout no_data_layout;

    ProgressBar pbar;
    CardView search_layout;

    String fromWhere;
    TextView title_txt;
    Toolbar toolbar;

    Fragment_Callback fragment_callback;
    @SuppressLint("ValidFragment")
    public Friends_F(Fragment_Callback fragment_callback, String fromWhere) {
        this.fragment_callback=fragment_callback;
        this.fromWhere = fromWhere;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_friends_layout, container, false);
        context=getContext();

        Bundle bundle=getArguments();
        if(bundle!=null){
            user_id=bundle.getString("id");
        }

        title_txt = view.findViewById(R.id.title_txt);

        datalist = new ArrayList<>();

        recyclerView = view.findViewById(R.id.recylerview);
        search_edit  =  view.findViewById(R.id.search_edit);
        toolbar  =  view.findViewById(R.id.toolbar);
        search_layout  =  view.findViewById(R.id.search_layout);


        Call_Api_For_get_Allfollowing();

        search_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //TODO implementation
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {
                String search_txt = search_edit.getText().toString();
                if(search_txt.length() > 0){
                   call_api_for_other_users();
                }

            }
            @Override
            public void afterTextChanged(Editable editable) {
                //TODO implementation
            }
        });

        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);


        adapter=new Users_Adapter(context, datalist, new Adapter_Click_Listener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {

                Users_Model item1 =(Users_Model) object;
                switch (view.getId()){
                    case R.id.mainlayout:
                            pass_data_back(item1);
                        break;

                }

            }
        });

        recyclerView.setAdapter(adapter);


        no_data_layout=view.findViewById(R.id.no_data_layout);
        pbar=view.findViewById(R.id.pbar);



        view.findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });



        return view;
    }


    private void call_api_for_other_users() {
        no_data_layout.setVisibility(View.GONE);
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("type","user");
            parameters.put("keyword", search_edit.getText().toString());
            parameters.put("starting_point","0");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        ApiRequest.Call_Api(getActivity(), ApiLinks.search, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                Parse_following_data(resp);
            }
        });


    }


    // Bottom two function will call the api and get all the videos form api and parse the json data
    private void Call_Api_For_get_Allfollowing() {
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id",Variables.sharedPreferences.getString(Variables.u_id,""));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(getActivity(), ApiLinks.showFollowing, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                Parse_following_data(resp);
            }
        });


    }

    public void Parse_following_data(String responce){

        try {
            JSONObject jsonObject=new JSONObject(responce);
            String code=jsonObject.optString("code");
            if(code.equals("200")){

                JSONArray msg=jsonObject.optJSONArray("msg");
                ArrayList<Users_Model> temp_list=new ArrayList<>();
                for (int i=0;i<msg.length();i++){
                    JSONObject data=msg.optJSONObject(i);

                    JSONObject User=data.optJSONObject("User");
                    if(User==null)
                        User=data.optJSONObject("FollowingList");

                    Users_Model user=new Users_Model();
                    user.fb_id=User.optString("id");
                    user.username=User.optString("username");
                    user.first_name=User.optString("first_name");
                    user.last_name=User.optString("last_name");
                    user.gender=User.optString("gender");

                    user.profile_pic=User.optString("profile_pic","");
                    if(!user.profile_pic.contains(Variables.http)) {
                        user.profile_pic = ApiLinks.BASE_URL + user.profile_pic;
                    }

                    user.followers_count=User.optString("followers_count","0");
                    user.videos=User.optString("video_count","0");



                    temp_list.add(user);


                }

                datalist.clear();
                datalist.addAll(temp_list);
                adapter.notifyDataSetChanged();

                pbar.setVisibility(View.GONE);

                if(datalist.isEmpty()){
                    no_data_layout.setVisibility(View.VISIBLE);
                }else
                    no_data_layout.setVisibility(View.GONE);

            }else {
                pbar.setVisibility(View.GONE);
                search_edit.setHint("Not following anyone yet");
                no_data_layout.setVisibility(View.VISIBLE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    // this will open the profile of user which have uploaded the currenlty running video
    private void pass_data_back(final Users_Model item) {
        Bundle bundle = new Bundle();
        if(fragment_callback!=null) {
            bundle.putSerializable("data", item);
            fragment_callback.Responce(bundle);
        }
        getActivity().getSupportFragmentManager().popBackStackImmediate();

    }

}