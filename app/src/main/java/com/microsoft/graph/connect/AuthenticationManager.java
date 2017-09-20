/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */
package com.microsoft.graph.connect;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Handles setup of OAuth library in API clients.
 */
public class AuthenticationManager {
    AuthorizationServiceConfiguration mConfig;
    AuthorizationRequest mAuthorizationRequest;
    AuthState mAuthState;
    AuthorizationService mAuthorizationService;

    private static final String TAG = "AuthenticationManager";
    private static AuthenticationManager INSTANCE;

    private Activity mContextActivity;
    private String mAccessToken;
    String responseback;

    private AuthenticationManager() {
        Uri authorityUrl = Uri.parse(Constants.AUTHORITY_URL);
        Uri authorizationEndpoint = Uri.withAppendedPath(authorityUrl, Constants.AUTHORIZATION_ENDPOINT);
        Uri tokenEndpoint = Uri.withAppendedPath(authorityUrl, Constants.TOKEN_ENDPOINT);
        mConfig = new AuthorizationServiceConfiguration(authorizationEndpoint, tokenEndpoint, null);

        List<String> scopes = new ArrayList<>(Arrays.asList(Constants.SCOPES.split(" ")));

        mAuthorizationRequest = new AuthorizationRequest.Builder(
                mConfig,
                Constants.CLIENT_ID,
                ResponseTypeValues.CODE,
                Uri.parse(Constants.REDIRECT_URI))
                .setScopes(scopes)
                .build();
    }

    /**
     * Starts the authorization flow, which continues to net.openid.appauth.RedirectReceiverActivity
     * and then to ConnectActivity
     */
    public void startAuthorizationFlow() {
        Intent intent = new Intent(mContextActivity, ConnectActivity.class);

        PendingIntent redirectIntent = PendingIntent.getActivity(mContextActivity, mAuthorizationRequest.hashCode(), intent, 0);

        mAuthorizationService.performAuthorizationRequest(
                mAuthorizationRequest,
                redirectIntent,
                mAuthorizationService.createCustomTabsIntentBuilder()
                        .setToolbarColor(getColorCompat(R.color.colorPrimary))
                        .build());
    }

    public void processAuthorizationCode(Intent redirectIntent, final AuthorizationService.TokenResponseCallback callback) {
        AuthorizationResponse authorizationResponse = AuthorizationResponse.fromIntent(redirectIntent);
        AuthorizationException authorizationException = AuthorizationException.fromIntent(redirectIntent);
        mAuthState = new AuthState(authorizationResponse, authorizationException);

        if (authorizationResponse != null) {
            HashMap<String, String> additionalParams = new HashMap<>();
            TokenRequest tokenRequest = authorizationResponse.createTokenExchangeRequest(additionalParams);

            mAuthorizationService.performTokenRequest(
                    tokenRequest,
                    new AuthorizationService.TokenResponseCallback() {
                        @Override
                        public void onTokenRequestCompleted(
                                @Nullable TokenResponse tokenResponse,
                                @Nullable AuthorizationException ex) {
                            mAuthState.update(tokenResponse, ex);
                            if (tokenResponse != null) {
                                mAccessToken = tokenResponse.accessToken;


                                String body = "";
                                final String accessToken = tokenResponse.accessToken;
                                 Log.d("toeken", accessToken);


                                //String url ="https://graph.microsoft.com/v1.0/me/sendMail";
                                //https://graph.microsoft.com/{version}/me/events
                                //String url2 ="https://graph.microsoft.com/v1.0/me/";



                                //// DELETE MEETING /////

//                                String url = "https://graph.microsoft.com/v1.0/me/events?$select=subject,body,bodyPreview,organizer,attendees,start,end,location";
//                                responseback = Request_Delete(url,accessToken,body, "Update");
//                                Log.d("Response BACK" , responseback);


                                /// Create Meeting /////
//                                String url ="https://graph.microsoft.com/v1.0/me/events";
//                                body = CreateMeeting("'2017-09-19T18:00:00'", "'2017-09-19T19:00:00'",  "'REST1'");
//                                responseback = Request_post(url,accessToken,body);


                                 //// Update MEETING /////

//                                String url = "https://graph.microsoft.com/v1.0/me/events?$select=subject,body,bodyPreview,organizer,attendees,start,end,location";
//                                responseback = Request_Update(url,accessToken,body, "Update3");
//                                Log.d("Response BACK" , responseback);


                            }
                            callback.onTokenRequestCompleted(tokenResponse, ex);

                        }
                    });


        } else {
            Log.i(TAG, "Authorization failed: " + authorizationException);
        }
    }




