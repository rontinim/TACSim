import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;

public class Scenario {
//	WETGraph theGraph;

	ServiceProvider theProvider;
	Participant[] allParticipants;
	Task[] allTasks;	
	
	// To manage the discrete event simulation
	Vector<IEvent> eventsList = new Vector<IEvent>();
	double currentTime;
	
	public Scenario(Participant[] allD, Task[] allR) {			
//		theGraph = g;
		// Make copies of each task and participant
		// Cloning is important here, to allow for multi-threading
		allParticipants = new Participant[allD.length];
		for(int  i=0; i<allD.length; i++)
			allParticipants[i] = allD[i].clone();			
		allTasks = new Task[allR.length];
		for(int  i=0; i<allR.length; i++)
			allTasks[i] = allR[i].clone();
		
		// Fill up the omega function
		Experiment.theGraph.FillUpOmega(allTasks);
		
		// Start up the service provider
		theProvider = new ServiceProvider();	
		
		//NOW, creating events for each driver's and task's arrival and departure
		
		// Go through drivers and add their arrival and departure to the event list
		for(Participant d: allParticipants) {
			addEventToList(new ParticipantArrivalEvent(d.departureTime, d, theProvider));
			addEventToList(new ParticipantDepartureEvent(d.maxArrivalTime, d));
		}
		
		// Go through rides and add their arrival to the event list
		for(Task t: allTasks) {
			addEventToList(new TaskArrivalEvent(t.submissionTime, t, theProvider));
			addEventToList(new TaskExpireEvent(t.completeBy+1, t, theProvider));
		}
	}
	
	
	void startSimulation() {
		//try {
			//PrintWriter writer = new PrintWriter(new FileWriter("C:/Users/Matteo Rontini/Desktop/" + "result.txt", true));
		while(eventsList.size() > 0) {
//			System.out.println(eventsList.size());
			//writer.println(theProvider.getSizeParticipant() + ";" + theProvider.getSizeTasl());
			IEvent currEvent = eventsList.remove(0);
			currentTime = currEvent.getEventTime();
			IEvent nextEvent = currEvent.executeEvent();
			if(nextEvent != null)
				addEventToList(nextEvent);
		}
		
	
		// After the simulation ends, record the results
		double numCompleted=0, numDrivers=0, detour=0, avgFare = 0, avgbenefit = 0;
		
//		for(Task t: allTasks) if(t.isCompleted) numCompleted++;
			
		for(Participant p: allParticipants) {
			p.CalculateDetour();
			if(p.tasksAssigned.size() > 0) {
				numCompleted+= p.tasksAssigned.size();
					numDrivers++;
					detour  += p.finalDetour;
					avgFare += p.totalFareCollected;
					avgbenefit += p.totalFareCollected / p.finalPathLength;
			}
		}
		
		detour /=numDrivers;
		avgbenefit /= numDrivers;
		avgFare /= numDrivers;		
		Experiment.simulationResults.add(new ResultTuple(numCompleted, numDrivers, detour, avgbenefit, avgFare));

        //writer.close();
	/* } catch (IOException e) {
		e.printStackTrace();
	}*/
	}
	
	
	
	void addEventToList(IEvent theEvent) {
		// The event will be added in the correct spot based on its time
		for(int i=0; i<eventsList.size(); i++) {
			if(eventsList.get(i).getEventTime() > theEvent.getEventTime()) {
				eventsList.insertElementAt(theEvent, i);
				return;
			}
		}
		eventsList.add(theEvent);
	}


	
}
