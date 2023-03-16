
public class ParticipantArrivalEvent implements IEvent{
	int arrivalTime;	
	Participant theParticipant;
	ServiceProvider theProvider;
	
	public ParticipantArrivalEvent(int time, Participant d, ServiceProvider provider) {
		arrivalTime = time;
		theParticipant = d;
		theProvider = provider;
	}

	public int getEventTime() {
		return arrivalTime;
	}

	public IEvent executeEvent() {
		theProvider.AddParticipant(theParticipant);
		return new ParticipantDecisionEvent(theParticipant.departureTime, theParticipant);

	}
}
