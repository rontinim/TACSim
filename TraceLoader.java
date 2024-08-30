import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

public class TraceLoader {
    static Random theRand = new Random(1);

    public static Task[] LoadInstantaneousTaskFile(String fileName, char granularity, int limit, int minRevenue, int maxRevenue) throws Exception {
		Scanner taskFile = new Scanner(new File(fileName));
		Vector<Task> tasksLoaded = new Vector<Task>();
        Experiment.theGraph = null;
		if (Experiment.theGraph == null) {  //vediamo che controllo subito se ci sia o meno il grafo, se non c'è ok, se no restituisce errore
			Hashtable<String, Integer> GPSVertices = new Hashtable<String, Integer>();
			Hashtable<Integer, GPSCoordinate> reverseTracking = new Hashtable<Integer, GPSCoordinate>();
	
			int vertexCounter = 0;
			int initialTimeStep = -1; // Muovo questa dichiarazione all'esterno del loop
	
			// Read the first line and check if it contains relevant data
			if (taskFile.hasNextLine()) {
				String[] line = taskFile.nextLine().split("\t");
	
				// Get the location of the simple task
				GPSVertices.put(line[0] + "," + line[1], vertexCounter);
				reverseTracking.put(vertexCounter, new GPSCoordinate(line[0] + "," + line[1]));
				vertexCounter++;
	
				// Get the time stamp associated with the task
				Long t = Long.parseLong(line[2]);
				switch (granularity) {
					case 's':
						t /= 1000;
						initialTimeStep = t.intValue();
						break;
					case 'm':
						t /= 60000;
						initialTimeStep = t.intValue();
						break;
					default:
						initialTimeStep = t.intValue();
						break;
				}
	
				// Create the first task
				int rev = (maxRevenue == minRevenue) ? maxRevenue : theRand.nextInt(maxRevenue - minRevenue) + minRevenue;
				tasksLoaded.add(new SimpleTask(initialTimeStep - 20, 0, initialTimeStep, 1, rev));
			}
	
			// Read subsequent lines and load tasks
			while (taskFile.hasNext()) {
				String[] line = taskFile.nextLine().split("\t");
	
				// Get the time stamp associated with the task
				Long t = Long.parseLong(line[2]);
				int timeStep = -1;
				switch (granularity) {
					case 's':
						t /= 1000;
						timeStep = t.intValue();
						break;
					case 'm':
						t /= 60000;
						timeStep = t.intValue();
						break;
					default:
						timeStep = t.intValue();
						break;
				}
	
				// Decide whether this task should be added
				// Or to just stop loading tasks based on the limit provided
				if (timeStep - initialTimeStep >= limit)
					break;
	
				// Get the location of the simple task
				int taskLocation = -1;
				if (GPSVertices.containsKey(line[0] + "," + line[1]))
					taskLocation = GPSVertices.get(line[0] + "," + line[1]);
				else {
					taskLocation = vertexCounter;
					GPSVertices.put(line[0] + "," + line[1], vertexCounter);
					reverseTracking.put(vertexCounter, new GPSCoordinate(line[0] + "," + line[1]));
					vertexCounter++;
				}
	
				int rev = (maxRevenue == minRevenue) ? maxRevenue : theRand.nextInt(maxRevenue - minRevenue) + minRevenue;
				tasksLoaded.add(new SimpleTask(timeStep - 20, taskLocation, timeStep, 1, rev));
			}
			taskFile.close();
            //System.out.println(reverseTracking);
			Experiment.theGraph = new Graph(reverseTracking);
            //Experiment.theGraph = new Graph("la_clean_2.txt");
	
			Task[] loadedTasks = new Task[tasksLoaded.size()];
			tasksLoaded.toArray(loadedTasks);
	
			return loadedTasks;
		} else {
			taskFile.close();
			throw new Exception("Not supported yet!");
		}
	}
	
    //I METODI SOTTO NON VENGONO RICHIAMATI!!!!!!!!!!!!!!


