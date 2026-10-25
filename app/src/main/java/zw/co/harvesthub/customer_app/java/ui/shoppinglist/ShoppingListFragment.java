package zw.co.harvesthub.customer_app.java.ui.shoppinglist;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import zw.co.harvesthub.customer_app.java.R;
import zw.co.harvesthub.customer_app.java.storage.ShoppingListStore;

/** Local-only, free-text shopping list — never synced to any backend, matching
 *  the Flutter app's ShoppingListPage exactly. */
public class ShoppingListFragment extends Fragment {

    private ShoppingListStore store;
    private LinearLayout listContainer;
    private TextInputEditText inputNewItem;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shopping_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        store = new ShoppingListStore(requireContext());
        listContainer = view.findViewById(R.id.listContainer);
        inputNewItem = view.findViewById(R.id.inputNewItem);

        view.findViewById(R.id.btnAddItem).setOnClickListener(v -> addItem());
        renderList();
    }

    private void addItem() {
        String text = inputNewItem.getText() != null ? inputNewItem.getText().toString().trim() : "";
        if (TextUtils.isEmpty(text)) return;
        store.addItem(text);
        inputNewItem.setText("");
        renderList();
    }

    private void renderList() {
        listContainer.removeAllViews();
        List<String> items = store.getItems();
        for (int i = 0; i < items.size(); i++) {
            int index = i;
            View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_shopping_list_entry, listContainer, false);
            TextView textItem = row.findViewById(R.id.textItem);
            textItem.setText(items.get(index));
            row.findViewById(R.id.btnRemove).setOnClickListener(v -> {
                store.removeItem(index);
                renderList();
            });
            listContainer.addView(row);
        }
    }
}
