from flask import Blueprint, jsonify, request
from app import db
from flask_jwt_extended import jwt_required, get_jwt_identity
from models.cell_data import CellData
from sqlalchemy import func, text, and_, cast, Date
import datetime

stats_bp = Blueprint('stats', __name__)

def parse_date(date_str):
    if date_str is None:
        return None
    try:
        return datetime.datetime.strptime(date_str, '%Y-%m-%d')
    except ValueError:
        return None

def daily_or_total_avg(query, start_date, end_date, group_by_fields):
    if start_date and end_date:
        return query.filter(
            and_(CellData.timestamp >= start_date, CellData.timestamp <= end_date)
        ).group_by(
            *group_by_fields
        ).all()
    else:
        return query.group_by(
            *group_by_fields
        ).all()

@stats_bp.route('/average_connectivity_time_per_operator', methods=['GET'])
@jwt_required()
def average_connectivity_time_per_operator():
    current_user_id = get_jwt_identity()
    start_date = parse_date(request.args.get('start_date'))
    end_date = parse_date(request.args.get('end_date'))

    subquery = db.session.query(
        CellData.operator,
        cast(CellData.timestamp, Date).label('date'),
        func.timestampdiff(text('SECOND'), func.lag(CellData.timestamp).over(partition_by=[CellData.operator, cast(CellData.timestamp, Date)]), CellData.timestamp).label('diff')
    ).filter(CellData.user_id == current_user_id).subquery()

    operator_stats_query = db.session.query(
        subquery.c.operator,
        subquery.c.date,
        func.avg(subquery.c.diff).label('avg_time')
    )

    group_by_fields = [subquery.c.operator]
    if start_date and end_date:
        group_by_fields.append(subquery.c.date)

    operator_stats = daily_or_total_avg(operator_stats_query, start_date, end_date, group_by_fields)

    result = [{
        'operator': operator,
        'date': date.strftime('%Y-%m-%d') if date else 'Overall', 
        'average_connectivity_time': max(avg_time, -avg_time) if avg_time and avg_time < 0 else avg_time
    } for operator, date, avg_time in operator_stats]

    return jsonify(result), 200

@stats_bp.route('/average_connectivity_time_per_network_type', methods=['GET'])
@jwt_required()
def average_connectivity_time_per_network_type():
    current_user_id = get_jwt_identity()
    start_date = parse_date(request.args.get('start_date'))
    end_date = parse_date(request.args.get('end_date'))

    subq = db.session.query(
        CellData.network_type,
        cast(CellData.timestamp, Date).label('date'),
        func.timestampdiff(text('SECOND'), func.lag(CellData.timestamp).over(partition_by=[CellData.network_type, cast(CellData.timestamp, Date)]), CellData.timestamp).label('diff')
    ).filter(CellData.user_id == current_user_id).subquery()

    network_stats_query = db.session.query(
        subq.c.network_type,
        subq.c.date,
        func.avg(subq.c.diff).label('avg_time')
    )

    group_by_fields = [subq.c.network_type]
    if start_date and end_date:
        group_by_fields.append(subq.c.date)

    network_stats = daily_or_total_avg(network_stats_query, start_date, end_date, group_by_fields)

    result = [{
        'network_type': network,
        'date': date.strftime('%Y-%m-%d') if date else 'Overall',
        'average_connectivity_time': max(avg_time, -avg_time) if avg_time and avg_time < 0 else avg_time
    } for network, date, avg_time in network_stats]

    return jsonify(result), 200


@stats_bp.route('/average_signal_power_per_network_type', methods=['GET'])
@jwt_required()
def average_signal_power_per_network_type():
    current_user_id = get_jwt_identity()
    start_date = parse_date(request.args.get('start_date'))
    end_date = parse_date(request.args.get('end_date'))

    signal_power_stats_query = db.session.query(
        CellData.network_type, func.avg(CellData.signal_power)
    ).filter(
        CellData.user_id == current_user_id,
        CellData.signal_power.isnot(None)
    )

    stats = daily_or_total_avg(signal_power_stats_query, start_date, end_date, CellData.network_type)
    result = [{'network_type': network, 'average_signal_power': avg_power} for network, avg_power in stats]
    return jsonify(result), 200

@stats_bp.route('/average_signal_power_per_device', methods=['GET'])
@jwt_required()
def average_signal_power_per_device():
    current_user_id = get_jwt_identity()
    start_date = parse_date(request.args.get('start_date'))
    end_date = parse_date(request.args.get('end_date'))

    device_stats_query = db.session.query(
        CellData.cell_id, func.avg(CellData.signal_power)
    ).filter(
        CellData.user_id == current_user_id,
        CellData.cell_id != 'N/A',
        CellData.signal_power.isnot(None)
    )

    stats = daily_or_total_avg(device_stats_query, start_date, end_date, CellData.cell_id)
    result = [{'device': device, 'average_signal_power': avg_power} for device, avg_power in stats]
    return jsonify(result), 200

@stats_bp.route('/average_snr_or_sinr_per_network_type', methods=['GET'])
@jwt_required()
def average_snr_or_sinr_per_network_type():
    current_user_id = get_jwt_identity()
    start_date = parse_date(request.args.get('start_date'))
    end_date = parse_date(request.args.get('end_date'))

    snr_sinr_stats_query = db.session.query(
        CellData.network_type, func.avg(CellData.sinr)
    ).filter(
        CellData.user_id == current_user_id,
        CellData.sinr.isnot(None)
    )

    stats = daily_or_total_avg(snr_sinr_stats_query, start_date, end_date, CellData.network_type)
    result = [{'network_type': network, 'average_sinr': max(avg_sinr, -avg_sinr) if avg_sinr and avg_sinr < 0 else avg_sinr} for network, avg_sinr in stats]
    return jsonify(result), 200
