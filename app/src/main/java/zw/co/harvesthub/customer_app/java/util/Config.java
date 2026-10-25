package zw.co.harvesthub.customer_app.java.util;

import zw.co.harvesthub.customer_app.java.network.ApiClient;

/** Thin named accessors over the BuildConfig-derived base URLs, so call sites
 *  read "Config.djangoBase()" instead of reaching into ApiClient directly. */
public final class Config {
    private Config() {}

    public static String djangoBase() {
        return ApiClient.DJANGO_BASE;
    }

    public static String goBase() {
        return ApiClient.GO_BASE;
    }
}
