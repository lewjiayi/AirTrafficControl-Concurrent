import java.util.concurrent.PriorityBlockingQueue;
import java.util.Random;
import java.util.LinkedList;
import java.util.Queue;;

public class AirTrafficControlDeparting implements Runnable {
	private AirTrafficControl ATC;
	private boolean isSendingTask = false;
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

	public boolean isSendingTask() {
		return isSendingTask;
	}

	private int checkDepartQueueCount() {
		int count = 0;
		for (Task t : taskQueue) {
			if (t.getTaskName() == 'D') {
				count++;
			}
		}
		return count;
	}

	private Task newDepartingTask() {
		Aircraft aircraft = requestList.poll();
		// Connect the aircraft with ATC to proceed further actions
		aircraft.connectATC(ATC);
		// Create and run the aircraft thread
		Thread t = new Thread(aircraft);
		t.start();
		aircraft.startThread();
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
				this.notifyAll();
				try {
					this.wait((r.nextInt(10) + 5) * 1000);
				} catch (InterruptedException e) {
				}
				if (requestList.peek() != null) {
					if (checkDepartQueueCount() < 5) {
						task = newDepartingTask();
						taskQueue.offer(task);
						Aircraft aircraft = task.getTaskAircraft();
						aircraft.setStatus("Depart Queue");
						synchronized (aircraft) {
							aircraft.notify();
						}
						System.out.println(clock.getTime() + " || ATC              >>>>>  Flight "
								+ task.getTaskAircraft().getFlightNumber() + " is waiting in queue to depart.");
						isSendingTask = false;
					}
				} else {
					if (checkDepartQueueCount() < 5) {
						// Force create a new aircraft
						Aircraft temp = new Aircraft("Boarding", clock, airport);
						temp.connectATC(ATC);
						airport.addAircraft(temp);
						Thread t = new Thread(temp);
						t.start();
						// During starvation, there airport will never have more than 3 aircrafts,
						// Hence, there is no need to check airport size
					}
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