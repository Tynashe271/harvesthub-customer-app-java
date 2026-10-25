package zw.co.harvesthub.customer_app.java.storage;

import java.util.HashMap;
import java.util.Map;

/**
 * In-memory, runtime-only session state shared across fragments — the direct
 * analogue of the Flutter app's cart/wishlist representation (plain
 * productId -> quantity / productId -> wishlistItemId maps derived from raw
 * API JSON, held in the MobileHome widget's state and never persisted to
 * disk). Cart and wishlist contents are always re-fetched from the Django
 * API; this cache only lets the Store screen render "in cart" / "favourited"
 * indicators without an extra round trip per product.
 */
public class AppState {

    private static final AppState INSTANCE = new AppState();

    public static AppState get() {
        return INSTANCE;
    }

    private final Map<Integer, Integer> cartQuantityByProductId = new HashMap<>();
    private final Map<Integer, Integer> wishlistItemIdByProductId = new HashMap<>();

    private AppState() {}

    public void setCartQuantity(int productId, int quantity) {
        cartQuantityByProductId.put(productId, quantity);
    }

    public void removeFromCart(int productId) {
        cartQuantityByProductId.remove(productId);
    }

    public int getCartQuantity(int productId) {
        Integer q = cartQuantityByProductId.get(productId);
        return q != null ? q : 0;
    }

    public void setWishlistEntry(int productId, int wishlistItemId) {
        wishlistItemIdByProductId.put(productId, wishlistItemId);
    }

    public void removeFromWishlist(int productId) {
        wishlistItemIdByProductId.remove(productId);
    }

    public boolean isWishlisted(int productId) {
        return wishlistItemIdByProductId.containsKey(productId);
    }

    public Integer getWishlistItemId(int productId) {
        return wishlistItemIdByProductId.get(productId);
    }

    public void clear() {
        cartQuantityByProductId.clear();
        wishlistItemIdByProductId.clear();
    }
}
