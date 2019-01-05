package com.bachkhoa.recognize_leaf.global;

import android.content.Context;
import android.content.SharedPreferences;

import com.bachkhoa.recognize_leaf.retrofit.ApiUtils;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by tranp on 1/22/2018.
 */

public class MethodStatic {
    public static void createGlobal(String domain) {
        VariableStatic.mService = ApiUtils.getApiService(domain);
    }

    public static void setDomain(Context context, String domain) {
        SharedPreferences.Editor editor = context.getSharedPreferences("domain", MODE_PRIVATE).edit();
        editor.putString("domain", domain);
        editor.apply();
    }

    public static String getDomain(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("domain", MODE_PRIVATE);
        String domain = prefs.getString("domain", "");
        return domain;
    }
}
