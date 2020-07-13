import java.util.Random;
import java.util.concurrent.PriorityBlockingQueue;

public class Runway implements Runnable {

	private Aircraft aircraft;
	PriorityBlockingQueue<Task> taskQueue = null;
	private String name;
	private Clock clock;
	private AirTrafficControl ATC;
	private int taskDone = 0;
	private Random r = new Random();

	public Runway(PriorityBlockingQueue<Task> taskQueue, String name, Clock clock, AirTrafficControl ATC) {
		this.taskQueue = taskQueue;
		this.name = name;
		this.clock = clock;
		this.ATC = ATC;
	}

	public String getName() {
		return name;
	}

	public void run() {
		Task task;
		while (true) {
			// Getting a task from queue
			task = taskQueue.poll();
			if (task != null) {
				aircraft = task.getTaskAircraft();
				// if the task is landing. get permission to land from aiport
				if (task.getTaskName() == 'L') {
					Airport airport = task.getDestination();
					boolean permission = airport.permissionToLand(aircraft);

					// If airport is full, requeue the task (wait for other aircraft to leave)
					if (!permission) {
						synchronized (ATC) {
							ATC.taskRequeue(task);
						}
						break;
					} else {
						// System.out.println(name + " permission granted to land");
						aircraft.landingOnAirport(task.getDestination());
					}
				}
				// Notify aircraft to land on this runway
				synchronized (aircraft) {
					aircraft.setRunway(this);
					aircraft.notify();
				}
				// Wait for aircraft to finish task
				synchronized (this) {
					try {
						this.wait();
					} catch (InterruptedException e) {
					}
					taskDone++;
					System.out.println(clock.getTime() + " || " + name + "         >>>>>  " + "Runway is cleared for next task. "
							+ taskDone + " task(s) done.");
				}
				task = null;
				aircraft = null;
			} else {
				// If there is no task in queue, wait for 30 seconds timeout
				// When a task is added this thread will be notified
				synchronized (taskQueue) {
					try {
						taskQueue.wait(30000);
					} catch (InterruptedException e) {
					}
				}
				// If there is still no task in queue force either depart or landing to
				// instantly create a new task
				if (taskQueue.peek() == null) {
					int getTaskFrom = r.nextInt(2);
					if (getTaskFrom == 0) {
						AirTrafficControlIncoming ATCI = ATC.getATCI();
						synchronized (ATCI) {
							ATCI.notify();
						}
					} else {
						AirTrafficControlDeparting ATCD = ATC.getATCD();
						synchronized (ATCD) {
							ATCD.notify();
						}
					}
				}
			}
		}
	}
}