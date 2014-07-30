# class to represent a data point 
class DataPoint:
    def __init__(self, coordinates):
        self.coordinates = coordinates
        self.dimension = len(coordinates)
        
    ''' to string method in python for human readability'''
    def __repr__(self):
        return str(self.coordinates)