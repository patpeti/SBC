package at.ac.tuwien.complang.carfactory.ui.jms.listener;

import at.ac.tuwien.complang.carfactory.domain.ICarPart;
import at.ac.tuwien.complang.carfactory.domain.Task;
import at.ac.tuwien.complang.carfactory.ui.IFactoryData;

/**
 * This interface serves as a connector between the data flow
 * and its representation in the user interface. The user interface
 * must be registered with this interface to receive updates.
 * 
 * The implementation should listen for all relevant queues of the
 * application and then inform the user interface.
 * 
 * @author Sebastian Geiger
 */
public interface IQueueListener {
	void setQueueObserver(IFactoryData gui);
	void connectToQueues();
	void disconnect();
	/**
	 * For direct in process notifications to the user interface
	 */
	void onObjectWrittenInQueue(ICarPart carPart);
	/**
	 * For direct in process notifications to the user interface
	 */
	void onTaskWrittenInQueue(Task task);
}
