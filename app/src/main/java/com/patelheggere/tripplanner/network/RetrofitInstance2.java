package com.patelheggere.tripplanner.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance2 {
   // private static final String BASE_URL = "https://maps.googleapis.com/maps/";

    //private static final String BASE_URL = "http://patelheggere.epizy.com/KuduchiAPI/";
    private static final String BASE_URL = "http://www.prajeev.net/API/";
   // private static final String BASE_URL = "http://kusavinibalaga.org/hamsa/endpoints/";
   // private static final String BASE_URL = " https://kgis.ksrsac.in:9000/genericwebservices/ws/";


    private static Retrofit retrofit = null;

    public  void setClient(){

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        int cacheSize = 10 * 1024 * 1024; // 10 MB
        Cache cache = new Cache(new File("goldfarm"), cacheSize);

        OkHttpClient client = new OkHttpClient.Builder().cache(cache).addInterceptor(interceptor).build();

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
