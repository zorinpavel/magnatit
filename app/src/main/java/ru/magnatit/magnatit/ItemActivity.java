package ru.magnatit.magnatit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ItemActivity extends Activity {

    private static final String TAG = "BarcodeItem";

    public ItemAdapter itemAdapter;

    API Api = new API(this);
    private ProgressDialog pDialog;
    private JSONObject jsonObj;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PLACE_BARCODE_CAPTURE = 2;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        Intent intent = getIntent();
        String Barcode = intent.getStringExtra("Barcode");
        if(Barcode != null) {
            GetParts(Barcode);
        }

    }

    private void ShuffleArray(String[] array) {
        int index;
        String temp;
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            index = random.nextInt(i + 1);
            temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }

    public String GetDebugBarcode() {
        String Barcodes[] = {
                "barcode307690394", "barcode469097934", "barcode163148608",
                "barcode315456211", "barcode964389481", "barcode400000259",
                "barcode634798030"
        };
        String PlaceBarcodes[] = {
                "place006"
        };
        ShuffleArray(Barcodes);

        int resID = getResources().getIdentifier("icon", "drawable", getPackageName());
        for(String rndBarcode : PlaceBarcodes) {
            resID = getResources().getIdentifier(rndBarcode , "drawable", getPackageName());
            break;
        }

        Bitmap myBitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), resID);
        BarcodeDetector detector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.CODE_128)
                .build();

        if(!detector.isOperational()){
            Log.d(TAG, "Could not set up the detector!");
            return null;
        }

        Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
        SparseArray<Barcode> detectedBarcodes = detector.detect(frame);

        int key = detectedBarcodes.keyAt(0);
        Barcode objBarcode = detectedBarcodes.get(key);

        Log.d(TAG, objBarcode.rawValue);
        return objBarcode.rawValue;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                if(resultCode == RESULT_OK)
                    itemAdapter.setImage();
                break;
            case PLACE_BARCODE_CAPTURE:
                if (resultCode == CommonStatusCodes.SUCCESS) {
                    if (data != null) {
                        Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                        Log.d(TAG, "Barcode read: " + barcode.displayValue);

//                    Intent intent = new Intent(this, ItemActivity.class);
//                    intent.putExtra("Barcode", barcode.displayValue);
//                    startActivity(intent);

                    } else {
                        Log.d(TAG, "No barcode captured, intent data is null");
                        Toast.makeText(this, "No barcode captured", Toast.LENGTH_SHORT).show();

                        // debugger
                        String Barcode = GetDebugBarcode();
                        new GetPlace().execute(Barcode);

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

    public void GetParts(String Barcode) {
        new GetParts().execute(Barcode);
    }

    private class GetParts extends AsyncTask<String, Void, Void> {

        ArrayList<PartItem> partItems = new ArrayList<>();

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
                    for(int i = 0; i < Errors.length(); i++) {
                        JSONObject ErrorText = Errors.getJSONObject(i);
                        String ErrorValue = ErrorText.getString("Error");
                        Api.showError(ErrorValue);
                    }
                } else {

                    JSONArray Items = jsonObj.getJSONArray("Items");

                    for(int i = 0; i < Items.length(); i++) {
                        JSONObject Item = Items.getJSONObject(i);
                        partItems.add(new PartItem(Item));
                    }

                    ListView itemsList = (ListView) findViewById(R.id.ItemsList);
                    itemAdapter = new ItemAdapter(ItemActivity.this, partItems);
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
