package at.ac.tuwien.complang.carfactory.ui.jms.listener;

import at.ac.tuwien.complang.carfactory.application.jms.enums.QueueChangeType;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;

/**
 * Implemented by the GUI and used to notify the GUI of
 * new car parts which should be shown.
 *  
 * @author Sebastian Geiger
 */
public interface IQueueObserver {
	public void onQueueChange(ICarPart carpart, QueueChangeType changeType);
}
