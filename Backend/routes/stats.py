from flask import Blueprint, jsonify, request
from app import db
from flask_jwt_extended import jwt_required, get_jwt_identity
from models.cell_data import CellData
from sqlalchemy import func, text, and_
import datetime

stats_bp = Blueprint('stats', __name__)

def parse_date(date_str):
    if date_str is None:
        return None
    try:
        return datetime.datetime.strptime(date_str, '%Y-%m-%d').date()
    except ValueError:
        return None

def daily_or_total_avg(query, start_date, end_date, group_by_fields):
    if start_date and end_date:
        query = query.filter(
            and_(CellData.timestamp >= start_date, CellData.timestamp <= end_date)
        )
    return query.group_by(*group_by_fields).all()


@stats_bp.route('/average_connectivity_time_per_operator', methods=['GET'])
@jwt_required()
def average_connectivity_time_per_operator():
    current_user_id = get_jwt_identity()
    start_date = parse_date(request.args.get('start_date'))
    end_date = parse_date(request.args.get('end_date'))

    base_query = db.session.query(
        CellData.operator,
        func.timestampdiff(
            text('SECOND'), 
            func.lag(CellData.timestamp).over(
                partition_by=[CellData.operator],
                order_by=CellData.timestamp
            ), 
            CellData.timestamp
        ).label('diff')
    ).filter(CellData.user_id == current_user_id)

    if start_date and end_date:
        base_query = base_query.filter(CellData.timestamp >= start_date, CellData.timestamp <= end_date)

    subquery = base_query.subquery()
    operator_stats_query = db.session.query(
        subquery.c.operator,
        func.avg(subquery.c.diff).label('avg_time')
    ).group_by(subquery.c.operator) 

    operator_stats = operator_stats_query.all()

    total_avg_time = sum([stat[1] for stat in operator_stats if stat[1] is not None])

    result = [{
        'operator': operator,
        'average_connectivity_percentage': (avg_time / total_avg_time * 100) if avg_time is not None and total_avg_time > 0 else 0
    } for operator, avg_time in operator_stats]

    return jsonify(result), 200


@stats_bp.route('/average_connectivity_time_per_network_type', methods=['GET'])
@jwt_required()
def average_connectivity_time_per_network_type():
    current_user_id = get_jwt_identity()
    start_date = parse_date(request.args.get('start_date'))
    end_date = parse_date(request.args.get('end_date'))

    base_query = db.session.query(
        CellData.network_type,
        func.timestampdiff(text('SECOND'), func.lag(CellData.timestamp).over(partition_by=[CellData.network_type]), CellData.timestamp).label('diff')
    ).filter(CellData.user_id == current_user_id)

    if start_date and end_date:
        base_query = base_query.filter(CellData.timestamp >= start_date, CellData.timestamp <= end_date)

    network_stats_query = db.session.query(
        CellData.network_type,
        func.avg(base_query.subquery().c.diff).label('avg_time')
    )

    group_by_fields = [CellData.network_type]

    network_stats = daily_or_total_avg(network_stats_query, start_date, end_date, group_by_fields)

    result = [{
        'network_type': network,
        'average_connectivity_time': max(avg_time, 0) if avg_time else 0
    } for network, avg_time in network_stats]

    return jsonify(result), 200

@stats_bp.route('/average_signal_power_per_network_type', methods=['GET'])
@jwt_required()
def average_signal_power_per_network_type():
    current_user_id = get_jwt_identity()
    start_date = parse_date(request.args.get('start_date'))
    end_date = parse_date(request.args.get('end_date'))

    query = db.session.query(
        CellData.network_type,
        func.avg(CellData.signal_power)
    ).filter(
        CellData.user_id == current_user_id,
        CellData.signal_power.isnot(None)
    )

    if start_date and end_date:
        query = query.filter(CellData.timestamp >= start_date, CellData.timestamp <= end_date)

    stats = daily_or_total_avg(query, start_date, end_date, [CellData.network_type])
    
    result = [{'network_type': network, 'average_signal_power1': avg_power} for network, avg_power in stats]
    return jsonify(result), 200

@stats_bp.route('/average_signal_power_per_device', methods=['GET'])
@jwt_required()
def average_signal_power_per_device():
    current_user_id = get_jwt_identity()
    start_date = parse_date(request.args.get('start_date'))
    end_date = parse_date(request.args.get('end_date'))

    query = db.session.query(
        CellData.cell_id, func.avg(CellData.signal_power)
    ).filter(
        CellData.user_id == current_user_id,
        CellData.cell_id != 'N/A',
        CellData.signal_power.isnot(None)
    )

    if start_date and end_date:
        query = query.filter(CellData.timestamp >= start_date, CellData.timestamp <= end_date)

    stats = daily_or_total_avg(query, start_date, end_date, [CellData.cell_id])
    
    result = [{'device': device, 'average_signal_power2': avg_power} for device, avg_power in stats]
    return jsonify(result), 200

@stats_bp.route('/average_snr_or_sinr_per_network_type', methods=['GET'])
@jwt_required()
def average_snr_or_sinr_per_network_type():
    current_user_id = get_jwt_identity()
    start_date = parse_date(request.args.get('start_date'))
    end_date = parse_date(request.args.get('end_date'))

    query = db.session.query(
        CellData.network_type, func.avg(CellData.sinr)
    ).filter(
        CellData.user_id == current_user_id,
        CellData.sinr.isnot(None)
    )

    if start_date and end_date:
        query = query.filter(CellData.timestamp >= start_date, CellData.timestamp <= end_date)

    stats = daily_or_total_avg(query, start_date, end_date, [CellData.network_type])
    
    result = [{'network_type': network, 'average_sinr': max(avg_sinr, 0) if avg_sinr else 0} for network, avg_sinr in stats]
    return jsonify(result), 200
