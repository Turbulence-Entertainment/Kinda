package com.qboxus.musictok.ActivitesFragment.Video_Recording;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.hendraanggrian.appcompat.widget.SocialEditText;
import com.qboxus.musictok.Main_Menu.MainMenuActivity;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Models.HashTagModel;
import com.qboxus.musictok.Adapters.HashTag_Adapter;
import com.qboxus.musictok.Models.Users_Model;
import com.qboxus.musictok.Interfaces.ServiceCallback;
import com.qboxus.musictok.Services.Upload_Service;
import com.qboxus.musictok.Interfaces.Adapter_Click_Listener;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.Interfaces.Fragment_Callback;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class Post_Video_A extends AppCompatActivity implements ServiceCallback, View.OnClickListener {


    ImageView video_thumbnail;
    String video_path;

    ServiceCallback serviceCallback;
    SocialEditText description_edit;

    String draft_file, duet_video_id, duet_video_username, duet_orientation;

    TextView privcy_type_txt, duet_username, aditional_details_text_count;
    Switch comment_switch, duet_switch;

    Bitmap bmThumbnail;

    int counter = -1;
    RelativeLayout duet_layout_username;
    ArrayList<Users_Model> taged_user = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_video);
        duet_username = findViewById(R.id.duet_username);
        duet_layout_username = findViewById(R.id.duet_layout_username);

        Intent intent = getIntent();
        if (intent != null) {
            draft_file = intent.getStringExtra("draft_file");
            duet_video_id = intent.getStringExtra("duet_video_id");
            duet_orientation = intent.getStringExtra("duet_orientation");
            duet_video_username = intent.getStringExtra("duet_video_username");
            if (duet_video_username != null && !duet_video_username.equals("")) {
                duet_layout_username.setVisibility(View.VISIBLE);
                duet_username.setText(duet_video_username);
            }
        }


        video_path = Variables.output_filter_file;
        video_thumbnail = findViewById(R.id.video_thumbnail);


        description_edit = findViewById(R.id.description_edit);
        aditional_details_text_count = findViewById(R.id.aditional_details_text_count);

        // this will get the thumbnail of video and show them in imageview

        bmThumbnail = ThumbnailUtils.createVideoThumbnail(video_path,
                MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);

        if (bmThumbnail != null && duet_video_id != null) {
            Bitmap duet_video_bitmap = null;
            if (duet_video_id != null) {
                duet_video_bitmap = ThumbnailUtils.createVideoThumbnail(Variables.app_showing_folder + duet_video_id + ".mp4",
                        MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
            }
            Bitmap combined = combineImages(bmThumbnail, duet_video_bitmap);
            Bitmap bitmap = Bitmap.createScaledBitmap(combined, (combined.getWidth() / 6), (combined.getHeight() / 6), false);

            bmThumbnail.recycle();
            combined.recycle();

            video_thumbnail.setImageBitmap(bitmap);
            Functions.getSharedPreference(this).edit().putString(Variables.uploading_video_thumb, Functions.Bitmap_to_base64(this, bitmap)).commit();


        } else if (bmThumbnail != null) {
            Bitmap bitmap = Bitmap.createScaledBitmap(bmThumbnail, (bmThumbnail.getWidth() / 6), (bmThumbnail.getHeight() / 6), false);

            bmThumbnail.recycle();

            video_thumbnail.setImageBitmap(bitmap);
            Functions.getSharedPreference(this).edit().putString(Variables.uploading_video_thumb, Functions.Bitmap_to_base64(this, bitmap)).commit();

        }


        privcy_type_txt = findViewById(R.id.privcy_type_txt);
        comment_switch = findViewById(R.id.comment_switch);
        duet_switch = findViewById(R.id.duet_switch);


        findViewById(R.id.Goback).setOnClickListener(this);

        findViewById(R.id.privacy_type_layout).setOnClickListener(this);
        findViewById(R.id.post_btn).setOnClickListener(this);
        findViewById(R.id.save_draft_btn).setOnClickListener(this);

        findViewById(R.id.hashtag_btn).setOnClickListener(this);
        findViewById(R.id.tag_user_btn).setOnClickListener(this);


        if (duet_video_id != null) {
            findViewById(R.id.duet_layout).setVisibility(View.GONE);
            findViewById(R.id.save_draft_btn).setVisibility(View.GONE);
            duet_switch.setChecked(false);
        } else if (Variables.is_enable_duet) {
            findViewById(R.id.duet_layout).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.duet_layout).setVisibility(View.GONE);
            duet_switch.setChecked(false);
        }
        aditional_details_text_count.setText("0" + "/" + Variables.Char_limit);

        description_edit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.Char_limit)});

        description_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int count) {

                if (description_edit.length() > counter) {
                    counter = description_edit.length();
                    if (description_edit.length() > 0) {
                        String lastChar = charSequence.toString().substring(charSequence.length() - 1);
                        if (lastChar.equals(" ")) {
                            findViewById(R.id.hashtag_layout).setVisibility(View.GONE);
                        } else if (lastChar.equals("#")) {
                            findViewById(R.id.hashtag_layout).setVisibility(View.VISIBLE);
                        } else if (lastChar.equals("@")) {
                            Open_Friends();
                        }

                        String hash_tags = description_edit.getText().toString();
                        String[] separated = hash_tags.split("#");
                        for (String item : separated) {
                            if (item != null && !item.equals("")) {
                                if (item.contains(" ")) {
                                    //stop calling api
                                } else {
                                    String string1 = item.replace("#", "");
                                    call_api_for_hash_tag(string1);
                                }
                            }
                        }

                    } else {
                        findViewById(R.id.hashtag_layout).setVisibility(View.GONE);
                    }
                } else {
                    if (description_edit.length() == 1) {
                        counter = -1;
                    } else {
                        counter--;
                    }
                }

                aditional_details_text_count.setText(description_edit.getText().length() + "/" + Variables.Char_limit);
            }


            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }

    public Bitmap combineImages(Bitmap c, Bitmap s) { // can add a 3rd parameter 'String loc' if you want to save the new image - left some code to do that at the bottom
        Bitmap cs = null;

        int width, height = 0;

        if (c.getWidth() > s.getWidth()) {
            width = c.getWidth() + s.getWidth();
            height = c.getHeight();
        } else {
            width = s.getWidth() + s.getWidth();
            height = c.getHeight();
        }

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s, c.getWidth(), 0f, null);

        return cs;
    }

    public Bitmap combineImagesVertical(Bitmap c, Bitmap s) { // can add a 3rd parameter 'String loc' if you want to save the new image - left some code to do that at the bottom
        Bitmap cs = null;

        int width, height = 0;

        if (c.getHeight() > s.getHeight()) {
            height = c.getHeight() / 2 + s.getHeight() / 2;
            width = c.getWidth();
        } else {
            height = s.getHeight() / 2 + s.getHeight() / 2;
            width = c.getWidth();
        }

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(cs);

        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s, c.getWidth(), 0f, null);

        return cs;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.Goback:
                onBackPressed();
                break;

            case R.id.privacy_type_layout:
                Privacy_dialog();
                break;

            case R.id.save_draft_btn:
                Save_file_in_draft();
                break;

            case R.id.post_btn:
                Make_mention_Arrays();
                Start_Service();
                break;

            case R.id.hashtag_btn:
                findViewById(R.id.hashtag_layout).setVisibility(View.VISIBLE);
                description_edit.setText(description_edit.getText().toString() + " #");
                description_edit.setSelection(description_edit.getText().length());
                call_api_for_hash_tag("");
                break;

            case R.id.tag_user_btn:
                Open_Friends();
                break;
        }
    }


    ArrayList<HashTagModel> hash_list = new ArrayList<>();
    RecyclerView recyclerView;

    void call_api_for_hash_tag(String lastChar) {
        JSONObject params = new JSONObject();
        try {
            params.put("type", "hashtag");
            params.put("keyword", lastChar.toString());
            params.put("starting_point", "0");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(this, ApiLinks.search, params, new Callback() {
            @Override
            public void Responce(String resp) {
                hash_list.clear();
                Functions.cancel_loader();
                try {
                    JSONObject response = new JSONObject(resp);
                    String code = response.optString("code");
                    if (code.equalsIgnoreCase("200")) {
                        JSONArray msgArray = response.optJSONArray("msg");
                        for (int i = 0; i < msgArray.length(); i++) {
                            JSONObject itemdata = msgArray.optJSONObject(i);
                            JSONObject Hashtag = itemdata.optJSONObject("Hashtag");

                            HashTagModel item = new HashTagModel();
                            item.id = Hashtag.optString("id");
                            item.name = Hashtag.optString("name");
                            item.videos_count = Hashtag.optString("videos_count");
                            hash_list.add(item);
                            setAdapter_for_hashtag();
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void setAdapter_for_hashtag() {
        HashTag_Adapter hashtag_adapter = new HashTag_Adapter(this, hash_list, new Adapter_Click_Listener() {
            @Override
            public void onItemClick(View view, int pos, Object object) {
                HashTagModel item = (HashTagModel) object;

                findViewById(R.id.hashtag_layout).setVisibility(View.GONE);
                StringBuilder sb = new StringBuilder();
                String desc = description_edit.getText().toString();
                String[] bits = desc.split(" ");
                String lastOne = bits[bits.length - 1];
                String newString = lastOne.replace(lastOne, item.name);
                for (int i = 0; i < bits.length - 1; i++) {
                    sb.append(bits[i] + " ");
                }
                sb.append("#" + newString + " ");
                description_edit.setText(sb);
                description_edit.setSelection(description_edit.getText().length());
            }
        });

        recyclerView = findViewById(R.id.hashtag_recylerview);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(hashtag_adapter);
    }


    public void Open_Friends() {

        Friends_F friends_f = new Friends_F(new Fragment_Callback() {
            @Override
            public void Responce(Bundle bundle) {

                if (bundle != null) {
                    Users_Model item = (Users_Model) bundle.getSerializable("data");
                    taged_user.add(item);

                    String lastChar = null;
                    if (!TextUtils.isEmpty(description_edit.getText().toString()))
                        lastChar = description_edit.getText().toString().substring(description_edit.getText().length() - 1);

                    if (lastChar != null && lastChar.contains("@"))
                        description_edit.setText(description_edit.getText().toString() + item.username + " ");
                    else
                        description_edit.setText(description_edit.getText().toString() + "@" + item.username + " ");

                    description_edit.setSelection(description_edit.getText().length());

                }

            }
        }, "fromPost");
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_bottom, R.anim.out_to_top, R.anim.in_from_top, R.anim.out_from_bottom);
        Bundle args = new Bundle();
        args.putString("id", Functions.getSharedPreference(this).getString(Variables.u_id, ""));
        friends_f.setArguments(args);
        transaction.addToBackStack(null);
        transaction.replace(R.id.post_fragment, friends_f).commit();

    }


    private void Privacy_dialog() {
        final CharSequence[] options = new CharSequence[]{"Public", "Private"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogCustom);

        builder.setTitle(null);

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int item) {
                privcy_type_txt.setText(options[item]);

            }

        });

        builder.show();

    }


    JSONArray hash_tag, friends_tag;

    public void Make_mention_Arrays() {
        hash_tag = new JSONArray();
        friends_tag = new JSONArray();

        String[] separated = description_edit.getText().toString().split(" ");
        for (String item : separated) {
            if (item != null && !item.equals("")) {
                if (item.contains("#")) {
                    String string1 = item.replace("#", "");
                    JSONObject tag_object = new JSONObject();
                    try {
                        tag_object.put("name", string1);
                        hash_tag.put(tag_object);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if (item.contains("@")) {
                    String string1 = item.replace("@", "");
                    JSONObject tag_object = new JSONObject();
                    try {
                        for (Users_Model user_model : taged_user) {
                            if (user_model.username.contains(string1)) {
                                tag_object.put("user_id", user_model.fb_id);
                                friends_tag.put(tag_object);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        Log.d(Variables.tag, hash_tag.toString());
        Log.d(Variables.tag, friends_tag.toString());


    }

    // this will start the service for uploading the video into database
    public void Start_Service() {

        serviceCallback = this;

        Upload_Service mService = new Upload_Service(serviceCallback);
        if (!Functions.isMyServiceRunning(this, mService.getClass())) {
            Intent mServiceIntent = new Intent(this.getApplicationContext(), mService.getClass());
            mServiceIntent.setAction("startservice");
            mServiceIntent.putExtra("draft_file", draft_file);
            mServiceIntent.putExtra("duet_video_id", duet_video_id);
            mServiceIntent.putExtra("uri", "" + video_path);
            mServiceIntent.putExtra("desc", "" + description_edit.getText().toString());
            mServiceIntent.putExtra("privacy_type", privcy_type_txt.getText().toString());
            mServiceIntent.putExtra("hashtags_json", hash_tag.toString());
            mServiceIntent.putExtra("mention_users_json", friends_tag.toString());
            mServiceIntent.putExtra("duet_orientation", duet_orientation);

            if (comment_switch.isChecked())
                mServiceIntent.putExtra("allow_comment", "true");
            else
                mServiceIntent.putExtra("allow_comment", "false");


            if (duet_switch.isChecked())
                mServiceIntent.putExtra("allow_duet", "1");
            else
                mServiceIntent.putExtra("allow_duet", "0");


            startService(mServiceIntent);


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendBroadcast(new Intent("uploadVideo"));
                    startActivity(new Intent(Post_Video_A.this, MainMenuActivity.class));
                }
            }, 1000);


        } else {
            Toast.makeText(Post_Video_A.this, "Please wait video already in uploading progress", Toast.LENGTH_SHORT).show();
        }


    }


    @Override
    public void onBackPressed() {

        int count = this.getSupportFragmentManager().getBackStackEntryCount();
        if (count > 0) {
            super.onBackPressed();
        } else {
            super.onBackPressed();
            finish();
            overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
        }


    }


    // when the video is uploading successfully it will restart the appliaction
    @Override
    public void ShowResponce(final String responce) {

        if (mConnection != null)
            unbindService(mConnection);

        Functions.show_toast(this, responce);
    }


    // this is importance for binding the service to the activity
    Upload_Service mService;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            Upload_Service.LocalBinder binder = (Upload_Service.LocalBinder) service;
            mService = binder.getService();

            mService.setCallbacks(Post_Video_A.this);


        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }
    };


    @Override
    protected void onDestroy() {
        if (bmThumbnail != null) {
            bmThumbnail.recycle();
        }
        super.onDestroy();
    }


    public void Save_file_in_draft() {
        File source = new File(video_path);
        File destination = new File(Variables.draft_app_folder + Functions.getRandomString() + ".mp4");
        try {
            if (source.exists()) {

                InputStream in = new FileInputStream(source);
                OutputStream out = new FileOutputStream(destination);

                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.close();

                Functions.show_toast(this, "File saved in Draft");
                startActivity(new Intent(Post_Video_A.this, MainMenuActivity.class));

            } else {
                Functions.show_toast(this, "File saved in Draft");

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
