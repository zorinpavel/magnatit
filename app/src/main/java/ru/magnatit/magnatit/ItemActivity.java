package ru.magnatit.magnatit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

//        String Barcodes[] = {
//                "barcode307690394", "barcode469097934", "barcode163148608",
//                "barcode315456211", "barcode964389481", "barcode400000259",
//                "barcode634798030"
//        };
//        ShuffleArray(Barcodes);
//
//        int resID = getResources().getIdentifier("icon", "drawable", getPackageName());;
//        for(String rndBarcode : Barcodes) {
//            resID = getResources().getIdentifier(rndBarcode , "drawable", getPackageName());
//            break;
//        }
//
//        Bitmap myBitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), resID);
//        BarcodeDetector detector = new BarcodeDetector.Builder(getApplicationContext())
//                .setBarcodeFormats(Barcode.CODE_128)
//                .build();
//
//        if(!detector.isOperational()){
//            Log.d(TAG, "Could not set up the detector!");
//            return;
//        }
//
//        Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
//        SparseArray<Barcode> detectedBarcodes = detector.detect(frame);
//
//        int key = detectedBarcodes.keyAt(0);
//        Barcode objBarcode = detectedBarcodes.get(key);
//        Log.d(TAG, objBarcode.rawValue);

        Intent intent = getIntent();
        String Barcode = intent.getStringExtra("Barcode");
        Log.d(TAG, Barcode);
        if(Barcode != null) {
            GetParts(Barcode);
        }

    }

    public void GetParts(String Barcode) {
        new GetParts().execute(Barcode);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
            itemAdapter.setImage();
        }
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

}
