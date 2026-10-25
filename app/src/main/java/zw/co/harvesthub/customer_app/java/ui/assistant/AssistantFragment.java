package zw.co.harvesthub.customer_app.java.ui.assistant;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import zw.co.harvesthub.customer_app.java.R;
import zw.co.harvesthub.customer_app.java.network.ApiCallback;
import zw.co.harvesthub.customer_app.java.network.ApiClient;
import zw.co.harvesthub.customer_app.java.network.Endpoints;

/** Chat UI against POST ai/chat/, mirroring the Flutter app's AssistantPage. */
public class AssistantFragment extends Fragment {

    private static class ChatMessage {
        final String role;
        final String content;
        ChatMessage(String role, String content) { this.role = role; this.content = content; }
    }

    private final List<ChatMessage> history = new ArrayList<>();

    private LinearLayout chatContainer;
    private ScrollView chatScroll;
    private TextInputEditText inputMessage;
    private View progress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_assistant, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chatContainer = view.findViewById(R.id.chatContainer);
        chatScroll = view.findViewById(R.id.chatScroll);
        inputMessage = view.findViewById(R.id.inputMessage);
        progress = view.findViewById(R.id.progress);

        view.findViewById(R.id.btnSend).setOnClickListener(v -> send());

        if (history.isEmpty()) {
            appendMessage("assistant", "Hi! Ask me about products, deals, or your orders — I can add items to your cart too.");
        }
    }

    private void send() {
        String text = inputMessage.getText() != null ? inputMessage.getText().toString().trim() : "";
        if (TextUtils.isEmpty(text)) return;

        appendMessage("user", text);
        inputMessage.setText("");
        setLoading(true);

        try {
            JSONArray historyJson = new JSONArray();
            for (ChatMessage m : history) {
                JSONObject entry = new JSONObject();
                entry.put("role", m.role);
                entry.put("content", m.content);
                historyJson.put(entry);
            }
            JSONObject body = new JSONObject();
            body.put("message", text);
            body.put("history", historyJson);

            ApiClient.get().post(ApiClient.DJANGO_BASE, Endpoints.AI_CHAT, body, new ApiCallback() {
                @Override
                public void onSuccess(int statusCode, String responseBody) {
                    setLoading(false);
                    try {
                        String answer = new JSONObject(responseBody).optString("answer", "");
                        appendMessage("assistant", answer);
                    } catch (JSONException e) {
                        appendMessage("assistant", "Sorry, I couldn't read that response.");
                    }
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

    private void appendMessage(String role, String content) {
        history.add(new ChatMessage(role, content));

        TextView bubble = new TextView(requireContext());
        bubble.setText(content);
        bubble.setPadding(24, 16, 24, 16);
        boolean isUser = "user".equals(role);
        bubble.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = isUser ? Gravity.END : Gravity.START;
        params.bottomMargin = 12;
        bubble.setLayoutParams(params);

        chatContainer.addView(bubble);
        chatScroll.post(() -> chatScroll.fullScroll(View.FOCUS_DOWN));
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
