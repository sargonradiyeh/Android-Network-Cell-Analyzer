package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends AppCompatActivity {

    private TelephonyManager telephonyManager;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String SERVER_URL = "YOUR_SERVER_URL_HERE"; // Replace with your server URL
    private static final long INTERVAL = 10 * 1000; // 10 seconds interval

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);

        // Check and request permissions for accessing phone state
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_CODE);
            } else {
                initializeTelephonyManager();
                startSendingDataToServer();
            }
        } else {
            initializeTelephonyManager();
            startSendingDataToServer();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeTelephonyManager();
                startSendingDataToServer();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initializeTelephonyManager() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    }

    private void startSendingDataToServer() {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendCellDataToServer();
            }
        }, 0, INTERVAL);
    }

    private void sendCellDataToServer() {
        String operator = telephonyManager.getNetworkOperatorName();
        int signalStrength = telephonyManager.getSignalStrength().getLevel();
        int networkTypeInt = telephonyManager.getNetworkType();
        String networkType = getNetworkType(networkTypeInt);
        String frequencyBand = telephonyManager.getNetworkBand();
        String cellId = telephonyManager.getCellLocation().toString();
        String timestamp = new Date().toString(); // Current timestamp

        JSONObject postData = new JSONObject();
        try {
            postData.put("operator", operator);
            postData.put("signal_power", signalStrength);
            postData.put("network_type", networkType);
            postData.put("frequency_band", frequencyBand);
            postData.put("cell_id", cellId);
            postData.put("timestamp", timestamp);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(SERVER_URL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setDoOutput(true);
                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                    outputStream.writeBytes(postData.toString());
                    outputStream.flush();
                    outputStream.close();

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Handle success response
                    } else {
                        // Handle failure response
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // Handle connection error
                }
            }
        }).start();
    }

    private String getNetworkType(int networkTypeInt) {
        switch (networkTypeInt) {
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return "Unknown";
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "2G (GPRS)";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "2G (EDGE)";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "3G (UMTS)";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "3G (HSDPA)";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "3G (HSUPA)";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "3G (HSPA)";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G (LTE)";
            default:
                return "Unknown";
        }
    }
}
