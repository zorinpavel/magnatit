package ru.magnatit.magnatit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class ItemsListAdapter extends ItemAdapter {

    private Context mContext;
    public ArrayList<PartItem> partItems;

    private static final String TAG = "BarcodeItemsListAdapter";

    ItemsListAdapter(Context c) {
        super(c);
        mContext = c;
        partItems = new ArrayList<>();

        layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.list_item, parent, false);
        }

        partItem = getPart(position);

        ((TextView) view.findViewById(R.id.P_Barcode)).setText(partItem.P_Barcode);
        ((TextView) view.findViewById(R.id.P_Name)).setText(partItem.P_Name);
        ((TextView) view.findViewById(R.id.CB_Name)).setText(partItem.CB_Name);
        ((TextView) view.findViewById(R.id.CM_Name)).setText(partItem.CM_Name);
        ((TextView) view.findViewById(R.id.P_Value)).setText(String.valueOf(partItem.P_Value));

//        LinearLayout itemLine = (LinearLayout) view.findViewById(R.id.ItemLine);
//        itemLine.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(mContext, ItemActivity.class);
//                intent.putExtra("Barcode", );
//                mContext.startActivity(intent);
//            }
//        });

        return view;
    }

    @Override
    public int getCount() {
        return partItems.size();
    }

    @Override
    public Object getItem(int position) {
        return partItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
