from flask import Flask, request
from extensions import db
from flask_bcrypt import Bcrypt
from flask_jwt_extended import JWTManager
from routes.api import api_bp
from routes.auth import auth_bp
from flask_migrate import Migrate
from routes.stats import stats_bp
from flask_socketio import SocketIO
import threading
import time

app = Flask(__name__)
app.config.from_pyfile('config.py')

db.init_app(app)
bcrypt = Bcrypt(app)
jwt = JWTManager(app)
socketio = SocketIO(app)

app.register_blueprint(auth_bp, url_prefix='/auth')
app.register_blueprint(api_bp, url_prefix='/api')
app.register_blueprint(stats_bp, url_prefix='/stats')

migrate = Migrate(app, db)

clients = {}

@socketio.on('connect')
def handle_connect():
    """
    Handles the 'connect' event when a client connects to the server.

    This function adds the client's ID and IP address to the 'clients' dictionary,
    and prints information about the new connection and the current connected clients.

    Parameters:
        None

    Returns:
        None
    """
    clients[request.sid] = request.remote_addr
    print(f"New connection: {request.sid} from {request.remote_addr}")
    print(f"Connected clients ({len(clients)}): {list(clients.values())}")


@socketio.on('disconnect')
def handle_disconnect():
    """
    Handles the 'disconnect' event from the client.

    If the client's session ID is found in the 'clients' dictionary,
    it removes the session ID from the dictionary and prints a message
    indicating that the client has been disconnected.

    Parameters:
        None

    Returns:
        None
    """
    if request.sid in clients:
        print(f"Disconnected: {request.sid} from {clients.pop(request.sid)}")

def print_connected_clients():
    """
    Continuously prints the number of connected clients and their IP addresses.

    This function runs in a separate thread and is called by the 'run_background_task' function.

    Parameters:
        None

    Returns:
        None
    """
    while True:
        time.sleep(5)
        print(f"Connected clients ({len(clients)}): {list(clients.values())}")

def run_background_task():
    """
    Runs the 'print_connected_clients' function in a separate thread.

    This function is called when the script is executed directly.

    Parameters:
        None

    Returns:
        None
    """
    thread = threading.Thread(target=print_connected_clients)
    thread.start()

if __name__ == '__main__':
    run_background_task()
    socketio.run(app, host='0.0.0.0', debug=True)
