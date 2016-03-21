package ru.magnatit.magnatit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PartItem {

    String P_Code;
    String P_Barcode;
    String P_Name;
    String CB_Name;
    String CM_Name;
    Integer P_Value;
    String P_Original;
    String St_Name;
    String St_Code;
    String R_Name;
    String R_Code;
    String Pl_Code;
    Integer P_Wear;
    String P_WearDesc;
    ArrayList Images = new ArrayList();

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
            P_Wear = Item.getString("P_Wear").equals("null") ? 0 : Integer.parseInt(Item.getString("P_Wear"));
            P_WearDesc = Item.getString("P_WearDesc");
            P_WearDesc = P_WearDesc.equals("null") ? "" : P_WearDesc;

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

            JSONObject Storage = Item.getJSONObject("Storage");
            St_Name = Storage.getString("St_Name");
            St_Name = St_Name.equals("") ? "не указано" : St_Name;
            St_Code = Storage.getString("St_Code");

            R_Name = Storage.getString("R_Name");
            R_Name = R_Name.equals("null") ? "" : R_Name;
            R_Code = Storage.getString("R_Code");

            Pl_Code = Storage.getString("Pl_Code");
            Pl_Code = Pl_Code.equals("null") ? "" : Pl_Code;

            JSONArray PImages = Item.getJSONArray("Images");
            for (int im = 0; im < PImages.length(); im++) {
                Images.add(PImages.getString(im));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
