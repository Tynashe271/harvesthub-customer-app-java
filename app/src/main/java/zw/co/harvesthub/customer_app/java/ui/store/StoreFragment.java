package zw.co.harvesthub.customer_app.java.ui.store;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import zw.co.harvesthub.customer_app.java.R;
import zw.co.harvesthub.customer_app.java.ui.Refreshable;
import zw.co.harvesthub.customer_app.java.models.CartLine;
import zw.co.harvesthub.customer_app.java.models.Product;
import zw.co.harvesthub.customer_app.java.models.WishlistEntry;
import zw.co.harvesthub.customer_app.java.network.ApiCallback;
import zw.co.harvesthub.customer_app.java.network.ApiClient;
import zw.co.harvesthub.customer_app.java.network.Endpoints;
import zw.co.harvesthub.customer_app.java.network.JsonUtils;
import zw.co.harvesthub.customer_app.java.storage.AppState;

public class StoreFragment extends Fragment implements ProductAdapter.Listener, Refreshable {

    /** Matches the Flutter app's MobileHome periodic 15s inventory refresh
     *  timer — keeps stock/prices fresh while the app is open, regardless of
     *  which tab the user is currently looking at. */
    private static final long AUTO_REFRESH_INTERVAL_MS = 15_000;

    private final List<Product> allProducts = new ArrayList<>();
    private ProductAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private final android.os.Handler autoRefreshHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable autoRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadAll();
            autoRefreshHandler.postDelayed(this, AUTO_REFRESH_INTERVAL_MS);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_store, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recycler = view.findViewById(R.id.recyclerProducts);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ProductAdapter(this);
        recycler.setAdapter(adapter);

        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(this::loadAll);

        TextInputEditText inputSearch = view.findViewById(R.id.inputSearch);
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        loadAll();
        autoRefreshHandler.postDelayed(autoRefreshRunnable, AUTO_REFRESH_INTERVAL_MS);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        autoRefreshHandler.removeCallbacks(autoRefreshRunnable);
    }

    @Override
    public void refresh() {
        loadAll();
    }

    private void loadAll() {
        swipeRefresh.setRefreshing(true);
        ApiClient.get().get(ApiClient.DJANGO_BASE, Endpoints.PRODUCTS, new ApiCallback() {
            @Override
            public void onSuccess(int statusCode, String body) {
                allProducts.clear();
                try {
                    JSONArray array = JsonUtils.extractArray(body);
                    for (int i = 0; i < array.length(); i++) {
                        allProducts.add(Product.fromJson(array.getJSONObject(i)));
                    }
                } catch (JSONException ignored) {
                    // Leave allProducts empty on unexpected shape.
                }
                adapter.submitList(allProducts);
                swipeRefresh.setRefreshing(false);
                loadWishlist();
                loadCart();
            }

            @Override
            public void onError(String message) {
                swipeRefresh.setRefreshing(false);
                toast(message);
            }
        });
    }

    private void loadWishlist() {
        ApiClient.get().get(ApiClient.DJANGO_BASE, Endpoints.WISHLIST, new ApiCallback() {
            @Override
            public void onSuccess(int statusCode, String body) {
                try {
                    JSONArray array = JsonUtils.extractArray(body);
                    for (int i = 0; i < array.length(); i++) {
                        WishlistEntry entry = WishlistEntry.fromJson(array.getJSONObject(i));
                        AppState.get().setWishlistEntry(entry.product.id, entry.wishlistItemId);
                    }
                    adapter.notifyDataSetChanged();
                } catch (JSONException ignored) {
                    // Non-fatal — wishlist indicators just won't be pre-filled this load.
                }
            }

            @Override
            public void onError(String message) {
                // Non-fatal for the store screen.
            }
        });
    }

    private void loadCart() {
        ApiClient.get().get(ApiClient.DJANGO_BASE, Endpoints.CART_ITEMS, new ApiCallback() {
            @Override
            public void onSuccess(int statusCode, String body) {
                try {
                    JSONArray array = JsonUtils.extractArray(body);
                    for (int i = 0; i < array.length(); i++) {
                        CartLine line = CartLine.fromJson(array.getJSONObject(i));
                        AppState.get().setCartQuantity(line.product.id, line.quantity);
                    }
                } catch (JSONException ignored) {
                    // Non-fatal for the store screen.
                }
            }

            @Override
            public void onError(String message) {
                // Non-fatal for the store screen.
            }
        });
    }

    private void filter(String query) {
        if (query == null || query.trim().isEmpty()) {
            adapter.submitList(allProducts);
            return;
        }
        String lower = query.trim().toLowerCase();
        List<Product> filtered = new ArrayList<>();
        for (Product p : allProducts) {
            if (p.name != null && p.name.toLowerCase().contains(lower)) {
                filtered.add(p);
            }
        }
        adapter.submitList(filtered);
    }

    @Override
    public void onAddToCart(Product product) {
        try {
            JSONObject body = new JSONObject();
            body.put("product_id", product.id);
            body.put("quantity", 1);
            ApiClient.get().post(ApiClient.DJANGO_BASE, Endpoints.CART_ITEMS, body, new ApiCallback() {
                @Override
                public void onSuccess(int statusCode, String responseBody) {
                    try {
                        JSONObject json = new JSONObject(responseBody);
                        AppState.get().setCartQuantity(product.id, json.optInt("quantity", 1));
                    } catch (JSONException ignored) {
                        AppState.get().setCartQuantity(product.id, AppState.get().getCartQuantity(product.id) + 1);
                    }
                    toast(product.name + " added to cart");
                }

                @Override
                public void onError(String message) {
                    toast(message);
                }
            });
        } catch (JSONException ignored) {
            // Never happens — fields are static.
        }
    }

    @Override
    public void onToggleWishlist(Product product) {
        boolean wishlisted = AppState.get().isWishlisted(product.id);
        if (wishlisted) {
            Integer wishlistItemId = AppState.get().getWishlistItemId(product.id);
            if (wishlistItemId == null) return;
            ApiClient.get().delete(ApiClient.DJANGO_BASE, Endpoints.wishlistItem(wishlistItemId), new ApiCallback() {
                @Override
                public void onSuccess(int statusCode, String body) {
                    AppState.get().removeFromWishlist(product.id);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onError(String message) {
                    toast(message);
                }
            });
        } else {
            try {
                JSONObject body = new JSONObject();
                body.put("product_id", product.id);
                ApiClient.get().post(ApiClient.DJANGO_BASE, Endpoints.WISHLIST, body, new ApiCallback() {
                    @Override
                    public void onSuccess(int statusCode, String responseBody) {
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            AppState.get().setWishlistEntry(product.id, json.optInt("id"));
                        } catch (JSONException ignored) {
                            // If parsing fails the icon just won't reflect it until next reload.
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(String message) {
                        toast(message);
                    }
                });
            } catch (JSONException ignored) {
                // Never happens — fields are static.
            }
        }
    }

    private void toast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
