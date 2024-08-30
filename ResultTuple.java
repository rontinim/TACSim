
public class ResultTuple {
	double avgTasksCompleted;
	double avgParticipation;
	double avgParticipantDetour;
	double avgParticipantBenefit;
	double avgRevenueCollected;
	
	public ResultTuple() {
		avgTasksCompleted = 0;
		avgParticipation = 0;
		avgParticipantDetour = 0;
		avgParticipantBenefit = 0;
		avgRevenueCollected = 0;
	}
	
	public ResultTuple(double numT, double numPart, double detour, double benefit, double rev) {
		avgTasksCompleted = numT;
		avgParticipation = numPart;
		avgParticipantDetour = detour;
		avgParticipantBenefit = benefit;
		avgRevenueCollected = rev;
	}
	
	public String toString() {
		return avgTasksCompleted + "\t" + avgParticipation + "\t" + avgParticipantDetour + "\t" + avgParticipantBenefit + "\t" + avgRevenueCollected;
	}
}