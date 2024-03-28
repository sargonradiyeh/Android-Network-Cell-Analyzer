from flask import Flask
from extensions import db
from routes.api import api_bp

app = Flask(__name__)
app.config.from_pyfile('config.py')

db.init_app(app)

app.register_blueprint(api_bp, url_prefix='/api')

def calculate_average_connectivity_time(operator):
    cell_data_records = CellData.query.filter_by(operator=operator).all()

    total_connectivity_time = 0
    num_records = len(cell_data_records)

    for record in cell_data_records:
        total_connectivity_time += record.timestamp.timestamp()  

    if num_records > 0:
        average_connectivity_time = total_connectivity_time / num_records
    else:
        average_connectivity_time = 0

    return average_connectivity_time

def calculate_avg_signal_power_per_network_type(network_type):
    cell_data_records = CellData.query.filter_by(network_type=network_type).all()

    total_signal_power = 0
    num_records = len(cell_data_records)

    for record in cell_data_records:
        total_signal_power += record.signal_power

    if num_records > 0:
        average_signal_power = total_signal_power / num_records
    else:
        average_signal_power = 0

    return average_signal_power

if __name__ == '__main__':
    app.run(debug=True)
