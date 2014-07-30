import DataPoint as dp
import math
'''
Class - PointCluster:Class to represent cluster of points which has the methods to get the centroid coordinates, update the centroid coordinates 
'''   

class DataPointCluster:
    def __init__(self,points):
        self.points = points
        self.dimension = points[0].dimension
        self.clusterCentroid = self.getCentroidCoordinates()
        
    def __repr__(self):
        return str(self.points)    
        
    def getCentroidCoordinates(self):
        newCentroid = lambda i:reduce(lambda x,p : x + p.coordinates[i],self.points,0.0)
        centroidPosition = [newCentroid(i)/len(self.points) for i in xrange(self.dimension)] 
        return dp.DataPoint(centroidPosition) 
    
    ''' update centroid to a new location for each iteration '''
    def getUpdatedCentroidCoordinates(self, points):
        prevCentroid = self.clusterCentroid
        self.points = points
        self.clusterCentroid = self.getCentroidCoordinates()
        return euclideanDistance(prevCentroid, self.clusterCentroid)  
 
 
def euclideanDistance(x,y):
    return math.sqrt(reduce(lambda i,j: i + pow((x.coordinates[j]-y.coordinates[j]), 2),xrange(x.dimension),0))   