    public JSONObject getClaims(String idToken) {
        JSONObject retValue = null;
        String payload = idToken.split("[.]")[1];

        try {
            // The token payload is in the 2nd element of the JWT
            String jsonClaims = new String(Base64.decode(payload, Base64.DEFAULT), "UTF-8");
            retValue = new JSONObject(jsonClaims);
        } catch ( JSONException | IOException e) {
            Log.e(TAG, "Couldn't decode id token: " + e.getMessage());
        }
        return retValue;
    }

    /**
     * Disconnects the app from Office 365 by clearing the token cache, setting the client objects
     * to null, and removing the user id from shred preferences.
     */
    public void disconnect() {
        // Reset the AuthenticationManager object
        AuthenticationManager.resetInstance();
    }

    public static synchronized AuthenticationManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AuthenticationManager();
        }
        return INSTANCE;
    }

    private static synchronized void resetInstance() {
        INSTANCE = null;
    }

    /**
     * Set the context activity before connecting to the currently active activity.
     *
     * @param contextActivity Currently active activity which can be utilized for interactive
     *                        prompt.
     */
    public void setContextActivity(final Activity contextActivity) {
        mContextActivity = contextActivity;
        mAuthorizationService = new AuthorizationService(mContextActivity);
    }

    /**
     * Returns the access token obtained in authentication
     *
     * @return mAccessToken
     */
    public String getAccessToken() throws TokenNotFoundException {
        if(mAccessToken == null) {
            throw new TokenNotFoundException();
        }
        return mAccessToken;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @SuppressWarnings("deprecation")
    private int getColorCompat(@ColorRes int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return mContextActivity.getColor(color);
        } else {
            return mContextActivity.getResources().getColor(color);
        }
    }






    public String Request_post(String url, final String accessToken, final String body) {

        final RequestQueue queue = Volley.newRequestQueue(mContextActivity.getApplicationContext());
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("ERROR", "error => " + error.getMessage());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + accessToken);
                String l = String.valueOf(body.getBytes().length);
                params.put("Content-Length", String.valueOf(body.getBytes().length));
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return body.getBytes();
            }
        };

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                queue.add(stringRequest);

            }
        });

     return  responseback;
    }

    Response.Listener myListner = new Response.Listener<String>() {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public void onResponse(String response) {
            Log.d("Response", response);


            /// do job
        }
    };
    public String Request_Delete(String url, final String accessToken, final String body, final String Subject) {

        final String[] responseto = {""};
        final RequestQueue queue = Volley.newRequestQueue(mContextActivity.getApplicationContext());
        final  StringRequest stringRequest1 = new StringRequest(Request.Method.GET,url,myListner,new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ERROR", "error => " + error.getMessage());
            }
        });

        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                myListner,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("ERROR", "error => " + error.getMessage());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + accessToken);
                String l = String.valueOf(body.getBytes().length);
                params.put("Content-Length", String.valueOf(body.getBytes().length));
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return body.getBytes();
            }
        };

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                queue.add(stringRequest);

            }
        });

        return responseto[0];
    }





    public String Request_Update(String url, final String accessToken, final String body, final String Subject) {

        final String[] responseto = {""};
        final RequestQueue queue = Volley.newRequestQueue(mContextActivity.getApplicationContext());
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @TargetApi(Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);

                        try {
                            JSONObject reader = new JSONObject(response);
                            JSONArray value = reader.getJSONArray("value");
                            Log.d("value", String.valueOf(value));

                            for (int i = 0; i < value.length(); i++) {
                                JSONObject v = value.getJSONObject(i);
                                String subject_pars = v.getString("subject");
                                String[] start_time = v.getString("start").substring(13).split(",");
                                String[] end_time = v.getString("end").substring(13).split(",");;
                                String id = v.getString("id");
                                Log.d("id", id);
                                Log.d("subject", subject_pars);
                                if (Objects.equals(Subject, subject_pars)){

                                    String url =   "https://graph.microsoft.com/v1.0/me/events/" + id;
                                    ////Update
                                    //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ss");
//                                    Calendar cal = Calendar.getInstance();
//                                   // cal.setTime(dateFormat.parse(end_time[0].substring(0, 19)));
//                                    cal.add(Calendar.MINUTE, 30);
//                                    Date end_time_ = cal.getTime();

                                    String body1 = UpdateMeeting("'2017-09-18T10:00:00'", "'2017-09-18T12:00:00'",  "'Update3'");
                                    String resp = call_Request_Update(url , accessToken, body1);


                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//                        catch (ParseException e) {
//                            e.printStackTrace();
//                        }


                        AuthenticationManager.this.responseback = response;

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("ERROR", "error => " + error.getMessage());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + accessToken);
                String l = String.valueOf(body.getBytes().length);
                params.put("Content-Length", String.valueOf(body.getBytes().length));
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return body.getBytes();
            }
        };

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                queue.add(stringRequest);

            }
        });

        return responseto[0];
    }





    public String CreateMeeting(String startm, String endm, String subject) {

        String url ="https://graph.microsoft.com/v1.0/me/events";
        final String body =  "{" +
                " subject: " + subject + "," +
                " body: {" +
                "     contentType: 'HTML'," +
                "       content: 'test' }," +
                "     start: {"+
                "           dateTime : " + startm + "," +
                "           timeZone : 'Pacific Standard Time'" +
                "            }," +
                "     end: {" +
                "           dateTime :" + endm + "," +
                "           timeZone : 'Pacific Standard Time'" +
                "            }," +
                "     location : { " +
                "          displayName : 'Room1'" +
                "         },"  +
                "       attendees : [ " +
                "                   {" +
                "                emailAddress : { " +
                "                  address : 'irfan.ifi650@gmail.com' ," +
                "                  name : 'Irfan Qureshi'" +
                "                   }," +
                "                 type : 'required'" +
                " } ] } ";



    return body; }


    public String UpdateMeeting(String startm, String endm, String subject) {

        String url ="https://graph.microsoft.com/v1.0/me/events";
        final String body =  "{" +
                " subject: " + subject + "," +
                " body: {" +
                "     contentType: 'HTML'," +
                "       content: 'test' }," +
                "     start: {"+
                "           dateTime : " + startm + "," +
                "           timeZone : 'Pacific Standard Time'" +
                "            }," +
                "     end: {" +
                "           dateTime :" + endm + "," +
                "           timeZone : 'Pacific Standard Time'" +
                "            }," +
                "     location : { " +
                "          displayName : 'Room1'" +
                "         },"  +
                "       attendees : [ " +
                "                   {" +
                "                emailAddress : { " +
                "                  address : 'irfan.ifi650@gmail.com' ," +
                "                  name : 'Irfan Qureshi'" +
                "                   }," +
                "                 type : 'required'" +
                " } ] } ";



        return body; }


    public String SendEmail() {
        String url ="https://graph.microsoft.com/v1.0/me/sendMail";

        final String body = "{" +
                "  Message: {" +
                "    subject: 'Sent using the Microsoft Graph REST API'," +
                "    body: {" +
                "      contentType: 'text'," +
                "      content: 'This is the email body'" +
                "    }," +
                "    toRecipients: [" +
                "      {" +
                "        emailAddress: {" +
                "          address: '<YOUR_EMAIL_ADDRESS>'" +
                "        }" +
                "      }" +
                "    ]}" +
                "}";


        return body; }






    public String call_Request_Update(String url, final String accessToken, final String body) {


        final RequestQueue queue = Volley.newRequestQueue(mContextActivity.getApplicationContext());
        final StringRequest stringRequest = new StringRequest(Request.Method.PATCH, url,
                new Response.Listener<String>() {
                    @TargetApi(Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("ERROR", "error => " + error.getMessage());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + accessToken);
                String l = String.valueOf(body.getBytes().length);
                params.put("Content-Length", String.valueOf(body.getBytes().length));
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return body.getBytes();
            }
        };

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                queue.add(stringRequest);

            }
        });

        return responseback;
    }


    public String call_Request_delete(String url, final String accessToken, final String body) {


        final RequestQueue queue = Volley.newRequestQueue(mContextActivity.getApplicationContext());
        final StringRequest stringRequest = new StringRequest(Request.Method.DELETE, url,
                new Response.Listener<String>() {
                    @TargetApi(Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(String response) {
                        Log.d("Response", response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("ERROR", "error => " + error.getMessage());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + accessToken);
                String l = String.valueOf(body.getBytes().length);
                params.put("Content-Length", String.valueOf(body.getBytes().length));
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                return body.getBytes();
            }
        };

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                queue.add(stringRequest);

            }
        });

        return responseback;
    }
}


