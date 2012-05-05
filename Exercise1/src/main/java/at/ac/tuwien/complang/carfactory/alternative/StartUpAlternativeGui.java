package at.ac.tuwien.complang.carfactory.alternative;

import javax.jms.Connection;

import org.apache.activemq.ActiveMQConnectionFactory;

import at.ac.tuwien.complang.carfactory.alternative.altGui.AltProductionUI;
import at.ac.tuwien.complang.carfactory.alternative.altGui.listener.IQueueListener;
import at.ac.tuwien.complang.carfactory.alternative.altGui.listener.QueueListenerImpl;

public class StartUpAlternativeGui {
	

	
	public static void main(String[] args) {
		
		
		/**TODO:
		 * Create queue for wheel
		 * Create queue for motor
		 * Create topic for Body --> because it can go to two endpoints: Painter and Assembler (or painter just writes back the painted body on the top of a queue?)
		 * Create topic for Car? -- > similarly to endpoints possible Painter and Supervisor
		 * 	
		 * 
		 */
		
		
		
		
		//insatnciate globale Listener
		IQueueListener listener = new QueueListenerImpl();
		//1. Start the User interface
		AltProductionUI gui = new AltProductionUI(listener);
		listener.setQueueObserver(gui);
	}
}
