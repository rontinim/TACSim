import java.util.Random;

public class DataGenerator {
	
	private static final long SEED = 12345L;
    // This random variable is created once with a single seed, and all randomness comes from here
	// This is done to maximize randomness
    static Random theRand = new Random(SEED);


	/**
	 * Basic method to generate a set of participants
	 * Graph must already be created for this method to work
	 * @param numParticipants
	 * @param flexLimit
	 * @param typeParticipant
	 * @param startTime
	 * @return
	 */
	public static Participant[] GenerateParticipants(int numParticipants, int flexLimit, Experiment.ParticipantBehavior typeParticipant, int startTime) {
		Participant[] generatedP = new Participant[numParticipants];
		int timeStep = startTime;
		switch(typeParticipant) {
		case AutoDistance:
			for(int i=0; i<numParticipants; i++) {
				int startV = theRand.nextInt(Experiment.theGraph.numVertices);
				int endV = theRand.nextInt(Experiment.theGraph.numVertices);
				timeStep += theRand.nextInt(5);
				generatedP[i] = new DistanceParticipant(startV, endV, timeStep, flexLimit);
			}
			break;
		case AutoRev:
			for(int i=0; i<numParticipants; i++) {
				int startV = theRand.nextInt(Experiment.theGraph.numVertices);
				int endV = theRand.nextInt(Experiment.theGraph.numVertices);
				timeStep += theRand.nextInt(5);
				generatedP[i] = new RevenueParticipant(startV, endV, timeStep, flexLimit);
			}
			break;
		case AutoDetour:
			for(int i=0; i<numParticipants; i++) {
				int startV = theRand.nextInt(Experiment.theGraph.numVertices);
				int endV = theRand.nextInt(Experiment.theGraph.numVertices);
				timeStep += theRand.nextInt(5);
				generatedP[i] = new DetourParticipant(startV, endV, timeStep, flexLimit);
			}
			break;
		case AutoDeadline:
			for(int i=0; i<numParticipants; i++) {
				int startV = theRand.nextInt(Experiment.theGraph.numVertices);
				int endV = theRand.nextInt(Experiment.theGraph.numVertices);
				timeStep += theRand.nextInt(5);
				generatedP[i] = new DeadlineParticipant(startV, endV, timeStep, flexLimit);
			}
			break;
		case AutoMCS:
			for(int i=0; i<numParticipants; i++) {
				int startV = theRand.nextInt(Experiment.theGraph.numVertices);
				int endV = theRand.nextInt(Experiment.theGraph.numVertices);
				timeStep += theRand.nextInt(5);
				generatedP[i] = new MCSParticipant(startV, endV, timeStep, flexLimit);
			}
			break;
//		case AutoRoute:
//			for(int i=0; i<numParticipants; i++) {
//				int startV = theRand.nextInt(Experiment.theGraph.numVertices);
//				int endV = theRand.nextInt(Experiment.theGraph.numVertices);
//				timeStep += theRand.nextInt(5);
//				generatedP[i] = new OptimalRoutingParticipant(startV, endV, timeStep, flexLimit);
//			}
//			break;
		default:
				break;
		}
		
		return generatedP;
		
	}	
	                     //METODO RICHIAMATO ATTUALMENTE QUELLO SOTTOSTANTE!!!!!!!
	/**
	 * Basic method to generate a set of participants
	 * Graph must already be created for this method to work
	 * This method uses the tasks already generated / loaded
	 * @param numParticipants
	 * @param flexLimit
	 * @param typeParticipant
	 * @param startTime
	 * @return
	 */                                                                                                                         //il tpo di partecipanti serve per determinare quali task acetterano e come si comporteranno
	public static Participant[] GenerateParticipants(int numParticipants, int flexLimit, Experiment.ParticipantBehavior typeParticipant, Task[] taskTraces) {
		Participant[] generatedP = new Participant[numParticipants];
		switch(typeParticipant) {
		case AutoDistance:
			for(int i=0; i<numParticipants; i++) {
				int randomTask = theRand.nextInt(taskTraces.length);
				int randomTask2 = theRand.nextInt(taskTraces.length);				
				generatedP[i] = new DistanceParticipant(taskTraces[randomTask].vertexLocation, 
						taskTraces[randomTask2].vertexLocation, taskTraces[randomTask].submissionTime-10, flexLimit);
			}
			break;
		case AutoRev:		
			for(int i=0; i<numParticipants; i++) {
				int randomTask = theRand.nextInt(taskTraces.length);
				int randomTask2 = theRand.nextInt(taskTraces.length);				
				generatedP[i] = new RevenueParticipant(taskTraces[randomTask].vertexLocation, 
						taskTraces[randomTask2].vertexLocation, taskTraces[randomTask].submissionTime-10, flexLimit);
			}
			break;
		case AutoDetour:
			for(int i=0; i<numParticipants; i++) {
				int randomTask = theRand.nextInt(taskTraces.length);
				int randomTask2 = theRand.nextInt(taskTraces.length);				
				generatedP[i] = new DetourParticipant(taskTraces[randomTask].vertexLocation, 
						taskTraces[randomTask2].vertexLocation, taskTraces[randomTask].submissionTime-10, flexLimit);
			}
			break;
		case AutoDeadline:
			for(int i=0; i<numParticipants; i++) {
				int randomTask = theRand.nextInt(taskTraces.length);
				int randomTask2 = theRand.nextInt(taskTraces.length);				
				generatedP[i] = new DeadlineParticipant(taskTraces[randomTask].vertexLocation, 
						taskTraces[randomTask2].vertexLocation, taskTraces[randomTask].submissionTime-10, flexLimit);
			}
			break;	
		case AutoMCS:
			for(int i=0; i<numParticipants; i++) {
				int randomTask = theRand.nextInt(taskTraces.length);
				int randomTask2 = theRand.nextInt(taskTraces.length);				
				generatedP[i] = new MCSParticipant(taskTraces[randomTask].vertexLocation, 
						taskTraces[randomTask2].vertexLocation, taskTraces[randomTask].submissionTime-10, flexLimit);
			}
			break;
//		case AutoRoute:
//			for(int i=0; i<numParticipants; i++) {
//				int randomTask = theRand.nextInt(taskTraces.length);
//				int randomTask2 = theRand.nextInt(taskTraces.length);				
//				generatedP[i] = new OptimalRoutingParticipant(taskTraces[randomTask].vertexLocation, 
//						taskTraces[randomTask2].vertexLocation, taskTraces[randomTask].submissionTime-10, flexLimit);
//			}
//			break;
		default:
				break;
		}
		return generatedP;
		
	}	
	
	public static void main(String[] args) {
				
	}

}
