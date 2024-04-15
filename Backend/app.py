from flask import Flask, jsonify
from models.cell_data import CellData
from extensions import db
from flask_bcrypt import Bcrypt
from flask_jwt_extended import JWTManager
from routes.api import api_bp
from routes.auth import auth_bp
from flask_migrate import Migrate
from routes.stats import stats_bp
from utils.connectedClients import socketio

app = Flask(__name__)
app.config.from_pyfile('config.py')

db.init_app(app)
bcrypt = Bcrypt(app)
jwt = JWTManager(app)
socketio.init_app(app)

app.register_blueprint(auth_bp, url_prefix='/auth')
app.register_blueprint(api_bp, url_prefix='/api')
app.register_blueprint(stats_bp, url_prefix='/stats')


from models import cell_data, user
migrate = Migrate(app, db)

@app.route('/connected_clients', methods=['GET'])
def get_connected_clients():
    return jsonify({'count': socketio.connected_clients}), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', debug=True)