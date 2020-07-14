import java.util.Random;

// Status
// 1 --- Land Queue
// 2 --- Landing
// 3 --- Landed
// 4 --- Deboarding
// 5 --- Boarding
// 6 --- Depart Queue
// 7 --- Departing
// 8 --- Departed
// 9 --- Others (Left Airport)

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

	// Generate landing aircraft, aircraft not in airport yet
	public Aircraft(String status, Clock clock) {
		this.status = status;
		flightNumber = generateName();
		model = aircraftModels[r.nextInt(aircraftModels.length)];
		this.clock = clock;
	}

	// Generate departing aircraft, aircraft is in airport
	public Aircraft(String status, Clock clock, Airport airport) {
		this.status = status;
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

	private String generateName() {
		// Generate a flight number with 2 random alphabets and 4 numbers
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
		// Change flight number last 4 digits, the alphabets are retained
		flightNumber = flightNumber.substring(0, 3);
		for (int i = 0; i < 4; i++) {
			flightNumber += r.nextInt(10);
		}
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setRunway(Runway runway) {
		this.runway = runway;
	}

	public void run() {
		loop: while (status != "Others") {
			switch (status) {
				case "Land Queue":
					synchronized (this) {
						// Waiitng in queue for runway to land
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
						// Landing on runway
						Thread.sleep(10000);
					} catch (InterruptedException e) {
					}
					status = "Landed";
					break;

				case "Landed":
					System.out.println(clock.getTime() + " || Flight " + flightNumber + "   >>>>>  "
							+ "Aircraft has successfully landed on " + runway.getName() + ".");
					// Aircraft left runway, notify runway for next task
					synchronized (runway) {
						runway.notify();
					}
					// Remove runway instance from aircraft
					runway = null;
					status = "Deboarding";
					break;

				case "Deboarding":
					try {
						// Moving to deboard
						Thread.sleep((r.nextInt(5) + 3) * 1000);
					} catch (InterruptedException e) {
					}
					System.out.println(clock.getTime() + " || Flight " + flightNumber + "   >>>>>  " + "Aircraft is deboarding.");
					try {
						// Deboarding passenger & System check for next flight
						Thread.sleep((r.nextInt(10) + 5) * 1000);
					} catch (InterruptedException e) {
					}
					int maintainance = r.nextInt(100);
					if (maintainance == 1) {
						// 1% chance aircraft sent for maintanance
						status = "Others";
						airport.aicraftDeparted(this);
					} else {
						// Change flight number for next flight
						String oldFlightNumber = flightNumber;
						changeFlightNumber();
						System.out.println(clock.getTime() + " || Flight " + oldFlightNumber + "   >>>>>  "
								+ "Aircraft is ready for next flight. Flight number for new route  >>>  " + flightNumber);
						try {
							// Getting ready for boarding
							Thread.sleep((r.nextInt(3) + 3) * 1000);
						} catch (InterruptedException e) {
						}
						status = "Boarding";
					}
					break;

				case "Boarding":
					System.out.println(clock.getTime() + " || Flight " + flightNumber + "   >>>>>  " + "Aircraft is boarding.");
					try {
						// Passenger boarding Aircraft
						Thread.sleep((r.nextInt(6) + 5) * 1000);
					} catch (InterruptedException e) {
					}
					// Requesting ATC to depart
					AirTrafficControlDeparting ATCD = ATC.getATCD();
					synchronized (ATCD) {
						ATCD.departRequest(this);
						ATCD.notify();
					}
					while (status == "Boarding") {
						synchronized (this) {
							try {
								this.wait();
							} catch (InterruptedException e) {
							}
						}
					}
					break;

				case "Depart Queue":
					// Waiting in queue to use runway
					synchronized (this) {
						try {
							this.wait();
						} catch (InterruptedException e) {
						}
					}
					status = "Departing";
					break;

				case "Departing":
					// Departing on runway
					System.out.println(clock.getTime() + " || Flight " + flightNumber + "   >>>>>  " + "Aircraft is departing on "
							+ runway.getName() + ".");
					try {
						// Check if any other aircraft is waiting in queue to depart
						if (ATC.otherDepartQueuing()) {
							Thread.sleep((r.nextInt(6) + 5) * 1000);
						} else {
							Thread.sleep((r.nextInt(11) + 5) * 1000);
						}
					} catch (InterruptedException e) {
					}
					status = "Departed";
					break;

				case "Departed":
					System.out.println(clock.getTime() + " || Flight " + flightNumber + "   >>>>>  "
							+ "Aircraft has successfully departed from " + runway.getName() + ".");
					// Aircraft left runway, notify runway for next task
					synchronized (runway) {
						runway.notify();
					}
					// Update airport aircraft had left
					airport.aicraftDeparted(this);
					break loop;
			}
		}
	}
}