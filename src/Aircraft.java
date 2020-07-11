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
	private Airport airport;

	public Aircraft(int status, Clock clock) {
		setStatus(status);
		flightNumber = generateName();
		model = aircraftModels[r.nextInt(aircraftModels.length)];
		this.clock = clock;
	}

	public Aircraft(int status, Clock clock, Airport airport) {
		setStatus(status);
		flightNumber = generateName();
		model = aircraftModels[r.nextInt(aircraftModels.length)];
		this.clock = clock;
		this.airport = airport;
	}

	public void landingOnAirport(Airport airport) {
		this.airport = airport;
	}

	public void connectATC(AirTrafficControl ATC) {
		this.ATC = ATC;
	}

	public String getFlightNumber() {
		return flightNumber;
	}

	public String getAircraftModel() {
		return model;
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
				this.status = "In Airport";
				break;
			case 5:
				this.status = "Depart Queue";
				break;
			case 6:
				this.status = "Departing";
				break;
			case 7:
				this.status = "Departed";
				break;
			case 8:
				this.status = "Others";
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

	private void changeFlightNumber() {
		flightNumber = flightNumber.substring(0, 3);
		for (int i = 0; i < 4; i++) {
			flightNumber += r.nextInt(10);
		}
	}

	public void setRunway(Runway runway) {
		this.runway = runway;
	}

	public void run() {
		loop: while (status != "Others") {
			switch (status) {
				case "Land Queue":
					synchronized (this) {
						try {
							this.wait();
						} catch (InterruptedException e) {
						}
					}
					status = "Landing";
					break;

				case "Landing":
					System.out.println(clock.getTime() + " || Flight " + flightNumber + "   >>>>>  " + "Aircraft is landing on "
							+ runway.getName() + ".");
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
					}
					status = "Landed";
					break;

				case "Landed":
					System.out.println(clock.getTime() + " || Flight " + flightNumber + "   >>>>>  "
							+ "Aircraft has successfully landed on " + runway.getName() + ".");
					synchronized (runway) {
						runway.notify();
					}
					runway = null;
					status = "In Airport";
					break;

				case "In Airport":
					int maintainance = r.nextInt(100);
					if (maintainance == 1) {
						status = "Others";
					} else {
						String oldFlightNumber = flightNumber;
						changeFlightNumber();
						System.out.println(clock.getTime() + " || Flight " + oldFlightNumber + "   >>>>>  "
								+ "Aircraft is for next flight. Flight number for new route  >>>  " + flightNumber);
						try {
							Thread.sleep((r.nextInt(3) + 1) * 1000);
						} catch (InterruptedException e) {
						}
						System.out.println(clock.getTime() + " || Flight " + flightNumber + "   >>>>>  " + "Aircraft is boarding.");
						try {
							Thread.sleep(5000 + (r.nextInt(1) * 1000));
						} catch (InterruptedException e) {
						}
						AirTrafficControlDeparting ATCD = ATC.getATCD();
						synchronized (ATCD) {
							if (ATCD.isSendingTask()) {
								try {
									ATCD.wait();
								} catch (InterruptedException e) {
								}
							}
							ATCD.departingAircraft(this);
							ATCD.notify();
						}
						status = "Depart Queue";
					}
					break;

				case "Depart Queue":
					synchronized (this) {
						try {
							this.wait();
						} catch (InterruptedException e) {
						}
					}
					status = "Departing";
					break;

				case "Departing":
					System.out.println(clock.getTime() + " || Flight " + flightNumber + "   >>>>>  " + "Aircraft is departing on "
							+ runway.getName() + ".");
					try {
						if (ATC.otherDepartQueuing()) {
							Thread.sleep(5000);
						} else {
							Thread.sleep(5000 + (r.nextInt(6) * 1000));
						}
					} catch (InterruptedException e) {
					}
					status = "Departed";
					break;

				case "Departed":
					System.out.println(clock.getTime() + " || Flight " + flightNumber + "   >>>>>  "
							+ "Aircraft has successfully departed from " + runway.getName() + ".");
					synchronized (runway) {
						runway.notify();
					}
					airport.aicraftDeparted(this);
					airport = null;
					runway = null;
					break loop;
			}
		}
	}
}