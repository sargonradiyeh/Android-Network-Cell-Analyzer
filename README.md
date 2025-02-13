# Network Cell Analyzer

## Overview

Network Cell Analyzer is an Android application designed to provide in-depth analysis of cellular network data. By capturing key parameters—such as cell IDs, frequency bands, operator details, signal strength, and network type—the app offers real-time insights into mobile network performance. The collected data is transmitted to a centralized Flask-based server, where it is processed, stored, and visualized.

---

## Features

- **Real-Time Data Collection:**  
  Gathers essential cellular information from mobile devices using Android telephony APIs.

- **Secure User Authentication:**  
  Implements login and sign-up functionalities with JWT-based authentication.

- **Data Transmission & Storage:**  
  Periodically sends collected cell data to a Flask server where it is stored using a database (managed via Flask-Migrate).

- **WebSocket Communication:**  
  Uses Flask-SocketIO for real-time updates between the server and client applications.

- **Statistics & Visualization:**  
  Provides statistical analysis (e.g., average connectivity time, signal power, and SNR) with data visualization through charts and graphs.

- **Server UI:**  
  Features a web-based interface for monitoring connected devices, viewing network performance, and managing data.

---

## Architecture

The project is divided into two main components:

1. **Android Frontend:**  
   - Developed using **Java** and **XML**.  
   - Handles user interactions (login, signup, home screen, and statistics display).  
   - Collects cell data from the device and communicates with the backend.

2. **Flask Backend:**  
   - Built with **Python** using the Flask framework.  
   - Manages user authentication, data processing, and API endpoints.  
   - Employs **Flask-SocketIO** for real-time communication and **Flask-Migrate** for database management.

---

## Setup and Installation

### Prerequisites

- **Python 3.x**
- **pip** (Python package manager)
- **Android Studio** (for the Android app)

### Backend Setup

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/your-username/your-repo.git
   cd your-repo
   ```

2. **Install Dependencies:**
   ```bash
   pip install -r requirements.txt
   ```

3. **Database Initialization:**
   Set up the database using Flask-Migrate:
   ```bash
   flask db init
   flask db migrate
   flask db upgrade
   ```

4. **Run the Server:**
   ```bash
   flask run --host=0.0.0.0 --debug --port=5000
   ```

### Android App Setup

- Open the project in **Android Studio**.
- Ensure all necessary permissions are declared in `AndroidManifest.xml`.
- Build and run the app on an Android device or emulator.

---

## Testing

The application has been rigorously tested across multiple functionalities:

- **Authentication:**  
  Secure login and signup processes have been validated with proper error handling.
  
- **Real-Time Updates:**  
  WebSocket connectivity has been verified to ensure seamless real-time communication.
  
- **Data Transmission:**  
  Periodic sending of cell data from the Android app to the backend has been confirmed.
  
- **Statistics & Visualization:**  
  Statistical data retrieval and chart displays have been tested for accuracy and responsiveness.

---

## Team Members

- **Sarjoun Radiyeh:**  
  Android Login, Signup, Home Screen, Cell Data Sender, Data Retrieval, WebSocket Integration, and Testing.

- **Jason Salem:**  
  Backend Development, Database Configuration, Realtime Socket Implementation, Deployment, and Testing.
  
- **Hadi El Nawfal:**  
  Android Statistics, XML Layouts for Statistics, Debugging/Testing, and Demo Video Production.
  
- **Serop Elmayan:**  
  Server UI Development and Presentation.
  
- **Adel El Kadi:**  
  Android XML Design and Presentation.

---

