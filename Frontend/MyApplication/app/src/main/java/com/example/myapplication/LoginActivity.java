package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.content.Intent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;




public class LoginActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private TextView attemptsLeftTextView;
    private int attemptsLeft;
    private CountDownTimer timer;
    private SharedPreferences sharedPreferences;
    private int timerDuration = 150000;//300000; // Initial timer duration: 2.5 minutes
    private Button loginButton;

    @Override
    // Method executed when the activity is created
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Call the superclass method to perform any initialization tasks
        setContentView(R.layout.activity_login); // Set the activity content to an explicit view

        // Initialize UI elements
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        attemptsLeftTextView = findViewById(R.id.attemptsLeftTextView);
        loginButton = findViewById(R.id.loginButton);
        Button signupButton = findViewById(R.id.signupButton);

        // Request Permissions
        // Request necessary permissions from the user
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CHANGE_NETWORK_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_WIFI_STATE}, PackageManager.PERMISSION_GRANTED);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);

        // Retrieve attempts_left value from SharedPreferences, default to 5
        attemptsLeft = sharedPreferences.getInt("attempts_left", 5);
        attemptsLeftTextView.setText(Integer.toString(attemptsLeft));

        // Retrieve timerDuration value from SharedPreferences, default to 2.5 minutes
        timerDuration = sharedPreferences.getInt("timerDuration", 150000);

        // Start the timer only if attempts_left is 0
        if (attemptsLeft == 0) {
            startTimer(); // Start a timer to limit login attempts
        }

        // Set OnClickListener to the login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Execute when the login button is clicked

                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                // Check if username or password is empty
                if (username.isEmpty() || password.isEmpty()) {
                    //Toast.makeText(getApplicationContext(), "Please enter both Username and Password", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class)); // Redirect to home activity if username or password is empty
                    startCellDataSender(); // Start background service to send cell data
                    finish(); // Close LoginActivity
                    return;
                }
                // Create a JSON object to send to the server
                JSONObject postData = new JSONObject();
                try {
                    postData.put("username", username);
                    postData.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Send a POST request to the server
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL("https://jason.hydra-polaris.ts.net/auth/login");
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("POST");
                            connection.setRequestProperty("Content-Type", "application/json");
                            connection.setDoOutput(true);
                            connection.setConnectTimeout(4000); // Set timeout to 4 seconds
                            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                            outputStream.writeBytes(postData.toString());
                            outputStream.flush();
                            outputStream.close();

                            int responseCode = connection.getResponseCode();
                            Log.d("LoginActivity", "Response Code: " + responseCode); // Log response code

                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                // Successful login
                                try {
                                    InputStream inputStream = connection.getInputStream();
                                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                                    StringBuilder response = new StringBuilder();
                                    String line;
                                    while ((line = bufferedReader.readLine()) != null) {
                                        response.append(line);
                                    }
                                    bufferedReader.close();
                                    inputStream.close();

                                    // Parse JSON response to get the token
                                    JSONObject jsonResponse = new JSONObject(response.toString());
                                    final String token = jsonResponse.getString("token");

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "Login successful!", Toast.LENGTH_SHORT).show();
                                            saveToken(token); // Save the token securely
                                            startWebSocketService(); // Start WebSocket service
                                            attemptsLeft = 5; // Reset attempts_left counter
                                            timerDuration = 150000; // Reset timerDuration
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putInt("attempts_left", attemptsLeft);
                                            editor.putInt("timerDuration", timerDuration);
                                            editor.apply();
                                            attemptsLeftTextView.setText(Integer.toString(attemptsLeft));
                                            startCellDataSender(); // Start background service to send cell data
                                            startActivity(new Intent(LoginActivity.this, HomeActivity.class)); // Proceed to next activity
                                            finish(); // Close LoginActivity
                                        }

                                    });


                                } catch (IOException | JSONException e) {
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), "Error processing response", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                                // Invalid username or password
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "Invalid username or password", Toast.LENGTH_SHORT).show();
                                        if (attemptsLeft>0){
                                            attemptsLeft--;
                                        }
                                        attemptsLeftTextView.setText(Integer.toString(attemptsLeft));
                                        Log.d("TimerValue", String.valueOf(timer));
                                        if (attemptsLeft == 0 && timer == null) {
                                            loginButton.setEnabled(false);
                                            startTimer(); // Start the timer
                                        }
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putInt("attempts_left", attemptsLeft);
                                        editor.apply();
                                    }
                                });
                            }
                            else {
                                // Handles other response codes as server error
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        Toast.makeText(getApplicationContext(), "Server error. Please try again later.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            // Handle connection error
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Unable to connect to the server. Please check your internet connection.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });

        // Set OnClickListener to the signup button
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Execute when the signup button is clicked
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)); // Start the SignupActivity
            }
        });
    }





    // Method to start the timer
    private void startTimer() {
        timer = new CountDownTimer(timerDuration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutesLeft = millisUntilFinished / 60000;
                long secondsLeft = (millisUntilFinished % 60000) / 1000;
                if (minutesLeft == 0 && secondsLeft == 0) {
                    attemptsLeftTextView.setText(Integer.toString(attemptsLeft));
                } else {
                    attemptsLeftTextView.setText(attemptsLeft + "\nTime Left: " + minutesLeft + ":" + String.format("%02d", secondsLeft));
                    attemptsLeftTextView.setGravity(Gravity.CENTER);
                }
            }

            @Override
            public void onFinish() {
                // Timer finished, reset attempts_left to , double timerDuration, and update SharedPreferences
                timer = null; // Set timer to null when finished
                attemptsLeft = 5;
                timerDuration = timerDuration * 2;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("attempts_left", attemptsLeft);
                editor.putInt("timerDuration", timerDuration);
                editor.apply();
                loginButton.setEnabled(true);
                attemptsLeftTextView.setText(Integer.toString(attemptsLeft));
            }
        }.start();
    }

    // Override onDestroy to cancel the timer and save the attempts_left value
    @Override
    // Method executed when the activity is destroyed
    protected void onDestroy() {
        super.onDestroy(); // Call the superclass method to perform any necessary cleanup tasks
        if (timer != null) {
            timer.cancel(); // Cancel the timer if it exists
        }
        // Update the attempts_left value in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("attempts_left", attemptsLeft);
        editor.apply();
    }

    // Method to save the token securely in SharedPreferences
    private void saveToken(String token) {
        // Save the token to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.apply();
    }

    // Method to start the background service for sending cell data
    private void startCellDataSender() {
        // Create an intent to start the CellDataSenderService
        Intent serviceIntent = new Intent(LoginActivity.this, CellDataSenderService.class);
        startService(serviceIntent); // Start the service
    }

    // Method to start the WebSocket service
    private void startWebSocketService() {
        // Create an intent to start the WebSocketService
        Intent serviceIntent = new Intent(LoginActivity.this, WebSocketService.class);
        startService(serviceIntent); // Start the service
    }

}