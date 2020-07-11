import java.util.concurrent.PriorityBlockingQueue;
import java.util.Comparator;
import java.util.ArrayList;

public class Main {
	public static void main(String[] args) {

		Comparator<Task> priorityComparator = Comparator.comparing(Task::getPriority);
		Clock clock = new Clock();

		Airport airport = new Airport(clock);

		PriorityBlockingQueue<Task> taskQueue = new PriorityBlockingQueue<Task>(10, priorityComparator);

		AirTrafficControl ATC = new AirTrafficControl(taskQueue, clock, airport);
		Runway runway1 = new Runway(taskQueue, "Runway 1", clock, ATC);
		Runway runway2 = new Runway(taskQueue, "Runway 2", clock, ATC);
		Runway runway3 = new Runway(taskQueue, "Runway 3", clock, ATC);

		Thread tATC = new Thread(ATC);
		Thread tRunway1 = new Thread(runway1);
		Thread tRunway2 = new Thread(runway2);
		Thread tRunway3 = new Thread(runway3);

		tATC.start();
		tRunway1.start();
		tRunway2.start();
		tRunway3.start();

		// ArrayList<String> aircraft = new ArrayList<String>();
		// aircraft.add("AA");
		// aircraft.add("BB");
		// aircraft.add("CC");
		// aircraft.add("DD");
		// for (String a : aircraft) {
		// System.out.println(a);
		// }
		// String b = aircraft.remove(0);
		// for (String a : aircraft) {
		// System.out.println(a);
		// }
		// System.out.println(b);
	}
}
