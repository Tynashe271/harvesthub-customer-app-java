package zw.co.harvesthub.customer_app.java.models;

import org.json.JSONObject;

/** Mirrors CartItemSerializer: {id, product, product_id, quantity, subtotal, created_at, updated_at}. */
public class CartLine {
    public final int cartItemId;
    public final Product product;
    public final int quantity;
    public final double subtotal;

    public CartLine(int cartItemId, Product product, int quantity, double subtotal) {
        this.cartItemId = cartItemId;
        this.product = product;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    public static CartLine fromJson(JSONObject json) {
        return new CartLine(
                json.optInt("id"),
                Product.fromJson(json.optJSONObject("product")),
                json.optInt("quantity", 0),
                json.optDouble("subtotal", 0)
        );
    }
}
