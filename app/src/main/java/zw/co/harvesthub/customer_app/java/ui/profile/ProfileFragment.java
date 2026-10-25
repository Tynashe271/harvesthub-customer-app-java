package zw.co.harvesthub.customer_app.java.ui.profile;

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
import zw.co.harvesthub.customer_app.java.storage.TokenStore;
import zw.co.harvesthub.customer_app.java.ui.main.MainActivity;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textName = view.findViewById(R.id.textName);
        TextView textEmail = view.findViewById(R.id.textEmail);

        JSONObject user = new TokenStore(requireContext()).getUser();
        if (user != null) {
            String firstName = user.optString("first_name", "");
            String lastName = user.optString("last_name", "");
            String fullName = (firstName + " " + lastName).trim();
            textName.setText(fullName.isEmpty() ? user.optString("username", "") : fullName);
            textEmail.setText(user.optString("email", ""));
        }

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).signOut();
            }
        });
    }
}
