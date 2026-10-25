package zw.co.harvesthub.customer_app.java.storage;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Local-only free-text shopping list. Never synced to any backend — this
 * matches the Flutter customer app's behavior exactly (see main.dart's
 * ShoppingListPage, which only persists to SharedPreferences).
 */
public class ShoppingListStore {

    private static final String PREFS_NAME = "harvesthub_prefs";
    private static final String KEY_SHOPPING_LIST = "harvesthub_shopping_list";

    private final SharedPreferences prefs;

    public ShoppingListStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public List<String> getItems() {
        List<String> items = new ArrayList<>();
        String raw = prefs.getString(KEY_SHOPPING_LIST, null);
        if (raw == null) return items;
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                items.add(array.optString(i));
            }
        } catch (Exception ignored) {
            // Corrupt stored value — treat as empty rather than crashing.
        }
        return items;
    }

    public void saveItems(List<String> items) {
        JSONArray array = new JSONArray();
        for (String item : items) {
            array.put(item);
        }
        prefs.edit().putString(KEY_SHOPPING_LIST, array.toString()).apply();
    }

    public void addItem(String item) {
        List<String> items = getItems();
        items.add(item);
        saveItems(items);
    }

    public void removeItem(int index) {
        List<String> items = getItems();
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            saveItems(items);
        }
    }
}
