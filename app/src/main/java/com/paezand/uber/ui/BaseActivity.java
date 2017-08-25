package com.paezand.uber.ui;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;

public class BaseActivity extends Application {

    @Override
    public void onCreate() {
        super.onCreate();


        Parse.enableLocalDatastore(getApplicationContext());

        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId("d958a597ce8edb75cc730ec57a7c33b6d432afb0")
                .clientKey("78ebf0f3b62c8c9178184254837681b530fd9f0c")
                .server("http://ec2-18-220-92-6.us-east-2.compute.amazonaws.com:80/parse/")
                .build()
        );

//        ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);
    }
}
