package zw.co.harvesthub.customer_app.java.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import zw.co.harvesthub.customer_app.java.R;
import zw.co.harvesthub.customer_app.java.network.ApiCallback;
import zw.co.harvesthub.customer_app.java.network.ApiClient;
import zw.co.harvesthub.customer_app.java.network.Endpoints;
import zw.co.harvesthub.customer_app.java.storage.TokenStore;
import zw.co.harvesthub.customer_app.java.ui.main.MainActivity;

/** Combined sign-in / sign-up screen, mirroring the Flutter app's single
 *  AuthScreen with a mode toggle instead of two separate screens. */
public class AuthActivity extends AppCompatActivity {

    private boolean signupMode = false;

    private View signupFieldsContainer;
    private View inputPassword2Layout;
    private TextInputEditText inputFirstName;
    private TextInputEditText inputLastName;
    private TextInputEditText inputEmail;
    private TextInputEditText inputPhone;
    private TextInputEditText inputUsername;
    private TextInputEditText inputPassword;
    private TextInputEditText inputPassword2;
    private TextView textError;
    private MaterialButton btnSubmit;
    private View progress;

    private TokenStore tokenStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        tokenStore = new TokenStore(this);

        signupFieldsContainer = findViewById(R.id.signupFieldsContainer);
        inputPassword2Layout = findViewById(R.id.inputPassword2Layout);
        inputFirstName = findViewById(R.id.inputFirstName);
        inputLastName = findViewById(R.id.inputLastName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPhone = findViewById(R.id.inputPhone);
        inputUsername = findViewById(R.id.inputUsername);
        inputPassword = findViewById(R.id.inputPassword);
        inputPassword2 = findViewById(R.id.inputPassword2);
        textError = findViewById(R.id.textError);
        btnSubmit = findViewById(R.id.btnSubmit);
        progress = findViewById(R.id.progress);

        findViewById(R.id.btnLoginMode).setOnClickListener(v -> setMode(false));
        findViewById(R.id.btnSignupMode).setOnClickListener(v -> setMode(true));
        btnSubmit.setOnClickListener(v -> submit());

        setMode(false);
    }

    private void setMode(boolean signup) {
        signupMode = signup;
        signupFieldsContainer.setVisibility(signup ? View.VISIBLE : View.GONE);
        inputPassword2Layout.setVisibility(signup ? View.VISIBLE : View.GONE);
        btnSubmit.setText(signup ? getString(R.string.action_signup) : getString(R.string.action_login));
        hideError();
    }

    private void submit() {
        hideError();
        String username = text(inputUsername);
        String password = text(inputPassword);

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            showError("Username and password are required.");
            return;
        }

        if (signupMode) {
            submitSignup(username, password);
        } else {
            submitLogin(username, password);
        }
    }

    private void submitLogin(String username, String password) {
        try {
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("password", password);
            setLoading(true);
            ApiClient.get().post(ApiClient.DJANGO_BASE, Endpoints.LOGIN, body, new ApiCallback() {
                @Override
                public void onSuccess(int statusCode, String responseBody) {
                    setLoading(false);
                    handleAuthSuccess(responseBody);
                }

                @Override
                public void onError(String message) {
                    setLoading(false);
                    showError(message);
                }
            });
        } catch (JSONException e) {
            showError("Unexpected error building request.");
        }
    }

    private void submitSignup(String username, String password) {
        String password2 = text(inputPassword2);
        String email = text(inputEmail);
        String phone = text(inputPhone);
        String firstName = text(inputFirstName);
        String lastName = text(inputLastName);

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(phone)
                || TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
            showError("All fields are required.");
            return;
        }
        if (!password.equals(password2)) {
            showError("Passwords don't match.");
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("password", password);
            body.put("password2", password2);
            body.put("email", email);
            body.put("phone_number", phone);
            body.put("first_name", firstName);
            body.put("last_name", lastName);
            setLoading(true);
            ApiClient.get().post(ApiClient.DJANGO_BASE, Endpoints.REGISTER, body, new ApiCallback() {
                @Override
                public void onSuccess(int statusCode, String responseBody) {
                    setLoading(false);
                    handleAuthSuccess(responseBody);
                }

                @Override
                public void onError(String message) {
                    setLoading(false);
                    showError(message);
                }
            });
        } catch (JSONException e) {
            showError("Unexpected error building request.");
        }
    }

    private void handleAuthSuccess(String responseBody) {
        try {
            JSONObject json = new JSONObject(responseBody);
            String token = json.optString("token", null);
            JSONObject user = json.optJSONObject("user");
            if (token == null || user == null) {
                showError("Unexpected response from server.");
                return;
            }
            tokenStore.save(token, user);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } catch (JSONException e) {
            showError("Failed to parse server response.");
        }
    }

    private String text(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!loading);
    }

    private void showError(String message) {
        textError.setText(message);
        textError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        textError.setVisibility(View.GONE);
    }
}
