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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


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


    String textBeforeChanged = "";
    int selectorLastPosition = 0;
    boolean ignoreNextIteration = true;

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

                    model.setChosenValutebyName(item.getTitle().toString());

                    Valute valute = binding.getValute();
                    valute.setID(model.getChosenValuteID());
                    valute.setValue(model.getValue());
                    valute.setCharCode(model.getCharCode());
                    valute.setName(model.getName());
                    valute.setNominal(model.getNominal());

                   // valute.setRublesAmount(Integer.toString(valute.getNominal()));

                    //todo проверить строчку ниже, возможно есть неточности
                    //valute.setValuteAmount(Double.toString(valute.getNominal() * Double.parseDouble(valute.getRublesAmount()) / valute.getValue()));
                    //valute.setValuteAmount(String.format(Locale.getDefault(),"%,.2f", valute.getNominal() * Double.parseDouble(valute.getRublesAmount()) / valute.getValue()));
                    setValuteTIET();

                    return false;
                }
            });
        });

//todo добвить поддержку 1,123,456.78
        binding.rublesTIET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                if (!ignoreNextIteration) {
                    textBeforeChanged = s.toString();
                    selectorLastPosition = binding.rublesTIET.getSelectionStart();
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (ignoreNextIteration) {
                    ignoreNextIteration = false;
                } else {

                    if (s.length() > 20) {
                        ignoreNextIteration = true;
                        valute.setRublesAmount(textBeforeChanged);

                        //character added
                    } else if (s.length() > textBeforeChanged.length()) {

                        if (s.charAt(selectorLastPosition) == '.') {

                            ignoreNextIteration = true;

                            if (selectorLastPosition == s.length() - 4) {
                                selectorLastPosition++;
                            }

                            binding.rublesTIET.setText(textBeforeChanged);

                        } else if (selectorLastPosition > textBeforeChanged.length() - 3) {

                            if (selectorLastPosition == textBeforeChanged.length() - 2) {

                                valute.setRublesAmount(s.toString().substring(0, selectorLastPosition + 1) + s.toString().substring(selectorLastPosition + 2));
                                selectorLastPosition++;

                            } else if (selectorLastPosition == textBeforeChanged.length() - 1) {

                                selectorLastPosition++;
                                valute.setRublesAmount(s.toString().substring(0, s.toString().length() - 1));

                            } else {

                                valute.setRublesAmount(textBeforeChanged);

                            }
                            ignoreNextIteration = true;

                        } else if (selectorLastPosition == 0 && s.charAt(0) == '0') {

                            valute.setRublesAmount(textBeforeChanged);
                            ignoreNextIteration = true;

                        } else {

                            NumberFormat format = NumberFormat.getInstance(Locale.getDefault());

                            try {
                                String formatted = String.format(Locale.getDefault(), "%,.2f", Objects.requireNonNull(format.parse(s.toString())).doubleValue());
                                valute.setRublesAmount(formatted);
                                selectorLastPosition += formatted.length() - textBeforeChanged.length();

                                if (textBeforeChanged.length() > 5 || s.charAt(0) == '0') {
                                    ignoreNextIteration = true;
                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        //character removed
                    } else {

                        if (selectorLastPosition == textBeforeChanged.length() - 2 || textBeforeChanged.charAt(selectorLastPosition - 1) == ' ') {

                            selectorLastPosition--;
                            valute.setRublesAmount(textBeforeChanged);
                            ignoreNextIteration = true;

                        } else if (selectorLastPosition > textBeforeChanged.length() - 3) {

                            if (selectorLastPosition == textBeforeChanged.length() - 1) {
                                valute.setRublesAmount(s.toString().substring(0, selectorLastPosition - 1) + "0" + s.toString().substring(selectorLastPosition - 1));
                            } else {
                                valute.setRublesAmount(s.toString() + '0');
                            }
                            selectorLastPosition--;
                            ignoreNextIteration = true;

                        } else {

                            NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
                            try {

                                String formatted = String.format(Locale.getDefault(), "%,.2f", Objects.requireNonNull(format.parse(s.toString())).doubleValue());
                                valute.setRublesAmount(formatted);

                                if (textBeforeChanged.length() > 6 || s.length() < 4) {
                                    ignoreNextIteration = true;
                                }
                                selectorLastPosition += formatted.length() - textBeforeChanged.length();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                if (selectorLastPosition < 0) {
                    selectorLastPosition = 0;
                } else if (selectorLastPosition > (valute.getRublesAmount().length())) {
                    selectorLastPosition = valute.getRublesAmount().length();
                }

                binding.rublesTIET.setSelection(selectorLastPosition);

                setValuteTIET();
            }
        });
    }

    private void setValuteTIET(){

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

//todo 7. implement recycler view currency list selection when clicked

//todo упомянуть СДР в пояснительной записке