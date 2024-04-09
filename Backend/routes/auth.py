from flask import Blueprint, request, jsonify
from models.user import User
from app import db
from flask_jwt_extended import create_access_token

auth_bp = Blueprint('auth', __name__)

import re

@auth_bp.route('/signup', methods=['POST'])
def sign_up():
    data = request.json

    if not {'username', 'password'}.issuperset(data.keys()):
        return jsonify({'error': 'Invalid request. Only username and password are allowed'}), 400

    if User.query.filter_by(username=data['username']).first() is not None:
        return jsonify({'error': 'Username already exists'}), 400

    password = data['password']
    if len(password) < 8:
        return jsonify({'error': 'Password must be at least 8 characters long'}), 400

    if not re.search(r'[A-Z]', password):
        return jsonify({'error': 'Password must contain at least one uppercase letter'}), 400

    if not re.search(r'[a-z]', password):
        return jsonify({'error': 'Password must contain at least one lowercase letter'}), 400

    if not re.search(r'\d', password):
        return jsonify({'error': 'Password must contain at least one digit'}), 400

    if not re.search(r'[!@#$%^&*(),.?":{}|<>]', password):
        return jsonify({'error': 'Password must contain at least one special character'}), 400

    new_user = User(username=data['username'])
    new_user.set_password(password)
    db.session.add(new_user)

    try:
        db.session.commit()
        return jsonify({'message': 'User registered successfully'}), 201
    except Exception as e:
        db.session.rollback()
        return jsonify({'error': str(e)}), 500
    finally:
        db.session.close()



@auth_bp.route('/login', methods=['POST'])
def login():
    data = request.json
    if 'username' not in data or 'password' not in data:
        return jsonify({'error': 'Username and password are required'}), 400

    username = data['username']
    password = data['password']

    user = User.query.filter_by(username=username).first()
    if user is None or not user.check_password(password):
        return jsonify({'error': 'Invalid username or password'}), 401

    access_token = create_access_token(identity=user.id)

    return jsonify({'token': access_token}), 200