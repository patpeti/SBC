package at.ac.tuwien.complang.carfactory.application.jms.constants;

/**
 * All Topic consumers except the GUI must register a durable subscription.
 * The GUI must monitor all Topics.
 * 
 * @author Sebastian Geiger<br/>Peter Patonai
 * 
 */
public class QueueConstants {
	public static final String MOTORQUEUE = "MOTORQUEUE"; //Only accessed by Assembler
	public static final String WHEELQUEUE = "WHEELQUEUE"; //Only accessed by Assembler
	public static final String BODYQUEUE = "BODYQUEUE"; //Subscribers are: Assembler and Painter, access is exclusive
	public static final String TASKQUEUE = "TASKQUEUE"; //Queue in which all tasks are written
	public static final String PAINTEDBODYTOPIC = "PAINTEDBODYTOPIC"; //Subscribers are: GUI and Assembler
	public static final String CARTOPIC = "CARTOPIC"; //Subscribers are: GUI and Painter
	public static final String PAINTEDCARTOPIC = "PAINTEDCARTOPIC"; //Subscribers are: GUI and CompletenessTester
	public static final String FINISHEDCARQUEUE = "FINISHEDCARQUEUE"; //Only accessed by GUI

	//Tester Topics
	public static final String COMPLETENESS_TESTED_TOPIC = "COMPLETENESS_TESTED_TOPIC"; //Subscribers are GUI and DefectTester
	public static final String DEFECT_TESTED_TOPIC = "DEFECT_TESTED_TOPIC"; //Subscribers are GUI and Supervisor
}
