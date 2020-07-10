import java.util.concurrent.PriorityBlockingQueue;
import java.util.Comparator;

public class Main {
	public static void main(String[] args) {

		Comparator<Task> priorityComparator = Comparator.comparing(Task::getPriority);
		Clock clock = new Clock();
		PriorityBlockingQueue<Task> taskQueue = new PriorityBlockingQueue<Task>(10, priorityComparator);

		Runway runway1 = new Runway(taskQueue, "Runway 1", clock);
		Runway runway2 = new Runway(taskQueue, "Runway 2", clock);
		Runway runway3 = new Runway(taskQueue, "Runway 3", clock);

		AirTrafficControl airTrafficControl = new AirTrafficControl(taskQueue, clock);

		Thread tATC = new Thread(airTrafficControl);
		Thread tRunway1 = new Thread(runway1);
		Thread tRunway2 = new Thread(runway2);
		Thread tRunway3 = new Thread(runway3);

		tATC.start();
		tRunway1.start();
		tRunway2.start();
		tRunway3.start();

	}
}
