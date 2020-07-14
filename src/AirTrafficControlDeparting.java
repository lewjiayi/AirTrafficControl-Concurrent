import java.util.concurrent.PriorityBlockingQueue;
import java.util.Random;
import java.util.LinkedList;
import java.util.Queue;;

public class AirTrafficControlDeparting implements Runnable {
	private AirTrafficControl ATC;
	private Random r = new Random();
	private Clock clock;
	private Airport airport;
	private Queue<Aircraft> requestList = new LinkedList<Aircraft>();
	PriorityBlockingQueue<Task> taskQueue;

	public AirTrafficControlDeparting(PriorityBlockingQueue<Task> taskQueue, AirTrafficControl ATC, Clock clock,
			Airport airport) {
		this.taskQueue = taskQueue;
		this.ATC = ATC;
		this.clock = clock;
		this.airport = airport;
	}

	public void departRequest(Aircraft aircraft) {
		requestList.add(aircraft);
	}

	// Check number of depart task in queue
	private int checkDepartQueueCount() {
		int count = 0;
		for (Task t : taskQueue) {
			if (t.getTaskName() == 'D') {
				count++;
			}
		}
		return count;
	}

	// Create task based of the request list
	private Task newDepartingTask() {
		Aircraft aircraft = requestList.poll();
		// Connect the aircraft with ATC to proceed further actions
		aircraft.connectATC(ATC);
		// 1% chance an aircraft has a high priority to use the runway first
		int priority = r.nextInt(100);
		if (priority == 0) {
			return new Task(aircraft, 'D', 'H');
		} else {
			return new Task(aircraft, 'D', 'N');
		}
	}

	public void run() {
		Task task;
		synchronized (this) {
			while (true) {
				try {
					// Wait for 20 to 30 seconds timeout, to prevent starvation
					// When the thread wake up after timeout it will create a random aircraft
					// This is also simulating real life airport where aircraft will be added to
					// airport for various other reasons than landing
					this.wait((r.nextInt(11) + 20) * 1000);
				} catch (InterruptedException e) {
				}
				if (requestList.peek() != null) {
					// Limit depart tasks in queue to 5,which is half of the desired task queue size
					if (checkDepartQueueCount() < 5) {
						task = newDepartingTask();
						taskQueue.offer(task);
						// Update and notify aircraft new status
						Aircraft aircraft = task.getTaskAircraft();
						aircraft.setStatus("Depart Queue");
						synchronized (aircraft) {
							aircraft.notify();
						}
						System.out.println(clock.getTime() + " || ATC              >>>>>  Flight "
								+ task.getTaskAircraft().getFlightNumber() + " is waiting in queue to depart.");
					}
				} else {
					if (checkDepartQueueCount() < 5) {
						// Force create a new aircraft and start the thread
						Aircraft temp = new Aircraft("Boarding", clock, airport);
						temp.connectATC(ATC);
						airport.addAircraft(temp);
						Thread t = new Thread(temp);
						t.start();
					}
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