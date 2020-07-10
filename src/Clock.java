import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Clock {
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MMM dd - HH:mm:ss");

	public String getTime() {
		LocalDateTime dateTime = LocalDateTime.now();
		return dateTime.format(formatter);
	}
}