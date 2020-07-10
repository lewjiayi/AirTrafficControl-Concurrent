public class Task {
	private Aircraft aircraft;
	private char task;
	private char priority;

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

	public char getTaskName() {
		return task;
	}

	public Aircraft getTaskAircraft() {
		return aircraft;
	}

	public char getPriority() {
		return priority;
	}

}