package zw.co.harvesthub.customer_app.java.ui.cart;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import zw.co.harvesthub.customer_app.java.R;
import zw.co.harvesthub.customer_app.java.models.CartLine;
import zw.co.harvesthub.customer_app.java.models.DeliverySettings;
import zw.co.harvesthub.customer_app.java.network.ApiCallback;
import zw.co.harvesthub.customer_app.java.network.ApiClient;
import zw.co.harvesthub.customer_app.java.network.Endpoints;
import zw.co.harvesthub.customer_app.java.network.JsonUtils;
import zw.co.harvesthub.customer_app.java.storage.AppState;
import zw.co.harvesthub.customer_app.java.ui.Refreshable;

public class CartFragment extends Fragment implements Refreshable {

    /** The store's WhatsApp ordering line — same number shown on the Profile
     *  and Shop location screens, digits-only for the wa.me deep link. */
    private static final String WHATSAPP_NUMBER = "263772910496";

    private final List<CartLine> lines = new ArrayList<>();
    private DeliverySettings deliverySettings = DeliverySettings.fallback();

    private LinearLayout cartItemsContainer;
    private TextView textEmpty;
    private TextView textSubtotal;
    private TextView textDeliveryFee;
    private TextView textTotal;
    private TextInputEditText inputShippingName;
    private TextInputEditText inputShippingPhone;
    private TextInputEditText inputShippingAddress;
    private View progress;
    private SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cartItemsContainer = view.findViewById(R.id.cartItemsContainer);
        textEmpty = view.findViewById(R.id.textEmpty);
        textSubtotal = view.findViewById(R.id.textSubtotal);
        textDeliveryFee = view.findViewById(R.id.textDeliveryFee);
        textTotal = view.findViewById(R.id.textTotal);
        inputShippingName = view.findViewById(R.id.inputShippingName);
        inputShippingPhone = view.findViewById(R.id.inputShippingPhone);
        inputShippingAddress = view.findViewById(R.id.inputShippingAddress);
        progress = view.findViewById(R.id.progress);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        swipeRefresh.setOnRefreshListener(this::loadAll);
        view.findViewById(R.id.btnPlaceOrder).setOnClickListener(v -> placeOrder());

