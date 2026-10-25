package zw.co.harvesthub.customer_app.java.models;

import org.json.JSONObject;

public class Product {
    public final int id;
    public final String name;
    public final String category;
    public final String slug;
    public final double price;
    public final int stock;
    public final String image;
    public final String description;
    public final String brand;
    public final Double rating;

    public Product(int id, String name, String category, String slug, double price, int stock,
                    String image, String description, String brand, Double rating) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.slug = slug;
        this.price = price;
        this.stock = stock;
        this.image = image;
        this.description = description;
        this.brand = brand;
        this.rating = rating;
    }

    public static Product fromJson(JSONObject json) {
        String category = json.has("category_name") && !json.isNull("category_name")
                ? json.optString("category_name") : "Grocery";
        Double rating = json.has("average_rating") && !json.isNull("average_rating")
                ? json.optDouble("average_rating") : null;
        return new Product(
                json.optInt("id"),
                json.optString("name"),
                category,
                json.optString("slug"),
                json.optDouble("price", 0),
                json.optInt("stock_quantity", 0),
                json.optString("image", null),
                json.optString("description", ""),
                json.optString("brand", ""),
                rating
        );
    }
}
