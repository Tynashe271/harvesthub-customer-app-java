package zw.co.harvesthub.customer_app.java.models;

import org.json.JSONObject;

public class NotificationItem {
    public final int id;
    public final String kind;
    public final String title;
    public final String body;
    public final boolean isRead;
    public final String createdAt;

    public NotificationItem(int id, String kind, String title, String body, boolean isRead, String createdAt) {
        this.id = id;
        this.kind = kind;
        this.title = title;
        this.body = body;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public static NotificationItem fromJson(JSONObject json) {
        return new NotificationItem(
                json.optInt("id"),
                json.optString("kind"),
                json.optString("title"),
                json.optString("body"),
                json.optBoolean("is_read", false),
                json.optString("created_at", "")
        );
    }
}
