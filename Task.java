import java.util.Objects;

public abstract class Task implements Comparable<Task>{
	int submissionTime;
	
	Integer vertexLocation;
	Integer completeBy;
	int taskDuration;
	
	boolean isClaimed;
	boolean isCompleted;
	
	double specifiedRevenue;
	
	public Task() {
		
	}
	public Task(int submit, int location, int deadline, int duration, double revenue) {
		submissionTime = submit;
		vertexLocation  = location;
		

		completeBy = deadline;
		taskDuration = duration;
		specifiedRevenue = revenue;
	
		isCompleted = false;		
		isClaimed = false;
	}
	
	
	public abstract Task clone();
	
	public String toString() {
		return "("+vertexLocation+","+completeBy+","+specifiedRevenue+")";
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;
        
        Task user = (Task) o;
        return vertexLocation == user.vertexLocation 
          && (completeBy.equals(user.completeBy));
    }
	
	@Override
    public int hashCode() {
//		String uniqueID = vertexLocation.toString()+","+completeBy.toString();
//        return Objects.hashCode(uniqueID);
		return Objects.hash(vertexLocation,completeBy);
    }
	
	public int compareTo(Task t)
    {
        return this.completeBy.compareTo(t.completeBy);
    }

}
