package com.estimote.examples.demos;

import android.app.Application;

import com.estimote.sdk.EstimoteSDK;


public class BeaconTwitApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initializes Estimote SDK with  App ID and App Token
        EstimoteSDK.initialize(this, "rajesh-pisode-gmail-com-s--hmi", "8a809d69ea5130413c012630d0d225a5");

        // Configure verbose debug logging.
        EstimoteSDK.enableDebugLogging(true);
    }
}
