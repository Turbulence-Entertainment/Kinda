package com.qboxus.musictok.ActivitesFragment;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.Player;
import com.google.android.material.tabs.TabLayout;
import com.hendraanggrian.appcompat.widget.SocialView;
import com.qboxus.musictok.ActivitesFragment.Accounts.Login_A;
import com.qboxus.musictok.ActivitesFragment.Profile.Profile_F;
import com.qboxus.musictok.Adapters.Comments_Adapter;
import com.qboxus.musictok.Interfaces.API_CallBack;
import com.qboxus.musictok.Interfaces.Fragment_Data_Send;
import com.qboxus.musictok.Interfaces.KeyboardHeightObserver;
import com.qboxus.musictok.Main_Menu.MainMenuFragment;
import com.qboxus.musictok.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.qboxus.musictok.Models.Comment_Get_Set;
import com.qboxus.musictok.Models.Home_Get_Set;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.KeyboardHeightProvider;
import com.qboxus.musictok.SimpleClasses.SoftKeyboardStateHelper;
import com.qboxus.musictok.SimpleClasses.Variables;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class Comment_F extends RootFragment implements
        KeyboardHeightObserver {

    View view;
    Context context;

    RecyclerView recyclerView;

    Comments_Adapter adapter;

    ArrayList<Comment_Get_Set> data_list;
    boolean isOpened = false;
    Home_Get_Set item;
    String video_id;
    String user_id;

    EditText message_edit;
    ImageButton send_btn;
    ProgressBar send_progress;

    TextView comment_count_txt, no_data_layout;

    FrameLayout comment_screen;
    String comment_id, parent_comment_id, reply_user_name, position;
    String reply_status = null;
    public static int comment_count = 0;

    public Comment_F() {

    }

    Fragment_Data_Send fragment_data_send;
    String from_where;

    private KeyboardHeightProvider keyboardHeightProvider;
    RelativeLayout write_layout;


    @SuppressLint("ValidFragment")
    public Comment_F(int count, Fragment_Data_Send fragment_data_send, String from_where) {
        comment_count = count;
        this.fragment_data_send = fragment_data_send;
        this.from_where = from_where;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_comment, container, false);
        context = getContext();

        comment_screen = view.findViewById(R.id.comment_screen);
        no_data_layout = view.findViewById(R.id.no_data_layout);
        comment_screen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity().onBackPressed();

            }
        });

        view.findViewById(R.id.Goback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getActivity().onBackPressed();
            }
        });


        Bundle bundle = getArguments();
        if (bundle != null) {
            video_id = bundle.getString("video_id");
            user_id = bundle.getString("user_id");
            item = (Home_Get_Set) bundle.getSerializable("data");
        }


        SoftKeyboardStateHelper.SoftKeyboardStateListener softKeyboardStateListener = new SoftKeyboardStateHelper.SoftKeyboardStateListener() {
            @Override
            public void onSoftKeyboardOpened(int keyboardHeightInPx) {

            }

            @Override
            public void onSoftKeyboardClosed() {
                if ((parent_comment_id != null && !parent_comment_id.equals("")) || reply_status != null) {
                    message_edit.setHint("Leave a comment...");
                    reply_status = null;
                    parent_comment_id = null;
                }
            }
        };


        final SoftKeyboardStateHelper softKeyboardStateHelper = new SoftKeyboardStateHelper(context, view.findViewById(R.id.comment_screen));
        softKeyboardStateHelper.addSoftKeyboardStateListener(softKeyboardStateListener);


        comment_count_txt = view.findViewById(R.id.comment_count);

        recyclerView = view.findViewById(R.id.recylerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);


        data_list = new ArrayList<>();
        adapter = new Comments_Adapter(context, data_list, new Comments_Adapter.OnItemClickListener() {
            @Override
            public void onItemClick(int positon, Comment_Get_Set item, View view) {

                switch (view.getId()) {

                    case R.id.message_layout:
                        message_edit.setHint("Reply to " + item.user_name);
                        message_edit.requestFocus();
                        reply_status = "reply";
                        comment_id = item.comment_id;
                        Functions.showKeyboard(getActivity());
                        break;

                    case R.id.like_layout:
                        if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {
                            Like_Comment(positon, item);
                        } else {
                            Functions.hideSoftKeyboard(getActivity());
                            Intent intent = new Intent(getActivity(), Login_A.class);
                            startActivity(intent);
                            getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
                        }
                        break;


                }
            }
        }, new Comments_Adapter.onRelyItemCLickListener() {
            @Override
            public void onItemClick(ArrayList<Comment_Get_Set> arrayList, int postion, View view) {
                switch (view.getId()) {
                    case R.id.reply_layout:
                        reply_user_name = arrayList.get(postion).replay_user_name;
                        message_edit.setHint("Reply to " + reply_user_name);
                        message_edit.requestFocus();
                        reply_status = "reply";
                        comment_id = arrayList.get(postion).comment_reply_id;
                        parent_comment_id = arrayList.get(postion).parent_comment_id;
                        Functions.showKeyboard(getActivity());
                        break;


                    case R.id.like_layout:
                        if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {
                            Comment_Get_Set item = arrayList.get(postion);
                            Like_Comment_Reply(postion, arrayList.get(postion));
                        } else {
                            Functions.hideSoftKeyboard(getActivity());
                            Intent intent = new Intent(getActivity(), Login_A.class);
                            startActivity(intent);
                            getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
                        }
                        break;
                }
            }
        }, new Comments_Adapter.LinkClickListener() {
            @Override
            public void onLinkClicked(SocialView view, String matchedText) {
                Functions.hideSoftKeyboard(getActivity());
                OpenProfile_byUsername(matchedText);
            }
        });

        recyclerView.setAdapter(adapter);


        message_edit = view.findViewById(R.id.message_edit);


        send_progress = view.findViewById(R.id.send_progress);
        send_btn = view.findViewById(R.id.send_btn);
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String message = message_edit.getText().toString();
                if (!TextUtils.isEmpty(message)) {
                    if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {
                        if (reply_status == null) {
                            Send_Comments(video_id, message);
                        } else if (parent_comment_id != null && !parent_comment_id.equals("")) {
                            message = "replied to " + "@" + reply_user_name + " " + message;
                            Send_Comments_reply(parent_comment_id, user_id, message);
                        } else {
                            Send_Comments_reply(comment_id, user_id, message);
                        }
                        message_edit.setText(null);
                        send_progress.setVisibility(View.VISIBLE);
                        send_btn.setVisibility(View.GONE);
                    } else {
                        Intent intent = new Intent(getActivity(), Login_A.class);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.out_to_top);
                    }
                }

            }
        });


        if (Functions.isShowContentPrivacy(item.apply_privacy_model.getVideo_comment(), item.follow_status_button.equalsIgnoreCase("friends"))) {
            send_btn.setEnabled(true);
        } else
            send_btn.setEnabled(false);


        Get_All_Comments();



        keyboardHeightProvider = new KeyboardHeightProvider(getActivity());
        keyboardHeightProvider.setKeyboardHeightObserver(this);

        write_layout=view.findViewById(R.id.write_layout);

        view.findViewById(R.id.comment_screen).post(new Runnable() {
            public void run() {
                keyboardHeightProvider.start();
            }
        });

        return view;
    }





    private void Like_Comment_Reply(int postion, Comment_Get_Set item) {

        String action = item.comment_reply_liked;

        String parent_id = item.parent_comment_id;

        if (action != null) {
            if (action.equals("1")) {
                action = "0";
                item.reply_liked_count = "" + (Functions.ParseInterger(item.reply_liked_count) - 1);
            } else {
                action = "1";
                item.reply_liked_count = "" + (Functions.ParseInterger(item.reply_liked_count) + 1);
            }
        }


        for (int i = 0; i < data_list.size(); i++) {

            if (!data_list.isEmpty() && !data_list.get(i).arrayList.isEmpty()) {

                if (item.parent_comment_id.equals(data_list.get(i).comment_id)) {

                    data_list.get(i).arrayList.remove(postion);
                    item.comment_reply_liked = action;
                    data_list.get(i).arrayList.add(postion, item);

                }

            }
        }
        adapter.notifyDataSetChanged();
        Functions.Call_Api_For_likeCommentReply(

                getActivity(), item.comment_reply_id, new

                        API_CallBack() {
                            @Override
                            public void ArrayData(ArrayList arrayList) {

                            }

                            @Override
                            public void OnSuccess(String responce) {

                            }

                            @Override
                            public void OnFail(String responce) {

                            }
                        });
    }


    private void Like_Comment(int positon, Comment_Get_Set item) {

        String action = item.liked;

        if (action != null) {
            if (action.equals("1")) {
                action = "0";
                item.like_count = "" + (Functions.ParseInterger(item.like_count) - 1);
            } else {
                action = "1";
                item.like_count = "" + (Functions.ParseInterger(item.like_count) + 1);
            }


            data_list.remove(positon);
            item.liked = action;
            data_list.add(positon, item);
            adapter.notifyDataSetChanged();
        }
        Functions.Call_Api_For_like_comment(getActivity(), item.comment_id, new API_CallBack() {
            @Override
            public void ArrayData(ArrayList arrayList) {

            }

            @Override
            public void OnSuccess(String responce) {

            }

            @Override
            public void OnFail(String responce) {

            }
        });
    }


    @Override
    public void onDetach() {
        Functions.hideSoftKeyboard(getActivity());
        super.onDetach();

        keyboardHeightProvider.setKeyboardHeightObserver(null);
        keyboardHeightProvider.close();


    }

    // this funtion will get all the comments against post
    public void Get_All_Comments() {

        Functions.Call_Api_For_get_Comment(getActivity(), video_id, new API_CallBack() {
            @Override
            public void ArrayData(ArrayList arrayList) {
                Log.d("tictic_logged","response at arrayList"+arrayList.toString());
                    no_data_layout.setVisibility(View.GONE);
                    ArrayList<Comment_Get_Set> arrayList1 = arrayList;
                    for (Comment_Get_Set item : arrayList1) {
                        data_list.add(item);
                    }
                    comment_count_txt.setText(data_list.size() + " comments");
                    adapter.notifyDataSetChanged();
            }

            @Override
            public void OnSuccess(String responce) {
                // this will return string type responce
                Log.d("tictic_logged","response at OnSuccess"+responce);
                no_data_layout.setVisibility(View.GONE);
            }

            @Override
            public void OnFail(String responce) {
                // this will return a failed responce
                Log.d("tictic_logged","response at OnFail"+responce);
                no_data_layout.setVisibility(View.VISIBLE);
            }

        });

    }

    // this function will call an api to upload your comment reply
    private void Send_Comments_reply(String comment_id, String user_id, String message) {
        Functions.Call_Api_For_Send_Comment_reply(getActivity(), comment_id, user_id, message, new API_CallBack() {
            @Override
            public void ArrayData(ArrayList arrayList) {
                send_progress.setVisibility(View.GONE);
                send_btn.setVisibility(View.VISIBLE);
                Functions.hideSoftKeyboard(getActivity());
                message_edit.setHint("Leave a comment...");

                ArrayList<Comment_Get_Set> arrayList1 = arrayList;

                for (int i = 0; i < data_list.size(); i++) {
                    if (parent_comment_id != null) {
                        if (parent_comment_id.equals(data_list.get(i).comment_id)) {
                            for (Comment_Get_Set item : arrayList1) {
                                data_list.get(i).arrayList.add(item);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        if (comment_id.equals(data_list.get(i).comment_id)) {
                            Comment_Get_Set comment_get_set = new Comment_Get_Set();
                            comment_get_set.item_count_replies = "1";
                            for (Comment_Get_Set item : arrayList1) {
                                data_list.get(i).arrayList.add(item);
                            }
                        }


                        adapter.notifyDataSetChanged();
                    }

                }
                reply_status = null;
                parent_comment_id = null;
            }

            @Override
            public void OnSuccess(String responce) {
                // this will return a string responce
            }

            @Override
            public void OnFail(String responce) {

                // this will return the failed responce
            }

        });

    }

    // this function will call an api to upload your comment
    public void Send_Comments(String video_id, final String comment) {

        Functions.Call_Api_For_Send_Comment(getActivity(), video_id, comment, new API_CallBack() {
            @Override
            public void ArrayData(ArrayList arrayList) {
                send_progress.setVisibility(View.GONE);
                send_btn.setVisibility(View.VISIBLE);
                no_data_layout.setVisibility(View.GONE);
                Functions.hideSoftKeyboard(getActivity());
                ArrayList<Comment_Get_Set> arrayList1 = arrayList;
                for (Comment_Get_Set item : arrayList1) {
                    data_list.add(0, item);
                    comment_count++;
                    comment_count_txt.setText(comment_count + " comments");

                    if (fragment_data_send != null)
                        fragment_data_send.onDataSent("" + comment_count);

                }
                adapter.notifyDataSetChanged();

            }

            @Override
            public void OnSuccess(String responce) {
                // this will return a string responce
            }

            @Override
            public void OnFail(String responce) {

                // this will return the failed responce
            }

        });

    }

    private void OpenProfile_byUsername(String username) {

        if (Variables.sharedPreferences.getString(Variables.u_name, "0").equals(username)) {

            TabLayout.Tab profile = MainMenuFragment.tabLayout.getTabAt(4);
            profile.select();

        } else {

            Profile_F profile_f = new Profile_F();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
            Bundle args = new Bundle();
            args.putString("user_name", username);
            profile_f.setArguments(args);
            transaction.addToBackStack(null);
            transaction.replace(R.id.comment_screen, profile_f).commit();

        }


    }




    int privious_height = 0;
    @Override
    public void onKeyboardHeightChanged(int height, int orientation) {

        Log.d(Variables.tag, "" + height);
        if (height < 0) {
            privious_height = Math.abs(height);
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(write_layout.getWidth(), write_layout.getHeight());
        params.bottomMargin = height + privious_height;
        write_layout.setLayoutParams(params);

    }

}
