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
    clients[request.sid] = request.remote_addr
    print(f"New connection: {request.sid} from {request.remote_addr}")
    print(f"Connected clients ({len(clients)}): {list(clients.values())}")


@socketio.on('disconnect')
def handle_disconnect():
    if request.sid in clients:
        print(f"Disconnected: {request.sid} from {clients.pop(request.sid)}")

def print_connected_clients():
    while True:
        time.sleep(5)
        print(f"Connected clients ({len(clients)}): {list(clients.values())}")

def run_background_task():
    thread = threading.Thread(target=print_connected_clients)
    thread.start()

if __name__ == '__main__':
    run_background_task()
    socketio.run(app, host='0.0.0.0', debug=True)
