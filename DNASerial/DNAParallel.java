import mpi.*;
import java.util.Arrays;
import java.io.EOFException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;


class DNAParallel{


	int rank;
	int totalNumberOfHosts;
	
	public DNAParallel() throws MPIException{
	
		this.rank = MPI.COMM_WORLD.Rank();
		this.totalNumberOfHosts = MPI.COMM_WORLD.Size()-1;
	}

	public int getRank(){
		return this.rank;	
	}


	

	public int getTotalNumberOfHosts(){
		return this.totalNumberOfHosts;
	}	
	
		
	public void toByteAndSend(LinkedList<String> list, int destinationID, int tag ) throws IOException,MPIException{
	
	
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			DataOutputStream out = new DataOutputStream(bout);
			
	
		int[] size = new int[1];

		
		if(list!=null){
			for(String dataContent : list){
				
					out.writeUTF(dataContent);
			
			}	
		

			byte[] bytes = bout.toByteArray();
		//	int[] size = new int[1];
			size[0] = bytes.length;
			MPI.COMM_WORLD.Send(size,0,size.length,MPI.INT,destinationID,2);
        	        MPI.COMM_WORLD.Send(bytes,0,bytes.length,MPI.BYTE,destinationID,tag);
		}
		else if(list==null){
			String noStr =new String( "noS");
			byte[] bytes = noStr.getBytes();
			

		//	int[] size = new int[1];
			size[0] = 0;

		MPI.COMM_WORLD.Send(size,0,size.length,MPI.INT,destinationID,2);
                MPI.COMM_WORLD.Send(bytes,0,bytes.length,MPI.BYTE,destinationID,tag);
		}
	
	
		//MPI.COMM_WORLD.Send(size,0,size.length,MPI.INT,destinationID,2);
		//MPI.COMM_WORLD.Send(bytes,0,bytes.length,MPI.BYTE,destinationID,tag);
		
		
	
	}
	
	
	public LinkedList<String> receiveAndToObject(int sourceId, int tag) throws IOException, MPIException{

		
		int[] size = new int[1];
		LinkedList<String> list = new LinkedList<String>();
		
		MPI.COMM_WORLD.Recv(size,0,size.length,MPI.INT,sourceId,2);
		
	//	System.out.println("Received Size "+size[0]);			
System.out.println("Received Size "+this.getRank()+" "+size[0]);
	

	if(size[0]!=0){
		byte[] bytes = new byte[size[0]];
		MPI.COMM_WORLD.Recv(bytes,0,size[0],MPI.BYTE,sourceId,tag);

	
		ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
		DataInputStream in = new DataInputStream(bin);
		
		//if(size[0]!=0){		
			while (in.available() > 0) {
    				try{
					String data  = in.readUTF();
    					list.add(data);
				}
				catch(EOFException e){
					in.close();
					bin.close();
					break;
				}
		}	
	}	
		
		
		else if(size[0] == 0){
			
			byte[] bytes = new byte[6];
			MPI.COMM_WORLD.Recv(bytes,0,6,MPI.BYTE,sourceId,tag);
			list = null;

		}
		return list;
	}



