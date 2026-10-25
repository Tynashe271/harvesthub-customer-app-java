package zw.co.harvesthub.customer_app.java.models;

import org.json.JSONObject;

public class OrderItem {
    public final String productName;
    public final double unitPrice;
    public final int quantity;

    public OrderItem(String productName, double unitPrice, int quantity) {
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public static OrderItem fromJson(JSONObject json) {
        return new OrderItem(
                json.optString("product_name"),
                json.optDouble("unit_price", 0),
                json.optInt("quantity", 0)
        );
    }
}
