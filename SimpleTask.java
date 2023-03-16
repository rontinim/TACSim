
public class SimpleTask extends Task{

	public SimpleTask(int submit, int location, int deadline, int duration, double revenue) {
		super(submit, location, deadline, duration, revenue);
	}
	
	@Override
	public Task clone() {
		return new SimpleTask(submissionTime, vertexLocation, completeBy, taskDuration, specifiedRevenue); 		
	}

}
