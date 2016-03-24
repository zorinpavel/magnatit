package ru.magnatit.magnatit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ItemActivity extends Activity {

    private static final String TAG = "BarcodeItem";

    public ItemAdapter itemAdapter;

    API Api = new API(this);
    private ProgressDialog pDialog;
    private JSONObject jsonObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        itemAdapter  = new ItemAdapter(this);

        Intent intent = getIntent();
        String Barcode = intent.getStringExtra("Barcode");
        if(Barcode != null)
            new GetParts().execute(Barcode);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case MainActivity.REQUEST_IMAGE_CAPTURE:
                if(resultCode == RESULT_OK)
                    itemAdapter.cropImage();
                break;
            case MainActivity.REQUEST_IMAGE_PICK:
            case MainActivity.REQUEST_IMAGE_CROP:
                if(resultCode == RESULT_OK)
                    itemAdapter.setImage();
                break;
            case MainActivity.REQUEST_ITEM_PLACE_BARCODE_CAPTURE:
                if (resultCode == CommonStatusCodes.SUCCESS) {
                    if (data != null) {
                        Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);

                        new GetPlace().execute(barcode.displayValue);

                    } else {
                        Toast.makeText(this, R.string.noBarcodeCaptured, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, String.valueOf(resultCode));
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private class GetParts extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ItemActivity.this);
            pDialog.setMessage("Get parts...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(String... barcodes) {
            try {
                Map<String, String> params = new HashMap<>();
                for (String barcode : barcodes) {
                    params.put("P_Barcode", barcode);
                }
                jsonObj = Api.Get("Parts", "GetItems", params);
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
                    for (int i = 0; i < Errors.length(); i++) {
                        JSONObject ErrorText = Errors.getJSONObject(i);
                        String ErrorValue = ErrorText.getString("Error");
                        Api.showError(ErrorValue);
                    }
                } else {

                    JSONArray Items = jsonObj.getJSONArray("Items");

                    for (int i = 0; i < Items.length(); i++) {
                        JSONObject Item = Items.getJSONObject(i);
                        itemAdapter.partItems.add(new PartItem(Item));
                    }

                    ListView itemsList = (ListView) findViewById(R.id.ItemsList);
                    itemsList.setAdapter(itemAdapter);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

    private class GetPlace extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ItemActivity.this);
            pDialog.setMessage("Get place...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(String... barcodes) {
            try {
                Map<String, String> params = new HashMap<>();
                for (String barcode : barcodes) {
                    params.put("Pl_Barcode", barcode);
                }
                jsonObj = Api.Get("Place", "GetPlace", params);
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
                        Api.showError(ErrorValue);
                    }
                } else {
                    JSONObject Place = jsonObj.getJSONObject("Place");
                    JSONObject Storage = Place.getJSONObject("Storage");
                    itemAdapter.partItem.St_Name = Storage.getString("St_Name");
                    itemAdapter.partItem.St_Name = itemAdapter.partItem.St_Name.equals("") ? "не указано" : itemAdapter.partItem.St_Name;
                    itemAdapter.partItem.St_Code = Storage.getString("St_Code");

                    JSONObject Rack = Place.getJSONObject("Rack");
                    itemAdapter.partItem.R_Name = Rack.getString("R_Name");
                    itemAdapter.partItem.R_Name = itemAdapter.partItem.R_Name.equals("null") ? "" : itemAdapter.partItem.R_Name;
                    itemAdapter.partItem.R_Code = Rack.getString("R_Code");

                    itemAdapter.partItem.Pl_Code = Place.getString("Pl_Code");
                    itemAdapter.partItem.Pl_Code = itemAdapter.partItem.Pl_Code.equals("null") ? "" : itemAdapter.partItem.Pl_Code;

                    itemAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

}
