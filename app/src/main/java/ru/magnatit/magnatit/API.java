package ru.magnatit.magnatit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class API {

    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");
    private static final MediaType MEDIA_TYPE_JPG = MediaType.parse("image/jpeg");
    public static String ApiUrlBase = null;
    private static String ApiKey = null;
    private String ApiUrlRequest = null;

    private Context mContext;

    private static final String TAG = "BarcodeAPI";

    public API(Context c) {
        this.mContext = c;
    }

    public JSONObject Get(String ClassName, String MethodName) throws IOException {
        Map<String, String> params = new HashMap<>();
        return this.Get(ClassName, MethodName, params);
    }

    public JSONObject Get(String ClassName, String MethodName, Map<String, String> params) throws IOException {
        JSONObject jsonObj = null;
        getApiUrlRequest(ClassName, MethodName, params);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(this.ApiUrlRequest)
                .build();
        this.ApiUrlRequest = null;

        // TODO: Catch SocketTimeoutException
        Response response = client.newCall(request).execute();
        try {
            jsonObj = new JSONObject(response.body().string());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, String.valueOf(jsonObj));
        return jsonObj;
    }

    public JSONObject Post(String ClassName, String MethodName, Map<String, String> postParams, String postFileName) throws IOException {
        JSONObject jsonObj = null;
        getApiUrlRequest(ClassName, MethodName, new HashMap<String, String>());

        OkHttpClient client = new OkHttpClient();

        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        for (Map.Entry param : postParams.entrySet()) {
            builder.addFormDataPart(String.valueOf(param.getKey()), String.valueOf(param.getValue()));
        }
        if(postFileName != null && !postFileName.isEmpty()) {
            builder.addFormDataPart("image", postFileName, RequestBody.create(MEDIA_TYPE_JPG, new File(postFileName)));
        }
        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(this.ApiUrlRequest)
                .post(requestBody)
                .build();
        this.ApiUrlRequest = null;

        // TODO: Catch SocketTimeoutException
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful())
            throw new IOException("Unexpected code " + response);

        try {
            jsonObj = new JSONObject(response.body().string());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, String.valueOf(jsonObj));
        return jsonObj;
    }

    public void getApiUrlRequest(String ClassName, String MethodName, Map<String, String> params) {
        SharedPreferences Settings = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        ApiUrlBase = Settings.getString("ApiUrlBase", null);
        ApiKey = Settings.getString("ApiKey", null);

        this.ApiUrlRequest = ApiUrlBase + "/" + ClassName + "." + MethodName;

        params.put("key", ApiKey);
//        params.put("debug", "1");

        for (Map.Entry entry : params.entrySet()) {
            this.ApiUrlRequest = this.ApiUrlRequest + "&" + entry.getKey() + "=" + entry.getValue();
        }
        Log.d(TAG, this.ApiUrlRequest);

    }

    public void showError(String message) {
        Log.w(TAG + " (Error) ", message);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(message)
                .setTitle("Response from server")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((Activity) mContext).finish();
                    }
                }).setIcon(android.R.drawable.stat_notify_error);
        AlertDialog alert = builder.create();
        alert.show();
    }

}
