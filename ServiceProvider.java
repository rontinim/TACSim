import java.util.Vector;

public class ServiceProvider {
//	WETGraph theGraph;
	
	Vector<Participant> availableParticipants;
	Vector<Task> availableTasks;	
	
	
	public ServiceProvider() {		
		availableParticipants = new Vector<Participant>();
		availableTasks = new Vector<Task>();
	}
	
	
	public Vector<Task> GetAvailableTasks(){
		Vector<Task> feasibleTasks = new Vector<Task>();
		for(Task t:availableTasks) {
			if(t.isClaimed || t.isCompleted)
				continue;
			feasibleTasks.add(t);
		}
		return feasibleTasks;
	}
	
	public Vector<Task> GetAvailableTasks(int currentLocation, int feasibleRadius){
		Vector<Task> feasibleTasks = new Vector<Task>();
		for(Task t:availableTasks) {
			if(t.isClaimed || t.isCompleted)
				continue;
			if(Experiment.theGraph.getDistanceBetween(t.vertexLocation, currentLocation) < feasibleRadius)
				feasibleTasks.add(t);
		}
		return feasibleTasks;
	}
	
	public Vector<Task> GetAvailableTasks(int firstEndpoint, int secondEndpoint, int startTime, int endTime){
		Vector<Task> feasibleTasks = new Vector<Task>();
		for(Task t:availableTasks) {
			if(t.isClaimed || t.isCompleted)
				continue;
			if(Experiment.theGraph.getDistanceBetween(firstEndpoint, t.vertexLocation) + startTime <= t.completeBy &&
					t.completeBy + Experiment.theGraph.getDistanceBetween(t.vertexLocation, secondEndpoint) <= endTime)
				feasibleTasks.add(t);
		}
		return feasibleTasks;
	}
	
	public Task GetTaskRecommendation() {
		return null;
	}
		
	
	public void AddParticipant(Participant theParticipant) {
		theParticipant.ConnectToServiceProvider(this);
		availableParticipants.add(theParticipant);
	}
	
	public void RemoveParticipant(Participant leavingParticipant) {
		availableParticipants.remove(leavingParticipant);
	}
	
	public void AddTask(Task theTask) {
		availableTasks.add(theTask);
	}
	
	public void RemoveTask(Task theTask) {
		availableTasks.remove(theTask);
	}
	
	public void MarkClaimed(Vector<Task> claimed) {
		for(Task t: claimed) {
			t.isClaimed = true;
		}
	}

}
