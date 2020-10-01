package com.iskhakoff.guessgame.model;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    private static final String PREF_NAME = "fourDigit";
    private static final String PREF_NAME_VALUE = "fourDigitValue";

    public static String getValue(Context context){
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(PREF_NAME_VALUE, "");
    }

    public static void setValue(Context context, String value){
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putString(PREF_NAME_VALUE, value).apply();
    }



}
