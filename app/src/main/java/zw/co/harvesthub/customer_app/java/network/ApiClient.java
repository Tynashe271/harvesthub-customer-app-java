package zw.co.harvesthub.customer_app.java.network;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import zw.co.harvesthub.customer_app.java.BuildConfig;
import zw.co.harvesthub.customer_app.java.storage.TokenStore;

/** Thin OkHttp wrapper: no Retrofit/Gson, matching the small, fully-enumerated
 *  API surface and mirroring the original Flutter app's own plain `http` + manual
 *  JSON decode approach. */
public class ApiClient {

    public static final String DJANGO_BASE = BuildConfig.HARVESTHUB_API_URL;
    public static final String GO_BASE = BuildConfig.HARVESTHUB_GO_API_URL;

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static ApiClient instance;

    private final OkHttpClient client;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ApiClient(Context context) {
        TokenStore tokenStore = new TokenStore(context);
        client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(tokenStore))
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
    }

    public static ApiClient get() {
        if (instance == null) {
            throw new IllegalStateException("ApiClient.init(context) must be called first (e.g. in Application.onCreate)");
        }
        return instance;
    }

    /** Joins baseUrl and path with exactly one "/" between them, regardless of
     *  whether either side already has one — a bare string concatenation
     *  silently produces a wrong (but still "valid-looking") URL otherwise,
     *  e.g. ".../api/v1" + "accounts/..." = ".../api/v1accounts/..." (404). */
    private static String join(String baseUrl, String path) {
        boolean baseHasSlash = baseUrl.endsWith("/");
        boolean pathHasSlash = path.startsWith("/");
        if (baseHasSlash && pathHasSlash) return baseUrl + path.substring(1);
        if (!baseHasSlash && !pathHasSlash) return baseUrl + "/" + path;
        return baseUrl + path;
    }

    public void get(String baseUrl, String path, ApiCallback callback) {
        Request request = new Request.Builder().url(join(baseUrl, path)).get().build();
        enqueue(request, callback);
    }

    public void post(String baseUrl, String path, JSONObject body, ApiCallback callback) {
        RequestBody requestBody = RequestBody.create(body.toString(), JSON);
        Request request = new Request.Builder().url(join(baseUrl, path)).post(requestBody).build();
        enqueue(request, callback);
    }

    public void patch(String baseUrl, String path, JSONObject body, ApiCallback callback) {
        RequestBody requestBody = RequestBody.create(body.toString(), JSON);
        Request request = new Request.Builder().url(join(baseUrl, path)).patch(requestBody).build();
        enqueue(request, callback);
    }

    public void put(String baseUrl, String path, JSONObject body, ApiCallback callback) {
        RequestBody requestBody = RequestBody.create(body.toString(), JSON);
        Request request = new Request.Builder().url(join(baseUrl, path)).put(requestBody).build();
        enqueue(request, callback);
    }

    public void delete(String baseUrl, String path, ApiCallback callback) {
        Request request = new Request.Builder().url(join(baseUrl, path)).delete().build();
        enqueue(request, callback);
    }

    private void enqueue(Request request, ApiCallback callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError(callback, e.getMessage() != null ? e.getMessage() : "Network error");
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String bodyText = response.body() != null ? response.body().string() : "";
                    int code = response.code();
                    if (response.isSuccessful()) {
                        postSuccess(callback, code, bodyText);
                    } else {
                        postError(callback, extractErrorMessage(bodyText, code));
                    }
                } catch (IOException e) {
                    postError(callback, "Failed to read response");
                } finally {
                    response.close();
                }
            }
        });
    }

    private String extractErrorMessage(String bodyText, int code) {
        try {
            JSONObject json = new JSONObject(bodyText);
            if (json.has("detail")) return json.getString("detail");
            if (json.has("error")) return json.getString("error");
        } catch (Exception ignored) {
            // Body wasn't a JSON object with detail/error — fall through.
        }
        return "Request failed (" + code + ")";
    }

    private void postSuccess(ApiCallback callback, int code, String body) {
        mainHandler.post(() -> callback.onSuccess(code, body));
    }

    private void postError(ApiCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}
