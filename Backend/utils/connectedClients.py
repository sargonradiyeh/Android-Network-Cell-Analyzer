from flask_socketio import SocketIO, emit

socketio = SocketIO()

connected_clients = 0

@socketio.on('connect')
def handle_connect():
    global connected_clients
    connected_clients += 1
    emit('client_count', {'count': connected_clients}, broadcast=True)

@socketio.on('disconnect')
def handle_disconnect():
    global connected_clients
    connected_clients -= 1
    emit('client_count', {'count': connected_clients}, broadcast=True)
