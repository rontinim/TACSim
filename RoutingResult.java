public class RoutingResult {
	
	// The actual path chosen b the algorithm
	Path recommendedPath;
	// The tasks that can be completed on the way
//	private Vector<Task> chosenTasks;
	
	// Total reward
	double reward;
	// Final detour
	int detour;
	
	//In case  of rides
//	Task rideDetails;
	
	public RoutingResult(Path path, double rew, int det) {//, Vector<Task> details) {
		reward =  rew;		
		
		if(path != null)
			recommendedPath = path;
		else
			recommendedPath = new Path();
				
		
		detour = det;
		
//		chosenTasks = details;
	}
}
