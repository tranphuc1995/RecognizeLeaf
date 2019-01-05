package com.bachkhoa.recognize_leaf.retrofit;


import com.bachkhoa.recognize_leaf.model.ResponseCheck;
import com.bachkhoa.recognize_leaf.model.ResponseUpload;

import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;

/**
 * Created by tranphuc on 7/3/2017.
 */

public interface ApiInterface {
    @Multipart
    @POST("upload")
    Call<ResponseUpload> uploadImage(@Part("profile_pic\"; filename=\"123.jpeg") RequestBody file
            , @PartMap Map<String, RequestBody> params);

    @GET("check")
    Call<ResponseCheck> getResult(@Query("id") String id
    );


}
