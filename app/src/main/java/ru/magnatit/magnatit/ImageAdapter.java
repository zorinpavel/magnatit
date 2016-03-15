package ru.magnatit.magnatit;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    public ArrayList partImages;

    private static final String TAG = "BarcodeImageAdapter";

    public ImageAdapter(Context c, ArrayList _partImages) {
        mContext = c;
        partImages = _partImages;
    }

    public int getCount() {
        return partImages.size();
    }

    @Override
    public Object getItem(int position) {
        return partImages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        } else {
            imageView = (ImageView) convertView;
        }

        int maxWidth = imageView.getWidth() < 300 ? 300 : imageView.getWidth();
        int maxHeight = imageView.getHeight() < 300 ? 300 : imageView.getHeight();

        if ((String.valueOf(getItem(position))).contains(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)))) {
            Log.d(TAG, "Local:" + position + ":" + String.valueOf(getItem(position)));

            Picasso.with(mContext)
                    .load("file://" + String.valueOf(getItem(position)))
                    .resize(maxWidth, maxHeight)
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(imageView);
        } else {
            Log.d(TAG, "Server:" + position + ":" + String.valueOf(getItem(position)));

            Picasso.with(mContext)
                    .load(API.ApiUrlBase + "/imagepreview/android" + getItem(position))
                    .resize(maxWidth, maxHeight)
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(imageView);
        }

        return imageView;
    }

}