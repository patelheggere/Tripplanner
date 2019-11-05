package com.patelheggere.tripplanner;

import android.app.Application;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class BaseApplication extends Application {
    private static BaseApplication mInstance;
    private static DatabaseReference databaseReference;
    private static StorageReference firebaseStorage;


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        // ApiClient.intialise();
       /* if(isDeve()) {
            ApiClient.setBaseUrl(AppConstants.appBaseUrlDev);
        }
        else
        {
            ApiClient.setBaseUrl(AppConstants.appBaseUrl);

        }*/

    }

    public static synchronized DatabaseReference getFireBaseRef()
    {
        if(BuildConfig.DEBUG) {
            System.out.println("debug");
            Log.d("", "getFireBaseRef: Debug");
            databaseReference = FirebaseDatabase.getInstance().getReference().child("test");
        }
        else {
            Log.d("", "getFireBaseRef: release");
            databaseReference = FirebaseDatabase.getInstance().getReference().child("prod");
        }
        return databaseReference;
    }
    public static synchronized StorageReference getFirebaseStorage()
    {
        if(BuildConfig.DEBUG) {
            System.out.println("debug");
            Log.d("", "getFireBaseRef: Debug");
            firebaseStorage = FirebaseStorage.getInstance().getReference().child("test");
        }
        else {
            Log.d("", "getFireBaseRef: release");
            firebaseStorage = FirebaseStorage.getInstance().getReference().child("prod");
        }
        return firebaseStorage;
    }

    public static synchronized BaseApplication getInstance() {
        return mInstance;
    }

}