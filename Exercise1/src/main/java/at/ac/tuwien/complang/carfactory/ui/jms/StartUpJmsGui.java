package at.ac.tuwien.complang.carfactory.ui.jms;

import at.ac.tuwien.complang.carfactory.ui.jms.listener.IQueueListener;
import at.ac.tuwien.complang.carfactory.ui.jms.listener.QueueListenerImpl;

public class StartUpJmsGui {
	
	public static void main(String[] args) {
		/**TODO:
		 * Create queue for wheel [Done, JmsWheelFactory]
		 * Create queue for motor [Done, JmsMotorFactory]
		 * Create topic for Body [Done, JmsBodyFactory]
		 * Create topic for Car? [Done, JmsAssembler]
		 */
		//instantiate global Listener
		IQueueListener listener = new QueueListenerImpl();
		//1. Start the User interface
		ProductionUI gui = new ProductionUI(listener);
		listener.setQueueObserver(gui);
	}
}
