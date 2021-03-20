package com.qboxus.musictok.ApiClasses;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.qboxus.musictok.Interfaces.Callback;
import com.qboxus.musictok.Main_Menu.MainMenuActivity;
import com.qboxus.musictok.R;
import com.qboxus.musictok.SimpleClasses.Variables;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.paperdb.Paper;

public class ApiRequest {


    public static void Call_Api (final Activity context, final String url, JSONObject jsonObject,
                                 final Callback callback){


        if(!Variables.is_secure_info) {
            final String[] urlsplit = url.split("/");
            Log.d(Variables.tag, url);

            if (jsonObject != null)
                Log.d(Variables.tag + urlsplit[urlsplit.length - 1], jsonObject.toString());
        }

         JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                url, jsonObject,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {


                        if(!Variables.is_secure_info) {
                            final String[] urlsplit = url.split("/");
                            Log.d(Variables.tag + urlsplit[urlsplit.length - 1], response.toString());
                        }

                        if(callback!=null) {
                            callback.Responce(response.toString());
                        }

                        if(response.optString("code","").equalsIgnoreCase("501")){
                            Logout(context);
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if(!Variables.is_secure_info) {
                    final String[] urlsplit = url.split("/");
                    Log.d(Variables.tag + urlsplit[urlsplit.length - 1], error.toString());
                }

                if(callback!=null)
                  callback .Responce(error.toString());

            }
        }) {
             @Override
             public String getBodyContentType() {
                 return "application/json; charset=utf-8";
             }

             @Override
             public Map<String, String> getHeaders() throws AuthFailureError {
                 HashMap<String, String> headers = new HashMap<String, String>();
                 headers.put("Api-Key",ApiLinks.API_KEY);
                 headers.put("User-Id",Variables.sharedPreferences.getString(Variables.u_id,null));
                 headers.put("Auth-Token", Variables.sharedPreferences.getString(Variables.auth_token,null));
                 Log.d(Variables.tag,headers.toString());
                 return headers;

             }
         };
        try {

            if(context!=null) {
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(60000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                requestQueue.getCache().clear();
                requestQueue.add(jsonObjReq);
        }


        }catch (Exception e){
            Log.d(Variables.tag,e.toString());
        }

    }


    public static void Call_Api_GetRequest (final Activity context, final String url,
                                 final Callback callback){


        if(!Variables.is_secure_info) {
            final String[] urlsplit = url.split("/");
            Log.d(Variables.tag, url);

        }

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        if(!Variables.is_secure_info) {
                            final String[] urlsplit = url.split("/");
                            Log.d(Variables.tag + urlsplit[urlsplit.length - 1], response.toString());
                        }

                        if(callback!=null) {
                            callback.Responce(response.toString());
                        }

                        if(response.optString("code","").equalsIgnoreCase("501")){
                            Logout(context);
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if(!Variables.is_secure_info) {
                    final String[] urlsplit = url.split("/");
                    Log.d(Variables.tag + urlsplit[urlsplit.length - 1], error.toString());
                }

                if(callback!=null)
                    callback .Responce(error.toString());

            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Api-Key",ApiLinks.API_KEY);
                headers.put("User-Id",Variables.sharedPreferences.getString(Variables.u_id,null));
                headers.put("Auth-Token", Variables.sharedPreferences.getString(Variables.auth_token,null));
                Log.d(Variables.tag,headers.toString());
                return headers;

            }
        };
        try {

        if(context!=null) {
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(60000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                requestQueue.getCache().clear();
                requestQueue.add(jsonObjReq);
        }
        }catch (Exception e){
            Log.d(Variables.tag,e.toString());
        }
    }


    public static void Logout(Activity activity){

        GoogleSignInOptions gso = new GoogleSignInOptions.
                Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                build();
        GoogleSignInClient googleSignInClient= GoogleSignIn.getClient(activity,gso);
        googleSignInClient.signOut();

        LoginManager.getInstance().logOut();

        SharedPreferences.Editor editor = Variables.sharedPreferences.edit();
        Paper.book("Setting").destroy();
        editor.putString(Variables.u_id, "");
        editor.putString(Variables.u_name, "");
        editor.putString(Variables.u_pic, "");
        editor.putString(Variables.auth_token,null);
        editor.putBoolean(Variables.islogin, false);
        editor.commit();
        activity.finish();
        activity.startActivity(new Intent(activity, MainMenuActivity.class));

    }


}
