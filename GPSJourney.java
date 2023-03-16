public class GPSJourney
	{
		GPSCoordinate startLoc;
		
		GPSCoordinate endLoc;
		
		public int startTime;
		public int endTime;
		
		private int originalEndTime;
		
		public int Length;
		public int extraTime;
	
		
		public GPSJourney()
		{
			
		}
		
		public GPSJourney(double startx, double starty, double endx, double endy, int startt, int endt)
		{
			startLoc = new GPSCoordinate(startx, starty);
			endLoc = new GPSCoordinate(endx, endy);
			
			Length = (int) (Math.abs(startx-endx) + Math.abs(starty-endy));
			
			extraTime = (endt-startt)-Length;
			
			startTime = startt;			
			endTime = startTime + Length;
			
			originalEndTime = endt;
					
		}
		
		public String toString() {
			return startLoc.latitude + "\t" + startLoc.longitude + "\t" + endLoc.latitude + "\t" + endLoc.longitude + "\t" + originalEndTime + "\t" + startTime;  			
		}
		
	}
