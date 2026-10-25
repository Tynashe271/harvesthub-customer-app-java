package zw.co.harvesthub.customer_app.java.ui.orders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import zw.co.harvesthub.customer_app.java.R;
import zw.co.harvesthub.customer_app.java.models.CustomerOrder;
import zw.co.harvesthub.customer_app.java.models.OrderItem;
import zw.co.harvesthub.customer_app.java.network.ApiCallback;
import zw.co.harvesthub.customer_app.java.network.ApiClient;
import zw.co.harvesthub.customer_app.java.network.Endpoints;
import zw.co.harvesthub.customer_app.java.network.JsonUtils;
import zw.co.harvesthub.customer_app.java.ui.Refreshable;

/** Mirrors the Flutter app's OrdersPage: order history with expandable
 *  tracking progress and an EcoCash "check payment" action. */
public class OrdersFragment extends Fragment implements Refreshable {

    private static final List<String> TRACKING_STAGES =
            Arrays.asList("pending", "paid", "processing", "shipped", "delivered");

    private LinearLayout ordersContainer;
    private SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ordersContainer = view.findViewById(R.id.ordersContainer);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(this::loadOrders);
        loadOrders();
    }

    @Override
    public void refresh() {
        loadOrders();
    }

    private void loadOrders() {
        swipeRefresh.setRefreshing(true);
        ApiClient.get().get(ApiClient.DJANGO_BASE, Endpoints.ORDERS, new ApiCallback() {
            @Override
            public void onSuccess(int statusCode, String body) {
                swipeRefresh.setRefreshing(false);
                ordersContainer.removeAllViews();
                try {
                    JSONArray array = JsonUtils.extractArray(body);
                    for (int i = 0; i < array.length(); i++) {
                        renderOrder(CustomerOrder.fromJson(array.getJSONObject(i)));
                    }
                } catch (JSONException ignored) {
                    // Leave the list empty on unexpected shape.
                }
            }

            @Override
            public void onError(String message) {
                swipeRefresh.setRefreshing(false);
                toast(message);
            }
        });
    }

    private void renderOrder(CustomerOrder order) {
        View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_order, ordersContainer, false);
        TextView textOrderNumber = row.findViewById(R.id.textOrderNumber);
        TextView textStatus = row.findViewById(R.id.textStatus);
        TextView textTotal = row.findViewById(R.id.textTotal);
        TextView textDate = row.findViewById(R.id.textDate);
        TextView textTracking = row.findViewById(R.id.textTracking);
        TextView textItems = row.findViewById(R.id.textItems);
        MaterialButton btnCheckPayment = row.findViewById(R.id.btnCheckPayment);

        textOrderNumber.setText(order.number());
        textStatus.setText(order.status);
        textTotal.setText(String.format("Total: $%.2f", order.total));
        textDate.setText(formatDate(order.createdAt));
        textTracking.setText(trackingLine(order.status));

        StringBuilder itemsText = new StringBuilder();
        for (OrderItem item : order.items) {
            if (itemsText.length() > 0) itemsText.append('\n');
            itemsText.append(item.quantity).append("x ").append(item.productName);
        }
        textItems.setText(itemsText.toString());

        boolean canCheckPayment = "EcoCash".equalsIgnoreCase(order.paymentMethod) && !"paid".equalsIgnoreCase(order.paymentStatus);
        btnCheckPayment.setVisibility(canCheckPayment ? View.VISIBLE : View.GONE);
        btnCheckPayment.setOnClickListener(v -> checkPayment(order.id, textStatus, textTracking));

        ordersContainer.addView(row);
    }

    /** Avoids java.time (not available without desugaring at minSdk 24) —
     *  just takes the date portion of the ISO-8601 timestamp DRF returns. */
    private String formatDate(String isoTimestamp) {
        if (isoTimestamp == null || isoTimestamp.length() < 10) return "";
        return "Placed " + isoTimestamp.substring(0, 10);
    }

    private String trackingLine(String status) {
        int currentIndex = TRACKING_STAGES.indexOf(status == null ? "" : status.toLowerCase());
        if (currentIndex < 0) return status;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < TRACKING_STAGES.size(); i++) {
            if (i > 0) sb.append(i <= currentIndex ? " > " : "  ");
            sb.append(i <= currentIndex ? "[" + TRACKING_STAGES.get(i) + "]" : TRACKING_STAGES.get(i));
        }
        return sb.toString();
    }

    private void checkPayment(int orderId, TextView textStatus, TextView textTracking) {
        ApiClient.get().get(ApiClient.DJANGO_BASE, Endpoints.paymentStatus(orderId), new ApiCallback() {
            @Override
            public void onSuccess(int statusCode, String body) {
                try {
                    JSONObject json = new JSONObject(body);
                    boolean paid = json.optBoolean("paid", false);
                    if (paid) {
                        textStatus.setText("paid");
                        textTracking.setText(trackingLine("paid"));
                        toast("Payment confirmed!");
                    } else {
                        toast("Payment status: " + json.optString("status", "pending"));
                    }
                } catch (JSONException e) {
                    toast("Could not read payment status.");
                }
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
