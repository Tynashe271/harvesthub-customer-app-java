package zw.co.harvesthub.customer_app.java;

import android.app.Application;

import zw.co.harvesthub.customer_app.java.network.ApiClient;

public class HarvestHubApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApiClient.init(this);
    }
}
