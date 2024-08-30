import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.io.PrintWriter;

public class Experiment {
	static Vector<ResultTuple> simulationResults;
	static FileWriter resultWriter;
	static FileWriter runningTimes;
	static Graph theGraph;
	public Experiment(){}
	static enum ParticipantBehavior {
		  AutoDistance,
		  AutoRev,
		  AutoDetour,
		  AutoMCS,
		  AutoDeadline,
//		  AutoRoute
//		  Follow
		}

	
	public static void main(String[] args) {
		try {			
			resultWriter= new FileWriter("filename.txt", true);
			runningTimes= new FileWriter("running-times.txt", true);
			simulationResults = new Vector<ResultTuple>();
				startFourSquareExperiment();
	
			
			runningTimes.close();
			resultWriter.close();	
					   
		 } 
		catch (Exception e) {			
			e.printStackTrace();
		}
	}
	public void alternativeMain(int i, String citta, int kindCache, int repetition){
		try {			
			resultWriter= new FileWriter("filename.txt", true);
			runningTimes= new FileWriter("running-times.txt", true);
			simulationResults = new Vector<ResultTuple>();
				startFourSquareExperiment2(i, citta, kindCache, repetition);
	
			
			runningTimes.close();
			resultWriter.close();	
					   
		 } 
		catch (Exception e) {			
			e.printStackTrace();
		}
	}
	
	public static void mobileApplicationMain(){
		try {			
			resultWriter= new FileWriter("filename.txt", true);
			runningTimes= new FileWriter("running-times.txt", true);
			simulationResults = new Vector<ResultTuple>();
				startFourSquareExperimentMobileApplication();
	
			
			runningTimes.close();
			resultWriter.close();	
					   
		 } 
		catch (Exception e) {			
			e.printStackTrace();
		}
	}

	public static void startFourSquareExperiment() throws Exception {
		long startTime = System.currentTimeMillis();//inizio misurazione

		int numParticipants = 200, maxSimtime = 240, maxParticipantFlex = 10, numTrials = 30;
		GraphPersistanSingleton.getInstance();
		long tempTime = System.currentTimeMillis();  
		long generatingDataTime = 0;
		long scenarioTime = 0;
																			//la_clean.txt
		Task[] loadedTasks = TraceLoader.LoadInstantaneousTaskFile("la_clean_new.txt",'m', maxSimtime, 1, 10);
		// Fill up the OMEGA function in the graph
		theGraph.FillUpOmega(loadedTasks);
		long loadingDuration = System.currentTimeMillis() - tempTime;						
		
//		for(ParticipantBehavior behavior: ParticipantBehavior.values()) {
//			resultWriter.write(behavior.toString()+"\n");
			// for(maxParticipantFlex = 20; maxParticipantFlex<=280; maxParticipantFlex+=20) {
			for(numParticipants = 20; numParticipants<300; numParticipants+=20) {
				for(int i=0; i<numTrials; i++) {	
					tempTime = System.currentTimeMillis();
					Participant[] generatedParticipants = DataGenerator.GenerateParticipants(numParticipants, maxParticipantFlex, ParticipantBehavior.AutoMCS, loadedTasks);//[0].submissionTime);
					generatingDataTime += (System.currentTimeMillis() - tempTime);
					tempTime = System.currentTimeMillis();
							
					Scenario theScenario = new Scenario(generatedParticipants, loadedTasks);
					theScenario.startSimulation(); //esegue le simulazioni so ogni partecipante
					
					scenarioTime += (System.currentTimeMillis() - tempTime);						
				}
				// Get averages of results if needed
				ResultTuple avgResults = new ResultTuple();
				for(ResultTuple r: simulationResults) {
					avgResults.avgTasksCompleted += r.avgTasksCompleted;
					avgResults.avgParticipation += r.avgParticipation;
					avgResults.avgParticipantDetour += r.avgParticipantDetour;
					avgResults.avgParticipantBenefit += r.avgParticipantBenefit;
					avgResults.avgRevenueCollected += r.avgRevenueCollected;
				}
				avgResults.avgTasksCompleted /= numTrials;
				avgResults.avgParticipation /= numTrials;
				avgResults.avgParticipantDetour /= numTrials;
				avgResults.avgParticipantBenefit /= numTrials;
				avgResults.avgRevenueCollected /= numTrials;
				
				resultWriter.write(maxSimtime + "\t" + loadedTasks.length + "\t" + numParticipants + "\t" + maxParticipantFlex + "\t" + avgResults.toString() + "\n");
				runningTimes.write(loadingDuration + ", " + generatingDataTime/numTrials+ "," + scenarioTime/numTrials + "\n");
				
				simulationResults.clear();
				scenarioTime = 0;
				generatingDataTime = 0;
			}
//		}		s
		long totalTime = System.currentTimeMillis() - startTime;
		System.out.println("Tempo totale di esecuzione: " + (totalTime / 1000.0) + " secondi");
	}
	
