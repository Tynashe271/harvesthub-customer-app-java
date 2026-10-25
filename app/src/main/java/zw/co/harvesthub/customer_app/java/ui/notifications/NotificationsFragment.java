package zw.co.harvesthub.customer_app.java.ui.notifications;

import android.graphics.Typeface;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import zw.co.harvesthub.customer_app.java.R;
import zw.co.harvesthub.customer_app.java.models.NotificationItem;
import zw.co.harvesthub.customer_app.java.network.ApiCallback;
import zw.co.harvesthub.customer_app.java.network.ApiClient;
import zw.co.harvesthub.customer_app.java.network.Endpoints;
import zw.co.harvesthub.customer_app.java.ui.Refreshable;

/** Backed by the new Go service — this screen has no Flutter-app equivalent. */
public class NotificationsFragment extends Fragment implements Refreshable {

    private LinearLayout notificationsContainer;
    private SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        notificationsContainer = view.findViewById(R.id.notificationsContainer);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(this::loadNotifications);
        view.findViewById(R.id.btnMarkAllRead).setOnClickListener(v -> markAllRead());
        loadNotifications();
    }

    @Override
    public void refresh() {
        loadNotifications();
    }

    private void loadNotifications() {
        swipeRefresh.setRefreshing(true);
        ApiClient.get().get(ApiClient.GO_BASE, Endpoints.GO_NOTIFICATIONS, new ApiCallback() {
            @Override
            public void onSuccess(int statusCode, String body) {
                swipeRefresh.setRefreshing(false);
                notificationsContainer.removeAllViews();
                try {
                    JSONObject json = new JSONObject(body);
                    JSONArray array = json.getJSONArray("results");
                    for (int i = 0; i < array.length(); i++) {
                        renderNotification(NotificationItem.fromJson(array.getJSONObject(i)));
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

    private void renderNotification(NotificationItem item) {
        View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_notification, notificationsContainer, false);
        TextView textTitle = row.findViewById(R.id.textTitle);
        TextView textBody = row.findViewById(R.id.textBody);
        TextView textCreatedAt = row.findViewById(R.id.textCreatedAt);

        textTitle.setText(item.title);
        textTitle.setTypeface(null, item.isRead ? Typeface.NORMAL : Typeface.BOLD);
        textBody.setText(item.body);
        textCreatedAt.setText(item.createdAt);

        if (!item.isRead) {
            row.setOnClickListener(v -> markRead(item.id, textTitle));
        }

        notificationsContainer.addView(row);
    }

    private void markRead(int id, TextView textTitle) {
        ApiClient.get().patch(ApiClient.GO_BASE, Endpoints.goNotificationRead(id), new JSONObject(), new ApiCallback() {
            @Override
            public void onSuccess(int statusCode, String body) {
                textTitle.setTypeface(null, Typeface.NORMAL);
            }

            @Override
            public void onError(String message) {
                toast(message);
            }
        });
    }

    private void markAllRead() {
        ApiClient.get().post(ApiClient.GO_BASE, Endpoints.GO_NOTIFICATIONS_READ_ALL, new JSONObject(), new ApiCallback() {
            @Override
            public void onSuccess(int statusCode, String body) {
                loadNotifications();
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
