package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class HomeActivity extends AppCompatActivity {
    private RetrieveData retrieveData;
    private TextView textViewCellData;
    private TelephonyManager telephonyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button statsButton = findViewById(R.id.statsButton);

        // Set an OnClickListener to the button
        statsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the StatsActivity
                startActivity(new Intent(HomeActivity.this, StatsActivity.class)); // Functionality: Starts the StatsActivity when the button is clicked. Input: View v
            }
        });
        // Initialize TelephonyManager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE); // Functionality: Initializes the TelephonyManager to access telephony services.
        TaskUpdate taskUpdate = new TaskUpdate(telephonyManager);

        // Initialize Capture info with TelephonyManager
        retrieveData = new RetrieveData(telephonyManager); // Functionality: Initializes RetrieveData object with TelephonyManager.

        // Get reference to TextView for displaying cell data
        textViewCellData = findViewById(R.id.textViewCellData); // Functionality: Gets a reference to the TextView for displaying cell data.

        // Call method to display cell data every 10 seconds
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                displayCellData(); // Functionality: Calls the method to display cell data.
                handler.postDelayed(this, 10000); // Schedule the method to run again after 10 seconds
            }
        }, 0);
    }


    // Method to display cell data in the TextView
    private void displayCellData() {
        // Get info data
        retrieveData = new RetrieveData(telephonyManager); // Functionality: Initializes RetrieveData object with TelephonyManager.
        String[] info = retrieveData.collectinfo(); // Functionality: Collects cell data. Output: Array of cell data

        // Display cell data in TextView
        System.out.println("Telephony hit:" + telephonyManager.getSignalStrength().getCellSignalStrengths().get(0).toString()); // Functionality: Prints telephony signal strength to console.
        final String cellDataString = "Operator: " + info[0] + "\n"
                + "Signal Power: " + info[1] + " dBm"+ "\n"
                + "SNR: " + info[2] + " dB"+ "\n"
                + "Network Type: " + info[3] + "\n"
                + "Frequency Band: " + info[4] + " MHz" + "\n"
                + "Cell ID: " + info[5] + "\n"
                + "Timestamp: " + info[6] + "\n"
                + "MAC: " + info[7] + "\n\n"; // Functionality: Formats cell data into a string. Input: Array of cell data, Output: Formatted cell data string
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewCellData.setText(cellDataString); // Functionality: Sets the text of the TextView to display cell data.
            }
        });
    }
}