	public static void startFourSquareExperimentMobileApplication() throws Exception {
		long startTime = System.currentTimeMillis();//inizio misurazione

		int numParticipants = 1, maxSimtime = 240, maxParticipantFlex = 0, numTrials = 30;
		GraphPersistanSingleton.getInstance();
		long tempTime = System.currentTimeMillis();  
		long generatingDataTime = 0;
		long scenarioTime = 0;
																			//la_clean.txt
		Task[] loadedTasks = TraceLoader.LoadInstantaneousTaskFile("la_clean_new.txt",'m', maxSimtime, 1, 10);
		// Fill up the OMEGA function in the graph
		theGraph.FillUpOmega(loadedTasks);
		long loadingDuration = System.currentTimeMillis() - tempTime;						
		
		for(numParticipants = 20; numParticipants<21; numParticipants+=10) {
				for(int i=0; i<numTrials; i++) {	
					tempTime = System.currentTimeMillis();
					Participant[] generatedParticipants = DataGenerator.GenerateParticipants(numParticipants, maxParticipantFlex, ParticipantBehavior.AutoMCS, loadedTasks);//[0].submissionTime);
					generatingDataTime += (System.currentTimeMillis() - tempTime);
					tempTime = System.currentTimeMillis();
							
					Scenario theScenario = new Scenario(generatedParticipants, loadedTasks);
					theScenario.startSimulation(); //esegue le simulazioni so ogni partecipante
					
					scenarioTime += (System.currentTimeMillis() - tempTime);						
				}
				// Get averages of results if needed
				ResultTuple avgResults = new ResultTuple();
				for(ResultTuple r: simulationResults) {
					avgResults.avgTasksCompleted += r.avgTasksCompleted;
					avgResults.avgParticipation += r.avgParticipation;
					avgResults.avgParticipantDetour += r.avgParticipantDetour;
					avgResults.avgParticipantBenefit += r.avgParticipantBenefit;
					avgResults.avgRevenueCollected += r.avgRevenueCollected;
				}
				avgResults.avgTasksCompleted /= numTrials;
				avgResults.avgParticipation /= numTrials;
				avgResults.avgParticipantDetour /= numTrials;
				avgResults.avgParticipantBenefit /= numTrials;
				avgResults.avgRevenueCollected /= numTrials;
				
				resultWriter.write(maxSimtime + "\t" + loadedTasks.length + "\t" + numParticipants + "\t" + maxParticipantFlex + "\t" + avgResults.toString() + "\n");
				runningTimes.write(loadingDuration + ", " + generatingDataTime/numTrials+ "," + scenarioTime/numTrials + "\n");
				
				simulationResults.clear();
				scenarioTime = 0;
				generatingDataTime = 0;
			}
//		}		s
		long totalTime = System.currentTimeMillis() - startTime;
		System.out.println("Tempo totale di esecuzione: " + (totalTime / 1000.0) + " secondi");
	}
	
	
	public static void startFourSquareExperiment2(int numberTask, String citta, int kindCache, int Repetition) throws Exception{
        long startTime = System.currentTimeMillis();
        GraphPersistanSingleton.getInstance();
        Task[] loadedTasks = TraceLoader.LoadInstantaneousTaskFile("la_clean_new.txt", 'm', 240, 1, 10);
        theGraph.FillUpOmega(loadedTasks);
		int numTrials = 30;//al variare non cambia
		theGraph.inizializeCache(); //per dijkstra
		       // Preparazione del file di risultati
        try (FileWriter fw = new FileWriter("Sperimentation.txt", true);
             PrintWriter resultWriter = new PrintWriter(fw)) {

            for (int numParticipants = 40; numParticipants <= 200; numParticipants += 40) {
				for (int repititio = 1; repititio <= 5; repititio++) {
					/*if(repititio == 1 || repititio == 5){
						theGraph.getSizeDIJ();
					}*/
					theGraph.setContaExpress(repititio);
					long generatingDataTime = 0, scenarioTime = 0, totalSimulationCount = numberTask;
					ResultTuple avgResults = new ResultTuple();

					for (int i = 0; i < numTrials; i++) {
						long tempTime = System.currentTimeMillis();
						Participant[] generatedParticipants = DataGenerator.GenerateParticipants(numParticipants, 10, ParticipantBehavior.AutoMCS, loadedTasks);
						generatingDataTime += System.currentTimeMillis() - tempTime;

						tempTime = System.currentTimeMillis();
						Scenario theScenario = new Scenario(generatedParticipants, loadedTasks);
						theScenario.startSimulation();
						scenarioTime += System.currentTimeMillis() - tempTime;

						// Calcolo della media dei risultati per questa simulazione e aggiunta alle medie totali
						for (ResultTuple result : simulationResults) {
							avgResults.avgTasksCompleted += result.avgTasksCompleted;
							avgResults.avgParticipation += result.avgParticipation;
							avgResults.avgParticipantDetour += result.avgParticipantDetour;
							avgResults.avgParticipantBenefit += result.avgParticipantBenefit;
							avgResults.avgRevenueCollected += result.avgRevenueCollected;
						}
						simulationResults.clear(); // Pulisci i risultati per la prossima simulazione
					}
					//System.out.println(avgResults.avgParticipantDetour +"   " + numberTask);
					// Calcola le medie finali
					if (totalSimulationCount > 0) {
						avgResults.avgTasksCompleted /= totalSimulationCount;
						avgResults.avgParticipation /= totalSimulationCount;
						avgResults.avgParticipantDetour /= totalSimulationCount;
						avgResults.avgParticipantBenefit /= totalSimulationCount;
						avgResults.avgRevenueCollected /= totalSimulationCount;
					}
					// Stampa dei risultati medi
					resultWriter.print("Num. Partecipanti: " + numParticipants + ", Medie -> Compiti completati: " + avgResults.avgTasksCompleted + ", Partecipazione: " + avgResults.avgParticipation + ", Deviazione: " + avgResults.avgParticipantDetour + ", Beneficio: " + avgResults.avgParticipantBenefit + ", Entrate: " + avgResults.avgRevenueCollected + ", Tempo Scenario: " + (scenarioTime / numTrials) + " ms.");
					resultWriter.println("");
					DataStore.exportDataPointsToCSVAndClear("D:/tacSim/TACSim/csvSimulazioniSPERIMENTSvuotando/" + citta + "-" + numberTask + "-" +  numParticipants + "-" +  Repetition);
					theGraph.clearCacheDijkstra();
					theGraph.clearCache();//per dijkstra

				}
				theGraph.clearCache();//per dijkstra
			}
            long totalTime = System.currentTimeMillis() - startTime;
            resultWriter.println("Tempo totale di esecuzione: " + (totalTime / 1000.0) + " secondi");
			resultWriter.println("");
		} catch (Exception e) {
            e.printStackTrace();
        }
	}
	

