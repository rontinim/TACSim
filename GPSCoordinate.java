
public class GPSCoordinate {
	double latitude;
	double longitude;
	
	public GPSCoordinate(double x, double y) {
		latitude = x;
		longitude = y;
	}
	public GPSCoordinate(String textCoordinates) {
		String[] details = textCoordinates.split(",");
		latitude = Double.parseDouble(details[0]);
		longitude = Double.parseDouble(details[1]);
	}
	
	public int GetDistance(GPSCoordinate otherLocation) {		
		
		double lat1 = this.latitude;
		double lat2 = otherLocation.latitude;
		double lon1 = this.longitude;
		double lon2 = otherLocation.longitude;
		
		if ((lat1 == lat2) && (lon1 == lon2)) {
			return 0;
		}
		else {
			double theta = lon1 - lon2;
			double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
			dist = Math.acos(dist);
			dist = Math.toDegrees(dist);
			dist = dist * 60 * 1.1515;				
			return (int)dist;
		}
	}
	public String toString() {
		return latitude+","+longitude;
	}
}
