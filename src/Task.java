public class Task {
	private Aircraft aircraft;
	private char task;
	private char priority;
	private Airport destination;
	private int requeueCount = 0;

	// Task
	// D --- Depart
	// L --- Landing

	// Priority
	// H --- High Priority
	// N --- Normal Priority

	public Task(Aircraft aircraft, char task, char priority) {
		this.aircraft = aircraft;
		this.task = task;
		this.priority = priority;
	}

	public Task(Aircraft aircraft, char task, char priority, Airport destination) {
		this.aircraft = aircraft;
		this.task = task;
		this.priority = priority;
		this.destination = destination;
	}

	public Airport getDestination() {
		return destination;
	}

	public char getTaskName() {
		return task;
	}

	public Aircraft getTaskAircraft() {
		return aircraft;
	}

	public char getPriority() {
		return priority;
	}

	// If the task is being requeue for 2 times, set it to high priority
	public void requeue() {
		requeueCount++;
		if (requeueCount >= 2) {
			priority = 'H';
		}
	}

}