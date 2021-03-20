package com.qboxus.musictok.ActivitesFragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qboxus.musictok.ActivitesFragment.Accounts.Login_A;
import com.qboxus.musictok.Adapters.Following_Adapter;
import com.qboxus.musictok.Models.Following_Get_Set;
import com.qboxus.musictok.ActivitesFragment.Profile.Profile_F;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.API_CallBack;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.Interfaces.Fragment_Callback;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class Following_F extends Fragment {

    View view;
    Context context;

    String user_id;


    Following_Adapter adapter;
    RecyclerView recyclerView;

    ArrayList<Following_Get_Set> datalist;


    RelativeLayout no_data_layout;

    ProgressBar pbar;

    String following_or_fan="Followers";

    TextView title_txt;
    public Following_F() {
        // Required empty public constructor
    }




    Fragment_Callback fragment_callback;
    @SuppressLint("ValidFragment")
    public Following_F(Fragment_Callback fragment_callback) {
        this.fragment_callback=fragment_callback;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_following, container, false);
        context=getContext();

        Bundle bundle=getArguments();
        if(bundle!=null){
             user_id=bundle.getString("id");
             following_or_fan=bundle.getString("from_where");
        }


        title_txt=view.findViewById(R.id.title_txt);

        datalist=new ArrayList<>();

        recyclerView = (RecyclerView) view.findViewById(R.id.recylerview);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);


        adapter=new Following_Adapter(context, following_or_fan,datalist, new Following_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, Following_Get_Set item) {

                switch (view.getId()){
                    case R.id.action_txt:
                        if(Variables.sharedPreferences.getBoolean(Variables.islogin,false)) {
                        if(!item.fb_id.equals(Variables.sharedPreferences.getString(Variables.u_id,"")))
                        Follow_unFollow_User(item,postion);
                        }
                        else {

                            Intent intent = new Intent(getActivity(), Login_A.class);
                            startActivity(intent);
                            getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
                        }
                        break;

                    case R.id.mainlayout:
                        OpenProfile(item);
                        break;

                }

            }
        }
        );

        recyclerView.setAdapter(adapter);


        no_data_layout=view.findViewById(R.id.no_data_layout);
        pbar=view.findViewById(R.id.pbar);



        view.findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });



        if(following_or_fan.equals("following")){
        Call_Api_For_get_Allfollowing();
        title_txt.setText("Following");
        }
        else {
            Call_Api_For_get_Allfan();
            title_txt.setText("Followers");
        }

        return view;
    }


    // Bottom two function will call the api and get all the videos form api and parse the json data
    private void Call_Api_For_get_Allfollowing() {

        JSONObject parameters = new JSONObject();
        try {

            if(Variables.sharedPreferences.getString(Variables.u_id,"0").equals(user_id))
                parameters.put("user_id",Variables.sharedPreferences.getString(Variables.u_id,""));

            else {
                parameters.put("user_id", Variables.sharedPreferences.getString(Variables.u_id, ""));
                parameters.put("other_user_id",user_id);

            }
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

        datalist.clear();

        try {
            JSONObject jsonObject=new JSONObject(responce);
            String code=jsonObject.optString("code");
            if(code.equals("200")){
                JSONArray msgArray=jsonObject.getJSONArray("msg");
                for (int i=0;i<msgArray.length();i++) {

                    JSONObject object = msgArray.optJSONObject(i);
                    JSONObject FollowingList=object.optJSONObject("FollowingList");


                    Following_Get_Set item=new Following_Get_Set();
                    item.fb_id=FollowingList.optString("id");
                    item.first_name=FollowingList.optString("first_name");
                    item.last_name=FollowingList.optString("last_name");
                    item.bio=FollowingList.optString("bio");
                    item.username=FollowingList.optString("username");

                     item.profile_pic=FollowingList.optString("profile_pic","");
                    if(!item.profile_pic.contains(Variables.http)) {
                        item.profile_pic = ApiLinks.BASE_URL + item.profile_pic;
                    }


                    item.follow_status_button=FollowingList.optString("button");



                    datalist.add(item);
                    adapter.notifyItemInserted(i);
                }

                adapter.notifyDataSetChanged();

                if(datalist.isEmpty()){
                    no_data_layout.setVisibility(View.VISIBLE);
                }else
                    no_data_layout.setVisibility(View.GONE);

            }else {
                no_data_layout.setVisibility(View.VISIBLE);
                }

            pbar.setVisibility(View.GONE);


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    // Bottom two function will call the api and get all the videos form api and parse the json data
    private void Call_Api_For_get_Allfan() {

        JSONObject parameters = new JSONObject();
        try
        {
            if(Variables.sharedPreferences.getString(Variables.u_id,"0").equals(user_id))
                parameters.put("user_id",Variables.sharedPreferences.getString(Variables.u_id,""));

            else {

                parameters.put("user_id", Variables.sharedPreferences.getString(Variables.u_id, ""));
                parameters.put("other_user_id",user_id);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        ApiRequest.Call_Api(getActivity(), ApiLinks.showFollowers, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                Parse_fans_data(resp);
            }
        });

    }

    public void Parse_fans_data(String responce){

        datalist.clear();

        try {
            JSONObject jsonObject=new JSONObject(responce);
            String code=jsonObject.optString("code");
            if(code.equals("200")){
                JSONArray msgArray=jsonObject.getJSONArray("msg");
                for (int i=0;i<msgArray.length();i++) {
                    JSONObject object = msgArray.optJSONObject(i);
                    JSONObject FollowingList=object.optJSONObject("FollowerList");


                    Following_Get_Set item=new Following_Get_Set();
                    item.fb_id=FollowingList.optString("id");
                    item.first_name=FollowingList.optString("first_name");
                    item.last_name=FollowingList.optString("last_name");
                    item.bio=FollowingList.optString("bio");
                    item.username=FollowingList.optString("username");

                    item.profile_pic=FollowingList.optString("profile_pic","");
                    if(!item.profile_pic.contains(Variables.http)) {
                        item.profile_pic = ApiLinks.BASE_URL + item.profile_pic;
                    }


                    item.follow_status_button=FollowingList.optString("button");

                    datalist.add(item);
                    adapter.notifyItemInserted(i);
                }

                adapter.notifyDataSetChanged();


                if(datalist.isEmpty()){
                    no_data_layout.setVisibility(View.VISIBLE);
                }else
                    no_data_layout.setVisibility(View.GONE);

            }
            else {
                no_data_layout.setVisibility(View.VISIBLE);
              }

            pbar.setVisibility(View.GONE);


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



    // this will open the profile of user which have uploaded the currenlty running video
    private void OpenProfile(final Following_Get_Set item) {
        Profile_F profile_f = new Profile_F();

        View view=getActivity().findViewById(R.id.MainMenuFragment);
        if(view!=null){

            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
            Bundle args = new Bundle();
            args.putString("user_id",item.fb_id);
            args.putString("user_name",item.username);
            args.putString("user_pic",item.profile_pic);
            profile_f.setArguments(args);
            transaction.addToBackStack(null);
            transaction.replace(R.id.MainMenuFragment, profile_f).commit();
        }
        else {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
            Bundle args = new Bundle();
            args.putString("user_id",item.fb_id);
            args.putString("user_name",item.first_name+" "+item.last_name);
            args.putString("user_pic",item.profile_pic);
            profile_f.setArguments(args);
            transaction.addToBackStack(null);
            transaction.replace(R.id.following_layout, profile_f).commit();
        }


    }


    public void Follow_unFollow_User(final Following_Get_Set item, final int position){

        Functions.Call_Api_For_Follow_or_unFollow(getActivity(),
                Variables.sharedPreferences.getString(Variables.u_id,""),
                item.fb_id,
                new API_CallBack() {
                    @Override
                    public void ArrayData(ArrayList arrayList) {


                    }

                    @Override
                    public void OnSuccess(String responce) {


                        if(following_or_fan.equals("following")){
                            Call_Api_For_get_Allfollowing();
                        }
                        else {
                            Call_Api_For_get_Allfan();
                        }


                    }

                    @Override
                    public void OnFail(String responce) {

                    }

                });


    }


    @Override
    public void onDetach() {

        if(fragment_callback!=null)
            fragment_callback.Responce(new Bundle());

        super.onDetach();
    }


}
