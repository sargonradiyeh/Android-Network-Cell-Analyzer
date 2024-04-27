package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;
import org.json.JSONArray;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.List;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
public class StatsActivity extends AppCompatActivity {

    private static final String TAG = "StatsActivity";

    private Button startDateButton, endDateButton, sendButton, returnButton;

    private Calendar startDateCalendar, endDateCalendar;

    private TextView resultsTextView;

    private int count1=0;

    // Array of endpoints to fetch statistics from the server
    private static final String[] ENDPOINTS = {
            "/average_connectivity_time_per_operator",
            "/average_connectivity_time_per_network_type",
            "/average_signal_power_per_network_type",
            "/average_signal_power_per_device",
            "/average_snr_or_sinr_per_network_type"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        // Initialize buttons, text view, and calendar instances
        startDateButton = findViewById(R.id.startDateButton);
        endDateButton = findViewById(R.id.endDateButton);
        sendButton = findViewById(R.id.sendButton);
        returnButton = findViewById(R.id.returnButton);
        resultsTextView = findViewById(R.id.resultsTextView);

        startDateCalendar = Calendar.getInstance();
        endDateCalendar = Calendar.getInstance();
        // Set up click listener for the return button
        returnButton.setOnClickListener(new View.OnClickListener() {
            // Set up click listener for the start date and end date buttons
            @Override
            public void onClick(View v) {
                onBackPressed(); // Navigate back to the previous activity
            }
        });

        startDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(startDateCalendar, startDateButton);
            }
        });

        endDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(endDateCalendar, endDateButton);
            }
        });
        // Set up click listener for the send button

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendDates();
            }
        });
    }

    /**
     * Displays a date picker dialog for selecting a date.
     * @param calendar The calendar instance associated with the button.
     * @param button The button to display the selected date.
     */
    private void showDatePickerDialog(final Calendar calendar, final Button button) {
        // Get current year, month, and day
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a new date picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(StatsActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Set the selected date to the calendar
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // Format month and day to have leading zeros if they are single digits
                        String formattedMonth = String.format("%02d", monthOfYear + 1);
                        String formattedDayOfMonth = String.format("%02d", dayOfMonth);

                        // Set the text of the button to the selected date
                        button.setText(year + "-" + formattedMonth + "-" + formattedDayOfMonth);
                    }
                }, year, month, dayOfMonth);

        // Show the date picker dialog
        datePickerDialog.show();
    }

    /**
     * Sends the selected start and end dates to fetch statistics data.
     */
    private void sendDates() {
        // Get the start and end dates from the buttons
        String startDate = startDateButton.getText().toString();
        String endDate = endDateButton.getText().toString();
        // Execute the FetchStatsTask to retrieve statistics data
        new FetchStatsTask(resultsTextView).execute(startDate, endDate);
    }

    /**
     * AsyncTask to fetch statistics data from the server.
     */
    public class FetchStatsTask extends AsyncTask<String, Void, List<JSONObject>> {

        private static final String TAG = "FetchStatsTask";

        // Base URL of the Flask server
        private static final String BASE_URL = "https://jason.hydra-polaris.ts.net/stats";

        // TextView to display the results
        private TextView resultsTextView;

        /**
         * Constructor to initialize the FetchStatsTask with the resultsTextView.
         * @param resultsTextView The TextView to display the results.
         */
        public FetchStatsTask(TextView resultsTextView) {
            this.resultsTextView = resultsTextView;
        }

        @Override
        protected List<JSONObject> doInBackground(String... params) {
            String startDate = params[0];
            String endDate = params[1];

            // Retrieve token from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
            String token = sharedPreferences.getString("token", "");
            Log.d("Token", token);

            // List to store JSON objects
            List<JSONObject> results = new ArrayList<>();

            // Iterate through each endpoint
            try {
                for (String endpoint : ENDPOINTS) {
                    // Construct URL with start and end dates
                    URL url = new URL(BASE_URL + endpoint + "?start_date=" + startDate + "&end_date=" + endDate);
                    // Debugging log: Print the URL being fetched
                    Log.d(TAG, "Fetching data from URL: " + url.toString());
                    // Open connection to the URL
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("Authorization", "Bearer " + token); // Add the token to the request header
                    // Get the response code from the server
                    int responseCode = connection.getResponseCode();
                    // If response code is OK, read the response from the server
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        // Debugging log: Print the response received from the server
                        Log.d(TAG, "Response from server: " + response.toString()); // Log the response before parsing

                        // Parse the JSON array from the response and add each object to the results list
                        JSONArray jsonArray = new JSONArray(response.toString());
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject result = jsonArray.getJSONObject(i);
                            results.add(result);
                        }
                    } else {
                        // If response code is not OK, log the error code
                        Log.e(TAG, "HTTP error code: " + responseCode);
                    }
                }
            } catch (IOException | JSONException e) {
                // Catch and log any IOException or JSONException that may occur during processing
                e.printStackTrace();
                Log.e(TAG, "Error: " + e.getMessage());
            }
            // Return the list of JSON objects
            return results;
        }

        // Method called after the background task is completed
        // Checks if the list of JSON objects received from the server is not empty
        @Override
        protected void onPostExecute(List<JSONObject> results) {
            if (!results.isEmpty()) {
                // If not empty, print a debugging log with the number of JSON objects received
                Log.d(TAG, "Received " + results.size() + " JSON objects from server."); // Debugging log
                // Call the displayStats method to process and display the statistics
                displayStats(results);
            } else {
                // If the list is empty, print an error log
                Log.e(TAG, "No results received from server."); // Error log
                // Set the text in the results TextView to indicate failure to fetch statistics
                resultsTextView.setText("Failed to fetch statistics");
            }
        }
        // Method to display statistics received from the server
        private void displayStats(List<JSONObject> statsList) {
            // Clear any existing data in the StringBuilder
            StringBuilder resultBuilder = new StringBuilder();

            // Clear any existing data in the TextView
            resultsTextView.setText("");

            // Counter for limiting the number of displayed stats
            int statsDisplayed = 0;

            //Initialize lists to store data for bar charts
            List<String> operatorLabels = new ArrayList<>();
            List<Float> operatorValues = new ArrayList<>();
            List<String> networkLabels = new ArrayList<>();
            List<Float> networkValues = new ArrayList<>();
            List<Float> powerValues = new ArrayList<>();
            List<String> deviceLabels = new ArrayList<>();
            List<Float> deviceValues = new ArrayList<>();
            List<String> snrSINRLabels = new ArrayList<>();
            List<Float> snrSINRValues = new ArrayList<>();

            int count1 = 0;

            // Iterate through the JSON objects in the list
            for (JSONObject stats : statsList) {
                try {

                    if (stats.has("operator")) {
                        // Parse and display average connectivity time per operator
                        if(count1==0){
                            resultBuilder.append("Average Connectivity Time per Operator: ");
                            count1++;
                        }
                        String operator = stats.getString("operator");
                        double avgConnectivityTime = stats.optDouble("average_connectivity_percentage", 0.0); // Use optDouble to handle null values
                        resultBuilder.append(operator).append(": ").append(avgConnectivityTime).append(", ");

                        // Add data to operator bar chart
                        operatorLabels.add(operator);
                        operatorValues.add((float) avgConnectivityTime);
                    }
                    if (stats.has("average_connectivity_time")) {
                        // Parse and display average connectivity time per network type
                        if(count1==1){
                            resultBuilder.append("\n\nAverage Connectivity Time per Network Type: ");
                            count1++;
                        }
                        String networkType = stats.getString("network_type");
                        double avgConnectivityTime = stats.optDouble("average_connectivity_time", 0.0); // Use optDouble to handle null values
                        resultBuilder.append(networkType).append(": ").append(avgConnectivityTime).append(", ");

                        // Add data to network type bar chart
                        networkLabels.add(networkType);
                        networkValues.add((float) avgConnectivityTime);
                    }
                    if (stats.has("average_signal_power1")) {
                        // Parse and display average signal power per network type
                        if(count1==2){
                            resultBuilder.append("\n\nAverage Signal Power per Network Type: ");
                            count1++;
                        }
                        String networkType = stats.getString("network_type");
                        double avgSignalPower = stats.optDouble("average_signal_power1", 0.0); // Use optDouble to handle null values
                        resultBuilder.append(networkType).append(": ").append(avgSignalPower).append(", ");

                        // Add data to signal power per network type bar chart
                        networkLabels.add(networkType);
                        powerValues.add((float) avgSignalPower);
                    }
                    if (stats.has("device")) {
                        // Parse and display average signal power per device
                        if(count1==3){
                            resultBuilder.append("\n\nAverage Signal Power per Device: ");
                            count1++;
                        }
                        String device = stats.getString("device");
                        double avgSignalPower = stats.optDouble("average_signal_power2", 0.0); // Use optDouble to handle null values
                        if (statsDisplayed <= 5) {
                            resultBuilder.append(device).append(": ").append(avgSignalPower).append(", ");
                            deviceLabels.add(device);
                            deviceValues.add((float) avgSignalPower);
                        }
                        statsDisplayed++;

                        // Add data to device bar chart
                    }
                    if (stats.has("average_sinr")) {
                        // Parse and display average SNR or SINR per network type
                        if(count1==4){
                            resultBuilder.append("\n\nAverage SNR per Network Type: ");
                            count1++;
                        }
                        String networkType = stats.getString("network_type");
                        double avgSnrOrSinr = stats.optDouble("average_sinr", 0.0); // Use optDouble to handle null values
                        resultBuilder.append(networkType).append(": ").append(avgSnrOrSinr).append(", ");
                        statsDisplayed++;

                        // Add data to SNR or SINR per network type bar chart
                        snrSINRLabels.add(networkType);
                        snrSINRValues.add((float) avgSnrOrSinr);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Failed to parse statistics: " + e.getMessage()); // Error log
                    resultsTextView.setText("Failed to parse statistics");
                }
            }

            // Display the result in TextView
            resultsTextView.setText(resultBuilder.toString());
            count1 = 0;


            // Create and populate bar charts

            BarChart operatorChart;
            BarChart networkChart;
            BarChart powerChart;
            BarChart snrSINRChart;
            BarChart deviceChart;

            // Find and assign BarChart views from the layout
            operatorChart = findViewById(R.id.operatorChart);
            networkChart = findViewById(R.id.networkChart);
            powerChart = findViewById(R.id.powerChart);
            snrSINRChart = findViewById(R.id.snrSINRChart);
            deviceChart = findViewById(R.id.deviceChart);

            // Create bar charts with corresponding data
            createBarChart(operatorLabels, operatorValues, operatorChart, "Avg Connectivity Time/Operator (Sec)");
            createBarChart(networkLabels, networkValues, networkChart, "Avg Connectivity Time/Network (Sec)");
            createBarChart(networkLabels, powerValues, powerChart, "Avg Signal Power/Network (dBm)");
            createBarChart(deviceLabels, deviceValues, deviceChart, "Avg Signal Power/Device (dBm)");
            createBarChart(snrSINRLabels, snrSINRValues, snrSINRChart, "Avg SNR/Network");
        }

        /*
        Creates a bar chart with the given data.

        Parameters:
        - labels: List of String labels for the X-axis.
        - values: List of Float values for the Y-axis.
        - chart: The BarChart view to populate with data.
        - description: Description text to set for the chart.

        Functionality:
        - Creates a list of BarEntry objects from the provided labels and values.
        - Initializes a BarDataSet with the list of entries and a default label.
        - Creates a BarData object with the dataset.
        - Sets the data for the chart.
        - Retrieves the X-axis of the chart and sets the value formatter with the provided labels.
        - Invalidates the chart to refresh its display.
        */
        private void createBarChart(List<String> labels, List<Float> values, BarChart chart, String description) {
            ArrayList<BarEntry> entries = new ArrayList<>();
            for (int i = 0; i < values.size(); i++) {
                entries.add(new BarEntry(i, values.get(i)));
            }
            BarDataSet dataSet = new BarDataSet(entries, "Data");

            BarData barData = new BarData(dataSet);
            chart.setData(barData);
            XAxis xAxis = chart.getXAxis();
            chart.getDescription().setText(description);
            xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
            chart.invalidate();
        }




    }
}
