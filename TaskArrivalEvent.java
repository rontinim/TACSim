
public class TaskArrivalEvent implements IEvent{
	int arrivalTime;	
	Task theTask;
	ServiceProvider theProvider;
	
	public TaskArrivalEvent(int time, Task t, ServiceProvider provider) {
		arrivalTime = time;
		theTask = t;
		theProvider = provider;
	}

	public int getEventTime() {
		return arrivalTime;
	}

	public IEvent executeEvent() {
		theProvider.AddTask(theTask);
		return null;

	}
}
