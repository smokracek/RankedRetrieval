
public class TimerUtil {
	
	long startTime;
	
	public TimerUtil() {
		this.startTime = System.currentTimeMillis();
	}
	
	private double getElapsedTime() {
		return (double) (System.currentTimeMillis() - this.startTime) / 1000.0;
	}
	
	public String getTime() {
		return String.valueOf(this.getElapsedTime());
	}
}
