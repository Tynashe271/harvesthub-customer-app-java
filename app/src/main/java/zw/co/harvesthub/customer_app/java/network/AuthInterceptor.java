package zw.co.harvesthub.customer_app.java.network;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import zw.co.harvesthub.customer_app.java.storage.TokenStore;

/** Adds "Authorization: Token <key>" to every request when a token is stored. */
public class AuthInterceptor implements Interceptor {

    private final TokenStore tokenStore;

    public AuthInterceptor(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String token = tokenStore.getToken();
        if (token == null) {
            return chain.proceed(original);
        }
        Request withAuth = original.newBuilder()
                .header("Authorization", "Token " + token)
                .build();
        return chain.proceed(withAuth);
    }
}
