from mpi4py import MPI
import numpy
import sys
import math
import copy
import csv
import random


def euclideanDistance(x,y):
    dist =  math.sqrt((x.coordinates[0] - y.coordinates[0])**2 + (x.coordinates[1] - y.coordinates[1])**2)
    return dist

def generate_DataPoints(dimension,lowerRange,upperRange):
    return DataPoint([random.gauss(lowerRange, upperRange) for dim in xrange(dimension)])

class DataPoint:
    def __init__(self, coordinates):
        self.coordinates = coordinates
        self.dimension = 2
        
    ''' to string method in python for human readability'''
    def __repr__(self):
        return str(self.coordinates)

    def __getitem__(self, k):
        return k


class DataPointCluster:
    def __init__(self,points):
        self.points = points
        self.dimension = 2
        self.clusterCentroid = self.getCentroidCoordinates()
        
    def __repr__(self):
        return str(self.points)
 
    def __getitem__(self, k):
    	return k    
        
    def getCentroidCoordinates(self):
        newCentroid = lambda i:reduce(lambda x,p : x + p.coordinates[i],self.points,0.0)
        if(len(self.points) is not 0):
        	centroidPosition = [newCentroid(i)/len(self.points) for i in xrange(2)]
        else:
		centroidPosition = 0,0 
        return DataPoint(centroidPosition) 
    
    ''' update centroid to a new location for each iteration '''
    def getUpdatedCentroidCoordinates(self, points):
        prevCentroid = self.clusterCentroid
        self.points = points
        self.clusterCentroid = self.getCentroidCoordinates()
        return euclideanDistance(prevCentroid, self.clusterCentroid) 
 

def SplitList(list, chunk_size):
    return [list[offs:offs+chunk_size] for offs in range(0, len(list), chunk_size)]
 
def main():
    comm = MPI.COMM_WORLD
    size = comm.Get_size()
    machinerank = comm.Get_rank()
    procname =MPI.Get_processor_name()
    writerInput = csv.writer(open("input", "w"))
    #dimension = int(raw_input("Please enter the dimension of the Point type"))
    ''' num_clusters = int(raw_input("Please enter the number of clusters you want "))
    totalPoints = int(raw_input("Enter the number of points on which  kmeans clustering needs to be performed"))
    lowerBound = float(raw_input("Enter the lower range for the randomly generated data set "))
    upperBound = float(raw_input("Enter the upper range for the randomly generated data set "))
    threshold = float(raw_input("Enter the threshold value for the datapoint "))
    '''
    dimension = 2
    lowerBound =0
    upperBound = 10
    totalPoints = 100
    num_clusters = 2
    threshold = 20
    randomPoints = map( lambda i: generate_DataPoints(dimension, lowerBound, upperBound), xrange(totalPoints) )
     
    if size > 1:   
    	chunkSize = int(totalPoints/ (size-1))
    else:
	chunkSize = int(totalPoints/size)

    machineID = machinerank
    centroidList = []
    centroidList = SplitList(randomPoints, chunkSize)
    temp = copy.deepcopy(centroidList)
    x=[]
    globalPointMap = {}
    clusterContainer=[]
    temp.reverse()
    converge = False
    iteration =0
    ''' get initial centroids '''
    if machinerank == 0:
   	 randomsample = random.sample(randomPoints,num_clusters)
         for i in range(1,size):
        	 comm.send(randomsample,dest=i,tag=11)
		 print "sending centroids to all slaves"
    else:
        randomsample = comm.recv(source=0,tag = 11)
	print "centroids received at slave" + str(randomsample)

    if machinerank == 0:
       	for i in range(1,size):
                x= temp.pop()
		comm.send(x,dest=i,tag = 11)
		print "sending data" + str(x)+ " to " + str(i) 
    else:
        x=comm.recv(source =0,tag =11)   
       	print "data received in slaves" 
    
    if machinerank == 0: 
	iteration =0
    	getCluster = [DataPointCluster([point]) for point in randomsample]
        for i in range(1,size):
                comm.send(getCluster,dest=i,tag = 11)
                print "sending one centroid at a time to " + str(i) 
        converge = False
        while not converge:
		iteration = iteration +1
	        if iteration > 10:
			text =comm.recv(source =i,tag = 11)
			if text == "done":
				converge = True
        	for i in range(1,size):
			clusterContainer = comm.recv(source=i,tag=12)
                	for clusterIndex in range(len(getCluster)):
				update = getCluster[clusterIndex].getUpdatedCentroidCoordinates(clusterContainer[clusterIndex])
                        	comm.send(update,dest=i,tag=12)
    else:
	getCluster = comm.recv(source=0,tag=11)
        print "CLUSTER RECEIVED " + str(getCluster)
        converge = False
        iteration =0        
    	while  not converge:
                iteration = iteration +1
		clusterContainer= [[] for temp in getCluster]
                print "My Chunk" + str(x)
       		MaxUpdatePosition= 0.0
		for point in x:
			min_distance = euclideanDistance(point,getCluster[0].clusterCentroid)
			count = 0
			for clusterIndex in xrange(len(getCluster[1:])):
				current_distance = euclideanDistance(point,getCluster[clusterIndex +1].clusterCentroid)
				print "CURRENT DISTANCE" + str(current_distance)
				if min_distance > current_distance:
					min_distance = current_distance
					print "MIN DISTANCE " + str(min_distance)
					count = clusterIndex +1
			clusterContainer[count].append(point)
			comm.send(clusterContainer,dest=0,tag = 12)
           		update  = comm.recv(source=0,tag = 12)
                        print "UPDATED value " + str(update)
			MaxUpdatePosition = max(MaxUpdatePosition,update)
		if MaxUpdatePosition < threshold or iteration > 10:
			comm.send("done",dest=0,tag=11)
			converge= True
	
    return getCluster


if __name__ == "__main__":
	main()
