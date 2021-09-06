package com.example.cft_test;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.cft_test.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    final String TAG = "myLogs";
    private final int UPDATE_DELAY = 60000;

    ActivityMainBinding binding;
    MainActivityViewModel model;

    List<String> valutes = new ArrayList<>();

    RecyclerView recyclerView;
    ValuteListAdapter adapter;


    Runnable runnable;
    Handler handler = new Handler(Looper.getMainLooper());

    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        queue = Volley.newRequestQueue(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        model = new ViewModelProvider(this).get(MainActivityViewModel.class);
        binding.setModel(model);


        model.updateValutesWithDateCheck(this, queue);

        if (valutes.size() > 0 && model.getChosenValuteID() == null)
            model.setChosenValutebyName(valutes.get(0));

        Valute valute = new Valute(model.getChosenValuteID(), model.getCharCode(), model.getNominal(), model.getName(), model.getValue());
        binding.setValute(valute);

        setRV();

        setTIL();

        binding.swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                model.updateValutesWithoutDateCheck(binding.getRoot().getContext(), queue);
            }
        });
    }

    private void setTIL() {

        binding.valuteTIL.setEndIconOnClickListener(v -> {

            Log.d(TAG, "icon clicked");

            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);


            for (String valute : valutes) {
                popupMenu.getMenu().add(valute);
            }

            popupMenu.show();

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    model.setChosenValutebyName(item.getTitle().toString());

                    Valute valute = binding.getValute();
                    valute.setID(model.getChosenValuteID());
                    valute.setValue(model.getValue());
                    valute.setCharCode(model.getCharCode());
                    valute.setName(model.getName());
                    valute.setNominal(model.getNominal());

                    valute.setRublesAmount(Integer.toString(valute.getNominal()));

                    valute.setValuteAmount(Double.toString(valute.getNominal() * Double.parseDouble(valute.getRublesAmount()) / valute.getValue()));

                    Log.d(TAG, "valute.getCurrancy: " + valute.getValuteAmount());

                    return false;
                }
            });
        });


        binding.rublesTIET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                Valute valute = binding.getValute();

                if (s.length() > 0) {

                    if (valute.getValuteAmount() != null) {
                        valute.setValuteAmount(
                                Double.toString(Double.parseDouble(s.toString()) * valute.getNominal()/ valute.getValue() ));
                    }
                } else {
                    valute.setValuteAmount("0");
                }
            }
        });

    }

    @Override
    protected void onResume() {

        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                model.updateValutesWithDateCheck(binding.getRoot().getContext(), queue);
                handler.postDelayed(runnable, UPDATE_DELAY);
            }
        }, UPDATE_DELAY);

        super.onResume();
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    private void setUpdateDataByTimer() {
    }

    private void setRV() {
        recyclerView = binding.valutesRv;

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                ((LinearLayoutManager) recyclerView.getLayoutManager()).getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        valutes.addAll(model.getValutes());
        adapter = new ValuteListAdapter(this, valutes);

        adapter.setClickListener(new ValuteListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.d(TAG, valutes.get(position) + " clicked");
            }
        });
        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

    public void refillValutes() {

        valutes.clear();
        valutes.addAll(model.getValutes());

        adapter.notifyDataSetChanged();


        Toast.makeText(this, "Данные были обновлены", Toast.LENGTH_SHORT).show();
    }

    public void stopRefresh() {
        binding.swiperefresh.setRefreshing(false);
    }
}

//todo 5.5. implement saving data when orientation change. Popupmenu too
//todo 4. add converting rubles to chosen currency
//todo 5. add update currency button
//todo 6. add periodic currency update
//todo 7. implement recycler view currency list selection when clicked

