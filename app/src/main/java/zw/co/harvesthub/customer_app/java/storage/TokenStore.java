package zw.co.harvesthub.customer_app.java.storage;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Stores the DRF auth token + cached user profile. Uses the same
 * SharedPreferences key names as the Flutter customer app
 * (harvesthub_token / harvesthub_user) for naming consistency.
 */
public class TokenStore {

    private static final String PREFS_NAME = "harvesthub_prefs";
    private static final String KEY_TOKEN = "harvesthub_token";
    private static final String KEY_USER = "harvesthub_user";

    private final SharedPreferences prefs;

    public TokenStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isSignedIn() {
        return getToken() != null;
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public JSONObject getUser() {
        String raw = prefs.getString(KEY_USER, null);
        if (raw == null) return null;
        try {
            return new JSONObject(raw);
        } catch (JSONException e) {
            return null;
        }
    }

    public void save(String token, JSONObject user) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER, user.toString())
                .apply();
    }

    public void clear() {
        prefs.edit().remove(KEY_TOKEN).remove(KEY_USER).apply();
    }
}
