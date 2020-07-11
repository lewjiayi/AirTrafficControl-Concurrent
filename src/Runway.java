import java.util.concurrent.PriorityBlockingQueue;

public class Runway implements Runnable {

	private Aircraft aircraft;
	PriorityBlockingQueue<Task> taskQueue = null;
	private String name;
	private Clock clock;
	private AirTrafficControl ATC;
	private int taskDone = 0;

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
			task = taskQueue.poll();
			if (task != null) {
				aircraft = task.getTaskAircraft();
				if (task.getTaskName() == 'L') {
					Airport airport = task.getDestination();
					boolean permission = airport.permissionToLand(aircraft);

					if (!permission) {
						synchronized (ATC) {
							ATC.taskRequeue(task);
						}
						break;
					} else {
						aircraft.landingOnAirport(task.getDestination());
					}
				}
				synchronized (aircraft) {
					aircraft.setRunway(this);
					aircraft.notify();
				}
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
				synchronized (taskQueue) {
					try {
						taskQueue.wait(30000);
					} catch (InterruptedException e) {
					}
				}
				if (taskQueue.peek() == null) {
					AirTrafficControlIncoming ATCI = ATC.getATCI();
					synchronized (ATCI) {
						ATCI.notify();
					}
				}
			}
		}
	}
}