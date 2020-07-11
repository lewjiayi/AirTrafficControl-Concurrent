import java.util.Random;

public class AirTrafficControlIncoming implements Runnable {
	private AirTrafficControl ATC;
	private Clock clock;
	private Random r = new Random();
	private Aircraft aircraft;

	public AirTrafficControlIncoming(AirTrafficControl ATC, Clock clock) {
		this.ATC = ATC;
		this.clock = clock;
	}

	private void incomingAircraft() {
		aircraft = new Aircraft(1, clock);
	}

	public Aircraft getAircraft() {
		return aircraft;
	}

	public void run() {
		while (true) {
			synchronized (ATC) {
				if (ATC.isCreatingTask()) {
					try {
						ATC.wait();
					} catch (InterruptedException e) {
					}
				}
				incomingAircraft();
				ATC.setNewTask("Land");
				ATC.notify();
			}
			synchronized (this) {
				try {
					this.wait((r.nextInt(26) + 5) * 1000);
				} catch (InterruptedException e) {
				}
			}
		}
	}
}