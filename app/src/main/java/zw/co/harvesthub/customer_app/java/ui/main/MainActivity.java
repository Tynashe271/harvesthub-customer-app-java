package zw.co.harvesthub.customer_app.java.ui.main;

import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.HashMap;
import java.util.Map;

import zw.co.harvesthub.customer_app.java.R;
import zw.co.harvesthub.customer_app.java.ui.Refreshable;
import zw.co.harvesthub.customer_app.java.ui.assistant.AssistantFragment;
import zw.co.harvesthub.customer_app.java.ui.cart.CartFragment;
import zw.co.harvesthub.customer_app.java.ui.favorites.FavoritesFragment;
import zw.co.harvesthub.customer_app.java.ui.location.LocationFragment;
import zw.co.harvesthub.customer_app.java.ui.notifications.NotificationsFragment;
import zw.co.harvesthub.customer_app.java.ui.orders.OrdersFragment;
import zw.co.harvesthub.customer_app.java.ui.profile.ProfileFragment;
import zw.co.harvesthub.customer_app.java.ui.shoppinglist.ShoppingListFragment;
import zw.co.harvesthub.customer_app.java.ui.store.StoreFragment;
import zw.co.harvesthub.customer_app.java.ui.support.SupportFragment;

/**
 * Single-Activity shell hosting all 10 destinations as Fragments kept alive
 * via show/hide (never replace) so state (scroll position, in-progress
 * search text, cart contents) survives tab switches — the direct analogue
 * of the Flutter app's IndexedStack-based MobileHome shell.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG_STORE = "store";
    private static final String TAG_CART = "cart";
    private static final String TAG_ORDERS = "orders";
    private static final String TAG_ASSISTANT = "assistant";
    private static final String TAG_PROFILE = "profile";
    private static final String TAG_FAVORITES = "favorites";
    private static final String TAG_SHOPPING_LIST = "shopping_list";
    private static final String TAG_LOCATION = "location";
    private static final String TAG_NOTIFICATIONS = "notifications";
    private static final String TAG_SUPPORT = "support";

    private final Map<String, Fragment> fragments = new HashMap<>();
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private String currentTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(this::onBottomNavSelected);

        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this::onDrawerItemSelected);

        if (savedInstanceState == null) {
            showFragment(TAG_STORE, StoreFragment::new, getString(R.string.nav_store));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            Fragment current = currentTag != null ? fragments.get(currentTag) : null;
            if (current instanceof Refreshable) {
                ((Refreshable) current).refresh();
            }
            return true;
        } else if (id == R.id.action_logout) {
            signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean onBottomNavSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_store) {
            showFragment(TAG_STORE, StoreFragment::new, getString(R.string.nav_store));
        } else if (id == R.id.nav_cart) {
            showFragment(TAG_CART, CartFragment::new, getString(R.string.nav_cart));
        } else if (id == R.id.nav_orders) {
            showFragment(TAG_ORDERS, OrdersFragment::new, getString(R.string.nav_orders));
        } else if (id == R.id.nav_assistant) {
            showFragment(TAG_ASSISTANT, AssistantFragment::new, getString(R.string.nav_assistant));
        } else if (id == R.id.nav_profile) {
            showFragment(TAG_PROFILE, ProfileFragment::new, getString(R.string.nav_profile));
        }
        return true;
    }

    private boolean onDrawerItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_favorites) {
            showFragment(TAG_FAVORITES, FavoritesFragment::new, getString(R.string.nav_favorites));
        } else if (id == R.id.nav_shopping_list) {
            showFragment(TAG_SHOPPING_LIST, ShoppingListFragment::new, getString(R.string.nav_shopping_list));
        } else if (id == R.id.nav_location) {
            showFragment(TAG_LOCATION, LocationFragment::new, getString(R.string.nav_location));
        } else if (id == R.id.nav_notifications) {
            showFragment(TAG_NOTIFICATIONS, NotificationsFragment::new, getString(R.string.nav_notifications));
        } else if (id == R.id.nav_support) {
            showFragment(TAG_SUPPORT, SupportFragment::new, getString(R.string.nav_support));
        }
        drawerLayout.closeDrawer(Gravity.START);
        return true;
    }

    private void showFragment(String tag, FragmentFactory factory, String title) {
        if (tag.equals(currentTag)) return;

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();

        Fragment current = currentTag != null ? fragments.get(currentTag) : null;
        if (current != null) {
            tx.hide(current);
        }

        Fragment target = fragments.get(tag);
        boolean alreadyExisted = target != null;
        if (!alreadyExisted) {
            target = factory.create();
            fragments.put(tag, target);
            tx.add(R.id.fragmentContainer, target, tag);
        } else {
            tx.show(target);
        }

        tx.commit();
        currentTag = tag;
        setTitle(title);

        // show()/hide() don't trigger onResume, so a fragment that's being
        // re-shown (not created for the first time) needs an explicit nudge
        // to reload its data — otherwise it would only ever load once, on
        // first creation. A brand-new fragment's own onViewCreated() already
        // does the initial load, and its view doesn't exist yet at this point
        // in the transaction (tx.commit() above only schedules attachment),
        // so calling refresh() on it here would NPE on unset view fields.
        if (alreadyExisted && target instanceof Refreshable) {
            ((Refreshable) target).refresh();
        }
    }

    public void signOut() {
        new zw.co.harvesthub.customer_app.java.storage.TokenStore(this).clear();
        zw.co.harvesthub.customer_app.java.storage.AppState.get().clear();
        startActivity(new android.content.Intent(this, zw.co.harvesthub.customer_app.java.ui.auth.AuthActivity.class));
        finish();
    }

    private interface FragmentFactory {
        Fragment create();
    }
}
