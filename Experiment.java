import java.io.FileWriter;
import java.util.Vector;

public class Experiment {
	static Vector<ResultTuple> simulationResults;
	static FileWriter resultWriter;
	static FileWriter runningTimes;
	static Graph theGraph;
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
	
	public static void startFourSquareExperiment() throws Exception {
		int numParticipants = 200, maxSimtime = 240, maxParticipantFlex = 10, numTrials = 30;
		
		long tempTime = System.currentTimeMillis();  
		long generatingDataTime = 0;
		long scenarioTime = 0;
		
		Task[] loadedTasks = TraceLoader.LoadInstantaneousTaskFile("la_clean.txt",'m', maxSimtime, 1, 10);
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
					theScenario.startSimulation();
					
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










