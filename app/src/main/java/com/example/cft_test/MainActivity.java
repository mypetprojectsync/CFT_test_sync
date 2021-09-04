package com.example.cft_test;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.cft_test.databinding.ActivityMainBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    final private String url = "https://www.cbr-xml-daily.ru/daily_json.js";
    final String TAG = "myLogs";

    ActivityMainBinding binding;
    List<String> valutes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        workWithJson(response);
                    }
                }, error -> {
                    Log.d(TAG, "Error: " + error);

                });
        queue.add(jsonObjectRequest);

        binding.valuteListBatton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);

            //TODO Set titles like this "Фунт стерлингов Соединенного королевства"

            for (String valute: valutes) {
                popupMenu.getMenu().add(valute);
            }
            popupMenu.show();
        });

    }

    private void workWithJson(JSONObject response) {

        try {

            JSONObject valute = response.getJSONObject("Valute");

            Iterator<String> keys = valute.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                valutes.add(key);
                Log.d(TAG, key + ": " + valute.getString(key));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

//todo 1. add view model
//todo 2. add realm
//todo 3. save currency list to realm when update
//todo 3.5. implement saving data when orientation change. Popupmenu too
//todo 4. add converting rubles to chosen currency
//todo 5. add update currency button
//todo 6. add periodic currency update
//todo 7. implement recycler view currency list

