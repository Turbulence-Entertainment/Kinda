package com.qboxus.musictok.ActivitesFragment.Profile;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.qboxus.musictok.ActivitesFragment.Profile.Private_Videos.PrivateVideo_F;
import com.qboxus.musictok.ActivitesFragment.Setting_F;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.Interfaces.Fragment_Callback;
import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qboxus.musictok.ActivitesFragment.Following_F;
import com.qboxus.musictok.Main_Menu.MainMenuActivity;
import com.qboxus.musictok.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.qboxus.musictok.ActivitesFragment.Profile.Liked_Videos.Liked_Video_F;
import com.qboxus.musictok.ActivitesFragment.Profile.UserVideos.UserVideo_F;
import com.qboxus.musictok.Models.PrivacyPolicySettingModel;
import com.qboxus.musictok.Models.PushNotificationSettingModel;
import com.qboxus.musictok.R;
import com.qboxus.musictok.ActivitesFragment.See_Full_Image_F;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.qboxus.musictok.ActivitesFragment.Video_Recording.DraftVideos_A;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class Profile_Tab_F extends RootFragment implements View.OnClickListener {
    View view;
    Context context;


    public TextView username, username2_txt, video_count_txt;
    public ImageView imageView;
    public TextView follow_count_txt, fans_count_txt, heart_count_txt, draft_count_txt;

    ImageView setting_btn;


    protected TabLayout tabLayout;

    protected ViewPager pager;

    private ViewPagerAdapter adapter;

    RelativeLayout tabs_main_layout;

    LinearLayout top_layout;

    public String pic_url;
    public LinearLayout create_popup_layout;

    public int myvideo_count = 0;

    PushNotificationSettingModel pushNotificationSetting_model;
    PrivacyPolicySettingModel privacyPolicySetting_model;

    public Profile_Tab_F() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_profile_tab, container, false);
        context = getContext();


        return init();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user_image:
                OpenfullsizeImage(pic_url);
                break;

            case R.id.message_btn:
                Open_Setting();
                break;

            case R.id.following_layout:
                Open_Following();
                break;

            case R.id.fans_layout:
                Open_Followers();
                break;

            case R.id.draft_btn:
                Intent upload_intent = new Intent(getActivity(), DraftVideos_A.class);
                startActivity(upload_intent);
                getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
                break;

        }
    }


    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if ((view != null && visible)) {

            if (Functions.getSharedPreference(context).getBoolean(Variables.islogin, false)) {
                update_profile();

                Call_Api_For_get_Allvideos();

            }
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        Show_draft_count();

    }

    public View init() {

        username = view.findViewById(R.id.username);
        username2_txt = view.findViewById(R.id.username2_txt);
        imageView = view.findViewById(R.id.user_image);
        imageView.setOnClickListener(this);

        video_count_txt = view.findViewById(R.id.video_count_txt);

        follow_count_txt = view.findViewById(R.id.follow_count_txt);
        fans_count_txt = view.findViewById(R.id.fan_count_txt);
        heart_count_txt = view.findViewById(R.id.heart_count_txt);
        draft_count_txt = view.findViewById(R.id.draft_count_txt);

        Show_draft_count();

        setting_btn = view.findViewById(R.id.message_btn);
        setting_btn.setOnClickListener(this);


        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        pager = view.findViewById(R.id.pager);
        pager.setOffscreenPageLimit(3);

        adapter = new ViewPagerAdapter(getResources(), getChildFragmentManager());
        pager.setAdapter(adapter);
        tabLayout.setupWithViewPager(pager);

        setupTabIcons();


        tabs_main_layout = view.findViewById(R.id.tabs_main_layout);
        top_layout = view.findViewById(R.id.top_layout);


        ViewTreeObserver observer = top_layout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {

                final int height = top_layout.getMeasuredHeight();

                top_layout.getViewTreeObserver().removeGlobalOnLayoutListener(
                        this);

                ViewTreeObserver observer = tabs_main_layout.getViewTreeObserver();
                observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tabs_main_layout.getLayoutParams();
                        params.height = (int) (tabs_main_layout.getMeasuredHeight() + height);
                        tabs_main_layout.setLayoutParams(params);
                        tabs_main_layout.getViewTreeObserver().removeGlobalOnLayoutListener(
                                this);

                    }
                });

            }
        });


        create_popup_layout = view.findViewById(R.id.create_popup_layout);


        view.findViewById(R.id.following_layout).setOnClickListener(this);
        view.findViewById(R.id.fans_layout).setOnClickListener(this);

        return view;
    }

    public void Show_draft_count() {
        try {

            String path = Variables.draft_app_folder;
            File directory = new File(path);
            File[] files = directory.listFiles();
            draft_count_txt.setText("" + files.length);
            if (files.length <= 0) {
                view.findViewById(R.id.draft_btn).setVisibility(View.GONE);
            } else {
                view.findViewById(R.id.draft_btn).setVisibility(View.VISIBLE);
                view.findViewById(R.id.draft_btn).setOnClickListener(this);
            }
        } catch (Exception e) {
            view.findViewById(R.id.draft_btn).setVisibility(View.GONE);
        }
    }

    public void update_profile() {
        username2_txt.setText(Functions.getSharedPreference(context).getString(Variables.u_name, ""));

        String first_name = Functions.getSharedPreference(context).getString(Variables.f_name, "");
        String last_name = Functions.getSharedPreference(context).getString(Variables.l_name, "");
        if (first_name.equalsIgnoreCase("") && last_name.equalsIgnoreCase("")) {
            username.setText(Functions.getSharedPreference(context).getString(Variables.u_name, ""));
        } else {
            username.setText(first_name + " " + last_name);
        }


        pic_url = Functions.getSharedPreference(context).getString(Variables.u_pic, "null");

        try {

            if (!pic_url.contains(Variables.http)) {
                pic_url = ApiLinks.BASE_URL + pic_url;
            }

            if (pic_url != null && !pic_url.equals(""))
                Picasso.get().load(pic_url)
                        .resize(200, 200)
                        .placeholder(R.drawable.profile_image_placeholder)
                        .centerCrop()
                        .into(imageView);

        } catch (Exception e) {

        }
    }


    private void setupTabIcons() {

        View view1 = LayoutInflater.from(context).inflate(R.layout.item_tabs_profile_menu, null);
        ImageView imageView1 = view1.findViewById(R.id.image);
        imageView1.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_video_color));
        tabLayout.getTabAt(0).setCustomView(view1);

        View view2 = LayoutInflater.from(context).inflate(R.layout.item_tabs_profile_menu, null);
        ImageView imageView2 = view2.findViewById(R.id.image);
        imageView2.setImageDrawable(getResources().getDrawable(R.drawable.ic_liked_video_gray));
        tabLayout.getTabAt(1).setCustomView(view2);

        View view3 = LayoutInflater.from(context).inflate(R.layout.item_tabs_profile_menu, null);
        ImageView imageView3 = view3.findViewById(R.id.image);
        imageView3.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock_gray));
        tabLayout.getTabAt(2).setCustomView(view3);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {


            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                ImageView image = v.findViewById(R.id.image);

                switch (tab.getPosition()) {
                    case 0:

                        if (myvideo_count > 0) {
                            create_popup_layout.setVisibility(View.GONE);
                        } else {
                            create_popup_layout.setVisibility(View.VISIBLE);
                            Animation aniRotate = AnimationUtils.loadAnimation(context, R.anim.up_and_down_animation);
                            create_popup_layout.startAnimation(aniRotate);
                        }

                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_video_color));
                        break;

                    case 1:
                        create_popup_layout.clearAnimation();
                        create_popup_layout.setVisibility(View.GONE);
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_liked_video_color));
                        break;

                    case 2:
                        create_popup_layout.clearAnimation();
                        create_popup_layout.setVisibility(View.GONE);
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock_black));
                        break;
                }
                tab.setCustomView(v);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View v = tab.getCustomView();
                ImageView image = v.findViewById(R.id.image);

                switch (tab.getPosition()) {
                    case 0:
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_my_video_gray));
                        break;
                    case 1:
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_liked_video_gray));
                        break;

                    case 2:
                        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_lock_gray));
                        break;
                }

                tab.setCustomView(v);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });


    }


    class ViewPagerAdapter extends FragmentPagerAdapter {

        private final Resources resources;

        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();


        public ViewPagerAdapter(final Resources resources, FragmentManager fm) {
            super(fm);
            this.resources = resources;
        }

        @Override
        public Fragment getItem(int position) {
            final Fragment result;
            switch (position) {
                case 0:
                    result = new UserVideo_F(true, Functions.getSharedPreference(context).getString(Variables.u_id, ""));
                    break;
                case 1:
                    result = new Liked_Video_F(true, Functions.getSharedPreference(context).getString(Variables.u_id, ""));
                    break;

                case 2:
                    result = new PrivateVideo_F();
                    break;

                default:
                    result = null;
                    break;
            }

            return result;
        }

        @Override
        public int getCount() {
            return 3;
        }


        @Override
        public CharSequence getPageTitle(final int position) {
            return null;
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }


        /**
         * Get the Fragment by position
         *
         * @param position tab position of the fragment
         * @return
         */
        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }


    }


    //this will get the all videos data of user and then parse the data
    private void Call_Api_For_get_Allvideos() {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.u_id, ""));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(getActivity(), ApiLinks.showUserDetail, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                Parse_data(resp);
            }
        });


    }

    public void Parse_data(String responce) {


        try {
            JSONObject jsonObject = new JSONObject(responce);
            String code = jsonObject.optString("code");
            if (code.equals("200")) {


                JSONObject msg = jsonObject.optJSONObject("msg");

                JSONObject User = msg.optJSONObject("User");
                JSONObject push_notification_setting = msg.optJSONObject("PushNotification");
                JSONObject privacy_policy_setting = msg.optJSONObject("PrivacySetting");


                pushNotificationSetting_model = new PushNotificationSettingModel();
                if (push_notification_setting != null) {
                    pushNotificationSetting_model.setComments("" + push_notification_setting.optString("comments"));
                    pushNotificationSetting_model.setLikes("" + push_notification_setting.optString("likes"));
                    pushNotificationSetting_model.setNewfollowers("" + push_notification_setting.optString("new_followers"));
                    pushNotificationSetting_model.setMentions("" + push_notification_setting.optString("mentions"));
                    pushNotificationSetting_model.setDirectmessage("" + push_notification_setting.optString("direct_messages"));
                    pushNotificationSetting_model.setVideoupdates("" + push_notification_setting.optString("video_updates"));
                }

                privacyPolicySetting_model = new PrivacyPolicySettingModel();
                if (privacy_policy_setting != null) {
                    privacyPolicySetting_model.setVideos_download("" + privacy_policy_setting.optString("videos_download"));
                    privacyPolicySetting_model.setDirect_message("" + privacy_policy_setting.optString("direct_message"));
                    privacyPolicySetting_model.setDuet("" + privacy_policy_setting.optString("duet"));
                    privacyPolicySetting_model.setLiked_videos("" + privacy_policy_setting.optString("liked_videos"));
                    privacyPolicySetting_model.setVideo_comment("" + privacy_policy_setting.optString("video_comment"));
                }

                Paper.book("Setting").write("PushSettingModel", pushNotificationSetting_model);
                Paper.book("Setting").write("PrivacySettingModel", privacyPolicySetting_model);

                username2_txt.setText(User.optString("username"));

                String first_name = User.optString("first_name", "");
                String last_name = User.optString("last_name", "");

                if (first_name.equalsIgnoreCase("") && last_name.equalsIgnoreCase("")) {
                    username.setText(User.optString("username"));
                } else {
                    username.setText(first_name + " " + last_name);
                }

                pic_url = User.optString("profile_pic");

                if (!pic_url.contains(Variables.http)) {
                    pic_url = ApiLinks.BASE_URL + pic_url;
                }


                if (pic_url != null && !pic_url.equals(""))
                    Picasso.get()
                            .load(pic_url)
                            .centerCrop()
                            .placeholder(context.getResources().getDrawable(R.drawable.profile_image_placeholder)).resize(200, 200)
                            .into(imageView);

                follow_count_txt.setText(User.optString("following_count"));
                fans_count_txt.setText(User.optString("followers_count"));
                heart_count_txt.setText(User.optString("likes_count"));

                myvideo_count = Functions.ParseInterger(User.optString("video_count", "0"));
                video_count_txt.setText(User.optString("video_count") + " videos");

                if (myvideo_count != 0) {
                    create_popup_layout.setVisibility(View.GONE);
                    create_popup_layout.clearAnimation();
                } else {

                    create_popup_layout.setVisibility(View.VISIBLE);
                    Animation aniRotate = AnimationUtils.loadAnimation(context, R.anim.up_and_down_animation);
                    create_popup_layout.startAnimation(aniRotate);

                }


                String verified = User.optString("verified");
                if (verified != null && verified.equalsIgnoreCase("1")) {
                    view.findViewById(R.id.varified_btn).setVisibility(View.VISIBLE);
                }


            } else {
                Functions.show_toast(getActivity(), jsonObject.optString("msg"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public void Open_Setting() {

        Open_menu_tab(setting_btn);


    }


    public void Open_Edit_profile() {
        Edit_Profile_F edit_profile_f = new Edit_Profile_F(new Fragment_Callback() {
            @Override
            public void Responce(Bundle bundle) {

                update_profile();
            }
        });
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, edit_profile_f).commit();
    }


    public void Open_setting() {
        Setting_F setting_f = new Setting_F();
        Bundle bundle = new Bundle();
        setting_f.setArguments(bundle);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, setting_f).commit();
    }


    //this method will get the big size of profile image.
    public void OpenfullsizeImage(String url) {
        See_Full_Image_F see_image_f = new See_Full_Image_F();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        Bundle args = new Bundle();
        args.putSerializable("image_url", url);
        see_image_f.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, see_image_f).commit();
    }


    public void Open_menu_tab(View anchor_view) {
        Context wrapper = new ContextThemeWrapper(context, R.style.AlertDialogCustom);
        PopupMenu popup = new PopupMenu(wrapper, anchor_view);
        popup.getMenuInflater().inflate(R.menu.menu, popup.getMenu());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            popup.setGravity(Gravity.TOP | Gravity.RIGHT);
        }
        popup.show();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {

                    case R.id.edit_Profile_id:
                        Open_Edit_profile();
                        break;

                    case R.id.favourite_id:
                        OpenFavouriteVideos();
                        break;

                    case R.id.setting_id:
                        Open_setting();
                        break;

                    case R.id.logout_id:
                        Logout();
                        break;

                }
                return true;
            }
        });

    }


    public void OpenFavouriteVideos() {
        Favourite_Main_F favourite_main_f = new Favourite_Main_F();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, favourite_main_f).commit();
    }


    public void Open_Following() {

        Following_F following_f = new Following_F(new Fragment_Callback() {
            @Override
            public void Responce(Bundle bundle) {

                Call_Api_For_get_Allvideos();

            }
        });
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("id", Functions.getSharedPreference(context).getString(Variables.u_id, ""));
        args.putString("from_where", "following");
        following_f.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, following_f).commit();

    }

    public void Open_Followers() {
        Following_F following_f = new Following_F(new Fragment_Callback() {
            @Override
            public void Responce(Bundle bundle) {
                Call_Api_For_get_Allvideos();
            }
        });
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("id", Functions.getSharedPreference(context).getString(Variables.u_id, ""));
        args.putString("from_where", "fan");
        following_f.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.MainMenuFragment, following_f).commit();

    }

    // this will erase all the user info store in locally and logout the user
    public void Logout() {

        JSONObject params = new JSONObject();
        try {
            params.put("user_id", Functions.getSharedPreference(context).getString(Variables.u_id, ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(getActivity(), ApiLinks.logout, params, new Callback() {
            @Override
            public void Responce(String resp) {

                try {
                    JSONObject jsonObject = new JSONObject(resp);
                    String code = jsonObject.optString("code");

                    if (code.equalsIgnoreCase("200")) {
                        Paper.book("Setting").destroy();

                        GoogleSignInOptions gso = new GoogleSignInOptions.
                                Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                                build();
                        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
                        googleSignInClient.signOut();

                        LoginManager.getInstance().logOut();

                        SharedPreferences.Editor editor = Functions.getSharedPreference(context).edit();
                        editor.putString(Variables.u_id, "");
                        editor.putString(Variables.u_name, "");
                        editor.putString(Variables.u_pic, "");
                        editor.putString(Variables.auth_token, null);
                        editor.putBoolean(Variables.islogin, false);
                        editor.commit();
                        getActivity().finish();
                        startActivity(new Intent(getActivity(), MainMenuActivity.class));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });

    }


    @Override
    public void onDetach() {
        super.onDetach();
        Functions.deleteCache(context);
    }


}
