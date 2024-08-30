import java.util.Vector;
import org.openstreetmap.gui.jmapviewer.Coordinate;

public abstract class Participant {
	
	int startLoc;
	int endLoc;
	SpatioTemporalPoint currentLocTime;	
	
	int departureTime;
	int flex;
	int maxArrivalTime;
	
	// Have a link to the service provider
	ServiceProvider theProvider;
	
	Vector<Task> tasksAssigned;

	int finalDetour;
	int finalPathLength;
	int numTasksCompleted;
	double totalFareCollected;
	
	public Participant(int start, int end, int time, int flex) {
		startLoc  = start;
		endLoc = end;
		departureTime = time;
		this.flex = flex;
		
		currentLocTime = new SpatioTemporalPoint(start, time);
		
	
		numTasksCompleted = 0;
		finalDetour = 0;
		finalPathLength = 0;
		totalFareCollected = 0;
		
		tasksAssigned = new Vector<Task>();
		if(flex == 0){
			Experiment.theGraph.printControl = true;
		}
		int pp = Experiment.theGraph.getDistanceBetween(startLoc,endLoc);		
		maxArrivalTime = pp + flex + departureTime;
	}
	public Coordinate getLocation() {
        return new Coordinate(currentLocTime.locationVertex, 0);  // Modifica la seconda coordinata se necessario
    }
	public void ConnectToServiceProvider(ServiceProvider provider) {
		theProvider = provider;
	}
	
	public void AdjustCurrentLocTime(int location, int timeStep) {
		currentLocTime.locationVertex = location;
		currentLocTime.timeStep = timeStep;
	}
	
	public void LeavePlatform() {//qua avevo aggiunto il controllo del theprovider==null salvata il leave,però non sembra terminare l'esecuzione
		//theProvider.AddParticipant(this);
		theProvider.RemoveParticipant(this);
	}//ho visto che se commento va in palla, l'ultimo valore si inlooppa
	
	public void CalculateDetour() {
		// My idea so far is to find the time and location of the last task
		if(tasksAssigned.size() > 0) {
			// 	If it exists, then compute distances
			Task lastTask = tasksAssigned.get(tasksAssigned.size()-1);
			int arrivalTime = lastTask.taskDuration+lastTask.completeBy + Experiment.theGraph.getDistanceBetween(lastTask.vertexLocation, endLoc);
			finalPathLength = arrivalTime - departureTime;
			finalDetour = finalPathLength + Experiment.theGraph.getDistanceBetween(startLoc, endLoc);
		}
		else {
			finalPathLength = Experiment.theGraph.getDistanceBetween(startLoc, endLoc);
			finalDetour = 0;
		}
		
	}
	
	public abstract Vector<Task> MakeNextTaskDecision();
	
	public abstract SpatioTemporalPoint MakeStepWithoutTask();
	
	public abstract void CompleteTask(Vector<Task> theTasks);
	
	public abstract Participant clone();
	
	public String toString() {
		return "("+startLoc+","+endLoc+","+departureTime+")";
	}

}
