public class RoutingResult {
	
	// The actual path chosen b the algorithm
	MyPath recommendedPath;
	// The tasks that can be completed on the way
//	private Vector<Task> chosenTasks;
	
	// Total reward
	double reward;
	// Final detour
	int detour;
	
	//In case  of rides
//	Task rideDetails;
	
	public RoutingResult(MyPath path, double rew, int det) {//, Vector<Task> details) {
		reward =  rew;		
		
		if(path != null)
			recommendedPath = path;
		else
			recommendedPath = new MyPath();
				
		
		detour = det;
		
//		chosenTasks = details;
	}
}
