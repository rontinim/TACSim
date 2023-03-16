import java.util.Vector;

public class DeadlineParticipant extends Participant{
	public DeadlineParticipant(int start, int end, int time, int flex) {
		super(start, end, time, flex);		
	}

	@Override
	public Vector<Task> MakeNextTaskDecision() {	
		Vector<Task> feasibleTasks = theProvider.GetAvailableTasks(currentLocTime.locationVertex, endLoc, currentLocTime.timeStep, maxArrivalTime);
		Task chosenTask = null;
		int earliestDeeadline = Integer.MAX_VALUE;		
		
		for(Task consideredTask: feasibleTasks) {						
			if(consideredTask.completeBy < earliestDeeadline) {
				earliestDeeadline = consideredTask.completeBy;
				chosenTask = consideredTask;
			}
		}	
		if(chosenTask == null)
			return null;
		return Experiment.theGraph.spatioTemporalTasks.get(chosenTask.hashCode());
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
		return new DetourParticipant(startLoc, endLoc, departureTime, flex);
	}

}
