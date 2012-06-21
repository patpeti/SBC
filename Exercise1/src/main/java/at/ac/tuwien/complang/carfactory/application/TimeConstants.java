package at.ac.tuwien.complang.carfactory.application;

public class TimeConstants {

	//Static Fields
	public static final double BODY_TIME_IN_SEC = 0;
	public static final double MOTOR_TIME_IN_SEC = 0;
	public static final double WHEEL_TIME_IN_SEC = 0;
	public static final double BASE_DELAY = 0;
	/** Half the time is used to take a part and the other half is used to paint it.
	 * We use this to relax the update intervals of the UI, so that there is no
	 * flickering, which happens, when a part is taken and written back immediately. */
	public static final long TIME_TO_PAINT = 0L; //time in milliseconds
}
