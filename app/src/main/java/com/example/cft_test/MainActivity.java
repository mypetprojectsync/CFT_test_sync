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

    int textLengthbeforeChanged = 0;
    String textBeforeChanged = "";
    int textLengthafterChanged = 0;
    int selectorLastPosition = 0;

    boolean ignoreNextIteration = false;

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

                    //todo проверить строчку ниже, возможно есть неточности
                    //valute.setValuteAmount(Double.toString(valute.getNominal() * Double.parseDouble(valute.getRublesAmount()) / valute.getValue()));
                    valute.setValuteAmount(String.format("%1$,.2f", valute.getNominal() * Double.parseDouble(valute.getRublesAmount()) / valute.getValue()));

                    Log.d(TAG, "valute.getCurrancy: " + valute.getValuteAmount());

                    return false;
                }
            });
        });


        binding.rublesTIET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                if (!ignoreNextIteration) {
                    textBeforeChanged = s.toString();
                    selectorLastPosition = start;
                }
                Log.d(TAG, "before text changed char sequence: " + textBeforeChanged);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                Log.d(TAG, "Text before changed: " + textBeforeChanged);

                if (ignoreNextIteration) {
                    ignoreNextIteration = false;

                } else {

                    if (s.length() > textBeforeChanged.length()) {
                        Log.d(TAG, "add symbol: " + s.charAt(selectorLastPosition));

                        if (s.charAt(selectorLastPosition) == '.') {
                            ignoreNextIteration = true;
                            if (selectorLastPosition == s.length() - 4) {
                                selectorLastPosition++;
                            }
                            binding.rublesTIET.setText(textBeforeChanged);

                        } else {
                            String formatted;

                            if (selectorLastPosition > textBeforeChanged.length() - 3) {
                                Log.d(TAG, "dot and after");
                            } else {
                                NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
                                try {

                                    formatted = String.format(Locale.getDefault(), "%,.2f", Objects.requireNonNull(format.parse(s.toString())).doubleValue());
                                    valute.setRublesAmount(formatted);
                                    selectorLastPosition += formatted.length() - textBeforeChanged.length();

                                    if (textBeforeChanged.length() > 5) {
                                        ignoreNextIteration = true;
                                    }

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }


                            }
                        }
                    } else {

                        Log.d(TAG, "reduce symbol: " + textBeforeChanged.charAt(selectorLastPosition));

                    }

                    Log.d(TAG, "selectorLastPosition: " + selectorLastPosition);


                /*textLengthafterChanged = s.length();

                int moveSelectorToRight = 0;

                Valute valute = binding.getValute();
                if (s.length() > 0) {

                    if (ignoreNextIteration) {

                        ignoreNextIteration = false;
                        binding.rublesTIET.setSelection(selectorLastPosition - 1);

                    } else {

//todo когда стираешь первый символ перед запятой, курсор смещается на предпоследнее место
                        if (selectorLastPosition > (s.length() - 4)) {

                            Log.d(TAG, "s.charAt(selectorLastPosition): " + s.charAt(selectorLastPosition-2));
                            Log.d(TAG, "textBeforeChanged.charAt(selectorLastPosition): " + textBeforeChanged.charAt(selectorLastPosition-1));

                            NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
                            String formatted = null;
                            try {
                                formatted = String.format(Locale.getDefault(), "%,.3f", Objects.requireNonNull(format.parse(s.toString())).doubleValue());

                                // valute.setRublesAmount(formatted.substring(0, formatted.length() - 1));

                                Log.d(TAG, "formatted: " + formatted);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            //int newSelectorPosition = selectorLastPosition + textLengthafterChanged - textLengthbeforeChanged + moveSelectorToRight;

                            if ((s.charAt(s.length() - 4) == '.' || s.charAt(s.length() - 4) == ',') && textLengthbeforeChanged < textLengthafterChanged) {
                                if (selectorLastPosition == s.length() - 3) {
                                    //todo менять местами сотые и тысячные
                                    valute.setRublesAmount(formatted.substring(0, selectorLastPosition + 1) + formatted.substring(selectorLastPosition + 2));
                                    binding.rublesTIET.setSelection(s.length() - 2);
                                } else {
                                    valute.setRublesAmount(formatted.substring(0, formatted.length() - 1));
                                    binding.rublesTIET.setSelection(s.length() - 1);

                                }
                            }  else {
                                binding.rublesTIET.setSelection(selectorLastPosition);
                            }


                        } else if (selectorLastPosition == (s.length() - 4) && (s.charAt(s.length() - 4) == '.' || s.charAt(s.length() - 4) == ',')) {

                            // moveSelectorToRight++;

                            valute.setRublesAmount(s.toString().substring(0, s.length() - 4) + s.toString().substring(s.length() - 3, s.length()));

                            int newSelectorPosition = selectorLastPosition + textLengthafterChanged - textLengthbeforeChanged;

                            if (newSelectorPosition >= 0) {
                                binding.rublesTIET.setSelection(newSelectorPosition);
                            } else {
                                binding.rublesTIET.setSelection(0);
                            }
                        } else if (s.charAt(selectorLastPosition) == '.') {
                            valute.setRublesAmount(s.toString().substring(0, selectorLastPosition) + s.toString().substring(selectorLastPosition + 1, s.length()));
                            ignoreNextIteration = true;
                        } else {

                            NumberFormat format = NumberFormat.getInstance(Locale.getDefault());

                            try {

                                String formatted = String.format(Locale.getDefault(), "%,.2f", Objects.requireNonNull(format.parse(s.toString())).doubleValue());

                                valute.setRublesAmount(formatted);

                                //   Log.d(TAG,"formatted: " + formatted);

                                //Если кликнута точка или запятая на позиции 3 с конца, передвигаем курсор на позицию 2 с конца

                                //Если кликнута точка или запятая в любой другой позиции, игнорируем

                                //Если курсор на позиции 0 с конца, игнорируем

                                //Если курсор на позиции 2 с конца, отбрасываем сотую без округления

                                Log.d(TAG, "Selector end after format at position: " + binding.rublesTIET.getSelectionEnd());


                                //valute.setRublesAmount(String.format(Locale.getDefault(),"%,.2f",format.parse(s.toString()).doubleValue()));


                                setValuteTIET(s);

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            int newSelectorPosition = selectorLastPosition + textLengthafterChanged - textLengthbeforeChanged;

                            if (newSelectorPosition >= 0) {
                                binding.rublesTIET.setSelection(newSelectorPosition);
                            } else {
                                binding.rublesTIET.setSelection(0);
                            }
                        }

                    }

                    // selectorLastPosition = binding.rublesTIET.getSelectionEnd();

                } else {
                    valute.setValuteAmount("0");
                }*/
                }
                binding.rublesTIET.setSelection(selectorLastPosition);
            }
        });

    }

    private void setValuteTIET(Editable s) throws ParseException {

        Valute valute = binding.getValute();

        NumberFormat format = NumberFormat.getInstance(Locale.getDefault());

        if (valute.getValuteAmount() != null) {
            valute.setValuteAmount(String.format(Locale.getDefault(), "%,.2f", (format.parse(s.toString()).doubleValue() * valute.getNominal() / valute.getValue())));
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