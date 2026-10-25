package zw.co.harvesthub.customer_app.java.models;

import org.json.JSONObject;

/** Mirrors WishlistItemSerializer: {id, product, product_id, created_at}. */
public class WishlistEntry {
    public final int wishlistItemId;
    public final Product product;

    public WishlistEntry(int wishlistItemId, Product product) {
        this.wishlistItemId = wishlistItemId;
        this.product = product;
    }

    public static WishlistEntry fromJson(JSONObject json) {
        return new WishlistEntry(
                json.optInt("id"),
                Product.fromJson(json.optJSONObject("product"))
        );
    }
}
