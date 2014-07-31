import sys
import csv
import numpy
import math
import random


def DNAStringSimilarity(p1, p2):
    assert len(p1) == len(p2)
    return sum(ch1 is not ch2 for ch1, ch2 in zip(p1, p2))

def tooClose(point, points, minDist):
    for pair in points:
        if DNAStringSimilarity(point, pair) < minDist:
                return True

    return False

def drawOrigin(maxValue):
    DNAstrand = ''.join(random.choice(['a', 't', 'c', 'g'])for i in range(maxValue))
    return DNAstrand


def main():
    numClusters = int(raw_input("Please enter the number of clusters you want \n "))
    totalPoints = int(raw_input("Enter the number of points on which  kmeans clustering needs to be performed \n"))
    maxValue =   int(raw_input("Enter the threshold value for the datapoint \n"))
    output =  "randomData"
    writer = csv.writer(open(output, "w"))


    centroids_radii = []
    minDistance = 0
    ''' generating K centroids randomly for DNA'''
    for i in range(0, numClusters):
        centroid_radius = drawOrigin(maxValue)
        # is it far en
        while (tooClose(centroid_radius, centroids_radii, minDistance)):
            centroid_radius = drawOrigin(maxValue)
        centroids_radii.append(centroid_radius)

    '''step 2: generate the DNA strands for each randomly calculated centroid'''
    points = []
    minClusterVar = 0
    maxClusterVar = 1
    randomString = None
    charList = ['a','t','c','g']
    for i in range(0, numClusters):
        # compute the variance for this cluster
        variance = numpy.random.uniform(minClusterVar, (maxClusterVar*maxValue))
        cluster = centroids_radii[i]
        print "Centroid " + cluster
        for j in range(0, totalPoints):
            dist = int(numpy.random.normal(minClusterVar,variance))
            if dist < 0:
                dist = dist * -1
                dist = maxValue
            if(dist > maxValue):
                dist = maxValue
            temp = random.sample([j for j in xrange(0,len(list(cluster)))] , dist)
            cls = list(cluster)
            for k in temp:
                #randomly generate combinations
                strandList = []
                for elem in charList:
                    strandList.append(elem)
                strandList.remove(cls[k])
                cls[k]=random.choice(strandList)
                randomString = ''.join(cls)
            writer.writerow(["Cluster Centroid",i , cluster, "Strand ", randomString])



if __name__ == "__main__":
    main()


