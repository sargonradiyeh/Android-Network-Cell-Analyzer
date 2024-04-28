from extensions import db

class CellData(db.Model):
    """
    Represents a cell data record.

    Attributes:
        id (int): The unique identifier of the cell data record.
        user_id (int): The foreign key referencing the user who owns the cell data record.
        operator (str): The operator of the network.
        signal_power (str): The signal power of the cell.
        sinr (str): The signal-to-interference-plus-noise ratio of the cell.
        network_type (str): The type of network.
        frequency_band (str): The frequency band of the cell.
        cell_id (str): The identifier of the cell.
        timestamp (datetime): The timestamp when the cell data was recorded.
    """

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('user.id'), nullable=False)
    operator = db.Column(db.String(50))
    signal_power = db.Column(db.String(20)) 
    sinr = db.Column(db.String(20)) 
    network_type = db.Column(db.String(10))
    frequency_band = db.Column(db.String(20))
    cell_id = db.Column(db.String(50))
    timestamp = db.Column(db.DateTime)

    def __repr__(self):
        return f"<CellData {self.id}>"
