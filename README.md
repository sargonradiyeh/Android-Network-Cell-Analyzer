# Cellular Data Analysis Project

## Overview
The Cellular Data Analysis Project is a Flask-based web application designed to analyze and visualize cellular network data. It provides various endpoints for users to access statistical insights into network performance metrics such as signal power, connectivity time, and network type. The project utilizes Flask, SQLAlchemy for database management, Flask-JWT-Extended for authentication, and Flask-SocketIO for WebSocket support.

## Setup and Running the Server

Before running the server, ensure that all dependencies are installed and the database is set up properly.

### Installation
Install the required packages from `requirements.txt`:

   ```bash
   pip install -r requirements.txt
   ```


### Database Setup
To set up and initialize the database, run the following commands:

```bash
flask db init
flask db migrate
flask db upgrade
```


### Running the Server
To run the server on your local machine, use the following command:

```bash
flask run --host=0.0.0.0 --debug --port=5000
```

### Accessing the Deployed Server
The backend has been deployed on a local server accessible via:

[https://jason.hydra-polaris.ts.net](https://jason.hydra-polaris.ts.net)

*Note: The server can be turned on upon request as it is typically kept offline to protect local ports from public access.*

## API Routes and Functionalities

This API consists of multiple endpoints grouped by their functionality, and statistical data retrieval.

1. `/auth/login`: Handles user authentication by accepting POST requests containing JSON data with 'username' and 'password' fields. Returns a JWT access token upon successful authentication.

2. `/api/cell_data`: Allows users to add cellular data records through POST requests. Requires JWT authentication. Validates and processes incoming data and stores it in the database.

3. `/stats/average_connectivity_time_per_operator`: Computes the average connectivity time per operator for a given user within a specified time range.

4. `/stats/average_connectivity_time_per_network_type`: Calculates the average connectivity time per network type for a given user within a specified time range.

5. `/stats/average_signal_power_per_network_type`: Computes the average signal power per network type for a given user within a specified time range.

6. `/stats/average_signal_power_per_device`: Calculates the average signal power per device (cell ID) for a given user within a specified time range.

7. `/stats/average_snr_or_sinr_per_network_type`: Computes the average Signal-to-Noise Ratio (SNR) or Signal-to-Interference-plus-Noise Ratio (SINR) per network type for a given user within a specified time range.

These endpoints provide detailed insights into various aspects of cellular network performance, facilitating informed decision-making for network optimization and management.
