import java.util.concurrent.PriorityBlockingQueue;
import java.util.Random;

public class AirTrafficControlIncoming implements Runnable {
	private AirTrafficControl ATC;
	private Clock clock;
	private Random r = new Random();
	private Aircraft aircraft;
	private Airport airport;
	PriorityBlockingQueue<Task> taskQueue;

	public AirTrafficControlIncoming(PriorityBlockingQueue<Task> taskQueue, AirTrafficControl ATC, Clock clock,
			Airport airport) {
		this.taskQueue = taskQueue;
		this.ATC = ATC;
		this.clock = clock;
		this.airport = airport;
	}

	private int checkLandQueueCount() {
		int count = 0;
		for (Task t : taskQueue) {
			if (t.getTaskName() == 'L') {
				count++;
			}
		}
		return count;
	}

	public Aircraft getAircraft() {
		return aircraft;
	}

	private Task newLandingTask() {
		Aircraft aircraft = new Aircraft("Land Queue", clock);
		// Connect the aircraft with ATC to proceed further actions
		aircraft.connectATC(ATC);
		// Create and run the aircraft thread
		Thread t = new Thread(aircraft);
		t.start();
		aircraft.startThread();
		// 1% chance an aircraft has a high priority to use the runway first
		int priority = r.nextInt(100);
		if (priority == 0) {
			return new Task(aircraft, 'L', 'H', airport);
		} else {
			return new Task(aircraft, 'L', 'N', airport);
		}
	}

	public void run() {
		Task task;
		synchronized (this) {
			while (true) {
				try {
					this.wait((r.nextInt(10) + 5) * 1000);
				} catch (InterruptedException e) {
				}
				if (checkLandQueueCount() < 5) {
					task = newLandingTask();
					taskQueue.offer(task);
					System.out.println(clock.getTime() + " || ATC              >>>>>  Flight "
							+ task.getTaskAircraft().getFlightNumber() + " is waiting in queue to land.");
				}
				if (taskQueue.size() == 1) {
					synchronized (taskQueue) {
						// Notify runway which are waiting for new task
						taskQueue.notify();
					}
				}
			}
		}
	}
}