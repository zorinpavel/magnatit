package ru.magnatit.magnatit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ItemAdapter extends BaseAdapter {
    private Context mContext;
    LayoutInflater layoutInflater;

    public ArrayList<PartItem> partItems;
    public PartItem partItem;

    public ImageAdapter imageAdapter;
    public static String mCurrentPhotoPath;

    private API Api;
    private ProgressDialog pDialog;
    private JSONObject jsonObj;

    private static final String TAG = "BarcodeItemAdapter";

    ItemAdapter(Context c, ArrayList<PartItem> _partItems) {
        this.mContext = c;
        Api = new API(this.mContext);
        this.partItems = _partItems;
        layoutInflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setImage() {
        imageAdapter.partImages.put(imageAdapter.getCount(), mCurrentPhotoPath);
        imageAdapter.notifyDataSetChanged();
    }

    public static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d(TAG, mCurrentPhotoPath);
        return image;
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return this.partItems.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return this.partItems.get(position);
    }

    // id по позиции
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.item, parent, false);
        }

        partItem = getPart(position);

        ((TextView) view.findViewById(R.id.P_Barcode)).setText(partItem.P_Barcode);
        ((TextView) view.findViewById(R.id.P_Name)).setText(partItem.P_Name);
        ((TextView) view.findViewById(R.id.CB_Name)).setText(partItem.CB_Name);
        ((TextView) view.findViewById(R.id.CM_Name)).setText(partItem.CM_Name);
        ((TextView) view.findViewById(R.id.P_Value)).setText(String.valueOf(partItem.P_Value));
        ((TextView) view.findViewById(R.id.P_Original)).setText(partItem.P_Original);
        ((TextView) view.findViewById(R.id.St_Name)).setText(partItem.St_Name);
        ((TextView) view.findViewById(R.id.R_Name)).setText(partItem.R_Name);
        ((TextView) view.findViewById(R.id.Pl_Code)).setText(partItem.Pl_Code);

//        LinearLayout itemLine = (LinearLayout) view.findViewById(R.id.ItemLine);
//        itemLine.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, String.valueOf(partItem.P_Code));
//            }
//        });

        StaticGridView gridView = (StaticGridView) view.findViewById(R.id.ImagesGrid);
        imageAdapter = new ImageAdapter(mContext, partItem.P_Code, partItem.Images);
        gridView.setAdapter(imageAdapter);

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView parent, View v, int position, long id) {
                showOptionsMenu(position, partItem);
                return true;
            }
        });

        ImageButton itemCancel = (ImageButton) view.findViewById(R.id.ItemCancel);
        itemCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity) mContext).finish();
            }
        });

        ImageButton itemAddPhoto = (ImageButton) view.findViewById(R.id.ItemAddPhoto);
        itemAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        Log.e(TAG, "Error occurred while creating the File");
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                        ((Activity) mContext).startActivityForResult(takePictureIntent, ItemActivity.REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });

        ImageButton itemPlace = (ImageButton) view.findViewById(R.id.ItemPlace);
        itemPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, BarcodeCaptureActivity.class);
                intent.putExtra("AutoFocus", true);

                ((Activity) mContext).startActivityForResult(intent, ItemActivity.PLACE_BARCODE_CAPTURE);
            }
        });

        ImageButton itemSave = (ImageButton) view.findViewById(R.id.ItemSave);
        itemSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SavePart().execute();
            }
        });

        return view;
    }

    PartItem getPart(int position) {
        return ((PartItem) getItem(position));
    }

    public void showOptionsMenu(final int position, final PartItem partItem) {
        new AlertDialog.Builder(mContext)
                .setTitle("Удалить фотографию?")
                .setCancelable(true)
                .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d(TAG, String.valueOf(partItem.P_Code) + ":" + String.valueOf(position) + ":" + String.valueOf(id));
                    }
                })
                .show();
    }

    private class SavePart extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            EditText pValueInput = (EditText) ((Activity) mContext).findViewById(R.id.P_Value);
            partItem.P_Value = Integer.parseInt(pValueInput.getText().toString(), 10);

            EditText pOriginalInput = (EditText) ((Activity) mContext).findViewById(R.id.P_Original);
            partItem.P_Original = pOriginalInput.getText().toString();

            pDialog = new ProgressDialog(mContext);
            pDialog.setMessage("Save part...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(String... Arg) {
            try {
                Map<String, String> params = new HashMap<>();
                params.put("P_Code", partItem.P_Code);
                params.put("P_Value", String.valueOf(partItem.P_Value));
                params.put("P_Original", partItem.P_Original);
                params.put("St_Code", partItem.St_Code);
                params.put("R_Code", partItem.R_Code);
                params.put("Pl_Code", partItem.Pl_Code);

                Boolean isPost = false;
                for(int i = 0; i < partItem.Images.size(); i++) {
                    String imageName = partItem.Images.get(i);
                    if((String.valueOf(imageName)).contains(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)))) {
                        isPost = true;
                        jsonObj = Api.Post("Parts", "SetItem", params, imageName);
                    }
                }

                if(!isPost)
                    jsonObj = Api.Post("Parts", "SetItem", params, null);

                Log.d(TAG, String.valueOf(params));
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
                    Log.d(TAG, "SAVED");
                    Toast.makeText(mContext, "Сохранено", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
