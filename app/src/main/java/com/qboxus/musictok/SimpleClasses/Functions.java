package com.qboxus.musictok.SimpleClasses;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.API_CallBack;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.Models.Comment_Get_Set;
import com.qboxus.musictok.Models.Home_Get_Set;
import com.qboxus.musictok.Models.PrivacyPolicySettingModel;
import com.qboxus.musictok.Models.PushNotificationSettingModel;
import com.qboxus.musictok.R;
import com.gmail.samehadar.iosdialog.CamomileSpinner;
import com.googlecode.mp4parser.authoring.Track;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * Created by AQEEL on 2/20/2019.
 */

public class Functions {


    public static void black_status_bar(Activity activity) {
        View view = activity.getWindow().getDecorView();

        int flags = view.getSystemUiVisibility();
        flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        view.setSystemUiVisibility(flags);
        activity.getWindow().setStatusBarColor(Color.BLACK);
    }

    public static void white_status_bar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        int flags = view.getSystemUiVisibility();
        flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        view.setSystemUiVisibility(flags);
        activity.getWindow().setStatusBarColor(Color.WHITE);
    }


    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public static void showKeyboard(Activity activity) {
        View view = activity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    public static SharedPreferences getSharedPreference(Context context){
        if(Variables.sharedPreferences!=null)
            return Variables.sharedPreferences;
        else {
            Variables.sharedPreferences = context.getSharedPreferences(Variables.pref_name, Context.MODE_PRIVATE);
            return Variables.sharedPreferences;
        }

    }


    public static int ParseInterger(String value){
        if(value!=null && !value.equals("")){
            return Integer.parseInt(value);
        }
        else
            return 0;
    }

    public static String GetSuffix(String value) {
        try {

            if(value!=null && (!value.equals("") && !value.equalsIgnoreCase("null")))
            {
                long count = Long.parseLong(value);
                if (count < 1000)
                    return "" + count;
                int exp = (int) (Math.log(count) / Math.log(1000));
                return String.format("%.1f %c",
                        count / Math.pow(1000, exp),
                        "kMBTPE".charAt(exp - 1));
            }
            else{
                return "0";
            }
            } catch (Exception e) {
            return value;
        }

    }

    public static String get_Random_string(int n) {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvxyz";

        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {

            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());

            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }


    public static void Show_Alert(Context context, String title, String Message) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(Message)
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }


    public static void Show_Alert(Context context, String title, String Message, String postivebtn, String negitivebtn, final Callback callback) {

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(Message)
                .setNegativeButton(negitivebtn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        callback.Responce("no");
                    }
                })
                .setPositiveButton(postivebtn, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        callback.Responce("yes");

                    }
                }).show();
    }

    public static Dialog dialog;

    public static void Show_loader(Context context, boolean outside_touch, boolean cancleable) {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.item_dialog_loading_view);
        dialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.d_round_white_background));


        CamomileSpinner loader = dialog.findViewById(R.id.loader);
        loader.start();


        if (!outside_touch)
            dialog.setCanceledOnTouchOutside(false);

        if (!cancleable)
            dialog.setCancelable(false);

        dialog.show();
    }

    public static void cancel_loader() {
        if (dialog != null) {
            dialog.cancel();
        }
    }


    public static void Show_video_option(Context context, final Home_Get_Set home_get_set, final Callback callback) {


        final Dialog alertDialog = new Dialog(context);
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(R.layout.alert_label_editor);
        alertDialog.getWindow().setBackgroundDrawable(context.getResources().getDrawable(R.drawable.d_round_white_background));

        RelativeLayout btn_add_to_fav = alertDialog.findViewById(R.id.btn_add_to_fav);
        RelativeLayout btn_not_insterested = alertDialog.findViewById(R.id.btn_not_insterested);
        RelativeLayout btn_report = alertDialog.findViewById(R.id.btn_report);


        TextView fav_unfav_txt = alertDialog.findViewById(R.id.fav_unfav_txt);


        if (home_get_set.favourite != null && home_get_set.favourite.equals("1"))
            fav_unfav_txt.setText("Remove from favourite");
        else
            fav_unfav_txt.setText("Add to favourite");


        if (home_get_set.user_id.equalsIgnoreCase(Variables.sharedPreferences.getString(Variables.u_id, ""))) {
            btn_report.setVisibility(View.GONE);
        }


        btn_add_to_fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.dismiss();
                callback.Responce("favourite");

            }
        });


        btn_not_insterested.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.dismiss();
                callback.Responce("not_intrested");

            }
        });


        btn_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                callback.Responce("report");
            }
        });

        alertDialog.show();
    }


    public static String show_username(String username) {
        if (username != null && username.contains("@"))
            return username;
        else
            return "@" + username;
    }


    public static float dpToPx(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("isMyServiceRunning?", true + "");
                return true;
            }
        }
        Log.i("isMyServiceRunning?", false + "");
        return false;
    }


    public static void Share_through_app(final Activity activity, final String link) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, link);
                activity.startActivity(Intent.createChooser(intent, ""));

            }
        }).start();
    }


    public static String ChangeDate_to_today_or_yesterday(Context context, String date) {
        try {
            Calendar current_cal = Calendar.getInstance();

            Calendar date_cal = Calendar.getInstance();

            SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy HH:mm:ssZZ");
            Date d = null;
            try {
                d = f.parse(date);
                date_cal.setTime(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }


            long difference = (current_cal.getTimeInMillis() - date_cal.getTimeInMillis()) / 1000;

            if (difference < 86400) {
                if (current_cal.get(Calendar.DAY_OF_YEAR) - date_cal.get(Calendar.DAY_OF_YEAR) == 0) {

                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                    return sdf.format(d);
                } else
                    return "yesterday";
            } else if (difference < 172800) {
                return "yesterday";
            } else
                return (difference / 86400) + " day ago";

        } catch (Exception e) {

            return date;
        }


    }


    public static String Bitmap_to_base64(Activity activity, Bitmap imagebitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imagebitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] byteArray = baos.toByteArray();
        String base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return base64;
    }

    public static Bitmap Base64_to_bitmap(String base_64) {
        Bitmap decodedByte = null;
        try {

            byte[] decodedString = Base64.decode(base_64, Base64.DEFAULT);
            decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } catch (Exception e) {

        }
        return decodedByte;
    }


    public static boolean isShowContentPrivacy(String string_case, boolean isFriend) {
        if (string_case == null)
            return true;
        else {
            string_case = StringParseFromServerRestriction(string_case);

            if (string_case.equalsIgnoreCase("Everyone")) {
                return true;
            } else if (string_case.equalsIgnoreCase("Friends") &&
                    Variables.sharedPreferences.getBoolean(Variables.islogin, false) && isFriend) {
                return true;
            } else {
                return false;
            }


        }

    }

    public static String StringParseFromServerRestriction(String res_string) {
        res_string = res_string.toUpperCase();
        res_string = res_string.replace("_", " ");
        return res_string;
    }

    public static String StringParseIntoServerRestriction(String res_string) {
        res_string = res_string.toLowerCase();
        res_string = res_string.replace(" ", "_");
        return res_string;
    }


    public static double correctTimeToSyncSample(Track track, double cutHere, boolean next) {
        double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
        long currentSample = 0;
        double currentTime = 0;
        for (int i = 0; i < track.getSampleDurations().length; i++) {
            long delta = track.getSampleDurations()[i];

            if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
                timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(), currentSample + 1)] = currentTime;
            }
            currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
            currentSample++;

        }
        double previous = 0;
        for (double timeOfSyncSample : timeOfSyncSamples) {
            if (timeOfSyncSample > cutHere) {
                if (next) {
                    return timeOfSyncSample;
                } else {
                    return previous;
                }
            }
            previous = timeOfSyncSample;
        }
        return timeOfSyncSamples[timeOfSyncSamples.length - 1];
    }


    public static void make_directry(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }


    public static String getRandomString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 10) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }


    public static long getfileduration(Context context, Uri uri) {
        try {

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(context, uri);
            String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            final int file_duration = Functions.ParseInterger(durationStr);

            return file_duration;
        } catch (Exception e) {

        }
        return 0;
    }


    // Bottom is all the Apis which is mostly used in app we have add it
    // just one time and whenever we need it we will call it

    public static void Call_Api_For_like_video(final Activity activity,
                                               String video_id, String action,
                                               final API_CallBack api_callBack) {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Variables.sharedPreferences.getString(Variables.u_id, "0"));
            parameters.put("video_id", video_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(activity, ApiLinks.likeVideo, parameters, new Callback() {
            @Override
            public void Responce(String resp) {

                if (api_callBack != null)
                    api_callBack.OnSuccess(resp);
            }
        });


    }


    public static void Call_Api_For_like_comment(final Activity activity,
                                                 String video_id,
                                                 final API_CallBack api_callBack) {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Variables.sharedPreferences.getString(Variables.u_id, "0"));
            parameters.put("comment_id", video_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(activity, ApiLinks.likeComment, parameters, new Callback() {
            @Override
            public void Responce(String resp) {

                if (api_callBack != null)
                    api_callBack.OnSuccess(resp);
            }
        });


    }

    public static void Call_Api_For_likeCommentReply(final Activity activity,
                                                     String comment_reply_id,
                                                     final API_CallBack api_callBack) {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Variables.sharedPreferences.getString(Variables.u_id, "0"));
            parameters.put("comment_reply_id", comment_reply_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(activity, ApiLinks.likeCommentReply, parameters, new Callback() {
            @Override
            public void Responce(String resp) {

                Log.d(Variables.tag, "resp at like comment reply : " + resp);

                if (api_callBack != null)
                    api_callBack.OnSuccess(resp);
            }
        });


    }


    public static void Call_Api_For_Send_Comment(final Activity activity, String video_id, String comment, final API_CallBack api_callBack) {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Variables.sharedPreferences.getString(Variables.u_id, "0"));
            parameters.put("video_id", video_id);
            parameters.put("comment", comment);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        ApiRequest.Call_Api(activity, ApiLinks.postCommentOnVideo, parameters, new Callback() {
            @Override
            public void Responce(String resp) {

                ArrayList<Comment_Get_Set> arrayList = new ArrayList<>();
                try {
                    JSONObject response = new JSONObject(resp);
                    String code = response.optString("code");
                    if (code.equals("200")) {

                        JSONObject msg = response.optJSONObject("msg");
                        JSONObject VideoComment = msg.optJSONObject("VideoComment");
                        JSONObject User = msg.optJSONObject("User");

                        Comment_Get_Set item = new Comment_Get_Set();

                        item.fb_id = User.optString("id");
                        item.user_name = User.optString("username");
                        item.first_name = User.optString("first_name");
                        item.last_name = User.optString("last_name");
                        item.profile_pic = User.optString("profile_pic");
                        if (!item.profile_pic.contains(Variables.http)) {
                            item.profile_pic = ApiLinks.BASE_URL + item.profile_pic;
                        }

                        item.video_id = VideoComment.optString("video_id");
                        item.comments = VideoComment.optString("comment");
                        item.created = VideoComment.optString("created");

                        arrayList.add(item);

                        api_callBack.ArrayData(arrayList);

                    } else {
                        Functions.show_toast(activity , "" + response.optString("msg"));
                    }

                } catch (JSONException e) {
                    api_callBack.OnFail(e.toString());
                    e.printStackTrace();
                }

            }
        });


    }


    public static void Call_Api_For_Send_Comment_reply(final Activity activity, String comment_id, String user_id, String comment, final API_CallBack api_callBack) {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("comment_id", comment_id);
            parameters.put("user_id", Variables.sharedPreferences.getString(Variables.u_id, "0"));
            parameters.put("comment", comment);

            Log.d(Variables.tag, "parameters at reply : " + parameters);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        ApiRequest.Call_Api(activity, ApiLinks.postCommentReply, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                Log.d(Variables.tag, "resp at reply : " + resp);

                ArrayList<Comment_Get_Set> arrayList = new ArrayList<>();
                try {
                    JSONObject response = new JSONObject(resp);
                    String code = response.optString("code");
                    if (code.equals("200")) {

                        JSONObject msg = response.optJSONObject("msg");
                        JSONObject VideoComment = msg.optJSONObject("VideoComment");
                        JSONObject VideoCommentReply = msg.optJSONObject("VideoCommentReply");
                        JSONObject User = msg.optJSONObject("User");

                        Comment_Get_Set item = new Comment_Get_Set();

                        item.fb_id = User.optString("id");
                        item.first_name = User.optString("first_name");
                        item.last_name = User.optString("last_name");
                        item.replay_user_name = User.optString("username");
                        item.replay_user_url = User.optString("profile_pic");

                        item.video_id = VideoComment.optString("video_id");
                        item.comments = VideoComment.optString("comment");
                        item.created = VideoComment.optString("created");


                        item.comment_reply_id = VideoCommentReply.optString("id");
                        item.comment_reply = VideoCommentReply.optString("comment");
                        item.parent_comment_id = VideoCommentReply.optString("comment_id");
                        item.reply_create_date = VideoCommentReply.optString("created");
                        item.reply_liked_count = "0";
                        item.comment_reply_liked = "0";

                        arrayList.add(item);
                        item.item_count_replies = "1";
                        api_callBack.ArrayData(arrayList);

                    } else {
                        Functions.show_toast(activity , "" + response.optString("msg"));
                    }

                } catch (JSONException e) {
                    api_callBack.OnFail(e.toString());
                    e.printStackTrace();
                }
            }
        });


    }

    public static void Call_Api_For_get_Comment(final Activity activity, String video_id, final API_CallBack api_callBack) {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("video_id", video_id);
            if (Variables.sharedPreferences.getBoolean(Variables.islogin, false)) {
                parameters.put("user_id", Variables.sharedPreferences.getString(Variables.u_id, "0"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(activity, ApiLinks.showVideoComments, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                Log.d(Variables.tag, "resp at comment : " + resp);
                ArrayList<Comment_Get_Set> arrayList = new ArrayList<>();
                try {
                    JSONObject response = new JSONObject(resp);
                    String code = response.optString("code");
                    if (code.equals("200")) {
                        JSONArray msgArray = response.getJSONArray("msg");
                        for (int i = 0; i < msgArray.length(); i++) {
                            JSONObject itemdata = msgArray.optJSONObject(i);

                            JSONObject VideoComment = itemdata.optJSONObject("VideoComment");
                            JSONObject User = itemdata.optJSONObject("User");

                            JSONArray VideoCommentReply = itemdata.optJSONArray("VideoCommentReply");

                            ArrayList<Comment_Get_Set> replyList = new ArrayList<>();
                            if (VideoCommentReply.length() > 0) {
                                for (int j = 0; j < VideoCommentReply.length(); j++) {
                                    JSONObject jsonObject = VideoCommentReply.getJSONObject(j);

                                    JSONObject reply_user = jsonObject.getJSONObject("User");
                                    Comment_Get_Set comment_get_set = new Comment_Get_Set();

                                    comment_get_set.comment_reply_id = jsonObject.optString("id");
                                    comment_get_set.reply_liked_count = jsonObject.optString("like_count");
                                    comment_get_set.comment_reply_liked = jsonObject.optString("like");
                                    comment_get_set.comment_reply = jsonObject.optString("comment");
                                    comment_get_set.created = jsonObject.optString("created");


                                    comment_get_set.replay_user_name = reply_user.optString("username");
                                    comment_get_set.replay_user_url = reply_user.optString("profile_pic");
                                    comment_get_set.parent_comment_id = VideoComment.optString("id");
                                    replyList.add(comment_get_set);
                                }
                            }

                            Comment_Get_Set item = new Comment_Get_Set();


                            item.fb_id = User.optString("id");
                            item.user_name = User.optString("username");
                            item.first_name = User.optString("first_name");
                            item.last_name = User.optString("last_name");
                            item.arraylist_size = String.valueOf(VideoCommentReply.length());
                            item.profile_pic = User.optString("profile_pic");
                            if (!item.profile_pic.contains(Variables.http)) {
                                item.profile_pic = ApiLinks.BASE_URL + item.profile_pic;
                            }

                            item.arrayList = replyList;
                            item.video_id = VideoComment.optString("video_id");
                            item.comments = VideoComment.optString("comment");
                            item.liked = VideoComment.optString("like");
                            item.like_count = VideoComment.optString("like_count");
                            item.comment_id = VideoComment.optString("id");
                            item.created = VideoComment.optString("created");


                            arrayList.add(item);
                        }

                        api_callBack.ArrayData(arrayList);
                        api_callBack.OnSuccess(code.toString());
                    } else {
                        api_callBack.OnFail(code.toString());
                    }

                } catch (JSONException e) {
                    api_callBack.OnFail(e.toString());
                    e.printStackTrace();
                }
            }
        });

    }


    public static void Call_Api_For_update_view(final Activity activity,
                                                String video_id) {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("device_id", Variables.sharedPreferences.getString(Variables.device_id, "0"));
            parameters.put("video_id", video_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(activity, ApiLinks.watchVideo, parameters, null);


    }


    public static void Call_Api_For_Follow_or_unFollow
            (final Activity activity,
             String fb_id,
             String followed_fb_id,
             final API_CallBack api_callBack) {

        Functions.Show_loader(activity, false, false);


        JSONObject parameters = new JSONObject();
        try {
            parameters.put("sender_id", fb_id);
            parameters.put("receiver_id", followed_fb_id);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(activity, ApiLinks.followUser, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                Functions.cancel_loader();
                try {
                    JSONObject response = new JSONObject(resp);
                    String code = response.optString("code");
                    if (code.equals("200")) {
                        api_callBack.OnSuccess(response.toString());

                    } else {
                        Functions.show_toast(activity , "" + response.optString("msg"));
                    }

                } catch (Exception e) {
                    api_callBack.OnFail(e.toString());
                    e.printStackTrace();
                }
            }
        });


    }


    public static void Call_Api_For_Get_User_data
            (final Activity activity,
             String fb_id,
             final API_CallBack api_callBack) {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", fb_id);
            if(Variables.sharedPreferences.getBoolean(Variables.islogin,false) && fb_id!=null) {
                parameters.put("user_id", Variables.sharedPreferences.getString(Variables.u_id, ""));
                parameters.put("other_user_id", fb_id);
            }
            else if(fb_id!=null) {
                parameters.put("user_id", fb_id);
            }
            else {
                parameters.put("username", fb_id);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("resp", parameters.toString());

        ApiRequest.Call_Api(activity, ApiLinks.showUserDetail, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                Functions.cancel_loader();
                try {
                    JSONObject response = new JSONObject(resp);
                    String code = response.optString("code");
                    if (code.equals("200")) {
                        api_callBack.OnSuccess(response.toString());

                    } else {
                        Functions.show_toast(activity , "" + response.optString("msg"));
                    }

                } catch (Exception e) {
                    api_callBack.OnFail(e.toString());
                    e.printStackTrace();
                }
            }
        });

    }


    public static void Call_Api_For_Delete_Video
            (final Activity activity,
             String video_id,
             final API_CallBack api_callBack) {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("video_id", video_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(activity, ApiLinks.deleteVideo, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                Functions.cancel_loader();

                try {
                    JSONObject response = new JSONObject(resp);
                    String code = response.optString("code");
                    if (code.equals("200")) {
                        if (api_callBack != null)
                            api_callBack.OnSuccess(response.toString());

                    } else {
                        Functions.show_toast(activity , "" + response.optString("msg"));
                    }

                } catch (Exception e) {
                    if (api_callBack != null)
                        api_callBack.OnFail(e.toString());
                    e.printStackTrace();
                }


            }
        });


    }


    public static Home_Get_Set Parse_video_data(JSONObject User, JSONObject Sound, JSONObject Video, JSONObject UserPrivacy, JSONObject UserPushNotification) {
        Home_Get_Set item = new Home_Get_Set();

        if (User != null) {
            item.user_id = User.optString("id");
            item.username = User.optString("username");
            item.first_name = User.optString("first_name", "");
            item.last_name = User.optString("last_name", "");
            item.profile_pic = User.optString("profile_pic", "null");
            if (!item.profile_pic.contains(Variables.http)) {
                item.profile_pic = ApiLinks.BASE_URL + item.profile_pic;
            }

            item.verified = User.optString("verified");
            item.follow_status_button = User.optString("button");
        }

        if (Sound != null) {
            item.sound_id = Sound.optString("id");
            item.sound_name = Sound.optString("sound_name");
            item.sound_pic = Sound.optString("thum");
            item.sound_url_mp3 = Sound.optString("audio");
            item.sound_url_acc = Sound.optString("audio");
        }

        if (Video != null) {
            item.like_count = "0" + Video.optInt("like_count");
            item.video_comment_count = Video.optString("comment_count");


            item.privacy_type = Video.optString("privacy_type");
            item.allow_comments = Video.optString("allow_comments");
            item.allow_duet = Video.optString("allow_duet");
            item.video_id = Video.optString("id");
            item.liked = Video.optString("like");
            item.favourite = Video.optString("favourite");

            item.views=Video.optString("view");

            item.video_description = Video.optString("description");
            item.favourite = Video.optString("favourite");
            item.created_date = Video.optString("created");

            item.thum = Video.optString("thum");
            item.gif = Video.optString("gif");
            item.video_url = Video.optString("video", "");


            if (!item.video_url.contains(Variables.http)) {
                item.video_url = ApiLinks.BASE_URL + item.video_url;
            }

            if (!item.thum.contains(Variables.http))
                item.thum = ApiLinks.BASE_URL + item.thum;

            Log.d(Variables.tag,"Video_thumb "+item.thum);

            if (!item.gif.contains(Variables.http))
                item.gif = ApiLinks.BASE_URL + item.gif;

            Log.d(Variables.tag,"Video_gif "+item.gif);

            item.allow_duet = Video.optString("allow_duet");
            item.duet_video_id = Video.optString("duet_video_id");
            if (Video.has("duet")) {
                JSONObject duet = Video.optJSONObject("duet");
                if(duet!=null) {
                    JSONObject duet_user_obj = duet.optJSONObject("User");
                    if(duet_user_obj!=null)
                    item.duet_username = duet_user_obj.optString("username");
                }

            }

        }


        item.apply_privacy_model = new PrivacyPolicySettingModel();
        item.apply_push_notification_model = new PushNotificationSettingModel();

        if (UserPrivacy != null) {
            item.apply_privacy_model.setVideo_comment(UserPrivacy.optString("video_comment"));
            item.apply_privacy_model.setLiked_videos(UserPrivacy.optString("liked_videos"));
            item.apply_privacy_model.setDuet(UserPrivacy.optString("duet"));
            item.apply_privacy_model.setDirect_message(UserPrivacy.optString("direct_message"));
            item.apply_privacy_model.setVideos_download(UserPrivacy.optString("videos_download"));
        }

        if (UserPushNotification != null) {
            item.apply_push_notification_model.setComments(UserPushNotification.optString("comments"));
            item.apply_push_notification_model.setDirectmessage(UserPushNotification.optString("direct_messages"));
            item.apply_push_notification_model.setLikes(UserPushNotification.optString("likes"));
            item.apply_push_notification_model.setMentions(UserPushNotification.optString("mentions"));
            item.apply_push_notification_model.setNewfollowers(UserPushNotification.optString("new_followers"));
            item.apply_push_notification_model.setVideoupdates(UserPushNotification.optString("video_updates"));
        }

        return item;

    }


    public static Dialog indeterminant_dialog;

    public static void Show_indeterminent_loader(Context context, boolean outside_touch, boolean cancleable) {

        indeterminant_dialog = new Dialog(context);
        indeterminant_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        indeterminant_dialog.setContentView(R.layout.item_indeterminant_progress_layout);
        indeterminant_dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.d_round_white_background));


        if (!outside_touch)
            indeterminant_dialog.setCanceledOnTouchOutside(false);

        if (!cancleable)
            indeterminant_dialog.setCancelable(false);

        indeterminant_dialog.show();

    }


    public static void cancel_indeterminent_loader() {
        if (indeterminant_dialog != null) {
            indeterminant_dialog.cancel();
        }
    }


    public static Dialog determinant_dialog;
    public static ProgressBar determinant_progress;

    public static void Show_determinent_loader(Context context, boolean outside_touch, boolean cancleable) {

        determinant_dialog = new Dialog(context);
        determinant_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        determinant_dialog.setContentView(R.layout.item_determinant_progress_layout);
        determinant_dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.d_round_white_background));

        determinant_progress = determinant_dialog.findViewById(R.id.pbar);

        if (!outside_touch)
            determinant_dialog.setCanceledOnTouchOutside(false);

        if (!cancleable)
            determinant_dialog.setCancelable(false);

        determinant_dialog.show();

    }

    public static void Show_loading_progress(int progress) {
        if (determinant_progress != null) {
            determinant_progress.setProgress(progress);

        }
    }


    public static void cancel_determinent_loader() {
        if (determinant_dialog != null) {
            determinant_progress = null;
            determinant_dialog.cancel();
        }
    }


    public static boolean Checkstoragepermision(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;

            } else {

                activity.requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {

            return true;
        }
    }


    public static boolean check_permissions(Activity context) {

        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
        };

        if (!hasPermissions(context, PERMISSIONS)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                context.requestPermissions(PERMISSIONS, 2);
            }
        } else {

            return true;
        }

        return false;
    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    // these function are remove the cache memory which is very helpfull in memmory managmet
    public static void deleteCache(Context context) {
        Glide.get(context).clearMemory();

        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public static void show_toast(Context context , String msg){
        if(Variables.is_toast_enable) {
            Toast.makeText(context, "" + msg, Toast.LENGTH_SHORT).show();
        }
    }


}
