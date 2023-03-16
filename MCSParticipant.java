import java.util.Vector;
public class MCSParticipant extends Participant{
	

	public MCSParticipant(int start, int end, int time, int flex) {
		super(start, end, time, flex);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Vector<Task> MakeNextTaskDecision() {
		Vector<Task> feasibleTasks = theProvider.GetAvailableTasks(currentLocTime.locationVertex, endLoc, currentLocTime.timeStep, maxArrivalTime);
		Task chosenTask = null;
		double maxUtility = Double.NEGATIVE_INFINITY;
		
		for(Task consideredTask: feasibleTasks) {
			double A = (finalPathLength+departureTime - 
					(currentLocTime.timeStep + Experiment.theGraph.getDistanceBetween(currentLocTime.locationVertex, consideredTask.vertexLocation)))/2.0;
			
			
			double inner = Experiment.theGraph.getDistanceBetween(consideredTask.vertexLocation, endLoc) / 2.0;
			
			double B = Math.sqrt((A*A) - Math.pow(inner, 2));
			
			double utility = Math.PI * A * B;
			
			if( utility >maxUtility) {
				maxUtility = utility;
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
