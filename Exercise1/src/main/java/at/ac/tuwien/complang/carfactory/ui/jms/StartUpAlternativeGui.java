package at.ac.tuwien.complang.carfactory.ui.jms;

import javax.jms.Connection;

import org.apache.activemq.ActiveMQConnectionFactory;

import at.ac.tuwien.complang.carfactory.ui.jms.listener.IQueueListener;
import at.ac.tuwien.complang.carfactory.ui.jms.listener.QueueListenerImpl;

public class StartUpAlternativeGui {
	
	public static void main(String[] args) {
		/**TODO:
		 * Create queue for wheel
		 * Create queue for motor
		 * Create topic for Body
		 * Create topic for Car?
		 */
		//instantiate global Listener
		IQueueListener listener = new QueueListenerImpl();
		//1. Start the User interface
		ProductionUI gui = new ProductionUI(listener);
		listener.setQueueObserver(gui);
	}
}
