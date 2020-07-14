import java.util.concurrent.PriorityBlockingQueue;
import java.util.Random;

public class AirTrafficControlIncoming implements Runnable {
	private AirTrafficControl ATC;
	private Clock clock;
	private Random r = new Random();
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

	private Task newLandingTask() {
		Aircraft aircraft = new Aircraft("Land Queue", clock);
		// Connect the aircraft with ATC to proceed further actions
		aircraft.connectATC(ATC);
		// Create and run the aircraft thread
		Thread t = new Thread(aircraft);
		t.start();
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
					// Wait for 20 to 30 seconds timeout
					// When the thread wake up after timeout it will create a random aircraft
					// The thread can also be woken up by runway to prevent starvation
					this.wait((r.nextInt(11) + 20) * 1000);
				} catch (InterruptedException e) {
				}
				// Limit land tasks in queue to 5,which is half of the desired task queue size
				if (checkLandQueueCount() < 5) {
					task = newLandingTask();
					taskQueue.offer(task);
					System.out.println(clock.getTime() + " || ATC              >>>>>  Flight "
							+ task.getTaskAircraft().getFlightNumber() + " is waiting in queue to land.");
				}
				// After processes above, the queue size is one means the queue was empty
				// Notify runway that is waiting on task queue for new task
				if (taskQueue.size() == 1) {
					synchronized (taskQueue) {
						taskQueue.notify();
					}
				}
			}
		}
	}
}