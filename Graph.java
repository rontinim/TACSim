import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Vector;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.Runtime;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

public class Graph {
	public int numVertices;
	public int[][] edges;
	private int[][] distances; 
	public static boolean printControl = false;
	private static long maxRamForDistance = 0;
	private static int contaExpress;
	private boolean isGrid;
	private boolean edgesProvided;
	private boolean isGPS;
	private boolean WETGenerated;
	public  Dijkstra dij = new Dijkstra();
	int gridWidth;
	int gridHeight;
	
	Hashtable<Integer, GPSCoordinate> vertexCoordinates;
	Hashtable<Integer, Vector<Task>> spatioTemporalTasks;
	private static Map<String, Integer> dijkstraResultsCache = new HashMap<>();	
	public Graph(){}
	/**
	 * Constructor that creates an empty graph with a specific number of vertices.
	 * This should be used to create non-grid graphs only
	 * VERY UNSAFE  - BE CAREFUL
	 * Must create a special type of graph from below if you use this
	 * @param numVertices Number of vertices in the graph
	 */
	public Graph(int numVertices){
		this.numVertices = numVertices;
		
		isGrid = false;
		isGPS = false;
		edgesProvided = false;
		WETGenerated = false;
	}
	
	/**
	 * Constructor that creates an empty 2D grid with a specific width and height
	 * This should be used to create grid graphs
	 * @param width The number of vertices in a single row
	 * @param height The number of vertices in a single column
	 */
	public Graph(int width, int height){
		// This special constructor is needed because grid graphs are generally too big
		this.numVertices = width*height;
		
		isGrid = true;
		isGPS = false;
		edgesProvided = false;
		WETGenerated = false;
		
		gridWidth = width;	
		gridHeight = height;
		
		distances = new int[numVertices][numVertices];
		CalculateDistances();
	}
	
	/**
	 * Constructor that creates a graph from a single tsv file with POI coordinates
	 * The expected graph file format is: 	 
	 * latitude	longitude
	 * @param vertextFile 	 
	 */
	