    private static Graph loadGraphFromGraphML(String fileName) throws FileNotFoundException {
        Scanner graphFile = new Scanner(new File(fileName));
        Hashtable<Integer, GPSCoordinate> nodeCoordinates = new Hashtable<>();
        Vector<Integer[]> edges = new Vector<>();
    
        // Leggi tutti i nodi e memorizza le coordinate
        while (graphFile.hasNextLine()) {
            String line = graphFile.nextLine().trim();
            if (line.startsWith("<node")) {
                int nodeId = Integer.parseInt(line.split("\"")[1]);
                double latitude = 0.0;
                double longitude = 0.0;
    
                // Leggi la latitudine
                line = graphFile.nextLine().trim(); // Legge la riga con la latitudine
                latitude = Double.parseDouble(line.split(">")[1].split("<")[0]);
    
                // Leggi la longitudine
                line = graphFile.nextLine().trim(); // Legge la riga con la longitudine
                longitude = Double.parseDouble(line.split(">")[1].split("<")[0]);
    
                nodeCoordinates.put(nodeId, new GPSCoordinate(latitude, longitude));
            } else if (line.startsWith("<edge")) {
                int sourceId = Integer.parseInt(line.split("\"")[3]);
                int targetId = Integer.parseInt(line.split("\"")[5]);
                edges.add(new Integer[]{sourceId, targetId});
            } else if (line.equals("</graph>")) {
                break; // Uscire dal ciclo una volta terminati i nodi
            }
        }
        graphFile.close();
    
        // Crea il grafo
        Graph graph = new Graph(nodeCoordinates.size());
        graph.vertexCoordinates = nodeCoordinates;
    
        // Aggiungi gli archi al grafo
        for (Integer[] edge : edges) {
            graph.edges[edge[0]][edge[1]] = 1; // Assumo che il costo dell'arco sia 1
            // Se il grafo è indiretto, aggiungi anche l'arco inverso
            // graph.edges[edge[1]][edge[0]] = 1;
        }
    
        // Calcola le distanze (se necessario)
        graph.CalculateDistances();
    
        return graph;
    }
    

    public static Vector<GPSJourney> LoadGPSTrajectoryFile(String fileName, char granularity) throws Exception {
        Vector<GPSJourney> allJourneys = new Vector<GPSJourney>();

        File theFile = new File(fileName);
        Scanner fileReader;
        try {
            fileReader = new Scanner(theFile);
            while (fileReader.hasNext()) {
                String line = fileReader.nextLine();
                String[] items = line.split("\t");
                GPSJourney j = new GPSJourney(Double.parseDouble(items[0]), Double.parseDouble(items[1]),
                        Double.parseDouble(items[2]), Double.parseDouble(items[3]),
                        Integer.parseInt(items[5]), Integer.parseInt(items[4]));
                allJourneys.add(j);
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Vector<Integer> indicesToDelete = new Vector<Integer>();
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, maxX = 0, maxY = 0;

        // Statistics on dataset
        Hashtable<Integer, Integer> frequencybyDay = new Hashtable<Integer, Integer>();
        Hashtable<String, Integer> frequencyByLocation = new Hashtable<String, Integer>();

        // Now clean up and analyze the journeys
        for (int jourIndex = 0; jourIndex < allJourneys.size(); jourIndex++) {
            GPSJourney currJourney = allJourneys.get(jourIndex);
            if (currJourney.Length == 0) {
                indicesToDelete.insertElementAt(jourIndex, 0);
                continue;
            }
            if (currJourney.endLoc.latitude < minX)
                minX = currJourney.endLoc.latitude;
            if (currJourney.startLoc.latitude < minX)
                minX = currJourney.startLoc.latitude;

            if (currJourney.endLoc.latitude > maxX)
                maxX = currJourney.endLoc.latitude;
            if (currJourney.startLoc.latitude > maxX)
                maxX = currJourney.startLoc.latitude;

            if (currJourney.endLoc.longitude < minY)
                minY = currJourney.endLoc.longitude;
            if (currJourney.startLoc.longitude < minY)
                minY = currJourney.startLoc.longitude;

            if (currJourney.endLoc.longitude > maxY)
                maxY = currJourney.endLoc.longitude;
            if (currJourney.startLoc.longitude > maxY)
                maxY = currJourney.startLoc.longitude;

            // Group them by frequency of location
            if (frequencyByLocation.containsKey(currJourney.startLoc.toString())) {
                int frequency = frequencyByLocation.get(currJourney.startLoc.toString());
                frequencyByLocation.put(currJourney.startLoc.toString(), frequency + 1);
            } else
                frequencyByLocation.put(currJourney.startLoc.toString(), 1);
        }
        for (Integer i : indicesToDelete)
            allJourneys.remove((int) i);

        indicesToDelete.clear();
        // Now go through them again, and remove any location with frequency <=5
        // Also remove bad dates
        for (int jourIndex = 0; jourIndex < allJourneys.size(); jourIndex++) {
            GPSJourney currJourney = allJourneys.get(jourIndex);

            int dayNum = (int) ((currJourney.startTime / (24 * 60.0 * 60.0)));

            if (frequencyByLocation.get(currJourney.startLoc.toString()) <= 5 || dayNum > 23) {
                indicesToDelete.insertElementAt(jourIndex, 0);
                continue;
            }

            // Group them by hour in day
            if (frequencybyDay.containsKey(dayNum)) {
                int frequency = frequencybyDay.get(dayNum);
                frequencybyDay.put(dayNum, frequency + 1);
            } else
                frequencybyDay.put(dayNum, 1);
        }
        for (Integer i : indicesToDelete)
            allJourneys.remove((int) i);

        try {
            FileWriter myWriter = new FileWriter("sf_journeys.txt");
            for (GPSJourney j : allJourneys)
                myWriter.write(j.toString() + "\n");

            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return allJourneys;
    }
}
