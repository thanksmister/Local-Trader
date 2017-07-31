/*
 * Copyright (c) 2017 ThanksMister LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed 
 * under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.thanksmister.bitcoin.localtrader.ui.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.zxing.android.IntentIntegrator;
import com.google.zxing.android.IntentResult;
import com.thanksmister.bitcoin.localtrader.R;
import com.thanksmister.bitcoin.localtrader.data.api.model.ExchangeRate;
import com.thanksmister.bitcoin.localtrader.data.database.ExchangeRateItem;
import com.thanksmister.bitcoin.localtrader.data.services.ExchangeService;
import com.thanksmister.bitcoin.localtrader.data.services.SyncAdapter;
import com.thanksmister.bitcoin.localtrader.data.services.SyncUtils;
import com.thanksmister.bitcoin.localtrader.events.AlertDialogEvent;
import com.thanksmister.bitcoin.localtrader.ui.BaseActivity;
import com.thanksmister.bitcoin.localtrader.ui.adapters.EditActivity;
import com.thanksmister.bitcoin.localtrader.ui.fragments.AboutFragment;
import com.thanksmister.bitcoin.localtrader.ui.fragments.DashboardFragment;
import com.thanksmister.bitcoin.localtrader.ui.fragments.RequestFragment;
import com.thanksmister.bitcoin.localtrader.ui.fragments.SearchFragment;
import com.thanksmister.bitcoin.localtrader.ui.fragments.SendFragment;
import com.thanksmister.bitcoin.localtrader.ui.fragments.WalletFragment;
import com.thanksmister.bitcoin.localtrader.utils.AuthUtils;
import com.thanksmister.bitcoin.localtrader.utils.NotificationUtils;
import com.thanksmister.bitcoin.localtrader.utils.WalletUtils;
import com.trello.rxlifecycle.ActivityEvent;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

@BaseActivity.RequiresAuthentication
public class MainActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener  {

    private static IntentFilter syncIntentFilter = new IntentFilter(SyncAdapter.ACTION_SYNC);
    
    private static final String BITCOIN_URI = "com.thanksmister.extra.BITCOIN_URI";
    private static final String DASHBOARD_FRAGMENT = "com.thanksmister.fragment.DASHBOARD_FRAGMENT";
    private static final String ABOUT_FRAGMENT = "com.thanksmister.fragment.ACCOUNT_FRAGMENT";
    private static final String RECEIVE_FRAGMENT = "com.thanksmister.fragment.RECEIVE_FRAGMENT";
    private static final String SEND_FRAGMENT = "com.thanksmister.fragment.SEND_FRAGMENT";
    private static final String SEARCH_FRAGMENT = "com.thanksmister.fragment.SEARCH_FRAGMENT";
    private static final String WALLET_FRAGMENT = "com.thanksmister.fragment.WALLET_FRAGMENT";

    public static String EXTRA_CONTACT = "extra_contact";
    public static String EXTRA_NOTIFICATION_ID = "extra_notification_id";
    public static String EXTRA_NOTIFICATION_TYPE = "extra_notification_type";
    public static String EXTRA_FRAGMENT = "extra_fragment";

    public static final int DRAWER_DASHBOARD = 0;
    public static final int DRAWER_SEARCH = 1;
    public static final int DRAWER_SEND = 2;
    public static final int DRAWER_RECEIVE = 3;
    public static final int DRAWER_WALLET = 4;
    public static final int DRAWER_ABOUT = 5;

    private static final int REQUEST_SCAN = 49374;

    @Inject
    ExchangeService exchangeService;

    @InjectView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @InjectView(R.id.navigation)
    NavigationView navigationView;

    @InjectView(R.id.bitcoinTitle)
    TextView bitcoinTitle;

    @InjectView(R.id.bitcoinPrice)
    TextView bitcoinPrice;

    @InjectView(R.id.bitcoinValue)
    TextView bitcoinValue;

    @InjectView(R.id.swipeLayout)
    SwipeRefreshLayout swipeLayout;

    private Fragment fragment;
    private int position = DRAWER_DASHBOARD;
    private int lastMenuItemId = R.id.navigationItemDashboard;
    TextView userName;
    TextView feedbackScore;
    TextView tradeCount;

    public static Intent createStartIntent(Context context, String bitcoinUri) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(BITCOIN_URI, bitcoinUri);
        return intent;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_main);
        } catch (NoClassDefFoundError e) {
            showAlertDialog(new AlertDialogEvent(getString(R.string.error_device_title), getString(R.string.error_device_softare_description)), new Action0() {
                @Override
                public void call() {
                    finish();
                }
            });
            return;
        }

        ButterKnife.inject(this);

        if (savedInstanceState != null) {
            position = savedInstanceState.getInt(EXTRA_FRAGMENT);
        }

        final String bitcoinUri = getIntent().getStringExtra(BITCOIN_URI);

        setupNavigationView();

        boolean authenticated = AuthUtils.hasCredentials(preference, sharedPreferences);
        if (authenticated) {
            if (bitcoinUri != null && validAddressOrAmount(bitcoinUri)) { // we have a uri request so override setting content
                handleBitcoinUri(bitcoinUri);
            } else {
                setContentFragment(position);
            }
        }

        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeColors(getResources().getColor(R.color.red));
        swipeLayout.setProgressViewOffset(false, 48, 186);
        swipeLayout.setDistanceToTriggerSync(250);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
        outState.putInt(EXTRA_FRAGMENT, position);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (((Object) this).getClass().isAnnotationPresent(RequiresAuthentication.class)) {
            boolean authenticated = AuthUtils.hasCredentials(preference, sharedPreferences);
            if (!authenticated) {
                launchPromoScreen();
                return;
            } else if (AuthUtils.showUpgradedMessage(getApplicationContext(), preference)) {
                String title = getString(R.string.text_whats_new) + AuthUtils.getCurrentVersionName(getApplicationContext());
                showAlertDialogLinks(new AlertDialogEvent(title, getString(R.string.whats_new_message)));
                AuthUtils.setUpgradeVersion(getApplicationContext(), preference);
            }
        }

        updateData();
        subscribeData();
        navigationView.getMenu().findItem(lastMenuItemId).setChecked(true);
        registerReceiver(syncBroadcastReceiver, syncIntentFilter);
    }

    @Override
    protected void handleNetworkDisconnect() {
        boolean retry = (position == DRAWER_DASHBOARD || position == DRAWER_WALLET);
        snack(getString(R.string.error_no_internet), retry);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(syncBroadcastReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (drawerLayout != null)
                    drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {

        Timber.d("onRefresh");

        // force refresh of advertisements
        AuthUtils.setForceUpdate(preference, true);
        SyncUtils.requestSyncNow(MainActivity.this);

        handleRefresh();
        updateData();
    }

    private void onRefreshStop() {
        if (swipeLayout != null) {
            swipeLayout.setRefreshing(false);
        }
    }
    
    private void updateData() {

        Timber.d("UpdateData");

        exchangeService.getSpotPrice()
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        Timber.i("Update Exchange subscription safely unsubscribed");
                    }
                })
                .compose(this.<ExchangeRate>bindUntilEvent(ActivityEvent.PAUSE))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ExchangeRate>() {
                    @Override
                    public void call(ExchangeRate exchange) {
                        if(exchange != null) {
                            dbManager.updateExchange(exchange);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        snackError(getString(R.string.toast_unable_update_currency_rate));
                    }
                });
    }

    private void subscribeData() {

        Timber.d("subscribeData");

        dbManager.exchangeQuery()
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        Timber.i("Exchange subscription safely unsubscribed");
                    }
                })
                .compose(this.<List<ExchangeRateItem>>bindUntilEvent(ActivityEvent.PAUSE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<ExchangeRateItem>>() {
                    @Override
                    public void call(List<ExchangeRateItem> exchanges) {
                        if (!exchanges.isEmpty()) {
                            String currency = exchangeService.getExchangeCurrency();
                            for (ExchangeRateItem rateItem : exchanges) {
                                if (rateItem.currency().equals(currency)) {
                                    setHeaderItem(rateItem);
                                    break;
                                }
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        reportError(throwable);
                    }
                });
    }

    private void launchPromoScreen() {
        Intent intent = new Intent(MainActivity.this, PromoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupNavigationView() {
        
        final View headerView = navigationView.getHeaderView(0);
        userName = (TextView) headerView.findViewById(R.id.userName);
        tradeCount = (TextView) headerView.findViewById(R.id.userTradeCount);
        feedbackScore = (TextView) headerView.findViewById(R.id.userTradeFeedback);

        userName.setText(AuthUtils.getUsername(preference, sharedPreferences));
        tradeCount.setText(AuthUtils.getUsername(preference, sharedPreferences));
        feedbackScore.setText(AuthUtils.getTrades(preference, sharedPreferences));

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(), 0);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                selectDrawerItem(menuItem);
                return true;
            }
        });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.navigationItemSearch:
                lastMenuItemId = menuItem.getItemId();
                setContentFragment(DRAWER_SEARCH);
                break;
            case R.id.navigationItemSend:
                lastMenuItemId = menuItem.getItemId();
                setContentFragment(DRAWER_SEND);
                break;
            case R.id.navigationItemReceive:
                lastMenuItemId = menuItem.getItemId();
                setContentFragment(DRAWER_RECEIVE);
                break;
            case R.id.navigationItemWallet:
                lastMenuItemId = menuItem.getItemId();
                setContentFragment(DRAWER_WALLET);
                break;
            case R.id.navigationItemAbout:
                lastMenuItemId = menuItem.getItemId();
                setContentFragment(DRAWER_ABOUT);
                break;
            case R.id.navigationItemSettings:
                Intent intent = SettingsActivity.createStartIntent(this);
                startActivity(intent);
                break;
            default:
                lastMenuItemId = menuItem.getItemId();
                setContentFragment(DRAWER_DASHBOARD);
        }

        // Highlight the selected item, update the title, and close the drawer
        menuItem.setChecked(true);
        drawerLayout.closeDrawers();
    }

    public void setContentFragment(int position) {
        
        Timber.d("setContentFragment position: " + position);
        Timber.d("setContentFragment isFinishing: " + isFinishing());
        
        this.position = position;

        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (NullPointerException e) {
            Timber.w("Error closing keyboard");
        }
        
        if (!isFinishing()) {
            if (position == DRAWER_WALLET) {
                swipeLayout.setEnabled(true);
                fragment = WalletFragment.newInstance();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, fragment, WALLET_FRAGMENT)
                        .commitAllowingStateLoss();
            } else if (position == DRAWER_SEARCH) {
                swipeLayout.setEnabled(false);
                fragment = SearchFragment.newInstance();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, fragment, SEARCH_FRAGMENT)
                        .commitAllowingStateLoss();
            } else if (position == DRAWER_SEND) {
                swipeLayout.setEnabled(false);
                fragment = SendFragment.newInstance();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, fragment, SEND_FRAGMENT)
                        .commitAllowingStateLoss();
            } else if (position == DRAWER_RECEIVE) {
                swipeLayout.setEnabled(false);
                fragment = RequestFragment.newInstance();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, fragment, RECEIVE_FRAGMENT)
                        .commitAllowingStateLoss();
            } else if (position == DRAWER_DASHBOARD) {
                swipeLayout.setEnabled(true);
                fragment = DashboardFragment.newInstance();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, fragment, DASHBOARD_FRAGMENT).commit();
            } else if (position == DRAWER_ABOUT) {
                swipeLayout.setEnabled(false);
                fragment = AboutFragment.newInstance();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_frame, fragment, ABOUT_FRAGMENT)
                        .commitAllowingStateLoss();
            }
        }
    }

    private void startSendFragment(String bitcoinAddress, String bitcoinAmount) {
        fragment = SendFragment.newInstance(bitcoinAddress, bitcoinAmount);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment, SEND_FRAGMENT)
                .commitAllowingStateLoss();

        navigationView.getMenu().findItem(R.id.navigationItemSend).setChecked(true);
        position = DRAWER_SEND;
    }

    @Override
    public void handleRefresh() {
        switch (fragment.getTag()) {
            case WALLET_FRAGMENT:
                ((WalletFragment) fragment).onRefresh();
                break;
        }
    }
    
    public void navigateDashboardViewAndRefresh() {
        setContentFragment(DRAWER_DASHBOARD);
        navigationView.getMenu().findItem(R.id.navigationItemDashboard).setChecked(true);
        onRefresh();
    }

    public void navigateSendView() {
        setContentFragment(DRAWER_SEND);
        navigationView.getMenu().findItem(R.id.navigationItemSend).setChecked(true);
    }

    public void navigateDashboardView() {
        setContentFragment(DRAWER_DASHBOARD);
        navigationView.getMenu().findItem(R.id.navigationItemDashboard).setChecked(true);
    }

    public void navigateSearchView() {
        setContentFragment(DRAWER_SEARCH);
    }

    public void restoreActionBar() {
        /*ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.setTitle(mTitle);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (drawerLayout != null && !drawerLayout.isDrawerOpen(GravityCompat.START)) {
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        Timber.d("Request Code: " + requestCode);
        Timber.d("Result Code: " + resultCode);
        
        if (requestCode == REQUEST_SCAN) {
            IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanningResult != null) {
                handleBitcoinUri(scanningResult.getContents());
            } else {
                toast(getString(R.string.toast_scan_canceled));
            }
        } else if (requestCode == EditAdvertisementActivity.REQUEST_CODE) {
            if (resultCode == EditAdvertisementActivity.RESULT_CREATED || resultCode == EditActivity.RESULT_UPDATED) {
                onRefresh();
            }
        } else if (requestCode == AdvertisementActivity.REQUEST_CODE) {
            if (resultCode == AdvertisementActivity.RESULT_DELETED) {
                onRefresh();
            }
        }
    }

    protected boolean validAddressOrAmount(String bitcoinUri) {
        String bitcoinAddress = WalletUtils.parseBitcoinAddress(bitcoinUri);
        String bitcoinAmount = WalletUtils.parseBitcoinAmount(bitcoinUri);

        if (bitcoinAddress == null) {
            return false;
        } else if (!WalletUtils.validBitcoinAddress(bitcoinAddress)) {
            toast(getString(R.string.toast_invalid_address));
            return false;
        }

        if (bitcoinAmount != null && !WalletUtils.validAmount(bitcoinAmount)) {
            toast(getString(R.string.toast_invalid_btc_amount));
            return false;
        }

        return true;
    }

    protected void handleBitcoinUri(String bitcoinUri) {
        String bitcoinAddress = WalletUtils.parseBitcoinAddress(bitcoinUri);
        String bitcoinAmount = WalletUtils.parseBitcoinAmount(bitcoinUri);
        startSendFragment(bitcoinAddress, bitcoinAmount);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent == null || intent.getExtras() == null)
            return;

        Bundle extras = intent.getExtras();
        int type = extras.getInt(EXTRA_NOTIFICATION_TYPE, 0);

        if (type == NotificationUtils.NOTIFICATION_TYPE_CONTACT) {
            String id = extras.getString(EXTRA_NOTIFICATION_ID);
            if (id != null) {
                Intent launchIntent = ContactActivity.createStartIntent(this, id);
                startActivity(launchIntent);
            }
        } else if (type == NotificationUtils.NOTIFICATION_TYPE_ADVERTISEMENT) {
            String id = extras.getString(EXTRA_NOTIFICATION_ID);
            if (id != null) {
                Intent launchIntent = AdvertisementActivity.createStartIntent(this, id);
                startActivity(launchIntent);
            }
        } else if (type == NotificationUtils.NOTIFICATION_TYPE_BALANCE) {
            setContentFragment(DRAWER_WALLET);
            navigationView.getMenu().findItem(R.id.navigationItemWallet).setChecked(true);
        }
    }
    
    private void setHeaderItem(ExchangeRateItem exchange) {
        String currency = exchange.currency();
        String rate = exchange.rate();
        bitcoinTitle.setText(R.string.text_title_market_price);
        bitcoinPrice.setText(rate + " " + exchange.currency() + "/" + getString(R.string.btc));
        bitcoinValue.setText(exchange.exchange() + " (" + currency + ")");
    }

    protected void handleSyncEvent(String syncActionType, String extraErrorMessage, int extraErrorCode) {
        Timber.d("handleSyncEvent: " + syncActionType);
        switch (syncActionType) {
            case SyncAdapter.ACTION_TYPE_START:
                break;
            case SyncAdapter.ACTION_TYPE_COMPLETE:
                onRefreshStop();
                break;
            case SyncAdapter.ACTION_TYPE_CANCELED:
                onRefreshStop();
                break;
            case SyncAdapter.ACTION_TYPE_ERROR:
                Timber.e("Sync error: " + extraErrorMessage + "code: " + extraErrorCode);
                onRefreshStop();
                break;
        }
    }

    private BroadcastReceiver syncBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String syncActionType = intent.getStringExtra(SyncAdapter.EXTRA_ACTION_TYPE);
            assert syncActionType != null; // this should never be null

            String extraErrorMessage = "";
            int extraErrorCode = SyncAdapter.SYNC_ERROR_CODE;

            if (intent.hasExtra(SyncAdapter.EXTRA_ERROR_MESSAGE)) {
                extraErrorMessage = intent.getStringExtra(SyncAdapter.EXTRA_ERROR_MESSAGE);
            }
            if (intent.hasExtra(SyncAdapter.EXTRA_ERROR_CODE)) {
                extraErrorCode = intent.getIntExtra(SyncAdapter.EXTRA_ERROR_CODE, SyncAdapter.SYNC_ERROR_CODE);
            }

            handleSyncEvent(syncActionType, extraErrorMessage, extraErrorCode);
        }
    };
}