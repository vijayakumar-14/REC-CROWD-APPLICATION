package com.vijayakumar.reccrowdmonitor.ui;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vijayakumar.reccrowdmonitor.R;
import com.vijayakumar.reccrowdmonitor.util.ThemeHelper;

public class MainActivity extends AppCompatActivity {

    private MaterialToolbar topAppBar;
    private View bannerOffline;
    private BottomNavigationView bottomNavigation;

    private LiveStatusFragment liveStatusFragment;
    private SupportFragment supportFragment;
    private Fragment activeFragment;

    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        topAppBar = findViewById(R.id.top_app_bar);
        bannerOffline = findViewById(R.id.banner_offline);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        liveStatusFragment = new LiveStatusFragment();
        supportFragment = new SupportFragment();

        // Default tab: Live Status
        activeFragment = liveStatusFragment;
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, supportFragment, "SUPPORT").hide(supportFragment)
                .add(R.id.fragment_container, liveStatusFragment, "LIVE_STATUS")
                .commit();

        setupTopAppBar();
        setupBottomNavigation();
        registerNetworkCallback();
    }

    private void setupTopAppBar() {
        topAppBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_sort) {
                if (activeFragment instanceof LiveStatusFragment) {
                    ((LiveStatusFragment) activeFragment).toggleSortOrder();
                }
                return true;
            } else if (id == R.id.action_share) {
                if (activeFragment instanceof LiveStatusFragment) {
                    ((LiveStatusFragment) activeFragment).shareCrowdStatus();
                }
                return true;
            }
            return false;
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_live_status) {
                switchFragment(liveStatusFragment);
                setTopAppBarActionsVisible(true);
                return true;
            } else if (itemId == R.id.nav_support) {
                switchFragment(supportFragment);
                setTopAppBarActionsVisible(false);
                return true;
            }
            return false;
        });
    }

    private void switchFragment(Fragment targetFragment) {
        if (activeFragment != targetFragment) {
            getSupportFragmentManager().beginTransaction()
                    .hide(activeFragment)
                    .show(targetFragment)
                    .commit();
            activeFragment = targetFragment;
        }
    }

    private void setTopAppBarActionsVisible(boolean visible) {
        MenuItem sortItem = topAppBar.getMenu().findItem(R.id.action_sort);
        MenuItem shareItem = topAppBar.getMenu().findItem(R.id.action_share);
        if (sortItem != null) sortItem.setVisible(visible);
        if (shareItem != null) shareItem.setVisible(visible);
    }

    private void registerNetworkCallback() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return;

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                runOnUiThread(() -> bannerOffline.setVisibility(View.GONE));
            }

            @Override
            public void onLost(@NonNull Network network) {
                runOnUiThread(() -> bannerOffline.setVisibility(View.VISIBLE));
            }
        };

        try {
            connectivityManager.registerNetworkCallback(request, networkCallback);
            // Initial check
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork == null) {
                bannerOffline.setVisibility(View.VISIBLE);
            } else {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
                boolean isConnected = capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                bannerOffline.setVisibility(isConnected ? View.GONE : View.VISIBLE);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectivityManager != null && networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (Exception ignored) {
            }
        }
    }
}
