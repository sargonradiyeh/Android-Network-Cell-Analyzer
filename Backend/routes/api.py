from datetime import datetime
from flask import Blueprint, request, jsonify
from models.cell_data import CellData
from app import db
from flask_jwt_extended import jwt_required, get_jwt_identity

api_bp = Blueprint('api', __name__)

def clean_data(value):
    if value in [None, "", "null", "none", "None"]:
        return "N/A"
    return value

@api_bp.route('/cell_data', methods=['POST'])
@jwt_required()
def add_cell_data():
    """
    Add cell data to the database.

    This function receives a POST request with cell data in JSON format and adds it to the database.
    The required fields in the JSON data are 'operator', 'signal_power', 'sinr', 'network_type',
    'frequency_band', 'cell_id', and 'timestamp'.

    Returns:
        A JSON response with a success message and HTTP status code 201 if the cell data is added successfully.
        A JSON response with an error message and HTTP status code 422 if there are missing required fields.
        A JSON response with an error message and HTTP status code 500 if there is an unexpected error.
    """
    try:
        data = request.json
        required_fields = ['operator', 'signal_power', 'sinr', 'network_type', 'frequency_band', 'cell_id', 'timestamp']
        if not all(field in data for field in required_fields):
            raise ValueError("Missing required field(s)")
        
        current_user_id = get_jwt_identity()
        
        new_cell_data = CellData(
            user_id=current_user_id,
            operator=clean_data(data['operator']),
            signal_power=clean_data(data['signal_power']),
            sinr=clean_data(data['sinr']),
            network_type=clean_data(data['network_type']),
            frequency_band=clean_data(data['frequency_band']),
            cell_id=clean_data(data['cell_id']),
            timestamp=datetime.strptime(clean_data(data['timestamp']), '%d %b %Y %I:%M %p')
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