	 public Graph(String vertexFile) {
		isGrid = false;
		isGPS = true;
		edgesProvided = false;
		WETGenerated = false;
		try {
			// Inizializza reverseTracking per il calcolo temporaneo delle distanze
			Hashtable<Integer, GPSCoordinate> reverseTracking = new Hashtable<>();
			int vertexCounter = 0;
	
			// Apertura e lettura del file contenente le coordinate dei vertici
			Scanner vertices = new Scanner(new File(vertexFile));
			while (vertices.hasNext()) {
				String[] line = vertices.nextLine().split("\t");
				// Assicurati che entrambe le coordinate siano presenti prima di aggiungere
				if (!line[0].isEmpty() && !line[1].isEmpty()) {
					GPSCoordinate coordinate = new GPSCoordinate(line[0] + "," + line[1]);
					reverseTracking.put(vertexCounter++, coordinate);
				}
			}
			vertices.close();
	
			// Assegnazione di reverseTracking a vertexCoordinates
			vertexCoordinates = reverseTracking;
	
			// Costruzione delle distanze tra i vertici
			numVertices = vertexCoordinates.size();
			distances = new int[numVertices][numVertices];
			for (int i = 0; i < numVertices; i++) {
				for (int j = 0; j < numVertices; j++) {
					if (i != j) {
						GPSCoordinate v1 = vertexCoordinates.get(i);
						GPSCoordinate v2 = vertexCoordinates.get(j);
						if (v1 != null && v2 != null) {
							int dist = v1.GetDistance(v2);
							distances[i][j] = dist;
							distances[j][i] = dist;
						}
					}
				}
			}
	
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	
	/**
	 * Constructor that creates a graph from tsv files; edges and distances
	 * You need to have at least one to generate the graph
	 * The expected graph file format is: 
	 * Line 1: number of vertices
	 * Lines 2 - end: v1 v2 cost (representing an edge)
	 * The distances file format is:
	 * Line 1: number of vertices
	 * Lines 2 - end : v1	v2	distance  
	 * @param graphFile 
	 * @param distancesFile
	 */
	public Graph(String graphFile, String distancesFile) {	
		isGrid = false;
		WETGenerated = false;
		try {
			// Stop from the very beginning if no files are provided
			if(graphFile == null && distancesFile ==null)
				throw new Exception("No files provided to create graph - shouldn't be here");
			
			// Read the edge information if provided
			if(graphFile != null) {		
				edgesProvided = true;
				Scanner graphEdges = new Scanner(new File(graphFile));
				this.numVertices = Integer.parseInt(graphEdges.nextLine());
		    	
		    	edges = new int[numVertices][numVertices];
		    	
		    	while(graphEdges.hasNext()) {
		    		int v1 = graphEdges.nextInt();
		    		int v2 = graphEdges.nextInt();
		    		
		    		int edgeCost = graphEdges.nextInt();
		    		
		    		edges[v1][v2] = edgeCost;
		    		
		    		graphEdges.nextLine();
		    	}
		    	graphEdges.close();
		    	edgesProvided = true;
		    	
		    	if(distancesFile == null) 
		    		CalculateDistances();
			}
			
			// Read the distance information if provided
			if(distancesFile!=null) {
				Scanner distScanner = new Scanner(new File(distancesFile));
				int numV = Integer.parseInt(distScanner.nextLine());
				
				// First check if we have the edges file
				// If yes, we need to check compatibility in terms of number of vertices
				if(edgesProvided && numV != this.numVertices) 
					throw new Exception("Incompatible files provided to create graph - check your files!");
								
				distances = new int[numV][numV];
				while(distScanner.hasNext()) {
		    		int v1 = distScanner.nextInt();
		    		int v2 = distScanner.nextInt();
		    		
		    		int dist = distScanner.nextInt();
		    		
		    		distances[v1][v2] = dist;
		    		
		    		distScanner.nextLine();
		    	}
				distScanner.close();
			}
	    	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}	    
	}
	
	/**
	 * Constructor that creates a graph after a set of traces are loaded
	 * @param locationsFromTraces 
	 */
	public Graph(Hashtable<Integer, GPSCoordinate> locationsFromTraces) { //METODO UTILIZZATO PER CREAZIONE GRAFO!!!!!!!!!!!!!
		this.numVertices = locationsFromTraces.size();
		isGrid = false;
		isGPS = true;
		edgesProvided = false;
		
		vertexCoordinates = locationsFromTraces;
	}
	
	public void FillUpOmega(Task[] allTasks) {
		this.WETGenerated = true;
		
		spatioTemporalTasks = new Hashtable<Integer, Vector<Task>>();
		for(Task t: allTasks) {
			if(spatioTemporalTasks.containsKey(t.hashCode())){//t.vertexLocation+","+t.completeBy) ) {
				Vector<Task> existing = spatioTemporalTasks.get(t.hashCode());
				existing.add(t);
				spatioTemporalTasks.put(t.hashCode(), existing);
			}
			else {
				Vector<Task> existing = new Vector<Task>();
				existing.add(t);
				spatioTemporalTasks.put(t.hashCode(), existing);
			}
				
		}
	}
	
	/**
	 * Method used to find subset of reachable locations on the network graph base don time constraints of the trip
	 * Hopefully, this should improve performance greatly
	 * @param startLoc
	 * @param endLoc
	 * @param tripDuration - total time of trip with flex
	 * @return a sorted array of the reachable locations only
	 */
	public int[] PruneVertices(int startLoc, int endLoc, int tripDuration) {
		Vector<Integer> result = new Vector<Integer>();

		// Very simple computation
		// MAake sure that the start and end locations are not included in this list
		for(int i=0; i<numVertices; i++) {
			if(i!= startLoc && i != endLoc)
				if(getDistanceBetween(startLoc,i) + getDistanceBetween(i,endLoc) <= tripDuration)
					result.add(i);
		}

		int[] toReturn = new int[result.size()+2];
		toReturn[0] = startLoc;
		toReturn[1] = endLoc;
		for(int i=2; i<toReturn.length; i++)
			toReturn[i] = result.get(i-2);
		return toReturn;
	}

	public double GetOmega(int vertex, int time) throws Exception {
		if(!WETGenerated)
			throw new Exception("You neeed to load the Omega function first");
		
		if(!spatioTemporalTasks.containsKey(Objects.hash(vertex,time)))
			return -1;
		double total = 0;
		for(Task t: spatioTemporalTasks.get(Objects.hash(vertex,time)))
			total += t.specifiedRevenue;		
		return total;
	}
	
	public RoutingResult FindCompleteRouteEdgeWeights(int startLoc, int endLoc, int departureTime, int flex) throws Exception{
		// Set up all necessary information for routing
		// Extract the pruned set of vertices
		// It's more correct to do it here, but we don't want to do it everytime
		// So , we'll just use what is sent as a parameter for now
		// Jan 2022
		
		
		if(!Experiment.theGraph.WETGenerated)
			return null;
		
		int tripDuration = Experiment.theGraph.getDistanceBetween(startLoc,endLoc) + flex;
		int[] reachableVertices = PruneVertices(startLoc, endLoc, tripDuration);

		//Extract the endpoints of the trip
//		int startLoc = reachableVertices[0];
//		int endLoc = reachableVertices[1];
		int numVertices = reachableVertices.length;

		// Calculate the duration of trip with correct definition of flex
		

		double[][] bestRevenue = new double[numVertices][tripDuration + 1];
		// To keep track of all the tasks completed on the way
		int[][] previousStep = new int [numVertices][tripDuration + 1];

		// Initially all revenue is set to -1  except for the start node
		for(int iNode = 0; iNode<numVertices; iNode++){
				bestRevenue[iNode][0] = Double.NEGATIVE_INFINITY;
		}
		bestRevenue[startLoc][0] = GetOmega(startLoc, departureTime%1440);

		for(int iNode = 0; iNode<numVertices; iNode++){
			for(int iTime=1; iTime < tripDuration+1; iTime++)
				bestRevenue[iNode][iTime] = Double.NEGATIVE_INFINITY;
		}


		for(int iTime = 1; iTime <= tripDuration; iTime++){
			for(int iNode = 0; iNode < numVertices; iNode++){
				double maxRev = -1;
				int chosenNode = -1;
				for(int jNode=0; jNode <  numVertices; jNode++){

					// Need a special case for when checking staying at place
					if(iNode == jNode) {
						if(bestRevenue[jNode][iTime-1] > maxRev) {
							maxRev = bestRevenue[jNode][iTime-1];
							chosenNode = jNode;
						}
					}
					else {
						// Get distance between them
						int theEdgeWeight = getDistanceBetween(reachableVertices[iNode],reachableVertices[jNode]);// theGraph.getWeightBetween(iNode,jNode);
						// Check if it's too far away, even if it's adjacent
						if(iTime - theEdgeWeight >= 0) {


							if(bestRevenue[jNode][iTime-theEdgeWeight] > maxRev) {
								maxRev = bestRevenue[jNode][iTime-theEdgeWeight];
								chosenNode = jNode;
							}
						}
					}
				}
				if(chosenNode == -1)
				{
					bestRevenue[iNode][iTime] = Double.NEGATIVE_INFINITY;
				}
				else{
					bestRevenue[iNode][iTime] = maxRev + GetOmega(reachableVertices[iNode],(iTime+departureTime)%1440);// temporalGraph.GetTemporalNodeWeight(iNode, iTime+theTrip.earliestDepartureTime);
					// This might not capture the last node
					previousStep[iNode][iTime] = chosenNode;
				}

			}
		}

		// After finishing the algorithm, backtrack to get the total rewards
		// And the path
		MyPath thePath = new MyPath();

		// Hope it works!
		int currentVertex = 1;// Index in reachable list
		int iTime = tripDuration;

		double totalReward = 0;

		while(iTime > 0) {
			thePath.AddStep(new SpatioTemporalPoint(reachableVertices[currentVertex], iTime), true);
			totalReward += GetOmega(reachableVertices[currentVertex],iTime%1440);

			int oldStep = currentVertex; // index in reachable list
			currentVertex = previousStep[currentVertex][iTime]; // contains indices in reachable list
			iTime -= getDistanceBetween(reachableVertices[currentVertex],reachableVertices[oldStep]);
		}

		int length = thePath.CalculateTotalDuration();
		length -= getDistanceBetween(startLoc,endLoc); // these are real vertex indices in graph
		return new RoutingResult(thePath, totalReward, length);//, null);
	}
	
	public RoutingResult FindCompleteRouteEdgeWeights(int startLoc, int endLoc, int departureTime, int flex, int[] reachableVertices) throws Exception{
		// Set up all necessary information for routing
		// Extract the pruned set of vertices
		// It's more correct to do it here, but we don't want to do it everytime
		// So , we'll just use what is sent as a parameter for now
		// Jan 2022
		
		
		if(!Experiment.theGraph.WETGenerated)
			return null;
		
		int tripDuration = Experiment.theGraph.getDistanceBetween(startLoc,endLoc) + flex;
//		int[] reachableVertices = PruneVertices(startLoc, endLoc, tripDuration);

		//Extract the endpoints of the trip
//		int startLoc = reachableVertices[0];
//		int endLoc = reachableVertices[1];
		int numVertices = reachableVertices.length;

		// Calculate the duration of trip with correct definition of flex
		

		double[][] bestRevenue = new double[numVertices][tripDuration + 1];
		// To keep track of all the tasks completed on the way
		int[][] previousStep = new int [numVertices][tripDuration + 1];

		// Initially all revenue is set to -1  except for the start node
		for(int iNode = 0; iNode<numVertices; iNode++){
				bestRevenue[iNode][0] = Double.NEGATIVE_INFINITY;
		}
		bestRevenue[startLoc][0] = GetOmega(startLoc, departureTime%1440);

		for(int iNode = 0; iNode<numVertices; iNode++){
			for(int iTime=1; iTime < tripDuration+1; iTime++)
				bestRevenue[iNode][iTime] = Double.NEGATIVE_INFINITY;
		}


		for(int iTime = 1; iTime <= tripDuration; iTime++){
			for(int iNode = 0; iNode < numVertices; iNode++){
				double maxRev = -1;
				int chosenNode = -1;
				for(int jNode=0; jNode <  numVertices; jNode++){

					// Need a special case for when checking staying at place
					if(iNode == jNode) {
						if(bestRevenue[jNode][iTime-1] > maxRev) {
							maxRev = bestRevenue[jNode][iTime-1];
							chosenNode = jNode;
						}
					}
					else {
						// Get distance between them
						int theEdgeWeight = getDistanceBetween(reachableVertices[iNode],reachableVertices[jNode]);// theGraph.getWeightBetween(iNode,jNode);
						// Check if it's too far away, even if it's adjacent
						if(iTime - theEdgeWeight >= 0) {


							if(bestRevenue[jNode][iTime-theEdgeWeight] > maxRev) {
								maxRev = bestRevenue[jNode][iTime-theEdgeWeight];
								chosenNode = jNode;
							}
						}
					}
				}
				if(chosenNode == -1)
				{
					bestRevenue[iNode][iTime] = Double.NEGATIVE_INFINITY;
				}
				else{
					bestRevenue[iNode][iTime] = maxRev + GetOmega(reachableVertices[iNode],(iTime+departureTime)%1440);// temporalGraph.GetTemporalNodeWeight(iNode, iTime+theTrip.earliestDepartureTime);
					// This might not capture the last node
					previousStep[iNode][iTime] = chosenNode;
				}

			}
		}

		// After finishing the algorithm, backtrack to get the total rewards
		// And the path
		MyPath thePath = new MyPath();

		// Hope it works!
		int currentVertex = 1;// Index in reachable list
		int iTime = tripDuration;

		double totalReward = 0;

		while(iTime > 0) {
			thePath.AddStep(new SpatioTemporalPoint(reachableVertices[currentVertex], iTime), true);
			totalReward += GetOmega(reachableVertices[currentVertex],iTime%1440);

			int oldStep = currentVertex; // index in reachable list
			currentVertex = previousStep[currentVertex][iTime]; // contains indices in reachable list
			iTime -= getDistanceBetween(reachableVertices[currentVertex],reachableVertices[oldStep]);
		}

		int length = thePath.CalculateTotalDuration();
		length -= getDistanceBetween(startLoc,endLoc); // these are real vertex indices in graph
		return new RoutingResult(thePath, totalReward, length);//, null);
	}


//	public RoutingResult FindCompleteShortestRoute(int[] reachableVertices, int departureTime, int flex) {
//
//		// Just run the routing algorithm with no flexibility
//		RoutingResult theResult = FindCompleteRouteEdgeWeights(reachableVertices, departureTime, 0);
//
//		return theResult;
//	}
	
	
	/**
	 * For this instance, find distances all-to-all.	 
	 * Always resets the distances 	
	 */
	public void CalculateDistances(){
		if(!isGrid) {
			for(int i=0; i<numVertices;i++)
	        	algo_dijkstra(i);
		}
		else {	
			CalculateDistancesGrid();	
		}
	
	
	}
	
	/**
	 * For this method, graph must be a grid to compute distances. Vertices are numbered from top to
	 * bottom, left to right. The calculated distances are stored in the distances matrix	
	 * @throws Exception 
	 */
	public void CalculateDistancesGrid() {		
		distances = new int[numVertices][numVertices];
		for(int i=0; i<numVertices; i++) {
			for(int j=0; j<numVertices; j++) {
				if(i==j)
					distances[i][j] = 0;
				else {
					// Get height difference
					int height = Math.abs(i-j)/gridWidth;
					// Get width difference
					int width = Math.abs(i%gridWidth - j%gridWidth);
					// Get distance
					distances[i][j] = height + width;
					distances[j][i] = height + width;
				}
			}
		}
	}
	
	/** Implementation of Dijkstra's algorithm for graph (adjacency matrix) 
	 * 
	 * @param graph
	 * @param src_node
	 */
    private void algo_dijkstra(int src_node)  { 
        int path_array[] = new int[numVertices]; // The output array. dist[i] will hold 
        // the shortest distance from src to i 
   
        // spt (shortest path set) contains vertices that have shortest path 
        Boolean sptSet[] = new Boolean[numVertices]; 
   
        // Initially all the distances are INFINITE and stpSet[] is set to false 
        for (int i = 0; i < numVertices; i++) { 
            path_array[i] = Integer.MAX_VALUE; 
            sptSet[i] = false; 
        } 
   
        // Path between vertex and itself is always 0 
        path_array[src_node] = 0; 
   // now find shortest path for all vertices  
        for (int count = 0; count < numVertices - 1; count++) { 
            // call minDistance method to find the vertex with min distance
            int u = minDistance(path_array, sptSet); 
              // the current vertex u is processed
            sptSet[u] = true; 
              // process adjacent nodes of the current vertex
            for (int v = 0; v < numVertices; v++) 
   
                // if vertex v not in sptset then update it  
                if (!sptSet[v] && edges[u][v] != 0 && path_array[u] != 
                            Integer.MAX_VALUE && path_array[u] 
                            + edges[u][v] < path_array[v]) 
                            path_array[v] = path_array[u] + edges[u][v]; 
        } 
   
        // print the path array 
//        printMinpath(path_array); 
        for (int i = 0; i < numVertices; i++) 
            distances[src_node][i] = path_array[i];
    } 
    
    /**
     * find a vertex with minimum distance
     * @param path_array
     * @param sptSet
     * @return
     */
    private int minDistance(int path_array[], Boolean sptSet[])   { 
        // Initialize min value 
        int min = Integer.MAX_VALUE, min_index = -1; 
        for (int v = 0; v < numVertices; v++) 
            if (sptSet[v] == false && path_array[v] <= min) { 
                min = path_array[v]; 
                min_index = v; 
            } 
   
        return min_index; 
    } 
	
	
	/**
	 * This method is a getter for the edges array
	 * It is created to help reduce the size of arrays used when working with grids
	 * @param vertex1, the first end point
	 * @param vertex2, the second end point
	 * @return the adjacency between two vertices
	 */
	public boolean isAdjacent(int vertex1, int vertex2) {		
		if(!isGrid) 
			return edges[vertex1][vertex2]> 0;
		
		if(vertex1 == vertex2)
			return true;
		
		int irow = vertex1/gridWidth;
		int jrow = vertex2/gridWidth;
		int icolumn = vertex1 % gridWidth;
		int jcolumn = vertex2 % gridWidth;
		if((irow==jrow && Math.abs(icolumn-jcolumn)==1) || (icolumn==jcolumn && Math.abs(irow-jrow)==1))
			return true;
		
		return false;
	}
	/**
	 * This method is a getter for the distances array
	 * It is created to help reduce the size of arrays used when working with trace-based grids
	 * @param vertex1, the first end point
	 * @param vertex2, the second end point
	 * @return
	 */
	public int getDistanceBetween(int vertex1, int vertex2) {
		MemoryUsageTracker memoryTracker = MemoryUsageTracker.getInstance();
		
		int distance = 0;
		if (isGPS) {
			GPSCoordinate v1 = vertexCoordinates.get(vertex1);
			GPSCoordinate v2 = vertexCoordinates.get(vertex2);
			
			String key = vertex1 + "-" + vertex2;
			String reverseKey = vertex2 + "-" + vertex1;
	
			if (dijkstraResultsCache.containsKey(key)) {
				saveParticipantDetails(v1, v2, dijkstraResultsCache.get(key));
				return dijkstraResultsCache.get(key);
			} else if (dijkstraResultsCache.containsKey(reverseKey)) {
				saveParticipantDetails(v1, v2, dijkstraResultsCache.get(reverseKey));
				return dijkstraResultsCache.get(reverseKey);
			} else {
                for(int method = 1; method <= 4; method++){
					if(method==3) continue;
					dij.setuseCache(method);
					long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
					long cpuTimeBefore = System.nanoTime();
					
					distance = (int)Math.round(dij.calculateDistance(v1, v2));

					long cpuTimeAfter = System.nanoTime();
					long memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
					long memoryUsed = (memoryAfter - memoryBefore) / (1024 * 1024);
					long processTime = (cpuTimeAfter - cpuTimeBefore) / 1_000_000;
					dijkstraResultsCache.put(key, distance);
					saveParticipantDetails(v1, v2, distance);
					memoryTracker.addMemoryUsage(memoryUsed);

					if (memoryAfter > maxRamForDistance) {
						maxRamForDistance = memoryAfter;
					}
					DataStore.addDataIfAbsent(new DataPoint(
						v1.getLatitudine(), v1.getLongitudine(), v2.getLatitudine(), v2.getLongitudine(), 
						processTime, memoryUsed, distance
					), contaExpress, method);
				}
				return distance;
			}
		}else{
			System.out.println("non GPS!!");
			return -1;
		}
		
	}
	public void getSizeDIJ(){
		dij.getSize();
	}
	public void inizializeCache(){
		dij.initializePersistentCaches();
	}
	public void clearCache(){
		dij.clearStorage();
	}
	public void clearCacheDijkstra() {
        dijkstraResultsCache.clear();
    }
	public long getMaxRamForDistance(){
		return maxRamForDistance;
	}
	public void setContaExpress( int conta){
		this.contaExpress = conta;
	}
	
	/*
	public int getDistanceBetween(int vertex1, int vertex2) {
		if(isGPS) {
			GPSCoordinate v1 = vertexCoordinates.get(vertex1);
			GPSCoordinate v2 = vertexCoordinates.get(vertex2);
	
			// Creazione di una chiave univoca per la coppia di vertici
			String key = vertex1 + "-" + vertex2;
			String reverseKey = vertex2 + "-" + vertex1; // Perché la distanza è simmetrica
	
			// Controlla se il risultato è già presente nella cache
			if(dijkstraResultsCache.containsKey(key)) {
				saveParticipantDetails(v1, v2, dijkstraResultsCache.get(key));
				return dijkstraResultsCache.get(key);
			} else if (dijkstraResultsCache.containsKey(reverseKey)) {
				saveParticipantDetails(v1, v2, dijkstraResultsCache.get(reverseKey));
				return dijkstraResultsCache.get(reverseKey);
			} else {
				Runtime runtime = Runtime.getRuntime();
				long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
				int distance = (int)dijkstra.calculateDistance(v1, v2);
				
				long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
				long memoryUsed = (memoryAfter - memoryBefore)/(1024 * 1024);
				long maxMemoryMB = runtime.maxMemory()/(1024 * 1024); //Max memoria in MB
				dijkstraResultsCache.put(key, distance);
				saveParticipantDetails(v1, v2, distance);

				try (PrintWriter out = new PrintWriter(new FileWriter("statusRam.txt", true))) {
					if(memoryUsed != 0) {
						out.print("Memoria utilizzata(MB): " + memoryUsed);
						out.println("          Max disponibile (MB): " + maxMemoryMB);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// Altri casi non legati alle coordinate GPS
		return -1; // O gestione alternativa per i casi non GPS
	}
	*/
	 private void saveParticipantDetails(GPSCoordinate start, GPSCoordinate end, int distance) {
        if(printControl == true){
			try (FileWriter fw = new FileWriter("ParticipantDetails.txt", true);
				PrintWriter pw = new PrintWriter(fw)) {
					if(distance != -1 && distance != 0){
						pw.println("Partenza: " + start + ", Arrivo: " + end + ", Distanza: " + distance + " km" + ", Revenue" + (int)distance*1.4);
					}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    }
	public void clearMaxRam(){
		maxRamForDistance = 0;
	}
	/**
	 * Calculates the column of the vertex based on the grid width
	 * @param vertex The vertex ID
	 * @return The x-coordinate of that vertex in the grid
	 */
	public int GetXCoordinate(int vertex) {
		return (vertex % gridWidth);
	}
	
	/**
	 * Calculates the row of the vertex based on the grid width
	 * @param vertex The vertex ID
	 * @return The y-coordinate of that vertex in the grid
	 */
	public int GetYCoordinate(int vertex) {
		return (vertex/gridWidth);
	}
	
	public double GetAngleOfLine(int vertex1, int vertex2) {
		int x1 = GetXCoordinate(vertex1);
		int y1 = GetYCoordinate(vertex1);
		int x2 = GetXCoordinate(vertex2);
		int y2 = GetYCoordinate(vertex2);
		
		if(x1 == x2) return Math.PI/2;
		if(y1 == y2) return 0;
		
		double slope = (y2-y1)/(double)(x2-x1);
		double angle = Math.atan(slope);
		
		if(angle <= 0) angle+= Math.PI;
		return angle;
	}
	
	public double CalculateCosineSimilarity(int vertex1, int vertex2) {
		int x1 = GetXCoordinate(vertex1) + 1;
		int y1 = GetYCoordinate(vertex1) + 1;
		int x2 = GetXCoordinate(vertex2) + 1;
		int y2 = GetYCoordinate(vertex2) + 1;
		
		return (x1*x2 + y1*y2) / (Math.sqrt(Math.pow(x1,2) + Math.pow(y1,2)) * Math.sqrt(Math.pow(x2,2) + Math.pow(y2,2)));
	}

	/**
	 * Method to create the krackhardt_kite_graph as done in NetworkX library.
	 * Needs no parameters because we follow the exact definition of the library.
	 * We also save the distances matrix, and centrality measures. All values are obtained from the library.
	 * @return The graph
	 */
	public static Graph CreateKiteGraph(){
		Graph theGraph = new Graph(10);
		
		theGraph.edges = new int[10][10];
		theGraph.distances = new int[10][10];
		
		// Create the edges
		//[(0, 1), (0, 2), (0, 3), (0, 5), (1, 3), (1, 4), (1, 6), (2, 3), (2, 5), (3, 4), (3, 5), (3, 6), (4, 6), (5, 6), (5, 7), (6, 7), (7, 8), (8, 9)]
		theGraph.edges[0][1] = 1; theGraph.edges[2][3] = 1; theGraph.edges[5][7] = 1;
		theGraph.edges[0][2] = 1; theGraph.edges[2][5] = 1; theGraph.edges[6][7] = 1;
		theGraph.edges[0][3] = 1; theGraph.edges[3][4] = 1; theGraph.edges[7][8] = 1;
		theGraph.edges[0][5] = 1; theGraph.edges[3][5] = 1; theGraph.edges[8][9] = 1;
		theGraph.edges[1][3] = 1; theGraph.edges[3][6] = 1;
		theGraph.edges[1][4] = 1; theGraph.edges[4][6] = 1;
		theGraph.edges[1][6] = 1; theGraph.edges[5][6] = 1;		
		//Make it undirected
		theGraph.edges[1][0] = 1; theGraph.edges[3][2] = 1; theGraph.edges[7][5] = 1;
		theGraph.edges[2][0] = 1; theGraph.edges[5][2] = 1; theGraph.edges[7][6] = 1;
		theGraph.edges[3][0] = 1; theGraph.edges[4][3] = 1; theGraph.edges[8][7] = 1;
		theGraph.edges[5][0] = 1; theGraph.edges[5][3] = 1; theGraph.edges[9][8] = 1;
		theGraph.edges[3][1] = 1; theGraph.edges[6][3] = 1;
		theGraph.edges[4][1] = 1; theGraph.edges[6][4] = 1;
		theGraph.edges[6][1] = 1; theGraph.edges[6][5] = 1;
		
//		// Store betweeness information
//		theGraph.betweenessCentrality[0] = 0.023;
//		theGraph.betweenessCentrality[1] =  0.023;
//		theGraph.betweenessCentrality[2] =  0.000;
//		theGraph.betweenessCentrality[3] =  0.102;
//		theGraph.betweenessCentrality[4] =  0.000;
//		theGraph.betweenessCentrality[5] =  0.231;
//		theGraph.betweenessCentrality[6] =  0.231;
//		theGraph.betweenessCentrality[7] =  0.389;
//		theGraph.betweenessCentrality[8] =  0.222;
//		theGraph.betweenessCentrality[9] =  0.000;
//		
//		// Store closeness centrality
//		theGraph.closenessCentrality[0] = 0.529;
//		theGraph.closenessCentrality[1] =  0.529;
//		theGraph.closenessCentrality[2] =  0.500;
//		theGraph.closenessCentrality[3] =  0.600;
//		theGraph.closenessCentrality[4] =  0.500;
//		theGraph.closenessCentrality[5] =  0.643;
//		theGraph.closenessCentrality[6] =  0.643;
//		theGraph.closenessCentrality[7] =  0.600;
//		theGraph.closenessCentrality[8] =  0.429;
//		theGraph.closenessCentrality[9] =  0.310;
		
		// Store the distances
		theGraph.distances = new int[theGraph.numVertices][theGraph.numVertices];
		theGraph.distances[0][1] = 1; theGraph.distances[0][4] = 2; theGraph.distances[0][7] = 2;
		theGraph.distances[0][2] = 1; theGraph.distances[0][5] = 1; theGraph.distances[0][8] = 3;
		theGraph.distances[0][3] = 1; theGraph.distances[0][6] = 2; theGraph.distances[0][9] = 4;
		theGraph.distances[1][0] = 1; theGraph.distances[1][4] = 1; theGraph.distances[1][7] = 2;
		theGraph.distances[1][2] = 2; theGraph.distances[1][5] = 2; theGraph.distances[1][8] = 3;
		theGraph.distances[1][3] = 1; theGraph.distances[1][6] = 1; theGraph.distances[1][9] = 4;
		theGraph.distances[2][0] = 1; theGraph.distances[2][4] = 2; theGraph.distances[2][7] = 2;
		theGraph.distances[2][1] = 2; theGraph.distances[2][5] = 1; theGraph.distances[2][8] = 3;
		theGraph.distances[2][3] = 1; theGraph.distances[2][6] = 2; theGraph.distances[2][9] = 4;
		theGraph.distances[3][1] = 1; theGraph.distances[3][4] = 1; theGraph.distances[3][7] = 2;
		theGraph.distances[3][2] = 1; theGraph.distances[3][5] = 1; theGraph.distances[3][8] = 3;
		theGraph.distances[3][0] = 1; theGraph.distances[3][6] = 1; theGraph.distances[3][9] = 4;
		theGraph.distances[4][1] = 1; theGraph.distances[4][0] = 2; theGraph.distances[4][7] = 2;
		theGraph.distances[4][2] = 2; theGraph.distances[4][5] = 2; theGraph.distances[4][8] = 3;
		theGraph.distances[4][3] = 1; theGraph.distances[4][6] = 1; theGraph.distances[4][9] = 4;
		
		theGraph.distances[5][1] = 2; theGraph.distances[5][4] = 2; theGraph.distances[5][7] = 1;
		theGraph.distances[5][2] = 1; theGraph.distances[5][0] = 1; theGraph.distances[5][8] = 2;
		theGraph.distances[5][3] = 1; theGraph.distances[5][6] = 1; theGraph.distances[5][9] = 3;
		theGraph.distances[6][1] = 1; theGraph.distances[6][4] = 1; theGraph.distances[6][7] = 1;
		theGraph.distances[6][2] = 2; theGraph.distances[6][5] = 1; theGraph.distances[6][8] = 2;
		theGraph.distances[6][3] = 1; theGraph.distances[6][0] = 2; theGraph.distances[6][9] = 3;
		theGraph.distances[7][1] = 2; theGraph.distances[7][4] = 2; theGraph.distances[7][0] = 2;
		theGraph.distances[7][2] = 2; theGraph.distances[7][5] = 1; theGraph.distances[7][8] = 1;
		theGraph.distances[7][3] = 2; theGraph.distances[7][6] = 1; theGraph.distances[7][9] = 2;
		theGraph.distances[8][1] = 3; theGraph.distances[8][4] = 3; theGraph.distances[8][7] = 1;
		theGraph.distances[8][2] = 3; theGraph.distances[8][5] = 2; theGraph.distances[8][0] = 3;
		theGraph.distances[8][3] = 3; theGraph.distances[8][6] = 2; theGraph.distances[8][9] = 1;
		theGraph.distances[9][1] = 4; theGraph.distances[9][4] = 4; theGraph.distances[9][7] = 2;
		theGraph.distances[9][2] = 4; theGraph.distances[9][5] = 3; theGraph.distances[9][8] = 1;
		theGraph.distances[9][3] = 4; theGraph.distances[9][6] = 3; theGraph.distances[9][0] = 4;
		
		return theGraph;
	}
	
	public static Graph CreateTutteGraph(){
		Graph theGraph = new Graph(46);
		
		theGraph.edges = new int[46][46];
		theGraph.distances = new int[46][46];	
		
			int[][] tempEdges = {
				{
					-1, 1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, 1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, 1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, 1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, 1, -1, 1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, 1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, 1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, 1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, 1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1 },
					{
					-1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1 },
					{
					-1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, 1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, 1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, 1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, 1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, 1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, 1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, 1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, 1, -1, 1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, 1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, 1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, 1, -1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, 1, -1, 1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, 1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, 1, -1, -1 },
					{
					-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, -1, -1, -1 }
					};
		theGraph.edges = tempEdges;
		
		int[][] tempDistances = {
				{ 0, 1, 1, 1, 2, 3, 4, 5, 4, 3, 2, 2, 3, 4, 5, 5, 4, 3, 2, 2, 3, 4, 5, 5, 4, 3, 2, 5, 5, 4, 4, 4, 5, 3, 5, 4, 4, 4, 5, 3, 5, 4, 4, 4, 5, 3 },
				{ 1, 0, 2, 2, 1, 2, 3, 4, 5, 4, 3, 3, 4, 5, 5, 6, 5, 4, 3, 3, 4, 5, 6, 4, 3, 2, 1, 4, 4, 3, 3, 3, 4, 2, 6, 5, 5, 5, 6, 4, 6, 5, 5, 5, 6, 4 },
				{ 1, 2, 0, 2, 3, 4, 5, 4, 3, 2, 1, 1, 2, 3, 4, 4, 5, 4, 3, 3, 4, 5, 5, 6, 5, 4, 3, 6, 6, 5, 5, 5, 6, 4, 4, 3, 3, 3, 4, 2, 6, 5, 5, 5, 6, 4 },
				{ 1, 2, 2, 0, 3, 4, 5, 6, 5, 4, 3, 3, 4, 5, 6, 4, 3, 2, 1, 1, 2, 3, 4, 4, 5, 4, 3, 5, 6, 5, 5, 5, 6, 4, 6, 5, 5, 5, 6, 4, 4, 3, 3, 3, 4, 2 },
				{ 2, 1, 3, 3, 0, 1, 2, 3, 4, 5, 4, 4, 5, 5, 4, 6, 6, 5, 4, 4, 5, 5, 6, 4, 4, 3, 2, 3, 3, 2, 2, 3, 4, 1, 5, 6, 6, 6, 5, 5, 7, 6, 6, 6, 7, 5 },
				{ 3, 2, 4, 4, 1, 0, 1, 2, 3, 4, 5, 5, 5, 4, 3, 5, 6, 6, 5, 5, 5, 4, 5, 3, 4, 4, 3, 2, 2, 1, 2, 3, 3, 2, 4, 5, 6, 5, 4, 6, 6, 6, 7, 7, 7, 6 },
				{ 4, 3, 5, 5, 2, 1, 0, 1, 2, 3, 4, 5, 4, 3, 2, 4, 5, 6, 6, 5, 4, 3, 4, 2, 3, 4, 4, 1, 2, 2, 3, 4, 3, 3, 3, 4, 5, 4, 3, 5, 5, 5, 6, 7, 6, 6 },
				{ 5, 4, 4, 6, 3, 2, 1, 0, 1, 2, 3, 4, 3, 2, 1, 3, 4, 5, 6, 6, 5, 4, 4, 3, 4, 5, 5, 2, 3, 3, 4, 5, 4, 4, 2, 3, 4, 3, 2, 4, 5, 6, 7, 6, 5, 7 },
				{ 4, 5, 3, 5, 4, 3, 2, 1, 0, 1, 2, 4, 4, 3, 2, 4, 5, 6, 6, 6, 6, 5, 5, 4, 5, 6, 6, 3, 4, 4, 5, 6, 5, 5, 2, 3, 3, 2, 1, 3, 6, 7, 8, 7, 6, 7 },
				{ 3, 4, 2, 4, 5, 4, 3, 2, 1, 0, 1, 3, 4, 4, 3, 5, 6, 6, 5, 5, 6, 6, 6, 5, 6, 6, 5, 4, 5, 5, 6, 7, 6, 6, 3, 3, 2, 1, 2, 2, 7, 7, 7, 7, 7, 6 },
				{ 2, 3, 1, 3, 4, 5, 4, 3, 2, 1, 0, 2, 3, 4, 4, 5, 6, 5, 4, 4, 5, 6, 6, 6, 6, 5, 4, 5, 6, 6, 6, 6, 7, 5, 4, 3, 2, 2, 3, 1, 7, 6, 6, 6, 7, 5 },
				{ 2, 3, 1, 3, 4, 5, 5, 4, 4, 3, 2, 0, 1, 2, 3, 3, 4, 5, 4, 4, 5, 5, 4, 6, 6, 5, 4, 6, 7, 6, 6, 6, 7, 5, 3, 2, 2, 3, 4, 1, 5, 6, 6, 6, 5, 5 },
				{ 3, 4, 2, 4, 5, 5, 4, 3, 4, 4, 3, 1, 0, 1, 2, 2, 3, 4, 5, 5, 5, 4, 3, 5, 6, 6, 5, 5, 6, 6, 7, 7, 7, 6, 2, 1, 2, 3, 3, 2, 4, 5, 6, 5, 4, 6 },
				{ 4, 5, 3, 5, 5, 4, 3, 2, 3, 4, 4, 2, 1, 0, 1, 1, 2, 3, 4, 5, 4, 3, 2, 4, 5, 6, 6, 4, 5, 5, 6, 7, 6, 6, 2, 2, 3, 4, 3, 3, 3, 4, 5, 4, 3, 5 },
				{ 5, 5, 4, 6, 4, 3, 2, 1, 2, 3, 4, 3, 2, 1, 0, 2, 3, 4, 5, 6, 5, 4, 3, 4, 5, 6, 6, 3, 4, 4, 5, 6, 5, 5, 1, 2, 3, 3, 2, 4, 4, 5, 6, 5, 4, 6 },
				{ 5, 6, 4, 4, 6, 5, 4, 3, 4, 5, 5, 3, 2, 1, 2, 0, 1, 2, 3, 4, 3, 2, 1, 3, 4, 5, 6, 4, 5, 6, 7, 6, 5, 7, 3, 3, 4, 5, 4, 4, 2, 3, 4, 3, 2, 4 },
				{ 4, 5, 5, 3, 6, 6, 5, 4, 5, 6, 6, 4, 3, 2, 3, 1, 0, 1, 2, 4, 4, 3, 2, 4, 5, 6, 6, 5, 6, 7, 8, 7, 6, 7, 4, 4, 5, 6, 5, 5, 2, 3, 3, 2, 1, 3 },
				{ 3, 4, 4, 2, 5, 6, 6, 5, 6, 6, 5, 5, 4, 3, 4, 2, 1, 0, 1, 3, 4, 4, 3, 5, 6, 6, 5, 6, 7, 7, 7, 7, 7, 6, 5, 5, 6, 7, 6, 6, 3, 3, 2, 1, 2, 2 },
				{ 2, 3, 3, 1, 4, 5, 6, 6, 6, 5, 4, 4, 5, 4, 5, 3, 2, 1, 0, 2, 3, 4, 4, 5, 6, 5, 4, 6, 7, 6, 6, 6, 7, 5, 6, 6, 6, 6, 7, 5, 4, 3, 2, 2, 3, 1 },
				{ 2, 3, 3, 1, 4, 5, 5, 6, 6, 5, 4, 4, 5, 5, 6, 4, 4, 3, 2, 0, 1, 2, 3, 3, 4, 5, 4, 4, 5, 6, 6, 6, 5, 5, 7, 6, 6, 6, 7, 5, 3, 2, 2, 3, 4, 1 },
				{ 3, 4, 4, 2, 5, 5, 4, 5, 6, 6, 5, 5, 5, 4, 5, 3, 4, 4, 3, 1, 0, 1, 2, 2, 3, 4, 5, 3, 4, 5, 6, 5, 4, 6, 6, 6, 7, 7, 7, 6, 2, 1, 2, 3, 3, 2 },
				{ 4, 5, 5, 3, 5, 4, 3, 4, 5, 6, 6, 5, 4, 3, 4, 2, 3, 4, 4, 2, 1, 0, 1, 1, 2, 3, 4, 2, 3, 4, 5, 4, 3, 5, 5, 5, 6, 7, 6, 6, 2, 2, 3, 4, 3, 3 },
				{ 5, 6, 5, 4, 6, 5, 4, 4, 5, 6, 6, 4, 3, 2, 3, 1, 2, 3, 4, 3, 2, 1, 0, 2, 3, 4, 5, 3, 4, 5, 6, 5, 4, 6, 4, 4, 5, 6, 5, 5, 1, 2, 3, 3, 2, 4 },
				{ 5, 4, 6, 4, 4, 3, 2, 3, 4, 5, 6, 6, 5, 4, 4, 3, 4, 5, 5, 3, 2, 1, 2, 0, 1, 2, 3, 1, 2, 3, 4, 3, 2, 4, 5, 6, 7, 6, 5, 7, 3, 3, 4, 5, 4, 4 },
				{ 4, 3, 5, 5, 4, 4, 3, 4, 5, 6, 6, 6, 6, 5, 5, 4, 5, 6, 6, 4, 3, 2, 3, 1, 0, 1, 2, 2, 2, 3, 3, 2, 1, 3, 6, 7, 8, 7, 6, 7, 4, 4, 5, 6, 5, 5 },
				{ 3, 2, 4, 4, 3, 4, 4, 5, 6, 6, 5, 5, 6, 6, 6, 5, 6, 6, 5, 5, 4, 3, 4, 2, 1, 0, 1, 3, 3, 3, 2, 1, 2, 2, 7, 7, 7, 7, 7, 6, 5, 5, 6, 7, 6, 6 },
				{ 2, 1, 3, 3, 2, 3, 4, 5, 6, 5, 4, 4, 5, 6, 6, 6, 6, 5, 4, 4, 5, 4, 5, 3, 2, 1, 0, 4, 4, 3, 2, 2, 3, 1, 7, 6, 6, 6, 7, 5, 6, 6, 6, 6, 7, 5 },
				{ 5, 4, 6, 5, 3, 2, 1, 2, 3, 4, 5, 6, 5, 4, 3, 4, 5, 6, 6, 4, 3, 2, 3, 1, 2, 3, 4, 0, 1, 2, 3, 3, 2, 4, 4, 5, 6, 5, 4, 6, 4, 4, 5, 6, 5, 5 },
				{ 5, 4, 6, 6, 3, 2, 2, 3, 4, 5, 6, 7, 6, 5, 4, 5, 6, 7, 7, 5, 4, 3, 4, 2, 2, 3, 4, 1, 0, 1, 2, 2, 1, 3, 5, 6, 7, 6, 5, 7, 5, 5, 6, 7, 6, 6 },
				{ 4, 3, 5, 5, 2, 1, 2, 3, 4, 5, 6, 6, 6, 5, 4, 6, 7, 7, 6, 6, 5, 4, 5, 3, 3, 3, 3, 2, 1, 0, 1, 2, 2, 2, 5, 6, 7, 6, 5, 7, 6, 6, 7, 8, 7, 7 },
				{ 4, 3, 5, 5, 2, 2, 3, 4, 5, 6, 6, 6, 7, 6, 5, 7, 8, 7, 6, 6, 6, 5, 6, 4, 3, 2, 2, 3, 2, 1, 0, 1, 2, 1, 6, 7, 8, 7, 6, 7, 7, 7, 8, 8, 8, 7 },
				{ 4, 3, 5, 5, 3, 3, 4, 5, 6, 7, 6, 6, 7, 7, 6, 6, 7, 7, 6, 6, 5, 4, 5, 3, 2, 1, 2, 3, 2, 2, 1, 0, 1, 2, 7, 8, 8, 8, 7, 7, 6, 6, 7, 8, 7, 7 },
				{ 5, 4, 6, 6, 4, 3, 3, 4, 5, 6, 7, 7, 7, 6, 5, 5, 6, 7, 7, 5, 4, 3, 4, 2, 1, 2, 3, 2, 1, 2, 2, 1, 0, 3, 6, 7, 8, 7, 6, 8, 5, 5, 6, 7, 6, 6 },
				{ 3, 2, 4, 4, 1, 2, 3, 4, 5, 6, 5, 5, 6, 6, 5, 7, 7, 6, 5, 5, 6, 5, 6, 4, 3, 2, 1, 4, 3, 2, 1, 2, 3, 0, 6, 7, 7, 7, 6, 6, 7, 7, 7, 7, 8, 6 },
				{ 5, 6, 4, 6, 5, 4, 3, 2, 2, 3, 4, 3, 2, 2, 1, 3, 4, 5, 6, 7, 6, 5, 4, 5, 6, 7, 7, 4, 5, 5, 6, 7, 6, 6, 0, 1, 2, 2, 1, 3, 5, 6, 7, 6, 5, 7 },
				{ 4, 5, 3, 5, 6, 5, 4, 3, 3, 3, 3, 2, 1, 2, 2, 3, 4, 5, 6, 6, 6, 5, 4, 6, 7, 7, 6, 5, 6, 6, 7, 8, 7, 7, 1, 0, 1, 2, 2, 2, 5, 6, 7, 6, 5, 7 },
				{ 4, 5, 3, 5, 6, 6, 5, 4, 3, 2, 2, 2, 2, 3, 3, 4, 5, 6, 6, 6, 7, 6, 5, 7, 8, 7, 6, 6, 7, 7, 8, 8, 8, 7, 2, 1, 0, 1, 2, 1, 6, 7, 8, 7, 6, 7 },
				{ 4, 5, 3, 5, 6, 5, 4, 3, 2, 1, 2, 3, 3, 4, 3, 5, 6, 7, 6, 6, 7, 7, 6, 6, 7, 7, 6, 5, 6, 6, 7, 8, 7, 7, 2, 2, 1, 0, 1, 2, 7, 8, 8, 8, 7, 7 },
				{ 5, 6, 4, 6, 5, 4, 3, 2, 1, 2, 3, 4, 3, 3, 2, 4, 5, 6, 7, 7, 7, 6, 5, 5, 6, 7, 7, 4, 5, 5, 6, 7, 6, 6, 1, 2, 2, 1, 0, 3, 6, 7, 8, 7, 6, 8 },
				{ 3, 4, 2, 4, 5, 6, 5, 4, 3, 2, 1, 1, 2, 3, 4, 4, 5, 6, 5, 5, 6, 6, 5, 7, 7, 6, 5, 6, 7, 7, 7, 7, 8, 6, 3, 2, 1, 2, 3, 0, 6, 7, 7, 7, 6, 6 },
				{ 5, 6, 6, 4, 7, 6, 5, 5, 6, 7, 7, 5, 4, 3, 4, 2, 2, 3, 4, 3, 2, 2, 1, 3, 4, 5, 6, 4, 5, 6, 7, 6, 5, 7, 5, 5, 6, 7, 6, 6, 0, 1, 2, 2, 1, 3 },
				{ 4, 5, 5, 3, 6, 6, 5, 6, 7, 7, 6, 6, 5, 4, 5, 3, 3, 3, 3, 2, 1, 2, 2, 3, 4, 5, 6, 4, 5, 6, 7, 6, 5, 7, 6, 6, 7, 8, 7, 7, 1, 0, 1, 2, 2, 2 },
				{ 4, 5, 5, 3, 6, 7, 6, 7, 8, 7, 6, 6, 6, 5, 6, 4, 3, 2, 2, 2, 2, 3, 3, 4, 5, 6, 6, 5, 6, 7, 8, 7, 6, 7, 7, 7, 8, 8, 8, 7, 2, 1, 0, 1, 2, 1 },
				{ 4, 5, 5, 3, 6, 7, 7, 6, 7, 7, 6, 6, 5, 4, 5, 3, 2, 1, 2, 3, 3, 4, 3, 5, 6, 7, 6, 6, 7, 8, 8, 8, 7, 7, 6, 6, 7, 8, 7, 7, 2, 2, 1, 0, 1, 2 },
				{ 5, 6, 6, 4, 7, 7, 6, 5, 6, 7, 7, 5, 4, 3, 4, 2, 1, 2, 3, 4, 3, 3, 2, 4, 5, 6, 7, 5, 6, 7, 8, 7, 6, 8, 5, 5, 6, 7, 6, 6, 1, 2, 2, 1, 0, 3 },
				{ 3, 4, 4, 2, 5, 6, 6, 7, 7, 6, 5, 5, 6, 5, 6, 4, 3, 2, 1, 1, 2, 3, 4, 4, 5, 6, 5, 5, 6, 7, 7, 7, 6, 6, 7, 7, 7, 7, 8, 6, 3, 2, 1, 2, 3, 0 }
				};
		
		theGraph.distances = tempDistances;
		
////		double[] close = {
////				0.278 , 0.257 , 0.257 , 0.257 , 0.239 , 0.243 , 0.263 , 0.259 , 0.232 , 0.222 , 0.230 , 0.239 , 0.243 , 0.263 , 0.257 , 0.259 , 0.232 , 0.222 , 0.230 , 0.239 , 0.243 , 0.263 , 0.257 , 0.259 , 0.232 , 0.222 , 0.230 , 0.257 , 0.224 , 0.218 , 0.198 , 0.197 , 0.209 , 0.210 , 0.224 , 0.218 , 0.198 , 0.197 , 0.209 , 0.210 , 0.224 , 0.218 , 0.198 , 0.197 , 0.209 , 0.210  };
////		double[] between = {
//				0.233 , 0.148 , 0.148 , 0.148 , 0.072 , 0.086 , 0.158 , 0.159 , 0.075 , 0.059 , 0.073 , 0.072 , 0.086 , 0.158 , 0.115 , 0.159 , 0.075 , 0.059 , 0.073 , 0.072 , 0.086 , 0.158 , 0.115 , 0.159 , 0.075 , 0.059 , 0.073 , 0.115 , 0.038 , 0.033 , 0.015 , 0.017 , 0.021 , 0.024 , 0.038 , 0.033 , 0.015 , 0.017 , 0.021 , 0.024 , 0.038 , 0.033 , 0.015 , 0.017 , 0.021 , 0.024  };
////		theGraph.betweenessCentrality = between;
////		theGraph.closenessCentrality = close;
		
		return theGraph;
	}

	public static Graph CreateChvatalGraph(){
		Graph theGraph = new Graph(12);
		
		theGraph.edges = new int[12][12];
		theGraph.distances = new int[12][12];	
		
		int[][] tempEdges = { { -1, 1, -1, -1, 1, -1, 1, -1, -1, 1, -1, -1 },
				{ 1, -1, 1, -1, -1, 1, -1, 1, -1, -1, -1, -1 },
				{ -1, 1, -1, 1, -1, -1, 1, -1, 1, -1, -1, -1 },
				{ -1, -1, 1, -1, 1, -1, -1, 1, -1, 1, -1, -1 },
				{ 1, -1, -1, 1, -1, 1, -1, -1, 1, -1, -1, -1 },
				{ -1, 1, -1, -1, 1, -1, -1, -1, -1, -1, 1, 1 },
				{ 1, -1, 1, -1, -1, -1, -1, -1, -1, -1, 1, 1 },
				{ -1, 1, -1, 1, -1, -1, -1, -1, 1, -1, -1, 1 },
				{ -1, -1, 1, -1, 1, -1, -1, 1, -1, -1, 1, -1 },
				{ 1, -1, -1, 1, -1, -1, -1, -1, -1, -1, 1, 1 },
				{ -1, -1, -1, -1, -1, 1, 1, -1, 1, 1, -1, -1 },
				{ -1, -1, -1, -1, -1, 1, 1, 1, -1, 1, -1, -1 },
				};
		theGraph.edges = tempEdges;
		
		int[][] tempDistances = 
			{ { 0, 1, 2, 2, 1, 2, 1, 2, 2, 1, 2, 2 },
			{ 1, 0, 1, 2, 2, 1, 2, 1, 2, 2, 2, 2 },
			{ 2, 1, 0, 1, 2, 2, 1, 2, 1, 2, 2, 2 },
			{ 2, 2, 1, 0, 1, 2, 2, 1, 2, 1, 2, 2 },
			{ 1, 2, 2, 1, 0, 1, 2, 2, 1, 2, 2, 2 },
			{ 2, 1, 2, 2, 1, 0, 2, 2, 2, 2, 1, 1 },
			{ 1, 2, 1, 2, 2, 2, 0, 2, 2, 2, 1, 1 },
			{ 2, 1, 2, 1, 2, 2, 2, 0, 1, 2, 2, 1 },
			{ 2, 2, 1, 2, 1, 2, 2, 1, 0, 2, 1, 2 },
			{ 1, 2, 2, 1, 2, 2, 2, 2, 2, 0, 1, 1 },
			{ 2, 2, 2, 2, 2, 1, 1, 2, 1, 1, 0, 2 },
			{ 2, 2, 2, 2, 2, 1, 1, 1, 2, 1, 2, 0 },
			};
		
		theGraph.distances = tempDistances;
		
//		double[] close = { 0.611 , 0.611 , 0.611 , 0.611 , 0.611 , 0.611 , 0.611 , 0.611 , 0.611 , 0.611 , 0.611 , 0.611 };
//		double[] between = { 0.070 , 0.070 , 0.061 , 0.061 , 0.070 , 0.070 , 0.061 , 0.061 , 0.061 , 0.061 , 0.061 , 0.061 };
//		theGraph.betweenessCentrality = between;
//		theGraph.closenessCentrality = close;
		
		return theGraph;
	}

	public static void main(String[] args) {
		Graph g = new Graph("graphs/graph1.txt","graphs/distances1.txt");
		for (int i = 0; i < g.numVertices; i++) {
        	for (int j = 0; j < g.numVertices; j++) 
        		System.out.print(g.distances[i][j] + "\t");
        	System.out.println();
        }
	}
}