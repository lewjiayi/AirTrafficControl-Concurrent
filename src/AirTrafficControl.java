import java.util.concurrent.PriorityBlockingQueue;
import java.util.Random;

public class AirTrafficControl implements Runnable {
	private Random r = new Random();
	PriorityBlockingQueue<Task> taskQueue = null;
	private Integer landingQueueCount = 0;
	private Integer departingQueueCount = 0;
	private Clock clock;
	private Airport airport;
	private String newTask;
	private boolean isCreatingTask = false;
	private AirTrafficControlDeparting ATCD;
	private AirTrafficControlIncoming ATCI;
	private Thread tATCD;
	private Thread tATCI;

	public AirTrafficControl(PriorityBlockingQueue<Task> taskQueue, Clock clock, Airport airport) {
		this.taskQueue = taskQueue;
		this.clock = clock;
		this.airport = airport;
		for (int i = 0; i < 2; i++) {
			Aircraft aircraft = new Aircraft(1, clock);
			aircraft.connectATC(this);
			Thread t = new Thread(aircraft);
			t.start();
			Task task = new Task(aircraft, 'L', 'N', this.airport);
			taskQueue.put(task);
			System.out.println(clock.getTime() + " || ATC              >>>>>  Flight "
					+ task.getTaskAircraft().getFlightNumber() + " is waiting in queue to land.");
		}
		for (int i = 0; i < 3; i++) {
			Aircraft aircraft = new Aircraft(5, clock, this.airport);
			aircraft.connectATC(this);
			airport.initialAircraft(aircraft);
			Thread t = new Thread(aircraft);
			t.start();
			Task task = new Task(aircraft, 'D', 'N');
			taskQueue.put(task);
			System.out.println(clock.getTime() + " || ATC              >>>>>  Flight "
					+ task.getTaskAircraft().getFlightNumber() + " is waiting in queue to depart.");
		}
		ATCD = new AirTrafficControlDeparting(this, clock, this.airport);
		ATCI = new AirTrafficControlIncoming(this, clock);
		tATCD = new Thread(ATCD);
		tATCI = new Thread(ATCI);
	}

	private Task newLandingTask() {
		Aircraft aircraft = ATCI.getAircraft();
		aircraft.connectATC(this);
		Thread t = new Thread(aircraft);
		t.start();
		int priority = r.nextInt(100);
		if (priority == 0) {
			return new Task(aircraft, 'L', 'H', this.airport);
		} else {
			return new Task(aircraft, 'L', 'N', this.airport);
		}
	}

	private Task newDepartingTask() {
		Aircraft aircraft = ATCD.getAircraft();
		aircraft.connectATC(this);
		Thread t = new Thread(aircraft);
		t.start();
		int priority = r.nextInt(100);
		if (priority == 0) {
			return new Task(aircraft, 'D', 'H');
		} else {
			return new Task(aircraft, 'D', 'N');
		}
	}

	public void setNewTask(String newTask) {
		isCreatingTask = true;
		this.newTask = newTask;
	}

	public void taskRequeue(Task task) {
		task.requeue();
		taskQueue.add(task);
	}

	public AirTrafficControlDeparting getATCD() {
		return this.ATCD;
	}

	public boolean otherDepartQueuing() {
		for (Task t : taskQueue) {
			if (t.getTaskName() == 'D') {
				return true;
			}
		}
		return false;
	}

	public boolean isCreatingTask() {
		return isCreatingTask;
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

	private int checkDepartQueueCount() {
		int count = 0;
		for (Task t : taskQueue) {
			if (t.getTaskName() == 'D') {
				count++;
			}
		}
		return count;
	}

	public void run() {
		tATCD.start();
		tATCI.start();
		Task task;
		while (true) {
			synchronized (this) {
				try {
					this.wait();
				} catch (InterruptedException e) {
				}

				switch (newTask) {
					case "Land":
						if (checkLandQueueCount() < 5) {
							task = newLandingTask();
							taskQueue.offer(task);
							landingQueueCount++;
							System.out.println(clock.getTime() + " || ATC              >>>>>  Flight "
									+ task.getTaskAircraft().getFlightNumber() + " is waiting in queue to land.");
						}
						break;

					case "Depart":
						if (checkDepartQueueCount() < 5) {
							task = newDepartingTask();
							taskQueue.offer(task);
							departingQueueCount++;
							System.out.println(clock.getTime() + " || ATC              >>>>>  Flight "
									+ task.getTaskAircraft().getFlightNumber() + " is waiting in queue to depart.");
						}
						break;
				}
				isCreatingTask = false;
				this.notifyAll();
			}
		}
	}
}