import java.util.Vector;

public class RevenueParticipant extends Participant{
	public RevenueParticipant(int start, int end, int time, int flex) {
		super(start, end, time, flex);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Vector<Task> MakeNextTaskDecision() {
		try {
			Vector<Task> feasibleTasks = theProvider.GetAvailableTasks(currentLocTime.locationVertex, endLoc, currentLocTime.timeStep, maxArrivalTime);
			Task chosenTask = null;
			double maxRevenue = Double.NEGATIVE_INFINITY;
			
			for(Task consideredTask: feasibleTasks) {	
				double 
					revenue = Experiment.theGraph.GetOmega(consideredTask.vertexLocation, consideredTask.completeBy);
				
				if( revenue > maxRevenue) {
					maxRevenue = revenue;
					chosenTask = consideredTask;
				}
			}	
			if(chosenTask == null)
				return null;
			return Experiment.theGraph.spatioTemporalTasks.get(chosenTask.hashCode());
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
		return new RevenueParticipant(startLoc, endLoc, departureTime, flex);
	}

}
