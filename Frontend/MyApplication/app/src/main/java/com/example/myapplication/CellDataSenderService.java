package com.example.myapplication;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CellDataSenderService extends Service {
    private RetrieveData retrieveData;
    private SharedPreferences sharedPreferences;
    private TelephonyManager telephonyManager;
    private ScheduledExecutorService scheduler;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Initialize CaptureSnapshot
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        retrieveData = new RetrieveData(telephonyManager);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Start sending cell data to backend every 10 seconds
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::sendCellDataToBackend, 0, 10, TimeUnit.SECONDS);

        return START_STICKY;
    }

    // Method to send cell data to the backend
    private void sendCellDataToBackend() {
        AsyncTask.execute(() -> {
            try {
                // Get snapshot data
                String[] snapshot = retrieveData.collectinfo();

                // Create JSON object with cell data
                JSONObject cellDataJson = new JSONObject();
                cellDataJson.put("operator", snapshot[0]);
                cellDataJson.put("signal_power", Float.parseFloat(snapshot[1]));
                cellDataJson.put("sinr", Float.parseFloat(snapshot[2]));
                cellDataJson.put("network_type", snapshot[3]);
                cellDataJson.put("frequency_band", snapshot[4]);
                cellDataJson.put("cell_id", snapshot[5]);
                cellDataJson.put("timestamp", snapshot[6]);

                // Retrieve token from SharedPreferences
                String token = sharedPreferences.getString("token", "");

                // Create HTTP connection
                URL url = new URL("YOUR_BACKEND_URL/cell_data");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setRequestProperty("Authorization", "Bearer " + token); // Include token in header
                httpURLConnection.setDoOutput(true);

                // Send data to backend
                try (OutputStream outputStream = httpURLConnection.getOutputStream()) {
                    outputStream.write(cellDataJson.toString().getBytes());
                }

                // Get response code from backend
                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    // Success: Cell data added successfully
                    Log.d("BackendResponse", "Cell data added successfully");
                } else {
                    // Error: Failed to add cell data
                    Log.e("BackendResponse", "Failed to add cell data. Response code: " + responseCode);
                }

                // Close HTTP connection
                httpURLConnection.disconnect();
            } catch (Exception e) {
                // Error: Exception occurred while sending data
                Log.e("BackendResponse", "Error sending cell data: " + e.getMessage());
            }
        });
    }

    @Override
    public void onDestroy() {
        // Shutdown scheduler when service is destroyed
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
        super.onDestroy();
    }
}
