package com.example.cft_test;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    final String TAG = "myLogs";
    private final int UPDATE_DELAY = 60000;

    ActivityMainBinding binding;
    MainActivityViewModel model;

    List<String> valutes = new ArrayList<>();

    RecyclerView recyclerView;
    ValuteListAdapter adapter;
    Valute valute;

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

        //todo если уже есть в realm список валют, загрузить первый, если нет, добавить проверку Valute.valuteAmount в updateValutesWithDateCheck. Если NaN, загрузить первый из списка. Или если valute.valuteAmount == null

        if (valutes.size() > 0 && model.getChosenValuteID() == null)
            model.setChosenValutebyName(valutes.get(0));

        valute = new Valute(model.getChosenValuteID(), model.getCharCode(), model.getNominal(), model.getName(), model.getValue());
        binding.setValute(valute);

        setRV();

        setValuteTIL();

        binding.swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                model.updateValutesWithoutDateCheck(binding.getRoot().getContext(), queue);
            }
        });

        refillValutes();

    }

    private void setValuteTIL() {

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

                    setValute(item.getTitle().toString());

                    return false;
                }
            });
        });

        binding.rublesTIET.addTextChangedListener(new CurrencyTextWatcher(binding));
    }

    private void setValute(String chosenValute) {
        model.setChosenValutebyName(chosenValute);

        Valute valute = binding.getValute();
        valute.setID(model.getChosenValuteID());
        valute.setValue(model.getValue());
        valute.setCharCode(model.getCharCode());
        valute.setName(model.getName());
        valute.setNominal(model.getNominal());

        setValuteTIET();

        setRecyclerViewSelection(chosenValute);
    }

    private void setRecyclerViewSelection(String chosenValute) {
        int oldPos = adapter.selectedPos;
        adapter.selectedPos = valutes.indexOf(chosenValute);

        adapter.notifyItemChanged(oldPos);
        adapter.notifyItemChanged(adapter.selectedPos);

        recyclerView.smoothScrollToPosition(valutes.indexOf(chosenValute));
    }

    public void setValuteTIET() {

        Valute valute = binding.getValute();

        NumberFormat format = NumberFormat.getInstance(Locale.getDefault());

        try {
            valute.setValuteAmount(String.format(Locale.getDefault(), "%,.2f", (format.parse(valute.getRublesAmount()).doubleValue() * valute.getNominal() / valute.getValue())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {

        setUpdateDataByTimer();

        super.onResume();
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    private void setUpdateDataByTimer() {

        handler.postDelayed(runnable = () -> {
            model.updateValutesWithDateCheck(binding.getRoot().getContext(), queue);
            handler.postDelayed(runnable, UPDATE_DELAY);
        }, UPDATE_DELAY);

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
                setValute(valutes.get(position));
            }
        });
        recyclerView.setAdapter(adapter);

        adapter.notifyDataSetChanged();

    }

    public void refillValutes() {

        valutes.clear();
        valutes.addAll(model.getValutes());

        adapter.notifyDataSetChanged();

        if (valute != null) {
            if ((valute.getValuteAmount().equals("NaN") || valute.getValuteAmount().equals("")) && valutes.size() > 0) {
                setValute(valutes.get(0));
            }
        }
    }

    public void stopRefresh() {
        binding.swiperefresh.setRefreshing(false);
    }
}

//todo 5.5. implement saving data when orientation change. Popupmenu too

//todo add refresh button in toolbar