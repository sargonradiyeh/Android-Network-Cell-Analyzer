package com.example.myapplication;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * Activity for user signup functionality.
 */
public class SignupActivity extends AppCompatActivity {

    // UI elements
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button signupButton;
    private Button returnButton;

    /*
     * Called when the activity is first created.
     * Responsible for setting up the UI and defining click listeners.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize UI elements
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signupButton = findViewById(R.id.signupButton);
        returnButton = findViewById(R.id.returnButton);

        // Set click listener for return button to navigate back
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Navigate back to the previous activity
            }
        });

        // Set click listener for signup button to handle user signup process
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get entered username and password
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                // Validate password
                if (!isValidPassword(password)) {
                    // Password does not meet requirements
                    Toast.makeText(getApplicationContext(), "Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one digit, and one special character", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create a JSON object to send to the server for signup
                JSONObject postData = new JSONObject();
                try {
                    postData.put("username", username);
                    postData.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Send a POST request to the server for signup
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Establish connection with server
                            URL url = new URL("https://jason.hydra-polaris.ts.net/auth/signup");
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setRequestMethod("POST");
                            connection.setRequestProperty("Content-Type", "application/json");
                            connection.setDoOutput(true);
                            connection.setConnectTimeout(4000); // Set timeout to 4 seconds
                            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                            outputStream.writeBytes(postData.toString());
                            outputStream.flush();
                            outputStream.close();

                            // Get response code from server
                            int responseCode = connection.getResponseCode();
                            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                                // Signup successful
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "Signup successful!", Toast.LENGTH_SHORT).show();
                                        Log.d("SignupActivity", "Response Code: " + responseCode); // Log response code

                                        // Optionally, redirect to login page
                                    }
                                });
                            } else {
                                // Signup failed
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "Signup failed. Please try again later.", Toast.LENGTH_SHORT).show();
                                        Log.d("SignupActivity", "Response Code: " + responseCode); // Log response code

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
    }

    /*
     * Method to validate password based on defined criteria.
     *
     * param password Password to be validated.
     * return True if password is valid, false otherwise.
     */
    private boolean isValidPassword(String password) {
        // Implement password validation logic
        // Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one digit, and one special character
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*") &&
                password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
    }
}
