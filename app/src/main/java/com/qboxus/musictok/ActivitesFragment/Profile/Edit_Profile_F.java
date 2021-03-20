package com.qboxus.musictok.ActivitesFragment.Profile;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qboxus.musictok.Interfaces.KeyboardHeightObserver;
import com.qboxus.musictok.Main_Menu.RelateToFragment_OnBack.RootFragment;
import com.qboxus.musictok.R;
import com.qboxus.musictok.Interfaces.API_CallBack;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.Interfaces.Fragment_Callback;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.KeyboardHeightProvider;
import com.qboxus.musictok.SimpleClasses.Variables;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class Edit_Profile_F extends RootFragment implements View.OnClickListener {

    View view;
    Context context;

    public Edit_Profile_F() {

    }

    Fragment_Callback fragment_callback;

    public Edit_Profile_F(Fragment_Callback fragment_callback) {
        this.fragment_callback = fragment_callback;
    }

    TextView aditional_details_text_count;

    ImageView profile_image;
    EditText username_edit, firstname_edit, lastname_edit, website_edit, user_bio_edit;

    RadioButton male_btn, female_btn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        context = getContext();


        view.findViewById(R.id.Goback).setOnClickListener(this);
        view.findViewById(R.id.save_btn).setOnClickListener(this);
        view.findViewById(R.id.upload_pic_btn).setOnClickListener(this);


        username_edit = view.findViewById(R.id.username_edit);
        profile_image = view.findViewById(R.id.profile_image);
        firstname_edit = view.findViewById(R.id.firstname_edit);
        lastname_edit = view.findViewById(R.id.lastname_edit);
        website_edit = view.findViewById(R.id.website_edit);
        user_bio_edit = view.findViewById(R.id.user_bio_edit);
        aditional_details_text_count = view.findViewById(R.id.aditional_details_text_count);

        aditional_details_text_count.setText("Maximum Characters" + "/" + Variables.Char_limit);

        username_edit.setText(Functions.getSharedPreference(context).getString(Variables.u_name, ""));
        firstname_edit.setText(Functions.getSharedPreference(context).getString(Variables.f_name, ""));
        lastname_edit.setText(Functions.getSharedPreference(context).getString(Variables.l_name, ""));

        String pic = Functions.getSharedPreference(context).getString(Variables.u_pic, "");
        if (pic != null && !pic.equalsIgnoreCase(""))
            Picasso.get()
                    .load(Functions.getSharedPreference(context).getString(Variables.u_pic, ""))
                    .placeholder(R.drawable.profile_image_placeholder)
                    .resize(200, 200)
                    .centerCrop()
                    .into(profile_image);


        male_btn = view.findViewById(R.id.male_btn);
        female_btn = view.findViewById(R.id.female_btn);

        user_bio_edit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Variables.Char_limit)});

        user_bio_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                aditional_details_text_count.setText(user_bio_edit.getText().length() + "/" + Variables.Char_limit);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        Call_Api_For_User_Details();

        setKeyboardListener();
        return view;
    }





    int privious_height = 0;
    public void setKeyboardListener() {

        KeyboardHeightProvider keyboardHeightProvider = new KeyboardHeightProvider(getActivity());
        keyboardHeightProvider.setKeyboardHeightObserver(new KeyboardHeightObserver() {
            @Override
            public void onKeyboardHeightChanged(int height, int orientation) {
                Log.d(Variables.tag, "" + height);
                if (height < 0) {
                    privious_height = Math.abs(height);
                }

                LinearLayout main_layout= view.findViewById(R.id.main_layout);

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(main_layout.getWidth(), main_layout.getHeight());
                params.bottomMargin = height + privious_height;
                main_layout.setLayoutParams(params);
            }
        });


        view.findViewById(R.id.Edit_Profile_F).post(new Runnable() {
            public void run() {
                keyboardHeightProvider.start();
            }
        });


    }




    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.Goback:

                getActivity().onBackPressed();
                break;

            case R.id.save_btn:
                if (Check_Validation()) {
                    Call_Api_For_Edit_profile();
                }
                break;

            case R.id.upload_pic_btn:
                selectImage();
                break;
        }
    }


    // this method will show the dialog of selete the either take a picture form camera or pick the image from gallary
    private void selectImage() {

        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};


        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);

        builder.setTitle("Add Photo!");

        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (options[item].equals("Take Photo")) {
                    if (Functions.check_permissions(getActivity()))
                        openCameraIntent();

                } else if (options[item].equals("Choose from Gallery")) {

                    if (Functions.check_permissions(getActivity())) {
                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, 2);
                    }
                } else if (options[item].equals("Cancel")) {

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
        if (pictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(context.getApplicationContext(), getActivity().getPackageName() + ".fileprovider", photoFile);
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
                imageFileName,
                ".jpg",
                storageDir
        );

        imageFilePath = image.getAbsolutePath();
        return image;
    }


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
                Uri selectedImage = (Uri.fromFile(new File(imageFilePath)));

                beginCrop(selectedImage);


            } else if (requestCode == 2) {
                Uri selectedImage = data.getData();
                beginCrop(selectedImage);

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                handleCrop(result.getUri());
            }

        }

    }


    // this will check the validations like none of the field can be the empty
    public boolean Check_Validation() {

        String uname = username_edit.getText().toString();
        String firstname = firstname_edit.getText().toString();
        String lastname = lastname_edit.getText().toString();

        if (TextUtils.isEmpty(uname)) {
            username_edit.setError("Please enter correct username");
            return false;
        } else if (uname.length() < 4 || uname.length() > 14) {
            username_edit.setError("Username Length should be between 4 and 14");
            return false;
        } else if (TextUtils.isEmpty(firstname)) {
            firstname_edit.setError("Please enter first name");
            return false;
        } else if (TextUtils.isEmpty(lastname)) {
            lastname_edit.setError("Please enter last name");
            return false;
        } else if (!male_btn.isChecked() && !female_btn.isChecked()) {
            Toast.makeText(context, "Please select your gender", Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(website_edit.getText().toString())) {
            Toast.makeText(context, "Please enter your web link here", Toast.LENGTH_SHORT).show();
            return false;
        }


        return true;
    }


    String image_bas64;

    private void beginCrop(Uri source) {

        CropImage.activity(source).start(getActivity());


    }

    private void handleCrop(Uri userimageuri) {

        InputStream imageStream = null;
        try {
            imageStream = getActivity().getContentResolver().openInputStream(userimageuri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        final Bitmap imagebitmap = BitmapFactory.decodeStream(imageStream);

        String path = userimageuri.getPath();
        Matrix matrix = new Matrix();
        android.media.ExifInterface exif = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            try {
                exif = new android.media.ExifInterface(path);
                int orientation = exif.getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, 1);
                switch (orientation) {
                    case android.media.ExifInterface.ORIENTATION_ROTATE_90:
                        matrix.postRotate(90);
                        break;
                    case android.media.ExifInterface.ORIENTATION_ROTATE_180:
                        matrix.postRotate(180);
                        break;
                    case android.media.ExifInterface.ORIENTATION_ROTATE_270:
                        matrix.postRotate(270);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Bitmap rotatedBitmap = Bitmap.createBitmap(imagebitmap, 0, 0, imagebitmap.getWidth(), imagebitmap.getHeight(), matrix, true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

        image_bas64 = Functions.Bitmap_to_base64(getActivity(), rotatedBitmap);

        Call_Api_For_image();
    }


    public void Call_Api_For_image() {

        JSONObject parameters = new JSONObject();
        try {
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.u_id, "0"));

            JSONObject file_data = new JSONObject();
            file_data.put("file_data", image_bas64);
            parameters.put("profile_pic", file_data);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        Functions.Show_loader(context, false, false);
        ApiRequest.Call_Api(getActivity(), ApiLinks.addUserImage, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                Functions.cancel_loader();
                try {
                    JSONObject response = new JSONObject(resp);
                    String code = response.optString("code");
                    JSONObject msg = response.optJSONObject("msg");
                    if (code.equals("200")) {

                        JSONObject User = msg.optJSONObject("User");


                        Functions.getSharedPreference(context).edit().putString(Variables.u_pic, ApiLinks.BASE_URL + User.optString("profile_pic")).commit();
                        Variables.user_pic = ApiLinks.BASE_URL + User.optString("profile_pic");

                        if (Variables.user_pic != null && !Variables.user_pic.equals(""))
                            Picasso.get()
                                    .load(Variables.user_pic)
                                    .placeholder(context.getResources().getDrawable(R.drawable.profile_image_placeholder))
                                    .resize(200, 200).centerCrop().into(profile_image);


                        Functions.show_toast(view.getContext(), "Image Update Successfully");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });


    }


    // this will update the latest info of user in database
    public void Call_Api_For_Edit_profile() {

        Functions.Show_loader(context, false, false);

        String uname = username_edit.getText().toString().toLowerCase().replaceAll("\\s", "");
        JSONObject parameters = new JSONObject();
        try {
            parameters.put("username", uname.replaceAll("@", ""));
            parameters.put("user_id", Functions.getSharedPreference(context).getString(Variables.u_id, "0"));
            parameters.put("first_name", firstname_edit.getText().toString());
            parameters.put("last_name", lastname_edit.getText().toString());

            if (male_btn.isChecked()) {
                parameters.put("gender", "Male");

            } else if (female_btn.isChecked()) {
                parameters.put("gender", "Female");
            }

            parameters.put("website", website_edit.getText().toString());
            parameters.put("bio", user_bio_edit.getText().toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        ApiRequest.Call_Api(getActivity(), ApiLinks.edit_profile, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                Functions.cancel_loader();
                try {
                    JSONObject response = new JSONObject(resp);
                    String code = response.optString("code");
                    JSONArray msg = response.optJSONArray("msg");
                    if (code.equals("200")) {

                        SharedPreferences.Editor editor = Functions.getSharedPreference(context).edit();

                        String u_name = username_edit.getText().toString();
                        if (!u_name.contains("@"))
                            u_name = "@" + u_name;

                        editor.putString(Variables.u_name, u_name);
                        editor.putString(Variables.f_name, firstname_edit.getText().toString());
                        editor.putString(Variables.l_name, lastname_edit.getText().toString());
                        editor.commit();

                        Variables.user_name = u_name;

                        getActivity().onBackPressed();
                    } else {
                        if (msg != null) {
                            JSONObject jsonObject = msg.optJSONObject(0);
                            Functions.show_toast(view.getContext(), jsonObject.optString("response"));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    // this will get the user data and parse the data and show the data into views
    public void Call_Api_For_User_Details() {
        Functions.Show_loader(getActivity(), false, false);
        Functions.Call_Api_For_Get_User_data(getActivity(),
                Variables.sharedPreferences.getString(Variables.u_id, ""),
                new API_CallBack() {
                    @Override
                    public void ArrayData(ArrayList arrayList) {

                    }

                    @Override
                    public void OnSuccess(String responce) {
                        Functions.cancel_loader();
                        Parse_user_data(responce);
                    }

                    @Override
                    public void OnFail(String responce) {

                    }
                });
    }

    public void Parse_user_data(String responce) {
        try {
            JSONObject jsonObject = new JSONObject(responce);

            String code = jsonObject.optString("code");

            if (code.equals("200")) {
                JSONObject msg = jsonObject.optJSONObject("msg");

                JSONObject User = msg.optJSONObject("User");

                firstname_edit.setText(User.optString("first_name"));
                lastname_edit.setText(User.optString("last_name"));

                String picture = User.optString("profile_pic");

                if (!picture.contains(Variables.http)) {
                    picture = ApiLinks.BASE_URL + picture;
                }

                if (picture != null && !picture.equalsIgnoreCase("")) {
                    Picasso.get()
                            .load(picture)
                            .placeholder(R.drawable.profile_image_placeholder)
                            .into(profile_image);
                }

                String gender = User.optString("gender");
                if (gender != null && gender.equalsIgnoreCase("male")) {
                    male_btn.setChecked(true);
                } else if (gender != null && gender.equalsIgnoreCase("female")) {
                    female_btn.setChecked(true);
                }


                website_edit.setText(User.optString("website"));
                user_bio_edit.setText(User.optString("bio"));

            } else {
                Functions.show_toast(getActivity(), jsonObject.optString("msg"));

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();

        if (fragment_callback != null)
            fragment_callback.Responce(new Bundle());
    }

}
