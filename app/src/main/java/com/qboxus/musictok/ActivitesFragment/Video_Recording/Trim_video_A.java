package com.qboxus.musictok.ActivitesFragment.Video_Recording;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import java.io.File;

import life.knowledge4.videotrimmer.K4LVideoTrimmer;
import life.knowledge4.videotrimmer.interfaces.OnTrimVideoListener;

public class Trim_video_A extends AppCompatActivity implements OnTrimVideoListener {

    private K4LVideoTrimmer mVideoTrimmer;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trim_video);

        Intent extraIntent = getIntent();
        String path = "";

        if (extraIntent != null) {
            path = extraIntent.getStringExtra("path");
        }

        //setting progressbar
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Please wait...");

        mVideoTrimmer = ((K4LVideoTrimmer) findViewById(R.id.timeLine));
        if (mVideoTrimmer != null) {
            mVideoTrimmer.setMaxDuration((Variables.max_recording_duration/1000));
            mVideoTrimmer.setMaxDuration((Variables.max_recording_duration/1000));
            mVideoTrimmer.setOnTrimVideoListener(this);
            mVideoTrimmer.setDestinationPath(Variables.app_showing_folder);
            mVideoTrimmer.setVideoURI(Uri.parse(path));
        }
        Log.d(Variables.tag+" orignal path",path);

    }


    @Override
    public void onTrimStarted() {
        mProgressDialog.show();
    }

    @Override
    public void getResult(final Uri uri) {
        mProgressDialog.dismiss();
        try {
            File video_file = new File(uri.getPath());
            Log.d(Variables.tag+" new path",video_file.getAbsolutePath());

            Functions.copyFile(video_file,
                    new File(Variables.gallery_resize_video));

            if(video_file.exists()){
                video_file.delete();
            }

            Intent intent=new Intent(Trim_video_A.this, GallerySelectedVideo_A.class);
            intent.putExtra("video_path",Variables.gallery_resize_video);
            startActivity(intent);

            mVideoTrimmer.destroy();
            finish();

        }catch (Exception e) {
            e.printStackTrace();
            Log.d(Variables.tag,e.toString());
        }


    }


    @Override
    public void cancelAction() {
        mProgressDialog.cancel();
        mVideoTrimmer.destroy();
        finish();
    }

    @Override
    public void onError(String message) {
        mProgressDialog.dismiss();
    }

}
