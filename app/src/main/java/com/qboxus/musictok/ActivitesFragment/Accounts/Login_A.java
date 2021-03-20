package com.qboxus.musictok.ActivitesFragment.Accounts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.qboxus.musictok.Models.User_Model;
import com.qboxus.musictok.Main_Menu.MainMenuActivity;
import com.qboxus.musictok.R;
import com.qboxus.musictok.ApiClasses.ApiLinks;
import com.qboxus.musictok.ApiClasses.ApiRequest;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.SimpleClasses.Functions;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Login_A extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{


    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;

    static GoogleApiClient googleApiClient;
    SharedPreferences sharedPreferences;
    User_Model user_model = new User_Model();
    View top_view;
    long mBackPressed;
    TextView login_title_txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.CREDENTIALS_API)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this)
                .build();
        googleApiClient.connect();

        if (Build.VERSION.SDK_INT == 26) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
        }

        getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        this.getWindow()
                .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_login);


        mAuth = FirebaseAuth.getInstance();
        firebaseUser=mAuth.getCurrentUser();

        // if the user is already login trought facebook then we will logout the user automatically
        LoginManager.getInstance().logOut();

        sharedPreferences=getSharedPreferences(Variables.pref_name,MODE_PRIVATE);

        findViewById(R.id.facebook_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Loginwith_FB();
            }
        });

        findViewById(R.id.email_login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Email_Phone_F emailPhoneF = new Email_Phone_F("login");
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
                Bundle bundle = new Bundle();
                bundle.putSerializable("user_model", user_model);
                emailPhoneF.setArguments(bundle);
                transaction.addToBackStack(null);
                transaction.replace(R.id.login_f, emailPhoneF).commit();
            }
        });

        findViewById(R.id.signUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_dob_fragment("signup");
            }
        });



        findViewById(R.id.google_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Sign_in_with_gmail();
            }
        });



        findViewById(R.id.Goback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

         top_view=findViewById(R.id.top_view);



        login_title_txt=findViewById(R.id.login_title_txt);
        login_title_txt.setText("You need a "+getString(R.string.app_name)+"\naccount to Continue");



        SpannableString ss = new SpannableString("By signing up, you confirm that you agree to our \n Terms of Use and have read and understood \n our Privacy Policy.");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Open_Privacy_policy();
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan, 99, ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView textView = (TextView) findViewById(R.id.login_terms_condition_txt);
        textView.setText(ss);
        textView.setClickable(true);
        textView.setMovementMethod(LinkMovementMethod.getInstance());


        printKeyHash();


    }


    public void Open_Privacy_policy(){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ApiLinks.privacy_policy));
        startActivity(browserIntent);
    }


    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(200);
        top_view.startAnimation(anim);
        top_view.setVisibility(View.VISIBLE);

    }


    @Override
    public void onBackPressed() {
            int count = this.getSupportFragmentManager().getBackStackEntryCount();
            if (count == 0) {
                if (mBackPressed + 2000 > System.currentTimeMillis()) {
                    super.onBackPressed();
                    return;
                } else {
                    top_view.setVisibility(View.GONE);
                    finish();
                    overridePendingTransition(R.anim.in_from_top, R.anim.out_from_bottom);
                }
            } else {
                super.onBackPressed();
            }
    }


    // Bottom two function are related to Fb implimentation
    private CallbackManager mCallbackManager;


    //facebook implimentation
    public void Loginwith_FB(){

        LoginManager.getInstance()
                .logInWithReadPermissions(Login_A.this,
                        Arrays.asList("public_profile","email"));

        // initialze the facebook sdk and request to facebook for login
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>()  {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
                Log.d("resp_token",loginResult.getAccessToken()+"");
            }

            @Override
            public void onCancel() {
                // App code
                Functions.show_toast(Login_A.this ,"Login Cancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d("resp",""+error.toString());
                Functions.show_toast(Login_A.this ,"Login Error"+error.toString());
            }

        });


    }

    private void handleFacebookAccessToken(final AccessToken token) {
        // if user is login then this method will call and
        // facebook will return us a token which will user for get the info of user
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        Log.d("resp_token",token.getToken()+"");
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Functions.Show_loader(Login_A.this,false,false);
                             final String id = Profile.getCurrentProfile().getId();
                            GraphRequest request = GraphRequest.newMeRequest(token, new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject user, GraphResponse graphResponse) {

                                    Functions.cancel_loader();
                                    Log.d("resp",user.toString());
                                    //after get the info of user we will pass to function which will store the info in our server

                                    String fname=""+user.optString("first_name");
                                    String lname=""+user.optString("last_name");
                                    String email= ""+user.optString("email");
                                    String auth_token = token.getToken();


                                    user_model = new User_Model();

                                    user_model.fname = fname;
                                    user_model.email = email;
                                    user_model.lname = lname;
                                    user_model.socail_id = id;
                                    user_model.socail_type = "facebook";
                                    user_model.auth_tokon = auth_token;




                                    call_api_for_login(""+id,
                                            "facebook",
                                            auth_token);

                                }
                            });

                            // here is the request to facebook sdk for which type of info we have required
                            Bundle parameters = new Bundle();
                            parameters.putString("fields", "last_name,first_name,email");
                            request.setParameters(parameters);
                            request.executeAsync();
                        } else {
                            Functions.cancel_loader();
                            Functions.show_toast(Login_A.this, "Authentication failed.");
                        }

                    }
                });
    }

    private void call_api_for_login(String social_id, String social, final String authtoken) {

        JSONObject parameters = new JSONObject();
        try {

            parameters.put("social_id", social_id);
            parameters.put("social",""+social);
            parameters.put("auth_token", ""+authtoken);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Functions.Show_loader(this,false,false);
        ApiRequest.Call_Api(this, ApiLinks.registerUser, parameters, new Callback() {
            @Override
            public void Responce(String resp) {
                Functions.cancel_loader();
                parse_login_data(resp,authtoken);

            }
        });


    }

    public void parse_login_data(String loginData,String authtoken){
        try {
            JSONObject jsonObject=new JSONObject(loginData);
            String code=jsonObject.optString("code");
            if(code.equals("200")){

                JSONObject jsonArray=jsonObject.getJSONObject("msg");
                JSONObject userdata = jsonArray.getJSONObject("User");
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putString(Variables.u_id,userdata.optString("id"));
                editor.putString(Variables.f_name,userdata.optString("first_name"));
                editor.putString(Variables.l_name,userdata.optString("last_name"));
                editor.putString(Variables.u_name,userdata.optString("username"));
                editor.putString(Variables.gender,userdata.optString("gender"));
                editor.putString(Variables.u_pic,userdata.optString("profile_pic"));
                editor.putString(Variables.auth_token,authtoken);
                editor.putBoolean(Variables.islogin,true);
                editor.commit();
               Variables.user_id=Functions.getSharedPreference(this).getString(Variables.u_id,"");

                sendBroadcast(new Intent("newVideo"));


                Variables.Reload_my_videos=true;
                Variables.Reload_my_videos_inner=true;
                Variables.Reload_my_likes_inner=true;
                Variables.Reload_my_notification=true;
                top_view.setVisibility(View.GONE);


                finish();
                startActivity(new Intent(this, MainMenuActivity.class));

            }
            else if(code.equals("201") && !jsonObject.optString("msg").contains("have been blocked")){
                open_dob_fragment("social");
            }
           else{
                Toast.makeText(this, jsonObject.optString("msg"), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void open_dob_fragment(String fromWhere) {
        DOB_F DOBF = new DOB_F();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left, R.anim.in_from_left, R.anim.out_to_right);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user_model", user_model);
        bundle.putString("fromWhere",fromWhere);
        DOBF.setArguments(bundle);
        transaction.addToBackStack(null);
        transaction.replace(R.id.login_f, DOBF).commit();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result back to the Facebook SDK
        if(requestCode==123){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        else if(mCallbackManager!=null)
            mCallbackManager.onActivityResult(requestCode, resultCode, data);

        else
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if(fragment instanceof Email_Phone_F) {
                    fragment.onActivityResult(requestCode, resultCode, data);
                }
            }
    }



    //google Implimentation
    GoogleSignInClient mGoogleSignInClient;
    public void Sign_in_with_gmail(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(Login_A.this);

        if (account != null) {

            String id=account.getId();
            String fname=""+account.getGivenName();
            String lname=""+account.getFamilyName();
            String email = account.getEmail();
            String auth_tokon=account.getIdToken();

            user_model = new User_Model();
            user_model.fname = fname;
            user_model.email = email;
            user_model.lname = lname;
            user_model.socail_id = id;
            user_model.auth_tokon =auth_tokon;


            user_model.socail_type = "google";



            String auth_token = ""+account.getIdToken();


            Log.d(Variables.tag, "signInResult:auth_token===" + auth_token);
           call_api_for_login(""+id,
                    "google",
                    auth_token);

        }
        else {

            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, 123);

        }

    }


    //Relate to google login
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String id=account.getId();
                String fname=""+account.getGivenName();
                String lname=""+account.getFamilyName();
                String auth_token =  account.getIdToken();
                String email = account.getEmail();

                Log.d(Variables.tag, "signInResult:auth_token =" + auth_token);
                // if we do not get the picture of user then we will use default profile picture


                user_model = new User_Model();

                user_model.fname = fname;
                user_model.email = email;
                user_model.lname = lname;
                user_model.socail_id = id;
                user_model.socail_type = "google";
                user_model.auth_tokon = account.getIdToken();


                call_api_for_login(""+id,
                        "google",
                        auth_token);


            }
        } catch (ApiException e) {
            Log.d("TicTic_log", "signInResult:failed code=" + e.getStatusCode());
        }
    }


    public void printKeyHash()  {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName() , PackageManager.GET_SIGNATURES);
            for(Signature signature:info.signatures)
            {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.i("keyhash" , Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Functions.show_toast(Login_A.this, "error");
    }


}
