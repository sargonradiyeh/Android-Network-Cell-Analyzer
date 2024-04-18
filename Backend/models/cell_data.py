from extensions import db

class CellData(db.Model):
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
