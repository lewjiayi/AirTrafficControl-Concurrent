import java.util.concurrent.PriorityBlockingQueue;

public class Runway implements Runnable {

	private boolean isOccupied;
	private Aircraft aircraft;
	PriorityBlockingQueue<Task> taskQueue = null;
	private String name;
	private Clock clock;
	private int taskDone = 0;

	public Runway(PriorityBlockingQueue<Task> taskQueue, String name, Clock clock) {
		this.isOccupied = false;
		this.taskQueue = taskQueue;
		this.name = name;
		this.clock = clock;
	}

	public String getName() {
		return name;
	}

	public boolean getIsOccupied() {
		return isOccupied;
	}

	public void run() {
		Task task;
		while (true) {
			isOccupied = true;
			task = taskQueue.poll();
			if (task != null) {
				aircraft = task.getTaskAircraft();
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
					System.out.println(clock.getTime() + " || " + name + "         >>>>>  "
							+ "Runway is cleared for next task. " + taskDone + " task(s) done.");
				}
			}
			task = null;
			aircraft = null;
			isOccupied = false;
		}
	}
}