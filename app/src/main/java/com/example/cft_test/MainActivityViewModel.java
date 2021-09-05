package com.example.cft_test;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.ViewModel;
import androidx.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivityViewModel extends ViewModel {

    final String TAG = "myLogs";
    final Realm realm = Realm.getDefaultInstance();
    final long MINIMAL_UPDATE_TIME = 60000;

    final private String url = "https://www.cbr-xml-daily.ru/daily_json.js";

    SharedPreferences sharedPreferences;

    public void updateValutesWithDataCheck(Context context) {

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                        String lastUpdate = sharedPreferences.getString("last_update_date", "1970-01-01T00:00:00+00:00");

                        Log.d(TAG, "lastUpdate: " + lastUpdate);

                        try {
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

                            Date lastUpdateDateTime = format.parse(lastUpdate);
                            Date dataDate = format.parse(response.getString("Date"));

                            long diff = dataDate.getTime() - lastUpdateDateTime.getTime();

                            if (diff > MINIMAL_UPDATE_TIME) {
                                workWithJson(response);

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("last_update_date", response.getString("Date"));
                                editor.apply();

                                ((MainActivity) context).refillValutes();
                            }
                        } catch (ParseException | JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, error -> {
                    Log.d(TAG, "Error: " + error);
                });
        queue.add(jsonObjectRequest);

    }

    private void workWithJson(JSONObject response) throws JSONException {

        JSONObject valute = response.getJSONObject("Valute");

        if (valute != null) writeNewValuteDataToRealm(valute);
    }

    private void writeNewValuteDataToRealm(JSONObject valute) throws JSONException {
        Iterator<String> keys = valute.keys();

        while (keys.hasNext()) {
            String key = keys.next();

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
