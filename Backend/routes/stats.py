from flask import Blueprint, jsonify, request
from app import db
from flask_jwt_extended import jwt_required, get_jwt_identity
from models.cell_data import CellData
from sqlalchemy import func, extract
from datetime import datetime, timedelta
from sqlalchemy.sql import func

stats_bp = Blueprint('stats', __name__)

from sqlalchemy import func, text

@stats_bp.route('/average_connectivity_time_per_operator', methods=['GET'])
@jwt_required()
def average_connectivity_time_per_operator():
    current_user_id = get_jwt_identity()

    subquery = db.session.query(
        CellData.operator,
        func.timestampdiff(text('SECOND'), func.lag(CellData.timestamp).over(partition_by=CellData.operator), CellData.timestamp).label('diff')
    ).filter(CellData.user_id == current_user_id).subquery()

    operator_stats = db.session.query(
        subquery.c.operator,
        func.avg(subquery.c.diff).label('avg_time')
    ).group_by(subquery.c.operator).all()

    result = [{'operator': operator, 'average_connectivity_time': avg_time} for operator, avg_time in operator_stats]

    return jsonify(result), 200


@stats_bp.route('/average_connectivity_time_per_network_type', methods=['GET'])
@jwt_required()
def average_connectivity_time_per_network_type():
    current_user_id = get_jwt_identity()

    # Subquery to calculate lagged timestamp values partitioned by network type
    subq = db.session.query(
        CellData.network_type,
        CellData.timestamp.label('current_timestamp'),
        func.lag(CellData.timestamp).over(partition_by=CellData.network_type).label('lagged_timestamp')
    ).filter(CellData.user_id == current_user_id).subquery()

    # Outer query to calculate the difference in seconds between current and lagged timestamps
    network_stats = db.session.query(
        subq.c.network_type,
        func.avg(func.timestampdiff(text('SECOND'), subq.c.lagged_timestamp, subq.c.current_timestamp)).label('avg_time')
    ).group_by(subq.c.network_type).all()

    result = [{'network_type': network, 'average_connectivity_time': avg_time} for network, avg_time in network_stats]

    return jsonify(result), 200



@stats_bp.route('/average_signal_power_per_network_type', methods=['GET'])
@jwt_required()
def average_signal_power_per_network_type():
    current_user_id = get_jwt_identity()
    
    signal_power_stats = db.session.query(CellData.network_type, func.avg(CellData.signal_power)) \
                                    .filter_by(user_id=current_user_id) \
                                    .group_by(CellData.network_type) \
                                    .all()
    
    result = [{'network_type': network, 'average_signal_power': avg_power} for network, avg_power in signal_power_stats]
    
    return jsonify(result), 200

@stats_bp.route('/average_signal_power_per_device', methods=['GET'])
@jwt_required()
def average_signal_power_per_device():
    current_user_id = get_jwt_identity()
    
    device_stats = db.session.query(CellData.cell_id, func.avg(CellData.signal_power)) \
                                .filter_by(user_id=current_user_id) \
                                .group_by(CellData.cell_id) \
                                .all()
    
    result = [{'device': device, 'average_signal_power': avg_power} for device, avg_power in device_stats]
    
    return jsonify(result), 200

@stats_bp.route('/average_snr_or_sinr_per_network_type', methods=['GET'])
@jwt_required()
def average_snr_or_sinr_per_network_type():
    current_user_id = get_jwt_identity()
    
    snr_sinr_stats = db.session.query(CellData.network_type, func.avg(CellData.sinr)) \
                                .filter_by(user_id=current_user_id) \
                                .group_by(CellData.network_type) \
                                .all()
    
    result = [{'network_type': network, 'average_sinr': avg_sinr} for network, avg_sinr in snr_sinr_stats]
    
    return jsonify(result), 200