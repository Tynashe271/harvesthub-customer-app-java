package zw.co.harvesthub.customer_app.java.ui.favorites;

import android.os.Bundle;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import zw.co.harvesthub.customer_app.java.R;
import zw.co.harvesthub.customer_app.java.models.Product;
import zw.co.harvesthub.customer_app.java.models.WishlistEntry;
import zw.co.harvesthub.customer_app.java.network.ApiCallback;
import zw.co.harvesthub.customer_app.java.network.ApiClient;
import zw.co.harvesthub.customer_app.java.network.Endpoints;
import zw.co.harvesthub.customer_app.java.network.JsonUtils;
import zw.co.harvesthub.customer_app.java.storage.AppState;
import zw.co.harvesthub.customer_app.java.ui.Refreshable;
import zw.co.harvesthub.customer_app.java.ui.store.ProductAdapter;

public class FavoritesFragment extends Fragment implements ProductAdapter.Listener, Refreshable {

    private final List<Product> favorites = new ArrayList<>();
    private ProductAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recycler = view.findViewById(R.id.recyclerFavorites);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ProductAdapter(this);
        recycler.setAdapter(adapter);

        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(this::loadWishlist);
        loadWishlist();
    }

    @Override
    public void refresh() {
        loadWishlist();
    }

    private void loadWishlist() {
        swipeRefresh.setRefreshing(true);
        ApiClient.get().get(ApiClient.DJANGO_BASE, Endpoints.WISHLIST, new ApiCallback() {
            @Override
            public void onSuccess(int statusCode, String body) {
                swipeRefresh.setRefreshing(false);
                favorites.clear();
                try {
                    JSONArray array = JsonUtils.extractArray(body);
                    for (int i = 0; i < array.length(); i++) {
                        WishlistEntry entry = WishlistEntry.fromJson(array.getJSONObject(i));
                        AppState.get().setWishlistEntry(entry.product.id, entry.wishlistItemId);
                        favorites.add(entry.product);
                    }
                } catch (JSONException ignored) {
                    // Leave favorites empty on unexpected shape.
                }
                adapter.submitList(favorites);
            }

            @Override
            public void onError(String message) {
                swipeRefresh.setRefreshing(false);
                toast(message);
            }
        });
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
        Integer wishlistItemId = AppState.get().getWishlistItemId(product.id);
        if (wishlistItemId == null) return;
        ApiClient.get().delete(ApiClient.DJANGO_BASE, Endpoints.wishlistItem(wishlistItemId), new ApiCallback() {
            @Override
            public void onSuccess(int statusCode, String body) {
                AppState.get().removeFromWishlist(product.id);
                favorites.remove(product);
                adapter.submitList(favorites);
            }

            @Override
            public void onError(String message) {
                toast(message);
            }
        });
    }

    private void toast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
