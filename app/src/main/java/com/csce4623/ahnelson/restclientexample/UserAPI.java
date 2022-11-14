package com.csce4623.ahnelson.restclientexample;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserAPI {
    @GET("users/{id}")
    Call<User> loadUserByUserId(@Path("id") int userId);
}
