import java.util.Vector;

public class OptimalRoutingParticipant extends Participant{
	int[] reachableLocations;
	
	public OptimalRoutingParticipant(int start, int end, int time, int flex) {
		super(start, end, time, flex);
		
		int tripDuration = Experiment.theGraph.getDistanceBetween(startLoc,endLoc) + flex;
		reachableLocations = Experiment.theGraph.PruneVertices(start, end, tripDuration);
	}

	@Override
	public Vector<Task> MakeNextTaskDecision() {			
		try {
			RoutingResult fullRoute = Experiment.theGraph.FindCompleteRouteEdgeWeights(currentLocTime.locationVertex, endLoc, departureTime, flex, reachableLocations);
		if(fullRoute == null)
			return null;
		
		int firstStep = -1;
		for(firstStep = 0; firstStep < fullRoute.recommendedPath.pathSteps.size(); firstStep++)
			if(fullRoute.recommendedPath.pathSteps.get(firstStep).locationVertex != currentLocTime.locationVertex &&
			fullRoute.recommendedPath.pathSteps.get(firstStep).locationVertex != endLoc)
				break;
		
		// nothing found
		if(firstStep == -1)
			return null;
		
		return Experiment.theGraph.spatioTemporalTasks.get(fullRoute.recommendedPath.pathSteps.get(firstStep).hashCode());
		
		} catch (Exception e) {			
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public SpatioTemporalPoint MakeStepWithoutTask() {
//		int nextLocation = -1;
//		int minDistance = Integer.MAX_VALUE;
//		for(int i=0; i< Experiment.theGraph.edges[currentLocTime.locationVertex].length; i++) {
//			int neighborIndex = Experiment.theGraph.edges[currentLocTime.locationVertex][i];
//			if()
//			int distance = Experiment.theGraph.distances[]
//		}
		return new SpatioTemporalPoint(currentLocTime.locationVertex, currentLocTime.timeStep+1);
	}

	@Override
	public void CompleteTask(Vector<Task> theTasks) {
		for(Task t: theTasks) {
			t.isCompleted = true;
			tasksAssigned.add(t);
			totalFareCollected += t.specifiedRevenue;
			theProvider.RemoveTask(t);
		}
	}

	@Override
	public Participant clone() {
		return new OptimalRoutingParticipant(startLoc, endLoc, departureTime, flex);
	}


}
