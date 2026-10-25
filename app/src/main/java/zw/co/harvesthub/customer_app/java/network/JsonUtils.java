package zw.co.harvesthub.customer_app.java.network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class JsonUtils {
    private JsonUtils() {}

    /**
     * DRF's DEFAULT_PAGINATION_CLASS is PageNumberPagination, so every list
     * endpoint actually returns {"count","next","previous","results":[...]}
     * rather than a bare JSON array. This unwraps either shape so callers
     * don't each need their own fallback parsing.
     */
    public static JSONArray extractArray(String body) throws JSONException {
        try {
            return new JSONArray(body);
        } catch (JSONException bareArrayFailed) {
            JSONObject wrapped = new JSONObject(body);
            return wrapped.getJSONArray("results");
        }
    }
}
