package zw.co.harvesthub.customer_app.java.models;

import org.json.JSONObject;

public class DeliverySettings {
    public final double fee;
    public final double freeThreshold;
    public final String areas;
    public final int estimatedMinutes;
    public final String openingHours;
    public final String policy;

    public DeliverySettings(double fee, double freeThreshold, String areas, int estimatedMinutes,
                             String openingHours, String policy) {
        this.fee = fee;
        this.freeThreshold = freeThreshold;
        this.areas = areas;
        this.estimatedMinutes = estimatedMinutes;
        this.openingHours = openingHours;
        this.policy = policy;
    }

    public static DeliverySettings fallback() {
        return new DeliverySettings(5.0, 50.0, "Harare metro area", 55, "08:00 - 18:00", "Standard delivery policy.");
    }

    public static DeliverySettings fromJson(JSONObject json) {
        return new DeliverySettings(
                json.optDouble("delivery_fee", 5.0),
                json.optDouble("free_delivery_threshold", 50.0),
                json.optString("delivery_areas", "Harare metro area"),
                json.optInt("estimated_minutes", 55),
                json.optString("opening_hours", "08:00 - 18:00"),
                json.optString("delivery_policy", "Standard delivery policy.")
        );
    }
}
