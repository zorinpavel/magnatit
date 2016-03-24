package ru.magnatit.magnatit;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ItemsListActivity extends ListActivity {

    private static final String TAG = "BarcodeItemsList";

    public ItemsListAdapter itemsListAdapter;

    API Api = new API(this);
    private ProgressDialog pDialog;
    private JSONObject jsonObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        itemsListAdapter = new ItemsListAdapter(this);

        Intent intent = getIntent();
        String Barcode = intent.getStringExtra("Barcode");
        if(Barcode != null)
            new GetPlace().execute(Barcode);

    }

    @Override
    protected void onListItemClick (ListView l, View v, int position, long id) {
        PartItem partItem = itemsListAdapter.getPart(position);
        Intent intent = new Intent(this, ItemActivity.class);
        intent.putExtra("Barcode", partItem.P_Barcode);
        startActivity(intent);
    }

    private class GetPlace extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ItemsListActivity.this);
            pDialog.setMessage("Get parts...");
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
                jsonObj = Api.Get("Place", "GetItems", params);
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
                        itemsListAdapter.partItems.add(new PartItem(Item));
                    }

                    setListAdapter(itemsListAdapter);
                    itemsListAdapter.notifyDataSetChanged();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }

}
