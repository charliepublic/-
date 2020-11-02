package com.example.myapplication;

import android.app.Application;

public class GlobalData {
    private static String PhoneNumber = "";

    public String getPhoneNumber(){
        return PhoneNumber;
    }
    public void setPhoneNumber(String c){
        PhoneNumber= c;
    }
}
