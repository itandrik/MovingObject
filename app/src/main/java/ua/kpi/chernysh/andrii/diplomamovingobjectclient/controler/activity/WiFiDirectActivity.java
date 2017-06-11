package ua.kpi.chernysh.andrii.diplomamovingobjectclient.controler.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ua.kpi.chernysh.andrii.diplomamovingobjectclient.R;
import ua.kpi.chernysh.andrii.diplomamovingobjectclient.controler.P2PReceiver;
import ua.kpi.chernysh.andrii.diplomamovingobjectclient.controler.fragment.DeviceDetailFragment;
import ua.kpi.chernysh.andrii.diplomamovingobjectclient.controler.fragment.DeviceListFragment;
import ua.kpi.chernysh.andrii.diplomamovingobjectclient.model.SettingsManager;

import static ua.kpi.chernysh.andrii.diplomamovingobjectclient.R.id.btnWifi;

/**
 * An activity that uses WiFi Direct APIs to discover and connect with available
 * devices. WiFi Direct APIs are asynchronous and rely on callback mechanism
 * using interfaces to notify the application of operation success or failure.
 * The application should also register a BroadcastReceiver for notification of
 * WiFi state related events.
 */
public class WiFiDirectActivity extends Activity implements
        WifiP2pManager.ChannelListener, DeviceListFragment.DeviceActionListener,
        View.OnClickListener {
    public static final String TAG = "wifidirect";
    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;
    private Locale locale;

    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
        ImageButton wifiBtn = (ImageButton) findViewById(btnWifi);
        TextView tvWifi = (TextView) findViewById(R.id.tvWifi);

        if (isWifiP2pEnabled) {
            wifiBtn.setBackgroundResource(R.drawable.ic_wifi_on);
            tvWifi.setText(R.string.wifi_on);
        } else {
            wifiBtn.setBackgroundResource(R.drawable.ic_wifi_off);
            tvWifi.setText(R.string.wifi_off);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // add necessary intent values to be matched.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        ImageButton btnWifi = (ImageButton) findViewById(R.id.btnWifi);
        ImageView btnLanguage = (ImageView) findViewById(R.id.ivLanguage);
        TextView tvLanguage = (TextView) findViewById(R.id.tvLanguage);

        btnWifi.setOnClickListener(this);
        btnLanguage.setOnClickListener(this);
        tvLanguage.setOnClickListener(this);
        findViewById(R.id.btnDiscover).setOnClickListener(this);

        SettingsManager sm = new SettingsManager(this);
        String lang = sm.getLocale();

        Log.d(TAG, "onCreate: lang = " + lang);
        Log.d(WiFiDirectActivity.TAG,"Сейчас времени : : " + new SimpleDateFormat("HH:mm:sss.SSS")
                .format(new Date()));
        locale = new java.util.Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, null);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        SettingsManager sm = new SettingsManager(this);
        String lang = sm.getLocale();
        locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, null);
    }

    /**
     * register the BroadcastReceiver with the intent values to be matched
     */
    @Override
    public void onResume() {
        super.onResume();
        receiver = new P2PReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
                .findFragmentById(R.id.frag_list);
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
        if (fragmentDetails != null) {
            fragmentDetails.resetViews();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
        return true;
    }

    /*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.atn_direct_enable:
                if (manager != null && channel != null) {
                    // Since this is the system wireless settings activity, it's
                    // not going to send us a result. We will be notified by
                    // WiFiDeviceBroadcastReceiver instead.
                    startActivity(new Intent(Settings.ACTION_WIFI_IP_SETTINGS));
                } else {
                    Log.e(TAG, "channel or manager is null");
                }
                return true;
            case R.id.atn_direct_discover:

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showDetails(WifiP2pDevice device) {
        DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.showDetails(device);
    }

    @Override
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(WiFiDirectActivity.this, getString(R.string.connecting_failure),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void disconnect() {
        final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.resetViews();
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
            }

            @Override
            public void onSuccess() {
                fragment.getView().setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void cancelDisconnect() {
        /*
         * A cancel abort request by user. Disconnect i.e. removeGroup if
         * already connected. Else, request WifiP2pManager to abort the ongoing
         * request
         */
        if (manager != null) {
            final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            if (fragment.getDevice() == null
                    || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
                    || fragment.getDevice().status == WifiP2pDevice.INVITED) {
                manager.cancelConnect(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(WiFiDirectActivity.this, getString(R.string.aborting_connection),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                                .findFragmentById(R.id.frag_detail);
                        if (fragment.progressDialog != null) {
                            fragment.progressDialog.dismiss();
                        }
                        Toast.makeText(WiFiDirectActivity.this,
                                getString(R.string.aborting_failure, reasonCode),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    public void onClick(View v) {
        final WifiManager wifi = (WifiManager) getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (v.getId() == btnWifi) {
            wifi.setWifiEnabled(!isWifiP2pEnabled);
        } else if (v.getId() == R.id.btnDiscover) {
            if (!isWifiP2pEnabled) {
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_wifi_on)
                        .setTitle(R.string.wifi_direct_is_not_enabled)
                        .setMessage(R.string.message_enable_wifi)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                wifi.setWifiEnabled(true);
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
                dialog.show();
            }
            final Handler h = new Handler();
            h.post(new Runnable() {
                @Override
                public void run() {
                    if (!isWifiP2pEnabled) {
                        h.post(this);
                    } else {
                        discoverPeers();
                    }
                }
            });
        } else if (v.getId() == R.id.tvLanguage || v.getId() == R.id.ivLanguage) {
            View content = getLayoutInflater().inflate(R.layout.language_content, null);

            final RadioButton rRu = (RadioButton) content.findViewById(R.id.rRu);
            final RadioButton rEn = (RadioButton) content.findViewById(R.id.rEn);

            final String[] codes = getResources().getStringArray(R.array.languages_codes);
            final SettingsManager sm = new SettingsManager(this);

            new AlertDialog.Builder(this)
                    .setView(content)
                    .setTitle(R.string.change_language_title)
                    .setPositiveButton(R.string.action_choose, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (rRu.isChecked()) {
                                if (sm.getLocale().equals(codes[0])) return;
                                sm.setLocale(codes[0]); // ua
                            } else if (rEn.isChecked()) {
                                if (sm.getLocale().equals(codes[1])) return;
                                sm.setLocale(codes[1]); // en
                            }

                            new AlertDialog.Builder(WiFiDirectActivity.this)
                                    .setTitle(R.string.need_restart)
                                    .setMessage(R.string.restart_app_info)
                                    .setPositiveButton(R.string.restart, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            System.exit(0);
                                        }
                                    })
                                    .setNegativeButton(R.string.later, null)
                                    .show();
                        }
                    })
                    .setNegativeButton(R.string.action_cancel, null)
                    .show();
        }
    }

    private void discoverPeers() {
        final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                .findFragmentById(R.id.frag_list);
        fragment.onInitiateDiscovery();
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(WiFiDirectActivity.this, getString(R.string.discover_init),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(WiFiDirectActivity.this, getString(R.string.discover_failed, reasonCode),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
