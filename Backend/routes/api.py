from datetime import datetime
from flask import Blueprint, request, jsonify
from models.cell_data import CellData
from app import db

api_bp = Blueprint('api', __name__)

@api_bp.route('/cell_data', methods=['POST'])
def add_cell_data():
    data = request.json

    new_cell_data = CellData(
        operator=data['operator'],
        signal_power=data['signal_power'],
        sinr=data['sinr'],
        network_type=data['network_type'],
        frequency_band=data['frequency_band'],
        cell_id=data['cell_id'],
        timestamp=datetime.strptime(data['timestamp'], '%d %b %Y %I:%M %p') 
    )

    db.session.add(new_cell_data)

    try:
        db.session.commit()
        return jsonify({'message': 'Cell data added successfully'}), 201
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500
    finally:
        db.session.close()

@api_bp.route('/cell_data', methods=['GET'])
def get_all_cell_data():
    cell_data_records = CellData.query.all()

    result = [{
        'operator': record.operator,
        'signal_power': record.signal_power,
        'sinr': record.sinr,
        'network_type': record.network_type,
        'frequency_band': record.frequency_band,
        'cell_id': record.cell_id,
        'timestamp': record.timestamp.strftime('%d %b %Y %I:%M %p')  # Convert datetime object to string
    } for record in cell_data_records]

    return jsonify(result), 200

# Additional routes for retrieving statistics and handling user requests can be added here
