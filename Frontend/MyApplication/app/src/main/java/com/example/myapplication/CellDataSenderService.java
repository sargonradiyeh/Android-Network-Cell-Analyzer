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

    @Override //responsible for providing the communication channel between the service and the activity or client components.
    public IBinder onBind(Intent intent) { //Input: Intent intent was used to bind to the service.
        return null; //Output: Returns null to indicate that the service does not support binding.
    }

    @Override //This method is called when the service is started using the startService() method.
    // It initializes necessary components for sending cell data to the backend and schedules periodic tasks.
    // It returns a value indicating how the system should handle the service after it has been started.
    public int onStartCommand(Intent intent, int flags, int startId) { //Input Intent intent: The intent that was used to start the service.
        // int flags: Flags indicating how the service should behave.
        // int startId: A unique integer representing the start request.
        Log.d("CellDataSenderService", "onStartCommand called");
        // Initialize Capture info
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        retrieveData = new RetrieveData(telephonyManager);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Start sending cell data to backend every 10 seconds
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::sendCellDataToBackend, 0, 10, TimeUnit.SECONDS);

        return START_STICKY; //Returns a value indicating how the system should handle the service after it has been started
    }

    // Method to send cell data to the backend
    //This method sends cell data to a backend server using HTTP POST request.
    // It collects cell data using the retrieveData.collectinfo() method, creates a JSON object with the collected data, and sends it to the backend server.
    // The method retrieves an authentication token from SharedPreferences to include in the request header for authorization.
    // It logs the JSON data before sending and after sending, as well as any errors that occur during the process.
    private void sendCellDataToBackend() {
        AsyncTask.execute(() -> {
            try {
                // Get data
                String[] info = retrieveData.collectinfo();

                // Create JSON object with cell data
                JSONObject cellDataJson = new JSONObject();
                cellDataJson.put("operator", info[0]); // Assign operator data to JSON object
                cellDataJson.put("signal_power", info[1]); // Assign signal power data to JSON object
                cellDataJson.put("sinr", info[2]); // Assign SINR data to JSON object
                cellDataJson.put("network_type", info[3]); // Assign network type data to JSON object
                cellDataJson.put("frequency_band", info[4]); // Assign frequency band data to JSON object
                cellDataJson.put("cell_id", info[5]); // Assign cell ID data to JSON object
                cellDataJson.put("timestamp", info[6]); // Assign timestamp data to JSON object
                Log.d("CellDataSenderService", "JSON data before sending: " + cellDataJson.toString()); // Log JSON data before sending

                // Retrieve token from SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
                String token = sharedPreferences.getString("token", ""); // Retrieve token from SharedPreferences
                Log.d("Token", token); // Log token

                // Create HTTP connection
                URL url = new URL("https://jason.hydra-polaris.ts.net/api/cell_data");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.setRequestProperty("Authorization", "Bearer " + token); // Include token in request header
                httpURLConnection.setDoOutput(true);
                String jsonData = cellDataJson.toString();
                Log.d("CellDataSenderService", "JSON data to send: " + jsonData); // Log JSON data to send

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