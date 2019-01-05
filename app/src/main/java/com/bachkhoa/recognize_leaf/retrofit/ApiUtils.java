package com.bachkhoa.recognize_leaf.retrofit;


import com.bachkhoa.recognize_leaf.global.MethodStatic;

/**
 * Created by tranphuc on 7/3/2017.
 */

public class ApiUtils {
    public static ApiInterface getApiService(String domain) {
        return RetrofitClient.getClient(domain).create(ApiInterface.class);
    }
}
