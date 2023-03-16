
public class TaskExpireEvent implements IEvent{
	int arrivalTime;	
	Task theTask;
	ServiceProvider theProvider;
	
	public TaskExpireEvent(int time, Task t, ServiceProvider provider) {
		arrivalTime = time;
		theTask = t;
		theProvider = provider;
	}

	public int getEventTime() {
		return arrivalTime;
	}

	public IEvent executeEvent() {
		theProvider.RemoveTask(theTask);
		return null;

	}
}
