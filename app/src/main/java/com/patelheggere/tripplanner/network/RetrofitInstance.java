package com.patelheggere.tripplanner.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {
   // private static final String BASE_URL = "https://maps.googleapis.com/maps/";

    //private static final String BASE_URL = "http://patelheggere.epizy.com/KuduchiAPI/";
    private static final String BASE_URL = "http://prajeev.net/ktdcl/endpoints/";
   // private static final String BASE_URL = "http://kusavinibalaga.org/hamsa/endpoints/";
   // private static final String BASE_URL = "http://117.254.85.204:8086/socialwelfarenew/webresources/myresource/";

    private static Retrofit retrofit = null;

    public  void setClient(){

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        int cacheSize = 10 * 1024 * 1024; // 10 MB
        Cache cache = new Cache(new File("goldfarm"), cacheSize);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS).cache(cache).addInterceptor(interceptor).build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        this.retrofit= retrofit;

    }

    public  Retrofit getClient() {
        return retrofit;
    }

}