        loadAll();
    }

    @Override
    public void refresh() {
        loadAll();
    }

    private void loadAll() {
        swipeRefresh.setRefreshing(true);
        ApiClient.get().get(ApiClient.DJANGO_BASE, Endpoints.DELIVERY_SETTINGS, new ApiCallback() {
            @Override
            public void onSuccess(int statusCode, String body) {
                try {
                    deliverySettings = DeliverySettings.fromJson(new JSONObject(body));
                } catch (JSONException ignored) {
                    deliverySettings = DeliverySettings.fallback();
                }
                loadCart();
            }

            @Override
            public void onError(String message) {
                deliverySettings = DeliverySettings.fallback();
                loadCart();
            }
        });
    }

    private void loadCart() {
        ApiClient.get().get(ApiClient.DJANGO_BASE, Endpoints.CART_ITEMS, new ApiCallback() {
            @Override
            public void onSuccess(int statusCode, String body) {
                swipeRefresh.setRefreshing(false);
                lines.clear();
                try {
                    JSONArray array = JsonUtils.extractArray(body);
                    for (int i = 0; i < array.length(); i++) {
                        lines.add(CartLine.fromJson(array.getJSONObject(i)));
                    }
                } catch (JSONException ignored) {
                    // Leave lines empty on unexpected shape.
                }
                renderLines();
                updateTotals();
            }

            @Override
            public void onError(String message) {
                swipeRefresh.setRefreshing(false);
                toast(message);
            }
        });
    }

    private void renderLines() {
        cartItemsContainer.removeAllViews();
        textEmpty.setVisibility(lines.isEmpty() ? View.VISIBLE : View.GONE);

        for (CartLine line : lines) {
            View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_cart_line, cartItemsContainer, false);
            TextView textName = row.findViewById(R.id.textName);
            TextView textQuantity = row.findViewById(R.id.textQuantity);
            TextView textLineSubtotal = row.findViewById(R.id.textSubtotal);
            ImageButton btnMinus = row.findViewById(R.id.btnMinus);
            ImageButton btnPlus = row.findViewById(R.id.btnPlus);

            textName.setText(line.product.name);
            textQuantity.setText(String.valueOf(line.quantity));
            textLineSubtotal.setText(String.format("$%.2f", line.subtotal));

            btnMinus.setOnClickListener(v -> changeQuantity(line, line.quantity - 1));
            btnPlus.setOnClickListener(v -> changeQuantity(line, line.quantity + 1));

            cartItemsContainer.addView(row);
        }
    }

    private void changeQuantity(CartLine line, int newQuantity) {
        if (newQuantity <= 0) {
            ApiClient.get().delete(ApiClient.DJANGO_BASE, Endpoints.cartItem(line.cartItemId), new ApiCallback() {
                @Override
                public void onSuccess(int statusCode, String body) {
                    AppState.get().removeFromCart(line.product.id);
                    loadCart();
                }

                @Override
                public void onError(String message) {
                    toast(message);
                }
            });
            return;
        }
        try {
            JSONObject body = new JSONObject();
            body.put("quantity", newQuantity);
            ApiClient.get().patch(ApiClient.DJANGO_BASE, Endpoints.cartItem(line.cartItemId), body, new ApiCallback() {
                @Override
                public void onSuccess(int statusCode, String responseBody) {
                    AppState.get().setCartQuantity(line.product.id, newQuantity);
                    loadCart();
                }

                @Override
                public void onError(String message) {
                    toast(message);
                }
            });
        } catch (JSONException ignored) {
            // Never happens — field is static.
        }
    }

    private double subtotal() {
        double sum = 0;
        for (CartLine line : lines) sum += line.subtotal;
        return sum;
    }

    private void updateTotals() {
        double subtotal = subtotal();
        double fee = deliveryFee();
        double total = subtotal + fee;

        textSubtotal.setText(String.format("Subtotal: $%.2f", subtotal));
        textDeliveryFee.setText(fee == 0 ? "Delivery: Free" : String.format("Delivery: $%.2f", fee));
        textTotal.setText(String.format("Total: $%.2f", total));
    }

    private void placeOrder() {
        if (lines.isEmpty()) {
            toast("Your cart is empty.");
            return;
        }
        String name = text(inputShippingName);
        String phone = text(inputShippingPhone);
        String address = text(inputShippingAddress);
        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            toast("Please fill in your delivery details.");
            return;
        }

        // Snapshot the lines/total now — the cart is cleared server-side as
        // part of checkout, so this fragment's own `lines` list is about to
        // be wiped by the loadAll() refresh that follows a successful order.
        List<CartLine> orderedLines = new ArrayList<>(lines);
        double orderedTotal = subtotal() + deliveryFee();

        setLoading(true);
        try {
            JSONObject body = new JSONObject();
            body.put("shipping_name", name);
            body.put("shipping_phone", phone);
            body.put("shipping_address", address);
            ApiClient.get().post(ApiClient.DJANGO_BASE, Endpoints.CHECKOUT, body, new ApiCallback() {
                @Override
                public void onSuccess(int statusCode, String responseBody) {
                    setLoading(false);
                    int orderId;
                    try {
                        orderId = new JSONObject(responseBody).optInt("id");
                    } catch (JSONException e) {
                        toast("Order placed, but the response couldn't be read.");
                        loadAll();
                        return;
                    }
                    AppState.get().clear();
                    loadAll();
                    openWhatsAppOrder(orderId, address, orderedLines, orderedTotal);
                }

                @Override
                public void onError(String message) {
                    setLoading(false);
                    toast(message);
                }
            });
        } catch (JSONException ignored) {
            setLoading(false);
        }
    }

    /** Opens WhatsApp with a prefilled message summarising the just-placed
     *  order, so the customer confirms and arranges payment with HarvestHub
     *  directly over WhatsApp instead of an in-app payment method. */
    private void openWhatsAppOrder(int orderId, String address, List<CartLine> orderedLines, double total) {
        StringBuilder message = new StringBuilder();
        message.append("Hi HarvestHub! I'd like to confirm my order ")
                .append(String.format("MAP-%06d", orderId)).append(":\n");
        for (CartLine line : orderedLines) {
            message.append("- ").append(line.quantity).append("x ").append(line.product.name)
                    .append(" ($").append(String.format("%.2f", line.subtotal)).append(")\n");
        }
        message.append("Delivery to: ").append(address).append('\n');
        message.append(String.format("Total: $%.2f", total));

        Uri uri = Uri.parse("https://wa.me/" + WHATSAPP_NUMBER + "?text=" + Uri.encode(message.toString()));
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (ActivityNotFoundException e) {
            toast("Order placed! Message us on WhatsApp at +263 77 291 0496 to confirm.");
        }
    }

    private double deliveryFee() {
        double subtotal = subtotal();
        return subtotal > 0 && subtotal < deliverySettings.freeThreshold ? deliverySettings.fee : 0;
    }

    private String text(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void toast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
