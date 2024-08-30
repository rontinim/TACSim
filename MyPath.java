import java.util.Vector;

public class MyPath {
	
	Vector <SpatioTemporalPoint> pathSteps;
	
	public MyPath() {
		pathSteps = new Vector<SpatioTemporalPoint>();
	}
	
	public void AddStep(SpatioTemporalPoint singlePathStep, boolean atFront) {
		if(atFront) {
			pathSteps.add(0, singlePathStep);
		}
		else {
			pathSteps.add(singlePathStep);
		}
	}
	
	public int CalculateTotalDuration() {
		// Find the time of arrival at destination
		int lastTimeStep = pathSteps.get(pathSteps.size()-1).timeStep;
		for(int i=pathSteps.size()-1; i>0; i--) {
			if(pathSteps.get(i).locationVertex != pathSteps.get(i-1).locationVertex) {
				lastTimeStep = pathSteps.get(i).timeStep;
				break;
			}
		}
		// return the duration of the path
		// Note that this works only if the first path step is the start location
		return lastTimeStep - pathSteps.get(0).timeStep;
	}
}
