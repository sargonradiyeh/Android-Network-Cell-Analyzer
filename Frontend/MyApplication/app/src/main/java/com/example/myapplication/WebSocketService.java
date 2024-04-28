package com.example.myapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class WebSocketService extends Service {

    private static final String TAG = "WebSocketService";

    private Socket mSocket;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "WebSocketService onCreate");

        try {
            // Initialize Socket.IO client
            mSocket = IO.socket("https://jason.hydra-polaris.ts.net");

            // Connect to the server
            mSocket.connect();

            // Define event listeners
            mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    // Handle connection success
                    Log.d(TAG, "Connected to server");
                }
            }).on("client_count", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    // Handle client count update
                    JSONObject data = (JSONObject) args[0];
                    try {
                        int count = data.getInt("count");
                        Log.d(TAG, "Connected clients: " + count);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    // Handle disconnection
                    Log.d(TAG, "Disconnected from server");
                }
            });
        } catch (URISyntaxException e) {
            Log.e(TAG, "URISyntaxException: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "WebSocketService onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "WebSocketService onDestroy");
        // Disconnect from the server when the service is destroyed
        if (mSocket != null) {
            mSocket.disconnect();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
