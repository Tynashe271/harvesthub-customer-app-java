package zw.co.harvesthub.customer_app.java.ui;

/**
 * Implemented by fragments that fetch server data and need to reload it every
 * time their tab becomes visible — not just the first time they're created.
 * FragmentTransaction.show()/hide() (used for tab switching, so state like
 * scroll position survives) does NOT trigger onPause/onResume, so without
 * this, a fragment only ever loads its data once.
 */
public interface Refreshable {
    void refresh();
}
