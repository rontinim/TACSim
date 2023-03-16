
public class SpatioTemporalPoint {
	
	int locationVertex;
	int timeStep;


	public SpatioTemporalPoint(int location, int time) {
		locationVertex = location;
		timeStep = time;
	}
	
	public String toString() {
		return locationVertex+","+timeStep;
	}

}
