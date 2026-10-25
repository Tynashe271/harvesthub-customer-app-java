package zw.co.harvesthub.customer_app.java.ui.store;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import java.util.ArrayList;
import java.util.List;

import zw.co.harvesthub.customer_app.java.R;
import zw.co.harvesthub.customer_app.java.models.Product;
import zw.co.harvesthub.customer_app.java.storage.AppState;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    public interface Listener {
        void onAddToCart(Product product);
        void onToggleWishlist(Product product);
    }

    private final List<Product> items = new ArrayList<>();
    private final Listener listener;

    public ProductAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submitList(List<Product> products) {
        items.clear();
        items.addAll(products);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = items.get(position);
        holder.textName.setText(product.name);
        holder.textCategory.setText(product.category);
        holder.textPrice.setText(String.format("$%.2f", product.price));

        if (product.image == null || product.image.isEmpty()) {
            Glide.with(holder.imageProduct).clear(holder.imageProduct);
            holder.imageProduct.setImageResource(R.drawable.ic_nav_store);
            holder.imageProduct.setPadding(14, 14, 14, 14);
        } else {
            holder.imageProduct.setPadding(0, 0, 0, 0);
            Glide.with(holder.imageProduct)
                    .load(product.image)
                    .transform(new CircleCrop())
                    .placeholder(R.drawable.ic_nav_store)
                    .into(holder.imageProduct);
        }

        boolean wishlisted = AppState.get().isWishlisted(product.id);
        int tintRes = wishlisted ? R.color.brand_amber : R.color.muted;
        holder.btnWishlist.setImageTintList(ColorStateList.valueOf(
                ContextCompat.getColor(holder.itemView.getContext(), tintRes)));

        holder.btnWishlist.setOnClickListener(v -> listener.onToggleWishlist(product));
        holder.btnAddToCart.setOnClickListener(v -> listener.onAddToCart(product));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageProduct;
        final TextView textName;
        final TextView textCategory;
        final TextView textPrice;
        final ImageButton btnWishlist;
        final com.google.android.material.button.MaterialButton btnAddToCart;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProduct = itemView.findViewById(R.id.imageProduct);
            textName = itemView.findViewById(R.id.textName);
            textCategory = itemView.findViewById(R.id.textCategory);
            textPrice = itemView.findViewById(R.id.textPrice);
            btnWishlist = itemView.findViewById(R.id.btnWishlist);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}
