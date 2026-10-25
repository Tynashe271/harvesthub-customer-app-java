package zw.co.harvesthub.customer_app.java.ui.support;

import android.os.Bundle;
import android.text.TextUtils;
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

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import zw.co.harvesthub.customer_app.java.R;
import zw.co.harvesthub.customer_app.java.models.SupportTicket;
import zw.co.harvesthub.customer_app.java.network.ApiCallback;
import zw.co.harvesthub.customer_app.java.network.ApiClient;
import zw.co.harvesthub.customer_app.java.network.Endpoints;
import zw.co.harvesthub.customer_app.java.ui.Refreshable;

/** Backed by the new Go service — this screen has no Flutter-app equivalent. */
public class SupportFragment extends Fragment implements Refreshable {

    private TextInputEditText inputSubject;
    private TextInputEditText inputMessage;
    private LinearLayout ticketsContainer;
    private SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_support, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        inputSubject = view.findViewById(R.id.inputSubject);
        inputMessage = view.findViewById(R.id.inputMessage);
        ticketsContainer = view.findViewById(R.id.ticketsContainer);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);

        swipeRefresh.setOnRefreshListener(this::loadTickets);
        view.findViewById(R.id.btnSubmit).setOnClickListener(v -> submitTicket());

        loadTickets();
    }

    @Override
    public void refresh() {
        loadTickets();
    }

    private void submitTicket() {
        String subject = text(inputSubject);
        String message = text(inputMessage);
        if (TextUtils.isEmpty(subject) || TextUtils.isEmpty(message)) {
            toast("Please fill in both fields.");
            return;
        }
        try {
            JSONObject body = new JSONObject();
            body.put("subject", subject);
            body.put("message", message);
            ApiClient.get().post(ApiClient.GO_BASE, Endpoints.GO_SUPPORT_TICKETS, body, new ApiCallback() {
                @Override
                public void onSuccess(int statusCode, String responseBody) {
                    inputSubject.setText("");
                    inputMessage.setText("");
                    toast("Ticket submitted.");
                    loadTickets();
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

    private void loadTickets() {
        swipeRefresh.setRefreshing(true);
        ApiClient.get().get(ApiClient.GO_BASE, Endpoints.GO_SUPPORT_TICKETS, new ApiCallback() {
            @Override
            public void onSuccess(int statusCode, String body) {
                swipeRefresh.setRefreshing(false);
                ticketsContainer.removeAllViews();
                try {
                    JSONObject json = new JSONObject(body);
                    JSONArray array = json.getJSONArray("results");
                    for (int i = 0; i < array.length(); i++) {
                        renderTicket(SupportTicket.fromJson(array.getJSONObject(i)));
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

    private void renderTicket(SupportTicket ticket) {
        View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_support_ticket, ticketsContainer, false);
        ((TextView) row.findViewById(R.id.textSubject)).setText(ticket.subject);
        ((TextView) row.findViewById(R.id.textMessage)).setText(ticket.message);
        ((TextView) row.findViewById(R.id.textStatus)).setText(ticket.status);
        ticketsContainer.addView(row);
    }

    private String text(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    private void toast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
