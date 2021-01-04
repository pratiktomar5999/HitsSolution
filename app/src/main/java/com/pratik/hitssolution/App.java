package com.pratik.hitssolution;

import android.app.Application;
import com.parse.Parse;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
        .applicationId(getString(R.string.app_id))
        .clientKey(getString(R.string.app_client_key))
        .server(getString(R.string.app_server_url))
        .build());

    }
}
