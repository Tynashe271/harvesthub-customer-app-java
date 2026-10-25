package zw.co.harvesthub.customer_app.java.ui.splash;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import zw.co.harvesthub.customer_app.java.R;
import zw.co.harvesthub.customer_app.java.storage.TokenStore;
import zw.co.harvesthub.customer_app.java.ui.auth.AuthActivity;
import zw.co.harvesthub.customer_app.java.ui.main.MainActivity;

/** Mirrors the Flutter app's SplashScreen: restores the session from local
 *  storage only (no server round trip) and routes to Auth or Main. */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TokenStore tokenStore = new TokenStore(this);
        Class<?> destination = tokenStore.isSignedIn() ? MainActivity.class : AuthActivity.class;
        startActivity(new Intent(this, destination));
        finish();
    }
}
