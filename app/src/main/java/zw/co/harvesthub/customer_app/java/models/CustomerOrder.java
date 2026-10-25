package zw.co.harvesthub.customer_app.java.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CustomerOrder {
    public final int id;
    public final double total;
    public final String status;
    public final String paymentMethod;
    public final String paymentStatus;
    public final String shippingName;
    public final String shippingPhone;
    public final String shippingAddress;
    public final List<OrderItem> items;
    public final String createdAt;
    public final String updatedAt;

    public CustomerOrder(int id, double total, String status, String paymentMethod, String paymentStatus,
                          String shippingName, String shippingPhone, String shippingAddress, List<OrderItem> items,
                          String createdAt, String updatedAt) {
        this.id = id;
        this.total = total;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.shippingName = shippingName;
        this.shippingPhone = shippingPhone;
        this.shippingAddress = shippingAddress;
        this.items = items;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Matches the Flutter app's "MAP-000123" order number format. */
    public String number() {
        return String.format("MAP-%06d", id);
    }

    public static CustomerOrder fromJson(JSONObject json) {
        List<OrderItem> items = new ArrayList<>();
        JSONArray itemsArray = json.optJSONArray("items");
        if (itemsArray != null) {
            for (int i = 0; i < itemsArray.length(); i++) {
                items.add(OrderItem.fromJson(itemsArray.optJSONObject(i)));
            }
        }
        return new CustomerOrder(
                json.optInt("id"),
                json.optDouble("total", 0),
                json.optString("status", "pending"),
                json.optString("payment_method", ""),
                json.optString("payment_status", "unpaid"),
                json.optString("shipping_name", ""),
                json.optString("shipping_phone", ""),
                json.optString("shipping_address", ""),
                items,
                json.optString("created_at", ""),
                json.optString("updated_at", "")
        );
    }
}
