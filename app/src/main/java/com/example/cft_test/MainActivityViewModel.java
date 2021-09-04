package com.example.cft_test;

import androidx.lifecycle.ViewModel;

import io.realm.Realm;

public class MainActivityViewModel extends ViewModel {

    final String TAG = "myLogs";
    final Realm realm = Realm.getDefaultInstance();

}
