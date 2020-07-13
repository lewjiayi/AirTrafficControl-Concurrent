import java.util.ArrayList;

public class Airport {
	private int airportSize = 10;
	private ArrayList<Aircraft> aircrafts = new ArrayList<Aircraft>();

	// Grant permission for aircraft to land based if airport has space
	public synchronized boolean permissionToLand(Aircraft aircraft) {
		if (aircrafts.size() >= airportSize) {
			return false;
		} else {
			aircrafts.add(aircraft);
			return true;
		}
	}

	// Used in initial constructor and starvation only
	public void addAircraft(Aircraft aircraft) {
		aircrafts.add(aircraft);
	}

	// Update airport when an aircraft left
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