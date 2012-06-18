package at.ac.tuwien.complang.carfactory.application.jms.constants;

public class QueueConstants {
	public static final String MOTORQUEUE = "MOTORQUEUE"; //Only accessed by Assembler
	public static final String WHEELQUEUE = "WHEELQUEUE"; //Only accessed by Assembler
	public static final String BODYQUEUE = "BODYQUEUE"; //Subscribers are: Assembler and Painter, access is exclusive
	public static final String PAINTEDBODYTOPIC = "PAINTEDBODYTOPIC"; //Subscribers are: GUI and Assembler
	public static final String CARTOPIC = "CARTOPIC"; //Subscribers are: GUI and Painter
	public static final String PAINTEDCARTOPIC = "PAINTEDCARTOPIC"; //Subscribers are: GUI and Supervisor
	public static final String FINISHEDCARQUEUE = "FINISHEDCARQUEUE"; //Only accessed by GUI
	public static final String TASKQUEUE = "TASKQUEUE"; //Queue in which all tasks are written
}
