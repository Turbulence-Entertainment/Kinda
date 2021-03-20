package com.qboxus.musictok.ActivitesFragment.Accounts;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import com.qboxus.musictok.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.qboxus.musictok.R;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.SimpleClasses.FileUtils;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class Request_Varification_F extends RootFragment implements View.OnClickListener {

    View view;
    Context context;

    EditText username_edit,fullname_edit;
    TextView file_name_txt;
    String base_64;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.activity_request_varification, container, false);
        context=getContext();

        view.findViewById(R.id.Goback).setOnClickListener(this);

        username_edit=view.findViewById(R.id.username_edit);
        fullname_edit=view.findViewById(R.id.fullname_edit);

        username_edit.setText(Functions.getSharedPreference(context).getString(Variables.u_name,""));
        fullname_edit.setText(Functions.getSharedPreference(context).getString(Variables.f_name,"")+" "+ Variables.sharedPreferences.getString(Variables.l_name,""));

        file_name_txt=view.findViewById(R.id.file_name_txt);

        view.findViewById(R.id.choose_file_btn).setOnClickListener(this);
        view.findViewById(R.id.send_btn).setOnClickListener(this);


        return view;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.Goback:
                getActivity().onBackPressed();
                break;

            case R.id.choose_file_btn:
                selectImage();
                break;

            case R.id.send_btn:
                if(Check_Validation()){
                    Call_api();
                }
                break;
        }

    }

    // this method will show the dialog of selete the either take a picture form camera or pick the image from gallary
    private void selectImage() {

        final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };



        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);

        builder.setTitle("Add Photo!");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override

            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo"))

                {
                    if(Functions.check_permissions(getActivity()))
                        openCameraIntent();

                }

                else if (options[item].equals("Choose from Gallery"))

                {

                    if(Functions.check_permissions(getActivity())) {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, 2);
                    }
                }

                else if (options[item].equals("Cancel")) {

                    dialog.dismiss();

                }

            }

        });

        builder.show();

    }





    // below three method is related with taking the picture from camera
    private void openCameraIntent() {
        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        if(pictureIntent.resolveActivity(getActivity().getPackageManager()) != null){
            //Create a file to store the image
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context.getApplicationContext(), getActivity().getPackageName()+".fileprovider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(pictureIntent, 1);
            }
        }
    }

    String imageFilePath;
    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        imageFilePath = image.getAbsolutePath();
        return image;
    }

    public  String getPath(Uri uri ) {
        String result = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
        if(cursor != null){
            if ( cursor.moveToFirst( ) ) {
                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                result = cursor.getString( column_index );
            }
            cursor.close( );
        }
        if(result == null) {
            result = "Not found";
        }
        return result;
    }



    File image_file;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            if (requestCode == 1) {
                Matrix matrix = new Matrix();
                try {
                    ExifInterface exif = new ExifInterface(imageFilePath);
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            matrix.postRotate(90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            matrix.postRotate(180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            matrix.postRotate(270);
                            break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                image_file=new File(imageFilePath);
                Uri selectedImage =(Uri.fromFile(image_file));

                InputStream imageStream = null;
                try {
                    imageStream =getActivity().getContentResolver().openInputStream(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                final Bitmap imagebitmap = BitmapFactory.decodeStream(imageStream);
                Bitmap rotatedBitmap = Bitmap.createBitmap(imagebitmap, 0, 0, imagebitmap.getWidth(), imagebitmap.getHeight(), matrix, true);

                Bitmap  resized = Bitmap.createScaledBitmap(rotatedBitmap,(int)(rotatedBitmap.getWidth()*0.7), (int)(rotatedBitmap.getHeight()*0.7), true);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.JPEG, 20, baos);

                base_64= Functions.Bitmap_to_base64(getActivity(),resized);

                if(image_file!=null)
                file_name_txt.setText(image_file.getName());
            }

            else if (requestCode == 2) {
                Uri selectedImage = data.getData();
                try {
                    image_file= FileUtils.getFileFromUri(context,selectedImage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                InputStream imageStream = null;
                try {
                    imageStream =getActivity().getContentResolver().openInputStream(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                final Bitmap imagebitmap = BitmapFactory.decodeStream(imageStream);

                String path=getPath(selectedImage);
                Matrix matrix = new Matrix();
                ExifInterface exif = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    try {
                        exif = new ExifInterface(path);
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                        switch (orientation) {
                            case ExifInterface.ORIENTATION_ROTATE_90:
                                matrix.postRotate(90);
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_180:
                                matrix.postRotate(180);
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_270:
                                matrix.postRotate(270);
                                break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Bitmap rotatedBitmap = Bitmap.createBitmap(imagebitmap, 0, 0, imagebitmap.getWidth(), imagebitmap.getHeight(), matrix, true);


                Bitmap  resized = Bitmap.createScaledBitmap(rotatedBitmap,(int)(rotatedBitmap.getWidth()*0.5), (int)(rotatedBitmap.getHeight()*0.5), true);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.JPEG, 20, baos);

                base_64= Functions.Bitmap_to_base64(getActivity(),resized);

                if(image_file!=null)
                    file_name_txt.setText(image_file.getName());


            }

        }

    }



    // this will check the validations like none of the field can be the empty
    public boolean Check_Validation(){

        String uname=username_edit.getText().toString();
        String fullname=fullname_edit.getText().toString();

        if(TextUtils.isEmpty(uname)|| uname.length()<2){
            Functions.show_toast(context, "Please enter correct  username");
            return false;
        }

       else if(TextUtils.isEmpty(fullname)){
            Functions.show_toast(context, "Please enter full  name");
            return false;
        }
       else if(base_64==null) {
            Functions.show_toast(context, "Please select the  image");
            return false;
        }

        return true;
    }



    public void Call_api(){
        JSONObject params=new JSONObject();
        try {
            params.put("user_id", Variables.user_id);
            JSONObject file_data=new JSONObject();
            file_data.put("file_data",base_64);
            params.put("attachment",file_data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Functions.Show_loader(context,false,false);
        ApiRequest.Call_Api(getActivity(), ApiLinks.userVerificationRequest, params, new Callback() {
            @Override
            public void Responce(String resp) {
                Functions.cancel_loader();
                try {
                    JSONObject jsonObject=new JSONObject(resp);
                    String code=jsonObject.optString("code");
                    if(code.equalsIgnoreCase("200")){
                        Functions.show_toast(context, "Request Sent Successfully");
                        getActivity().onBackPressed();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }


}
