package at.ac.tuwien.complang.carfactory.ui.jms;

import at.ac.tuwien.complang.carfactory.application.ITaskController;
import at.ac.tuwien.complang.carfactory.application.jms.JmsFactoryFacade;
import at.ac.tuwien.complang.carfactory.application.jms.JmsTaskController;
import at.ac.tuwien.complang.carfactory.ui.ProductionUI;
import at.ac.tuwien.complang.carfactory.ui.jms.listener.IQueueListener;
import at.ac.tuwien.complang.carfactory.ui.jms.listener.QueueListenerImpl;

public class StartUpJmsGui {
	
	public static void main(String[] args) {
		/**
		 * Workflow:
		 * Create queue for wheel [Done, JmsWheelFactory]
		 * Create queue for motor [Done, JmsMotorFactory]
		 * Create queue for Body [Done, JmsBodyFactory]
		 * Create topics for Car [Done, JmsAssembler]
		 * For all queue and topics available, see the application.jms.constants.QueueConstanst Class
		 */
		//instantiate global Listener
		final IQueueListener listener = new QueueListenerImpl();
		listener.connectToQueues();
		
		//1. Start the User interface
		ITaskController taskController = new JmsTaskController();
		ProductionUI gui = new ProductionUI(JmsFactoryFacade.getInstance(listener), taskController);
		listener.setQueueObserver(gui);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("Shutting down gracefully, please wait.");
				listener.disconnect();
			}
		});
	}
}
