package com.example.cft_test;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivityViewModel extends ViewModel {

    final String TAG = "myLogs";
    final Realm realm = Realm.getDefaultInstance();

    final private String url = "https://www.cbr-xml-daily.ru/daily_json.js";

    private JSONObject valute;

    public void updateValutes(Context context) {

        Log.d(TAG, "updateValutes(Context context)");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int lastUpdateDate = sharedPreferences.getInt("last_update_date", 0);

        if (lastUpdateDate == 0) {
            getValutesData(context);
        }

    }

    private void getValutesData(Context context) {
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        workWithJson(response);

                        try {
                            Log.d(TAG, "response date: " + response.getString("Date"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        ((MainActivity) context).refillValutes();
                    }
                }, error -> {
                    Log.d(TAG, "Error: " + error);

                });
        queue.add(jsonObjectRequest);
    }

    private void workWithJson(JSONObject response) {

        try {

            valute = response.getJSONObject("Valute");

            if (valute != null) writeNewValuteDataToRealm();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void writeNewValuteDataToRealm() {
        Iterator<String> keys = valute.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            try {
                JSONObject chosenValute = valute.getJSONObject(key);

                realm.beginTransaction();

                ValuteModel valuteModel = realm.where(ValuteModel.class).equalTo("id", chosenValute.getString("ID")).findFirst();

                if (valuteModel == null) {
                    valuteModel = realm.createObject(ValuteModel.class, chosenValute.getString("ID"));
                    valuteModel.setCharCode(chosenValute.getString("CharCode"));
                    valuteModel.setName(chosenValute.getString("Name"));
                }

                valuteModel.setNominal(chosenValute.getInt("Nominal"));
                valuteModel.setValue(chosenValute.getDouble("Value"));

                realm.commitTransaction();


            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, e.toString());
            }
        }
    }

    public void showAllRealmDatabase() {
        RealmResults<ValuteModel> valuteModels = realm.where(ValuteModel.class).findAll();

        for (ValuteModel valuteModel : valuteModels) {
            Log.d(TAG, valuteModel.toString());
        }

    }

    public List<String> getValutes() {

        List<String> valutes = new ArrayList<>();

        RealmResults<ValuteModel> valuteModels = realm.where(ValuteModel.class).findAll();

        for (ValuteModel valuteModel : valuteModels) {
            valutes.add(valuteModel.getName());
        }

        return valutes;
    }
}