		public LinkedList<String> newMasterMean(LinkedList<LinkedList<String>> newClusteLinkedList){

		for(LinkedList<String> list : newClusteLinkedList){
			if(!list.isEmpty()){
				String centroidStringForThisList = list.get(0);
				int prevMaxWeightForStr = 0;

				for(String str: list){

					int newMaxWeightForStr = 0;

					//Compare with every other string in the list to get weight of commons
					for(String strToCmp: list){

						if(list.indexOf(str) == list.indexOf(strToCmp)){
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
		LinkedList<String> newMean = new LinkedList<String>();
		for(LinkedList<String> list : newClusteLinkedList){
			newMean.add(list.get(0));
		}
		return newMean;
	}
	
	//Find number of matches between 2 strings
	//Add this
		int numberOfMatchesInString(String a , String b){

			int matches = 0;

			for(int i = 0; i<a.length();i++){
				if(a.charAt(i)==b.charAt(i)){
					matches ++;
				}
			}
			return matches;
		}
			



	public static void main(String[] args) throws MPIException,IOException{

		MPI.Init(args);
		DNAParallel dna = new DNAParallel(); 
		
		
	
		LinkedList<String> centroidList = new LinkedList<String>();
		LinkedList<String> entireDataSet = new LinkedList<String>();

		LinkedList<LinkedList<String>> computedResult = new LinkedList<LinkedList<String>>();
		
		if(dna.getRank() == 0){

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
		
		
		
		//Divide the chunks based on the number of core
		LinkedList<LinkedList<String>> chunks = new LinkedList<LinkedList<String>>();
		
		double numberOfSystems = dna.getTotalNumberOfHosts();
		double dataSetSize = entireDataSet.size();
		
		int numberOfChunks = (int) Math.ceil(dataSetSize/numberOfSystems);
		System.out.println("number of chunks "+numberOfChunks+" number of Systems: "+numberOfSystems +" dataset Size: "+dataSetSize);

		int chunkCount = 0;

		while(chunkCount != numberOfSystems){

			chunkCount++;

				LinkedList<String> list = new LinkedList<String>();
				for(int i =0; i< numberOfChunks; i++){
					try{
						list.add(entireDataSet.remove());
					}

					catch(NoSuchElementException e){
						break;
					}
				}
				chunks.add(list);
		}
		System.out.println("Chunks to be sent to the different machines");
		System.out.println(chunks);
 

		//Send data to slaves for processing
		for(int i = 1; i<=numberOfSystems; i++ ){
		
			dna.toByteAndSend(chunks.get(i-1),i,10);
		}
		
				
		//while loop
		
		for(int i = 0; i<  numberOfChunks * numberOfChunks; i++){

			//Send centroid list to all slaves
			for(int j = 1; j<=numberOfSystems; j++){
			
				dna.toByteAndSend(centroidList,j,12);
				
			}		

		
			int breakCount = 0;
			LinkedList<LinkedList<String>> allclist = new LinkedList<LinkedList<String>>();
			//Receive Centroid list from all slaves
			for(int k = 1;k<=numberOfSystems; k++){
				breakCount++;
				System.out.println("Break Count "+breakCount);
				LinkedList<String> list= new LinkedList<String>();
				list = dna.receiveAndToObject(k,22);
				System.out.println("Master received the following list");
				System.out.println(list);
				allclist.add(list);
				if(breakCount == numberOfSystems){
				
					break;

				}			 
							
			
			
			
			}		

			System.out.println(allclist);

			//Compute overall mean and store in centroidList
			//Compute overall mean and store in centroidList
				//Make new list of same centroid means
				LinkedList<LinkedList<String>> similarcentroidValues = new LinkedList<LinkedList<String>>();
				int listsize = centroidList.size();

				for(int m = 0; m < centroidList.size(); m++){
					LinkedList<String> strlist = new LinkedList<String>();
					similarcentroidValues.add(strlist);
				}



				//Agregate all the same cluster values in the asme list
				for(LinkedList<String> list: allclist){

					for(int m = 0; m< list.size();m++){
						similarcentroidValues.get(m).add(list.get(m));					
					}

				}
				
				//Get new mean
				centroidList.clear();
				centroidList = dna.newMasterMean(similarcentroidValues);
		
		}


		for(int i =1;i<=numberOfSystems; i++){
	
			LinkedList<String> list = null;
	
			System.out.println("END");
			
			dna.toByteAndSend(list,i,12);
		}

		}

		
		else if(dna.getRank()!= 0){
		
			LinkedList<String> dataList = new LinkedList<String>();
			LinkedList<String> slaveCentroidList = new LinkedList<String>();

			dataList = dna.receiveAndToObject(0, 10);
			System.out.println("Data received at machine: "+dna.getRank());
			System.out.println(dataList);
		

			int noOfCent = slaveCentroidList.size();
			LinkedList<LinkedList<String>> compResult = null;	
			while(true){

				slaveCentroidList = dna.receiveAndToObject(0,12);
				if(slaveCentroidList == null){
					System.out.println("DID BREAK");	
					break;
				}
				System.out.println("Centroid list obtained from master: ");
				System.out.println(slaveCentroidList);
				
				//use serial code to calculate new mean and send the list to master
				DNASerial dnaSerial = new DNASerial();
				LinkedList<LinkedList<String>> intialClusterwithCentroids = new LinkedList<LinkedList<String>>();
				compResult = new LinkedList<LinkedList<String>>();
				
				
				intialClusterwithCentroids = dnaSerial.intialClusterwithCentroids(slaveCentroidList);

				System.out.println("Inital clusters with only centroids");
				System.out.println(intialClusterwithCentroids);
		
				System.out.println("Entire data set: of length"+dataList.size());
				System.out.println(dataList);	
			
				for(int i = 0 ; i<intialClusterwithCentroids.size() * dataList.size(); i++){
					compResult = dnaSerial.computeClusterElements(intialClusterwithCentroids, dataList);
				//System.out.println("Clusters after iteration");
				//	System.out.println(compResult);
					intialClusterwithCentroids = dnaSerial.meanInCluster(compResult);
				//	System.out.println("New means");
				//	System.out.println(intialClusterwithCentroids);
				}
				
				LinkedList<String> sendStringToMaster = new LinkedList<String>();
				for(LinkedList<String> list : intialClusterwithCentroids){

					sendStringToMaster.add(list.get(0));	

				}
				System.out.println("Means sent to master from worker: "+ dna.getRank());
				System.out.println(sendStringToMaster);
				dna.toByteAndSend(sendStringToMaster,0,22);	
			
			}

			System.out.println("Computaional result:"+ compResult);
			System.out.println("COMPLETE");
		}
		
		MPI.Finalize();
		System.out.println("DONE");
	}

}
