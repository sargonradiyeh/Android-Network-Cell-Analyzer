package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import android.telephony.TelephonyManager;

public class HomeActivity extends AppCompatActivity {
    private RetrieveData retrieveData;
    private TextView textViewCellData;
    private TelephonyManager telephonyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize TelephonyManager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        TaskUpdate taskUpdate = new TaskUpdate(telephonyManager);

        // Initialize CaptureSnapshot with TelephonyManager
        retrieveData = new RetrieveData(telephonyManager);


        // Get reference to TextView for displaying cell data
        textViewCellData = findViewById(R.id.textViewCellData);

        // Call method to display cell data every 10 seconds
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                displayCellData();
                handler.postDelayed(this, 10000); // Schedule the method to run again after 10 seconds
            }
        }, 0);
    }

    // Method to display cell data in the TextView
    private void displayCellData() {
        // Get snapshot data
        retrieveData = new RetrieveData(telephonyManager);
        String[] snapshot = retrieveData.collectinfo();

        // Display cell data in TextView
        System.out.println("Telephony hit:" + telephonyManager.getSignalStrength().getCellSignalStrengths().get(0).toString());
        final String cellDataString = "Operator: " + snapshot[0] + "\n"
                + "Signal Power: " + snapshot[1] + "\n"
                + "SINR: " + snapshot[2] + "\n"
                + "Network Type: " + snapshot[3] + "\n"
                + "Frequency Band: " + snapshot[4] + "\n"
                + "Cell ID: " + snapshot[5] + "\n"
                + "Timestamp: " + snapshot[6] + "\n\n";
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewCellData.setText(cellDataString);
            }
        });
    }
}
