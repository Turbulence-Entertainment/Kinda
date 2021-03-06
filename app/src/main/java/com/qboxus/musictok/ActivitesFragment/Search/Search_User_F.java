package com.qboxus.musictok.ActivitesFragment.Search;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.qboxus.musictok.Adapters.Users_Adapter;
import com.qboxus.musictok.Models.Users_Model;
import com.qboxus.musictok.Main_Menu.MainMenuFragment;
import com.qboxus.musictok.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.qboxus.musictok.ActivitesFragment.Profile.Profile_F;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.Adapter_Click_Listener;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.qboxus.musictok.ActivitesFragment.Search.Search_Main_F.search_edit;

/**
 * A simple {@link Fragment} subclass.
 */
public class Search_User_F extends RootFragment {

    View view;
    Context context;
    String type;
    ShimmerFrameLayout shimmerFrameLayout;
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    RelativeLayout no_data_layout;
    ProgressBar load_more_progress;

    int page_count = 0;
    boolean ispost_finsh;

    ArrayList<Users_Model> data_list;
    Users_Adapter users_adapter;

    public Search_User_F(String type) {
        this.type = type;
    }

    public Search_User_F() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_search, container, false);
        context = getContext();

        shimmerFrameLayout = view.findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();

        recyclerView = view.findViewById(R.id.recylerview);
        no_data_layout = view.findViewById(R.id.no_data_layout);
        linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        data_list = new ArrayList<>();
        users_adapter = new Users_Adapter(context, data_list, new Adapter_Click_Listener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                Users_Model item = (Users_Model) object;
                Functions.hideSoftKeyboard(getActivity());
                Open_Profile(item.fb_id, item.username, item.profile_pic);

            }
        });
        recyclerView.setAdapter(users_adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean userScrolled;
            int scrollOutitems;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                scrollOutitems = linearLayoutManager.findLastVisibleItemPosition();

                Log.d("resp", "" + scrollOutitems);
                if (userScrolled && (scrollOutitems == data_list.size() - 1)) {
                    userScrolled = false;

                    if (load_more_progress.getVisibility() != View.VISIBLE && !ispost_finsh) {
                        load_more_progress.setVisibility(View.VISIBLE);
                        page_count = page_count + 1;
                        Call_Api();
                    }
                }


            }
        });


        load_more_progress = view.findViewById(R.id.load_more_progress);
        page_count = 0;
        Call_Api();

        return view;
    }


    public void Call_Api() {

        JSONObject params = new JSONObject();
        try {

            params.put("type", type);
            params.put("keyword", search_edit.getText().toString());
            params.put("starting_point", "" + page_count);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(getActivity(), ApiLinks.search, params, new Callback() {
            @Override
            public void Responce(String resp) {
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);

                if (type.equalsIgnoreCase("user")) {
                    Parse_users(resp);
                }


            }
        });

    }


    public void Parse_users(String responce) {


        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equalsIgnoreCase("200")) {

                JSONArray msg = jsonObject.optJSONArray("msg");
                ArrayList<Users_Model> temp_list = new ArrayList<>();
                for (int i = 0; i < msg.length(); i++) {
                    JSONObject data = msg.optJSONObject(i);
                    JSONObject User = data.optJSONObject("User");

                    Users_Model user = new Users_Model();
                    user.fb_id = User.optString("id");
                    user.username = User.optString("username");
                    user.first_name = User.optString("first_name");
                    user.last_name = User.optString("last_name");
                    user.gender = User.optString("gender");

                    user.profile_pic = User.optString("profile_pic", "");
                    if (!user.profile_pic.contains(Variables.http)) {
                        user.profile_pic = ApiLinks.BASE_URL + user.profile_pic;
                    }

                    user.followers_count = User.optString("followers_count", "0");
                    user.videos = User.optString("video_count", "0");

                    temp_list.add(user);


                }

                if (page_count == 0) {

                    data_list.addAll(temp_list);

                    if (data_list.isEmpty()) {
                        no_data_layout.setVisibility(View.VISIBLE);
                    } else {
                        no_data_layout.setVisibility(View.GONE);

                        recyclerView.setAdapter(users_adapter);
                    }
                } else {

                    if (temp_list.isEmpty())
                        ispost_finsh = true;
                    else {
                        data_list.addAll(temp_list);
                        users_adapter.notifyDataSetChanged();
                    }

                }

            } else {
                if (data_list.isEmpty())
                    no_data_layout.setVisibility(View.VISIBLE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            load_more_progress.setVisibility(View.GONE);
        }

    }


    public void Open_Profile(String fb_id, String username, String profile_pic) {
        if (Functions.getSharedPreference(context).getString(Variables.u_id, "0").equals(fb_id)) {

            TabLayout.Tab profile = MainMenuFragment.tabLayout.getTabAt(4);
            profile.select();

        } else {

            Profile_F profile_f = new Profile_F();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
            Bundle args = new Bundle();
            args.putString("user_id", fb_id);
            args.putString("user_name", username);
            args.putString("user_pic", profile_pic);
            profile_f.setArguments(args);
            transaction.addToBackStack(null);
            transaction.replace(R.id.Search_Main_F, profile_f).commit();

        }

    }

}
