import java.util.concurrent.PriorityBlockingQueue;
import java.util.Random;

public class AirTrafficControl implements Runnable {
	private Random r = new Random();
	PriorityBlockingQueue<Task> taskQueue = null;
	private Integer landingQueueCount = 0;
	private Integer departingQueueCount = 0;
	private Clock clock;

	public AirTrafficControl(PriorityBlockingQueue<Task> taskQueue, Clock clock) {
		this.taskQueue = taskQueue;
		this.clock = clock;
		for (int i = 0; i < 3; i++) {
			Task task = incomingAircraft();
			taskQueue.put(task);
			System.out.println(clock.getTime() + " || ATC              >>>>>  Flight "
					+ task.getTaskAircraft().getFlightNumber() + " is waiting in queue to land.");
		}
	}

	private Task incomingAircraft() {
		Aircraft aircraft = new Aircraft(1, this, clock);
		Thread t = new Thread(aircraft);
		t.start();
		int priority = r.nextInt(100);
		if (priority == 0) {
			return new Task(aircraft, 'L', 'H');
		} else {
			return new Task(aircraft, 'L', 'N');
		}
	}

	private Task outgoingAircraft() {
		Aircraft aircraft = new Aircraft(4, this, clock);
		Thread t = new Thread(aircraft);
		t.start();
		int priority = r.nextInt(100);
		if (priority == 0) {
			return new Task(aircraft, 'D', 'H');
		} else {
			return new Task(aircraft, 'D', 'N');
		}
	}

	public void aircraftLanding() {
		landingQueueCount--;
	}

	public void aircraftDeparting() {
		departingQueueCount--;
	}

	public boolean otherDepartQueuing() {
		for (Task t : taskQueue) {
			if (t.getTaskName() == 'D') {
				return true;
			}
		}
		return false;
	}

	public void run() {
		Task task;
		while (true) {
			landingQueueCount = 0;
			departingQueueCount = 0;
			for (Task t : taskQueue) {
				if (t.getTaskName() == 'L') {
					landingQueueCount++;
				} else {
					departingQueueCount++;
				}
			}

			if (landingQueueCount < 5) {
				task = incomingAircraft();
				taskQueue.offer(task);
				landingQueueCount++;
				System.out.println(clock.getTime() + " || ATC              >>>>>  Flight "
						+ task.getTaskAircraft().getFlightNumber() + " is waiting in queue to land.");
				try {
					Thread.sleep(1000 + (r.nextInt(10) * 100));
				} catch (InterruptedException e) {
				}
			}

			if (departingQueueCount < 5) {
				task = outgoingAircraft();
				taskQueue.offer(task);
				departingQueueCount++;
				System.out.println(clock.getTime() + " || ATC              >>>>>  Flight "
						+ task.getTaskAircraft().getFlightNumber() + " is waiting in queue to depart.");
				try {
					Thread.sleep(1000 + (r.nextInt(10) * 100));
				} catch (InterruptedException e) {
				}
			}
		}
	}

}