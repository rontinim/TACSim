import java.util.Vector;

public class ParticipantMoveEvent implements IEvent{
	int arrivalTime;	
	Participant theDriver;
	int theLocation;
	Vector<Task> assignedTasks;
	
	public ParticipantMoveEvent(int time, Participant d, int loc, Vector<Task> t) {//, MatchingGraph match) {
		arrivalTime = time;
		theDriver = d;
		theLocation = loc;
		assignedTasks = t;
	}

	@Override
	public int getEventTime() {
		return arrivalTime;
	}

	@Override
	public IEvent executeEvent() {
		// Mark current location of driver
		theDriver.AdjustCurrentLocTime(theLocation, arrivalTime);
		//If the move happened towards completing a task
		if(assignedTasks != null) {			
			// Complete task
			// Actual completion depends on the profile of the participant
			theDriver.CompleteTask(assignedTasks);	
			
			// Participant will stay there till biggest task is completed
			int maxDuration = 0;
			for(Task t: assignedTasks) {
				if(t.taskDuration > maxDuration)
					maxDuration = t.taskDuration;
			}
			
			// Make decision to where to move next
			return new ParticipantDecisionEvent(arrivalTime + maxDuration, theDriver);
		}
		
		return new ParticipantDecisionEvent(arrivalTime, theDriver);
	}
}
