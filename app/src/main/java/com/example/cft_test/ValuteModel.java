package com.example.cft_test;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ValuteModel extends RealmObject {

    @PrimaryKey
    private int id;
    private String charCode;
    private int nominal;
    private String name;
    private float value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCharCode() {
        return charCode;
    }

    public void setCharCode(String charCode) {
        this.charCode = charCode;
    }

    public int getNominal() {
        return nominal;
    }

    public void setNominal(int nominal) {
        this.nominal = nominal;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }


}
