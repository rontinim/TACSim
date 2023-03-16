import java.util.Vector;

public class ParticipantDecisionEvent implements IEvent{
	int decisionTime;	
	Participant theDriver;
	
	public ParticipantDecisionEvent(int time, Participant d) {
		decisionTime = time;
		theDriver = d;
	}

	@Override
	public int getEventTime() {
		return decisionTime;
	}

	@Override
	public IEvent executeEvent() {
		// Mark current location of driver
		theDriver.AdjustCurrentLocTime(theDriver.currentLocTime.locationVertex, decisionTime);
		
		// Decide on next task to complete
		Vector<Task> nextTasks = theDriver.MakeNextTaskDecision();
		if(nextTasks != null) {	
			for(Task eachTask: nextTasks)
				eachTask.isClaimed = true;
			// Schedule next move step towards that task
			return new ParticipantMoveEvent(nextTasks.get(0).completeBy, theDriver, nextTasks.get(0).vertexLocation, nextTasks);
		}
		
		// If there are no tasks to choose from
		// Find the neighboring location that is closest to destination
		SpatioTemporalPoint nextLocation = theDriver.MakeStepWithoutTask();
		// Schedule to go there
		// Only if within time limits
		if(nextLocation.timeStep < theDriver.maxArrivalTime)
			return new ParticipantMoveEvent(nextLocation.timeStep, theDriver, nextLocation.locationVertex, null);
		return null;

	}
}

