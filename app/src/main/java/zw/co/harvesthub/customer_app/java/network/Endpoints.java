package zw.co.harvesthub.customer_app.java.network;

/** Relative paths against the Django API base URL (BuildConfig.HARVESTHUB_API_URL). */
public final class Endpoints {
    private Endpoints() {}

    public static final String LOGIN = "accounts/users/login/";
    public static final String REGISTER = "accounts/users/register/";
    public static final String PRODUCTS = "products/products/";
    public static final String ORDERS = "orders/";
    public static final String DELIVERY_SETTINGS = "orders/delivery-settings/";
    public static final String WISHLIST = "products/wishlist/";
    public static String wishlistItem(int id) { return "products/wishlist/" + id + "/"; }
    public static final String AI_CHAT = "ai/chat/";
    public static final String PAYMENTS_INITIATE = "payments/initiate/";
    public static String paymentStatus(int orderId) { return "payments/status/" + orderId + "/"; }
    public static final String CART_ITEMS = "cart/items/";
    public static String cartItem(int id) { return "cart/items/" + id + "/"; }
    public static final String CHECKOUT = "orders/checkout/";

    /** Relative paths against the Go API base URL (BuildConfig.HARVESTHUB_GO_API_URL). */
    public static final String GO_NOTIFICATIONS = "notifications";
    public static String goNotificationRead(int id) { return "notifications/" + id + "/read"; }
    public static final String GO_NOTIFICATIONS_READ_ALL = "notifications/read-all";
    public static final String GO_SUPPORT_TICKETS = "support/tickets";
}
