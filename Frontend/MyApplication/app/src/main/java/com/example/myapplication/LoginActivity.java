package com.example.myapplication;

import android.os.Bundle;
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




public class LoginActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private TextView attemptsLeftTextView;
    private int attemptsLeft;
    private CountDownTimer timer;
    private SharedPreferences sharedPreferences;
    private int timerDuration = 300000; // Initial timer duration: 5 minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        attemptsLeftTextView = findViewById(R.id.attemptsLeftTextView);
        Button loginButton = findViewById(R.id.loginButton);
        Button signupButton = findViewById(R.id.signupButton);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);

        // Retrieve attempts_left value from SharedPreferences, default to 5
        attemptsLeft = sharedPreferences.getInt("attempts_left", 5);
        attemptsLeftTextView.setText(Integer.toString(attemptsLeft));

        // Start the timer only if attempts_left is 5
        if (attemptsLeft == 5) {
            startTimer();
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                // Check if username or password is empty
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please enter both Username and Password", Toast.LENGTH_SHORT).show();
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
                            URL url = new URL("https://f287-194-146-32-177.ngrok-free.app/auth/login");
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
                                // Successful login
                                try {
                                    // Read token from response body
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
                                            // Store token securely
                                            saveToken(token);
                                            // Reset attempts_left and update SharedPreferences
                                            attemptsLeft = 5;
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putInt("attempts_left", attemptsLeft);
                                            editor.apply();
                                            attemptsLeftTextView.setText(Integer.toString(attemptsLeft));
                                            // Proceed to next activity or perform any necessary action
                                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
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
                                        attemptsLeft--;
                                        attemptsLeftTextView.setText(Integer.toString(attemptsLeft));
                                        if (attemptsLeft == 0) {
                                            loginButton.setEnabled(false);
                                        } else {
                                            // Double the timer duration for the next attempt
                                            timerDuration *= 2;
                                            startTimer();
                                        }
                                        // Update SharedPreferences
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putInt("attempts_left", attemptsLeft);
                                        editor.apply();
                                    }
                                });
                            }
                             else {
                                // Handle other response codes
                                // For example, server error
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
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start SignupActivity
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }


    // Method to start the timer
    private void startTimer() {
        timer = new CountDownTimer(timerDuration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Timer is ticking
            }

            @Override
            public void onFinish() {
                // Timer finished, reset attempts_left to 5 and update SharedPreferences
                attemptsLeft = 5;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("attempts_left", attemptsLeft);
                editor.apply();
                attemptsLeftTextView.setText(Integer.toString(attemptsLeft));
            }
        }.start();
    }

    // Override onDestroy to cancel the timer and save the attempts_left value
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("attempts_left", attemptsLeft);
        editor.apply();
    }
    private void saveToken(String token) {
        // Save the token to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.apply();
    }
}

