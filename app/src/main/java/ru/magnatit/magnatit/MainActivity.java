package ru.magnatit.magnatit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class MainActivity extends Activity {

    private static final int RC_BARCODE_CAPTURE = 9001;
    private static final String TAG = "BarcodeMain";

    private ProgressDialog pDialog;

    API Api = new API(this);
    private JSONObject jsonObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences Settings = PreferenceManager.getDefaultSharedPreferences(this);
        String ApiUrlBase = Settings.getString("ApiUrlBase", null);
        String ApiKey = Settings.getString("ApiKey", null);

        if(ApiUrlBase == null || ApiKey == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Необъодимо установить ключ API и адрес сервера в настройках")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // do anything
                        }
                    }).setIcon(android.R.drawable.stat_notify_error);
            AlertDialog alert = builder.create();
            alert.show();
        }

//        new GetKey().execute();

    }

    public void newBarcodeClick(View v) {
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        intent.putExtra("AutoFocus", true);

        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    Log.d(TAG, "Barcode read: " + barcode.displayValue);

                    Intent intent = new Intent(this, ItemActivity.class);
                    intent.putExtra("Barcode", barcode.displayValue);
                    startActivity(intent);

                } else {
                    Log.d(TAG, "No barcode captured, intent data is null");
                    Toast.makeText(this, "No barcode captured", Toast.LENGTH_SHORT).show();

                    // debug
                    Intent intent = new Intent(this, ItemActivity.class);
                    intent.putExtra(BarcodeCaptureActivity.BarcodeObject, "634798030");
                    startActivity(intent);

                }
            } else {
                Log.d(TAG, String.valueOf(resultCode));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem mi = menu.add(0, 1, 0, "Настройки");
        mi.setIntent(new Intent(this, SettingsActivity.class));
        return super.onCreateOptionsMenu(menu);
    }

    private class GetKey extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Read API key...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                jsonObj = Api.Get("Key", "CheckForKey");
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();

            try {
                String Error = jsonObj.getString("Error");
                if (Error.equals("1")) {
                    JSONArray Errors = jsonObj.getJSONArray("Errors");
                    for(int i = 0; i < Errors.length(); i++) {
                        JSONObject ErrorText = Errors.getJSONObject(i);
                        String ErrorValue = ErrorText.getString("Error");
                        Log.d("Error ", ErrorValue);
                        Api.showError(ErrorValue);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

}
