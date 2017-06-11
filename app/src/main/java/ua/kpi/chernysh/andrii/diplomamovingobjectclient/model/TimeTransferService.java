package ua.kpi.chernysh.andrii.diplomamovingobjectclient.model;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import ua.kpi.chernysh.andrii.diplomamovingobjectclient.controler.activity.WiFiDirectActivity;
import ua.kpi.chernysh.andrii.diplomamovingobjectclient.controler.fragment.DeviceDetailFragment;

import static ua.kpi.chernysh.andrii.diplomamovingobjectclient.model.FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS;
import static ua.kpi.chernysh.andrii.diplomamovingobjectclient.model.FileTransferService.EXTRAS_GROUP_OWNER_PORT;

/**
 * Created by Dron on 10-Jun-17.
 */

public class TimeTransferService extends IntentService {
    public static final String ACTION_SEND_TIME = "com.example.android.wifidirect.SEND_TIME";
    public static final String EXTRAS_GROUP_OWNER_PORT_TIME = "go_port";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS_TIME = "go_host";
    private static final int SOCKET_TIMEOUT_TIME = 5000;

    public TimeTransferService(String name) {
        super(name);
    }
    public TimeTransferService() {
        super("TimeTransferService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_TIME)) {
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT_TIME);
            try {
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT_TIME);

                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeLong(new Date().getTime());
                Log.d(WiFiDirectActivity.TAG,"Start transfer time : " +
                        new SimpleDateFormat("HH:mm:ss.SSS").format(new Date().getTime()));
            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
