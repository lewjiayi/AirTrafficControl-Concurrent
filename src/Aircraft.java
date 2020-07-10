import java.util.concurrent.PriorityBlockingQueue;
import java.util.Random;

// Status
// 1 ---Land Queue
// 2 ---Landing
// 3 ---Landed
// 4 ---Depart Queue
// 5 ---Departing
// 6 ---Departed

// Priority
// 1 --- High Priority
// 2 --- Normal Priority

public class Aircraft implements Runnable {

	private String flightNumber;
	private Random r = new Random();
	private String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private String status;
	private String[] aircraftModels = { "Airbus A330", "Airbus A380", "Boeing 747", "Boeing 777" };
	private String model;
	private Runway runway;
	private AirTrafficControl ATC;
	private Clock clock;

	public Aircraft(int status, AirTrafficControl ATC, Clock clock) {
		setStatus(status);
		flightNumber = generateName();
		model = aircraftModels[r.nextInt(aircraftModels.length)];
		this.ATC = ATC;
		this.clock = clock;
	}

	public String getFlightNumber() {
		return flightNumber;
	}

	public String getAircraftModel() {
		return model;
	}

	public String changeFlightNumber() {
		flightNumber = generateName(flightNumber);
		return flightNumber;
	}

	public void setStatus(int status) {
		switch (status) {
			case 1:
				this.status = "Land Queue";
				break;
			case 2:
				this.status = "Landing";
				break;
			case 3:
				this.status = "Landed";
				break;
			case 4:
				this.status = "Depart Queue";
				break;
			case 5:
				this.status = "Departing";
				break;
			case 6:
				this.status = "Departed";
				break;
		}
	}

	private String generateName() {
		String flightNumber;
		StringBuilder sb = new StringBuilder(2);
		for (int i = 0; i < 2; i++) {
			sb.append(upperCase.charAt(r.nextInt(upperCase.length())));
		}
		flightNumber = sb.toString() + " ";
		for (int i = 0; i < 4; i++) {
			flightNumber += r.nextInt(10);
		}
		return flightNumber;
	}

	private String generateName(String origName) {
		String flightNumber = origName.substring(0, 3);
		for (int i = 0; i <= 4; i++) {
			flightNumber += r.nextInt(10);
		}
		return flightNumber;
	}

	public void setRunway(Runway runway) {
		this.runway = runway;
	}

	private void landing() {
		System.out.println(clock.getTime() + " || Flight " + flightNumber + "   >>>>>  " + "Aircraft is landing on "
				+ runway.getName() + ".");
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
		}
		System.out.println(clock.getTime() + " || Flight " + flightNumber + "   >>>>>  "
				+ "Aircraft has successfully landed on " + runway.getName() + ".");
	}

	private void departing(boolean otherACQueuing) {
		System.out.println(clock.getTime() + " || Flight " + flightNumber + "   >>>>>  " + "Aircraft is departing on "
				+ runway.getName() + ".");
		try {
			if (otherACQueuing) {
				Thread.sleep(5000);
			} else {
				Thread.sleep(5000 + (r.nextInt(6) * 1000));
			}
		} catch (InterruptedException e) {
		}
		System.out.println(clock.getTime() + " || Flight " + flightNumber + "   >>>>>  "
				+ "Aircraft has successfully departed from " + runway.getName() + ".");
	}

	public void run() {
		synchronized (this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
			}
			switch (status) {
				case "Land Queue":
					landing();
					break;

				case "Depart Queue":
					departing(ATC.otherDepartQueuing());
					break;
			}
		}
		synchronized (runway) {
			runway.notify();
		}
		runway = null;
	}

}