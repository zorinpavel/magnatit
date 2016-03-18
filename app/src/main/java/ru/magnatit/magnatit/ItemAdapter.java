package ru.magnatit.magnatit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
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

    ItemAdapter(Context c) {
        mContext = c;
        Api = new API(mContext);
        partItems = new ArrayList<>();

        layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setImage() {
        // TODO check if file exists
        partItem.Images.add(mCurrentPhotoPath);
        imageAdapter.notifyDataSetChanged();
    }

    public void cropImage() {
        Intent photoCropIntent = new Intent("com.android.camera.action.CROP");
        if (photoCropIntent.resolveActivity(mContext.getPackageManager()) != null) {
            photoCropIntent.setType("image/*");
            photoCropIntent.setData(Uri.parse(mCurrentPhotoPath));
            photoCropIntent.putExtra("outputX", 880);
            photoCropIntent.putExtra("outputY", 660);
            photoCropIntent.putExtra("aspectX", 88);
            photoCropIntent.putExtra("aspectY", 66);
            photoCropIntent.putExtra("scale", true);
            photoCropIntent.putExtra("return-data", false);
            ((Activity) mContext).startActivityForResult(photoCropIntent, ItemActivity.REQUEST_IMAGE_CROP);
        } else {
            resizeImage();
            setImage();
        }
    }

    private void resizeImage() {
        int targetW = 880, targetH = 660;
        Bitmap old = BitmapFactory.decodeFile(mCurrentPhotoPath);
        Bitmap bmp = Bitmap.createScaledBitmap(old, targetW, targetH, true);

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(mCurrentPhotoPath);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // кол-во элементов
    @Override
    public int getCount() {
        return partItems.size();
    }

    // элемент по позиции
    @Override
    public Object getItem(int position) {
        return partItems.get(position);
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

        final TextView mWearDesc = (TextView) view.findViewById(R.id.P_WearDesc);
        mWearDesc.setText(partItem.P_WearDesc);

        final CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.P_Wear_checkBox);
        if(partItem.P_Wear > 0) {
            mCheckBox.setChecked(true);
            mCheckBox.setText(String.format("%s %d%%", mContext.getString(R.string.P_Wear_checkBox_Text), partItem.P_Wear));
        }
        else
            mCheckBox.setChecked(false);
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mCheckBox.isChecked()) {
                    Integer newWear = (partItem.P_Wear > 0 ? partItem.P_Wear : 10);
                    partItem.P_Wear = newWear;
                    mCheckBox.setText(String.format("%s %d%%", mContext.getString(R.string.P_Wear_checkBox_Text), newWear));
                    mWearDesc.setText(String.format("%s\r\n%s", mContext.getString(R.string.P_WearDesc_Default), mWearDesc.getText().toString()));
                } else {
                    partItem.P_Wear = 0;
                    mCheckBox.setText(R.string.P_Wear_checkBox_Text);
                    mWearDesc.setText(partItem.P_WearDesc);
                }
            }
        });

        StaticGridView gridView = (StaticGridView) view.findViewById(R.id.ImagesGrid);
        imageAdapter = new ImageAdapter(mContext, partItem.Images);
        gridView.setAdapter(imageAdapter);

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView parent, View v, int position, long id) {
                showOptionsMenu(position);
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

                final CharSequence[] items = { "Камера", "Выбрать из галереи", "Cancel" };
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.add_photos);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {
                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
                                File photoFile = null;
                                try {
                                    photoFile = createImageFile();
                                } catch (IOException ex) {
                                    Log.e(TAG, "Error occurred while creating the File");
                                }
                                if (photoFile != null) {
                                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                                    takePictureIntent.putExtra("return-data", false);
                                    ((Activity) mContext).startActivityForResult(takePictureIntent, ItemActivity.REQUEST_IMAGE_CAPTURE);
                                }
                            }
                        } else if (item == 1) {
                            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            if (photoPickerIntent.resolveActivity(mContext.getPackageManager()) != null) {
                                File photoFile = null;
                                try {
                                    photoFile = createImageFile();
                                } catch (IOException ex) {
                                    Log.e(TAG, "Error occurred while creating the File");
                                }
                                if (photoFile != null) {
                                    photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                                    photoPickerIntent.setType("image/*");
                                    photoPickerIntent.putExtra("crop", "true");
                                    photoPickerIntent.putExtra("outputX", 880); // imageprevew 900
                                    photoPickerIntent.putExtra("outputY", 660);
                                    photoPickerIntent.putExtra("aspectX", 88);
                                    photoPickerIntent.putExtra("aspectY", 66);
                                    photoPickerIntent.putExtra("scale", true);
                                    photoPickerIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                                    photoPickerIntent.putExtra("return-data", false);
                                    ((Activity) mContext).startActivityForResult(photoPickerIntent, ItemActivity.REQUEST_IMAGE_PICK);
                                }
                            }
                        } else if (items[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
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

    public void showOptionsMenu(final int position) {
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
                        if ((String.valueOf(imageAdapter.getItem(position))).contains(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)))) {
                            File file = new File(String.valueOf(imageAdapter.getItem(position)));
                            file.delete();
                        }
                        new DeleteImage().execute(String.valueOf(imageAdapter.getItem(position)));
                        partItem.Images.remove(position);
                        imageAdapter.notifyDataSetChanged();
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

            EditText pWearDescInput = (EditText) ((Activity) mContext).findViewById(R.id.P_WearDesc);
            partItem.P_WearDesc = pWearDescInput.getText().toString();

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
                params.put("P_Wear", String.valueOf(partItem.P_Wear));
                params.put("P_WearDesc", partItem.P_WearDesc);

                Boolean isPost = false;
                for(int i = 0; i < partItem.Images.size(); i++) {
                    String imageName = (String) partItem.Images.get(i);
                    if((String.valueOf(imageName)).contains(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)))) {
                        jsonObj = Api.Post("Parts", "SetItem", params, imageName);
                        File file = new File(String.valueOf(imageName));
                        file.delete();
                        isPost = true;
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

    private class DeleteImage extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(mContext);
            pDialog.setMessage("Deleting image...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(String... Arg) {
            try {
                Map<String, String> params = new HashMap<>();
                params.put("P_Code", partItem.P_Code);
                params.put("image", Arg[0]);
                jsonObj = Api.Post("Parts", "DeleteImage", params, null);
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
                    Log.d(TAG, "Image deleted");
                }
            } catch (JSONException e) {
                Log.e(TAG, "No jsonObj");
            }
        }
    }

}
