import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.io.FileWriter;

public class DNASerial {

	// Setup initial lists with the initial centroids as the first elements
	LinkedList<LinkedList<String>> intialClusterwithCentroids(LinkedList<String> centroidList){

		int numberOfClusters = centroidList.size();
		LinkedList<LinkedList<String>> clusterList = new LinkedList<LinkedList<String>>();

		for(int i = 0; i < numberOfClusters;i++ ){

			LinkedList<String> individualCluster = new LinkedList<String>();
			individualCluster.add(0, centroidList.get(i)); // the 0th element is always 
			clusterList.add(individualCluster);
		}

		return clusterList;

	}



	//Find number of matches between 2 strings
	int numberOfMatchesInString(String a , String b){

		int matches = 0;

		for(int i = 0; i<a.length();i++){
			if(a.charAt(i)==b.charAt(i)){
				matches ++;
			}
		}
		return matches;
	}



	// List with all elements separated into clusters
	LinkedList<LinkedList<String>> computeClusterElements(LinkedList<LinkedList<String>> clusterList , LinkedList<String> entireDnaList){

		LinkedList<LinkedList<String>> newClusterLinkedList = new LinkedList<LinkedList<String>>();
		newClusterLinkedList = clusterList;

		int prevDistance = 0;
		int indexofListElement = 0;


		for(String str : entireDnaList){

			for(LinkedList<String> list : clusterList){

				list.get(0);
				int currentDistance = numberOfMatchesInString(list.get(0), str);
				if(currentDistance > prevDistance){

					prevDistance = currentDistance;
					indexofListElement = clusterList.indexOf(list);

				}

			}

			newClusterLinkedList.get(indexofListElement).add(str);
			prevDistance = 0;
			indexofListElement = 0;


		}

		return newClusterLinkedList;

	}

	// Calculate new mean
	public LinkedList<LinkedList<String>> meanInCluster(LinkedList<LinkedList<String>> newClusteLinkedList){

		for(LinkedList<String> list : newClusteLinkedList){
			if(!list.isEmpty() && list.size()>1){
				String centroidStringForThisList = list.get(1);
				int prevMaxWeightForStr = 0;

				for(String str: list){

					int newMaxWeightForStr = 0;

					if(list.indexOf(str) == 0){
						continue;
					}

					//Compare with every other string in the list to get weight of commons
					for(String strToCmp: list){

						if(list.indexOf(str) == list.indexOf(strToCmp) || list.indexOf(strToCmp) == 0){
							continue;
						}

						else{
							newMaxWeightForStr += numberOfMatchesInString(str, strToCmp);  
						}

						if(newMaxWeightForStr > prevMaxWeightForStr){
							prevMaxWeightForStr =  newMaxWeightForStr;
							centroidStringForThisList = str;
						}
					}


				}

				list.clear();
				list.add(centroidStringForThisList);

			}
		}
		return newClusteLinkedList;
	}




	public static void main(String args[]) throws IOException{

		DNASerial dnaSerial = new DNASerial();
		LinkedList<String> centroidList = new LinkedList<String>();
		LinkedList<String> entireDataSet = new LinkedList<String>();

		LinkedList<LinkedList<String>> intialClusterwithCentroids = new LinkedList<LinkedList<String>>();
		LinkedList<LinkedList<String>> computedResult = new LinkedList<LinkedList<String>>();


		File file = new File ("./randomData");

		BufferedReader in = new BufferedReader(new FileReader(file));

		String line = "";
		String arg[];
		while ((line = in.readLine()) != null){

			arg = line.split(",");
			if(arg.length<2){
				continue;
			}
			if(!centroidList.contains(arg[0])){
				centroidList.add(arg[0]);
			}
			entireDataSet.add(arg[1]);

		}

		intialClusterwithCentroids = dnaSerial.intialClusterwithCentroids(centroidList);

                File file1 = new File("DNAserialOutput.txt");
                FileWriter writer = new FileWriter(file1);

		for(int i = 0 ; i<intialClusterwithCentroids.size() * entireDataSet.size(); i++){
			computedResult = dnaSerial.computeClusterElements(intialClusterwithCentroids, entireDataSet);
			for(LinkedList<String> str: computedResult){
                            for(String tostr: str){
                             writer.write(tostr + " " );
                          }
                        }
                  
			intialClusterwithCentroids = dnaSerial.meanInCluster(computedResult);
		}
                System.out.println("Output file created....DNA Serial K means is completed !!"); 
                writer.flush();
                writer.close();

	}

}
