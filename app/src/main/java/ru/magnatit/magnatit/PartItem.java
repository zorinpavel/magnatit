package ru.magnatit.magnatit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PartItem {

    String P_Code;
    String P_Barcode;
    String P_Name;
    String CB_Name;
    String CM_Name;
    Integer P_Value;
    String P_Original;
    Map<Integer, String> Images = new HashMap<>();

    JSONObject Brand;
    JSONObject Model;

    PartItem(JSONObject Item) {

        try {
            P_Code = Item.getString("P_Code");
            P_Barcode = Item.getString("P_Barcode");
            P_Name = Item.getString("P_Name");
            P_Value = Integer.parseInt(Item.getString("P_Value"));
            P_Original = Item.getString("P_Original");
            P_Original = P_Original.equals("null") ? "" : P_Original;

            JSONArray Brands = Item.getJSONArray("P_Brands");
            for (int b = 0; b < Brands.length(); b++) {
                Brand = Brands.getJSONObject(b);
            }
            CB_Name = Brand.getString("CB_Name");

            JSONArray PModels = Item.getJSONArray("P_Models");
            for (int m = 0; m < PModels.length(); m++) {
                JSONArray BModels = PModels.getJSONArray(m);
                for (int bm = 0; bm < BModels.length(); bm++) {
                    Model = BModels.getJSONObject(bm);
                    if (Model.getString("CP_Main").equals("1"))
                        break;
                }
            }
            CM_Name = Model.getString("CM_Name");

            JSONArray PImages = Item.getJSONArray("Images");
            for (int im = 0; im < PImages.length(); im++) {
                Images.put(im, PImages.getString(im));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
