package zw.co.harvesthub.customer_app.java.models;

import org.json.JSONObject;

public class SupportTicket {
    public final int id;
    public final String subject;
    public final String message;
    public final String status;
    public final String createdAt;

    public SupportTicket(int id, String subject, String message, String status, String createdAt) {
        this.id = id;
        this.subject = subject;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static SupportTicket fromJson(JSONObject json) {
        return new SupportTicket(
                json.optInt("id"),
                json.optString("subject"),
                json.optString("message"),
                json.optString("status", "open"),
                json.optString("created_at", "")
        );
    }
}
