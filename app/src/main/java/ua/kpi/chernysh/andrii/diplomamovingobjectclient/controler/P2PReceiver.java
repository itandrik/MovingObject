package ua.kpi.chernysh.andrii.diplomamovingobjectclient.controler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import ua.kpi.chernysh.andrii.diplomamovingobjectclient.R;
import ua.kpi.chernysh.andrii.diplomamovingobjectclient.controler.activity.WiFiDirectActivity;
import ua.kpi.chernysh.andrii.diplomamovingobjectclient.controler.fragment.DeviceDetailFragment;
import ua.kpi.chernysh.andrii.diplomamovingobjectclient.controler.fragment.DeviceListFragment;

/**
 * Created by Dron on 13-May-17.
 */

public class P2PReceiver extends BroadcastReceiver {
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WiFiDirectActivity activity;
    /**
     * @param manager WifiP2pManager system service
     * @param channel Wifi p2p channel
     * @param activity activity associated with the receiver
     */
    public P2PReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       WiFiDirectActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }
    /*
     * (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
     * android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
                activity.setIsWifiP2pEnabled(true);
            } else {
                activity.setIsWifiP2pEnabled(false);
                activity.resetData();
            }
            Log.d(WiFiDirectActivity.TAG, "P2P state changed - " + state);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (manager != null) {
                manager.requestPeers(channel,
                        (WifiP2pManager.PeerListListener) activity.getFragmentManager()
                        .findFragmentById(R.id.frag_list));
            }
            Log.d(WiFiDirectActivity.TAG, "P2P peers changed");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (manager == null) {
                return;
            }
            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                // we are connected with the other device, request connection
                // info to find group owner IP
                DeviceDetailFragment fragment = (DeviceDetailFragment) activity
                        .getFragmentManager().findFragmentById(R.id.frag_detail);
                manager.requestConnectionInfo(channel, fragment);
            } else {
                // It's a disconnect
                activity.resetData();
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
        }
    }
   /* private static final String LOG_TAG = P2PReceiver.class.getName();

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WiFiDirectActivity mActivity;
    private WifiP2pManager.PeerListListener mPeerListListener;
    private List<WifiP2pDevice> mPeersList = new ArrayList<>();

    public P2PReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, WiFiDirectActivity activity) {
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;

        setDiscoverPeersListener();
        setAvailablePeersListener();
    }

    private void setDiscoverPeersListener() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(LOG_TAG,"Success discovering peers :)");
            }

            @Override
            public void onFailure(int i) {
                Log.e(LOG_TAG,"Falure discovering peers");
            }
        });
    }

    private void setAvailablePeersListener(){
        mPeerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peerList) {

                List<WifiP2pDevice> refreshedPeers = (List<WifiP2pDevice>) peerList.getDeviceList();
                if (!refreshedPeers.equals(mPeersList)) {
                    mPeersList.clear();
                    mPeersList.addAll(refreshedPeers);

                    // If an AdapterView is backed by this data, notify it
                    // of the change.  For instance, if you have a ListView of
                    // available peers, trigger an update.
                   // ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();

                    // Perform any other updates needed based on the new list of
                    // peers connected to the Wi-Fi P2P network.
                    int i = 0;
                    for (WifiP2pDevice device: mPeersList) {
                        Log.d(LOG_TAG, "deivce[" + i + "] address : " + device.deviceAddress);
                        Log.d(LOG_TAG, "deivce[" + i + "] name : " + device.deviceName);
                        Log.d(LOG_TAG, "deivce[" + i + "] toString : " + device.toString());
                    }

                }

                if (mPeersList.size() == 0) {
                    Log.d(LOG_TAG, "No devices found");
                    return;
                }
            }
        };
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate mActivity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED){
                Log.i(LOG_TAG, "Wi-Fi P2p state enabled :)");

            } else {
                Log.e(LOG_TAG,"Wi-Fi P2p state disabled");
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Log.d(LOG_TAG,"WIFI P2P Peers changed action");
            // Call WifiP2pManager.requestPeers() to get a list of current peers

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (mManager != null) {
                mManager.requestPeers(mChannel, mPeerListListener);
            }

//            WifiP2pDeviceList deviceList = new WifiP2pDeviceList();
  //          mPeerListListener.onPeersAvailable(deviceList);
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing

        }
    }*/
}
