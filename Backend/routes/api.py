from datetime import datetime
from flask import Blueprint, request, jsonify
from models.cell_data import CellData
from app import db
from flask_jwt_extended import jwt_required, get_jwt_identity

api_bp = Blueprint('api', __name__)

@api_bp.route('/cell_data', methods=['POST'])
@jwt_required()
def add_cell_data():
    try:
        data = request.json
        required_fields = ['operator', 'signal_power', 'sinr', 'network_type', 'frequency_band', 'cell_id', 'timestamp']
        if not all(field in data for field in required_fields):
            raise ValueError("Missing required field(s)")
        
        current_user_id = get_jwt_identity()
        
        signal_power = data['signal_power']
        sinr = data['sinr']
        network_type = data['network_type']
        
        if network_type in ['2G', '3G'] and signal_power == 'N/A':
            signal_power = "n/a"  # Set to None to store NULL in the database
        if network_type in ['2G', '3G'] and sinr == 'N/A':
            sinr = "n/a"  # Set to None to store NULL in the database
        
        new_cell_data = CellData(
            user_id=current_user_id,
            operator=data['operator'],
            signal_power=signal_power,
            sinr=sinr,
            network_type=network_type,
            frequency_band=data['frequency_band'],
            cell_id=data['cell_id'],
            timestamp=datetime.strptime(data['timestamp'], '%d %b %Y %I:%M %p')
        )
        
        db.session.add(new_cell_data)
        db.session.commit()
        
        return jsonify({'message': 'Cell data added successfully'}), 201
    except ValueError as ve:
        db.session.rollback()
        print('Error adding cell data:', str(ve))
        return jsonify({'error': str(ve)}), 422
    except Exception as e:
        db.session.rollback()
        print('Error adding cell data:', str(e))
        return jsonify({'error': str(e)}), 500


@api_bp.route('/cell_data', methods=['GET'])
@jwt_required() 
def get_all_cell_data():
    current_user_id = get_jwt_identity()
    cell_data_records = CellData.query.filter_by(user_id=current_user_id).all()

    result = [{
        'operator': record.operator,
        'signal_power': record.signal_power,
        'sinr': record.sinr,
        'network_type': record.network_type,
        'frequency_band': record.frequency_band,
        'cell_id': record.cell_id,
        'timestamp': record.timestamp.strftime('%d %b %Y %I:%M %p')  
    } for record in cell_data_records]

    return jsonify(result), 200