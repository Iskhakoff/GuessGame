package com.iskhakoff.guessgame.model;

import android.content.Context;

public class RepositoryPreferences {

    Context context;

    public RepositoryPreferences(Context ctx) {
        this.context = ctx;
    }

    public String getValueFromPrefs(){
        return Preferences.getValue(context);
    }

    public void setValueToPrefs(String value){
        Preferences.setValue(context, value);
    }

    public void deleteValueFromPrefs(){
        Preferences.delete(context);
    }
}
