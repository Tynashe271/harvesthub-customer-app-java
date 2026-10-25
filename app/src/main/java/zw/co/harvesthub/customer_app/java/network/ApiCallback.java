package zw.co.harvesthub.customer_app.java.network;

/** Delivered on the main thread. onSuccess receives the raw response body text
 *  (caller decides whether to parse it as a JSONObject or JSONArray). */
public interface ApiCallback {
    void onSuccess(int statusCode, String body);
    void onError(String message);
}
