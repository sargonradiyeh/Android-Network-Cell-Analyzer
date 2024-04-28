from app import db
from flask_bcrypt import Bcrypt

bcrypt = Bcrypt()

class User(db.Model):
    """
    Represents a user in the system.

    Attributes:
        id (int): The unique identifier for the user.
        username (str): The username of the user.
        password_hash (str): The hashed password of the user.

    Methods:
        set_password(password): Sets the password for the user.
        check_password(password): Checks if the provided password matches the user's password.

    """

    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(64), index=True, unique=True)
    password_hash = db.Column(db.String(128))
    
    def set_password(self, password):
        """
        Sets the password for the user.

        Args:
            password (str): The password to set.

        Returns:
            None

        """
        self.password_hash = bcrypt.generate_password_hash(password)

    def check_password(self, password):
        """
        Checks if the provided password matches the user's password.

        Args:
            password (str): The password to check.

        Returns:
            bool: True if the password matches, False otherwise.

        """
        return bcrypt.check_password_hash(self.password_hash, password)