	public static void smallTesting() throws Exception {
		
		theGraph = new Graph(4, 4);
		
		Task[] loadedTasks = new Task[3];//TraceLoader.LoadInstantaneousTaskFile("la_clean.txt",'m', maxSimtime, 1, 10);
		loadedTasks[0] = new SimpleTask(0, 2, 3, 1, 1);
		loadedTasks[1] = new SimpleTask(0, 2, 3, 1, 1);
		loadedTasks[2] = new SimpleTask(0, 2, 3, 1, 1);
//		theGraph.FillUpOmega(loadedTasks);
						
		
//		for(ParticipantBehavior behavior: ParticipantBehavior.values()) {
//			resultWriter.write(behavior.toString()+"\n");
//			for(numParticipants = 100; numParticipants<=300; numParticipants+=20) {
//				for(int i=0; i<numTrials; i++) {	
					
					Participant[] generatedParticipants = new Participant[2];//DataGenerator.GenerateParticipants(numParticipants, maxParticipantFlex, ParticipantBehavior.AutoDistance, loadedTasks[0].submissionTime);
					generatedParticipants[0] = new DistanceParticipant(0, 6, 1, 3);
					generatedParticipants[1] = new DistanceParticipant(0, 6, 1, 3);
							
					Scenario theScenario = new Scenario(generatedParticipants, loadedTasks);
					theScenario.startSimulation();
														
//				}
	}
		
}










