package zw.co.harvesthub.customer_app.java.ui.location;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import zw.co.harvesthub.customer_app.java.R;
import zw.co.harvesthub.customer_app.java.models.DeliverySettings;
import zw.co.harvesthub.customer_app.java.network.ApiCallback;
import zw.co.harvesthub.customer_app.java.network.ApiClient;
import zw.co.harvesthub.customer_app.java.network.Endpoints;
import zw.co.harvesthub.customer_app.java.ui.Refreshable;

/** Mostly-static store info screen; opening hours come from the delivery
 *  settings endpoint, matching the Flutter app's LocationPage. */
public class LocationFragment extends Fragment implements Refreshable {

    private static final String STORE_ADDRESS = "123 Main Street, Harare, Zimbabwe";
    private static final String WHATSAPP_CONTACT = "WhatsApp us: +263 77 123 4567";

    private TextView textAddress;
    private TextView textHours;
    private TextView textWhatsapp;
    private TextView textPolicy;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textAddress = view.findViewById(R.id.textAddress);
        textHours = view.findViewById(R.id.textHours);
        textWhatsapp = view.findViewById(R.id.textWhatsapp);
        textPolicy = view.findViewById(R.id.textPolicy);

        textAddress.setText(STORE_ADDRESS);
        textWhatsapp.setText(WHATSAPP_CONTACT);
        applyDeliverySettings(DeliverySettings.fallback());
        loadDeliverySettings();
    }

    @Override
    public void refresh() {
        loadDeliverySettings();
    }

    private void loadDeliverySettings() {
        ApiClient.get().get(ApiClient.DJANGO_BASE, Endpoints.DELIVERY_SETTINGS, new ApiCallback() {
            @Override
            public void onSuccess(int statusCode, String body) {
                try {
                    applyDeliverySettings(DeliverySettings.fromJson(new JSONObject(body)));
                } catch (Exception ignored) {
                    // Keep the fallback values already shown.
                }
            }

            @Override
            public void onError(String message) {
                // Keep the fallback values already shown.
            }
        });
    }

    private void applyDeliverySettings(DeliverySettings settings) {
        textHours.setText("Opening hours: " + settings.openingHours);
        textPolicy.setText(settings.policy);
    }
}
