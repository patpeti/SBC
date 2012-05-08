package at.ac.tuwien.complang.carfactory.ui.jms.listener;

import at.ac.tuwien.complang.carfactory.application.jms.enums.QueueChangeType;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;

public interface IQueueObserver {

	public void onQueueChange(ICarPart carpart, QueueChangeType changeType);

	
}
