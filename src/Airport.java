import java.util.ArrayList;

public class Airport {
	private int airportSize = 10;
	private ArrayList<Aircraft> aircrafts = new ArrayList<Aircraft>();
	private Clock clock;

	public Airport(Clock clock) {
		this.clock = clock;
		for (int i = 0; i < 2; i++) {
			aircrafts.add(new Aircraft(4, this.clock));
		}
	}

	public synchronized boolean permissionToLand(Aircraft aircraft) {
		if (aircrafts.size() >= airportSize) {
			return false;
		} else {
			aircrafts.add(aircraft);
			return true;
		}
	}

	public void initialAircraft(Aircraft aircraft) {
		aircrafts.add(aircraft);
	}

	public synchronized void aicraftDeparted(Aircraft aircraft) {
		int index = 0;
		for (Aircraft a : aircrafts) {
			if (a.getFlightNumber() == aircraft.getFlightNumber()) {
				break;
			}
			index++;
		}
		aircrafts.remove(index);
	}

}