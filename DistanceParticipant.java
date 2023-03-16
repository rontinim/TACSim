import java.util.Vector;

public class DistanceParticipant extends Participant{
	public DistanceParticipant(int start, int end, int time, int flex) {
		super(start, end, time, flex);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Vector<Task> MakeNextTaskDecision() {
		Vector<Task> feasibleTasks = theProvider.GetAvailableTasks(currentLocTime.locationVertex, endLoc, currentLocTime.timeStep, maxArrivalTime);
		Task chosenTask = null;
		int minDistance = Integer.MAX_VALUE;
		
		for(Task consideredTask: feasibleTasks) {
			int distance = Experiment.theGraph.getDistanceBetween(currentLocTime.locationVertex,consideredTask.vertexLocation);
			if( distance < minDistance) {
				minDistance = distance;
				chosenTask = consideredTask;
			}
		}	
			
		if(chosenTask == null)
			return null;
		
		Vector<Task> tasksClaimed = Experiment.theGraph.spatioTemporalTasks.get(chosenTask.hashCode());
		theProvider.MarkClaimed(tasksClaimed);
		return tasksClaimed;
	}

	@Override
	public SpatioTemporalPoint MakeStepWithoutTask() {
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
		return new DistanceParticipant(startLoc, endLoc, departureTime, flex);
	}

}
