
public class ParticipantDepartureEvent implements IEvent{
	int departureTime;	
	Participant theDriver;
	
	public ParticipantDepartureEvent(int time, Participant d) {
		departureTime = time;
		theDriver = d;
	}

	public int getEventTime() {
		return departureTime;
	}

	public IEvent executeEvent() {
		theDriver.LeavePlatform();
		return null;

	}
}
