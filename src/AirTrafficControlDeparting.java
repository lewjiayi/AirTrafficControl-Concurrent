import java.util.Random;

public class AirTrafficControlDeparting implements Runnable {
	private AirTrafficControl ATC;
	private Clock clock;
	private Random r = new Random();
	private Aircraft aircraft;
	private boolean isSendingTask = false;

	public AirTrafficControlDeparting(AirTrafficControl ATC, Clock clock, Airport airport) {
		this.ATC = ATC;
		this.clock = clock;
	}

	public void departingAircraft(Aircraft aircraft) {
		this.aircraft = aircraft;
		isSendingTask = true;
	}

	public boolean isSendingTask() {
		return isSendingTask;
	}

	public Aircraft getAircraft() {
		return aircraft;
	}

	public void run() {
		while (true) {
			synchronized (this) {
				try {
					this.wait();
				} catch (InterruptedException e) {
				}
			}

			synchronized (ATC) {
				if (ATC.isCreatingTask()) {
					try {
						ATC.wait();
					} catch (InterruptedException e) {
					}
				}
				ATC.setNewTask("Depart");
				ATC.notify();
				isSendingTask = false;
			}

			synchronized (this) {
				this.notifyAll();
			}

		}

	}

}