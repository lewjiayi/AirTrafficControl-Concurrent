import java.util.concurrent.PriorityBlockingQueue;

public class AirTrafficControl {
	PriorityBlockingQueue<Task> taskQueue = null;
	private AirTrafficControlDeparting ATCD;
	private AirTrafficControlIncoming ATCI;
	private Thread tATCD;
	private Thread tATCI;

	public AirTrafficControl(PriorityBlockingQueue<Task> taskQueue, Clock clock, Airport airport) {
		this.taskQueue = taskQueue;

		// Initializing the sub ATC unit and their thread
		ATCD = new AirTrafficControlDeparting(taskQueue, this, clock, airport);
		ATCI = new AirTrafficControlIncoming(taskQueue, this, clock, airport);
		tATCD = new Thread(ATCD);
		tATCI = new Thread(ATCI);
		tATCD.start();
		tATCI.start();
	}

	// Requeue a task, used when the airport is full for more aircraft to land
	public synchronized void taskRequeue(Task task) {
		task.requeue();
		taskQueue.add(task);
	}

	public AirTrafficControlDeparting getATCD() {
		return this.ATCD;
	}

	public AirTrafficControlIncoming getATCI() {
		return this.ATCI;
	}

	// Check if any aircraft is queueing to depart
	public boolean otherDepartQueuing() {
		for (Task t : taskQueue) {
			if (t.getTaskName() == 'D') {
				return true;
			}
		}
		return false;
	}
}