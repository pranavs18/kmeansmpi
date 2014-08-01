'''
@author - Pranav Saxena / Vaibhav Suresh Kumar
Serialized version of K means algorithm 
Distributed Systems , Carnegie Mellon University

References - 
http://datasciencelab.wordpress.com/2013/12/12/clustering-with-k-means-in-python/
http://stackoverflow.com/questions/1545606/python-k-means-algorithm
http://stackoverflow.com/questions/8170562/python-reduce-on-tuple-of-tuples - using Lambda function to calculate euclidean distance for any dimension
'''
import DataPointCluster as dpc
import DataPoint as dp
import random
import csv
#import numpy as np

def kmeansDataPointSerialClustering(points,num_clusters,threshold):
    '''
     collect a sample of points without replacement from the total set and create a list of length k 
    '''
    converge = False
    '''Get a random sample of points to act as initial centroids'''
    randomsample = random.sample(points,num_clusters)  
    getCluster = [dpc.DataPointCluster([point]) for point in randomsample]
    while not converge:
        clusterContainer = [[] for temp in getCluster]
        MaxUpdatePosition = 0.0
        for point in points:
            min_distance = dpc.euclideanDistance(point,getCluster[0].clusterCentroid)
            count = 0
            for clusterIndex in xrange(len(getCluster[1:])):
                current_distance = dpc.euclideanDistance(point, getCluster[clusterIndex+1].clusterCentroid)
                if  min_distance > current_distance:
                    min_distance = current_distance
                    count = clusterIndex+1
            clusterContainer[count].append(point)
       
        for clusterIndex in xrange(len(getCluster)):
            update = getCluster[clusterIndex].getUpdatedCentroidCoordinates(clusterContainer[clusterIndex])
            MaxUpdatePosition = max(MaxUpdatePosition, update)
        if MaxUpdatePosition < threshold: 
            converge = True
            
    return getCluster
    
''' Gaussian distribution to generate dataset'''     
def generate_DataPoints(dimension,lowerRange,upperRange):
    return dp.DataPoint([random.gauss(lowerRange, upperRange) for dim in xrange(dimension)])

      
''' Take input from the user about the dimension, number of points to be generated, lower bound, upper bound.''' 
def main():
    
    error = "Inappropriate input"
    dimension = int(raw_input("Please enter the dimension of the Point type ( 2 for 2D) \n"))
    num_clusters = int(raw_input("Please enter the number of clusters you want \n "))
    totalPoints = int(raw_input("Enter the number of points on which  kmeans clustering needs to be performed \n"))
    lowerBound = float(raw_input("Enter the lower range for the randomly generated data set \n"))
    upperBound = float(raw_input("Enter the upper range for the randomly generated data set \n"))
    threshold = float(raw_input("Enter the threshold value for the datapoint \n"))
    result = str(raw_input("Enter the name of the file in which you want the output"))
    writerInput = csv.writer(open("input", "w"))
    
    ''' validation'''
    if dimension < 0 or totalPoints < 0 or upperBound < lowerBound or threshold < 0 or num_clusters < 0 or totalPoints < num_clusters:
        print error
        return error
    
    
    ''' generic function to generate d-dimensional data'''
    randomPoints = map( lambda i: generate_DataPoints(dimension, lowerBound, upperBound), xrange(totalPoints) )
    writerInput.writerow([randomPoints])
    ''' randomPoints = np.array([(random.uniform(lowerBound, upperBound), random.uniform(lowerBound, upperBound)) for i in range(num_clusters)])'''
    clusters = kmeansDataPointSerialClustering(randomPoints, num_clusters, threshold)
    writer = csv.writer(open(result+".csv", "w"))
    for cluster,temp in enumerate(clusters): 
        for point in temp.points:
            writer.writerow( ['Cluster', cluster ,'Point', point])
            
if __name__ == "__main__": 
    main()    