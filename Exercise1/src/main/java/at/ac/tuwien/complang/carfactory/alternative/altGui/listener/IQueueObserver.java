package at.ac.tuwien.complang.carfactory.alternative.altGui.listener;

import at.ac.tuwien.complang.carfactory.alternative.enums.QueueChangeType;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;

public interface IQueueObserver {

	public void onQueueChange(ICarPart carpart, QueueChangeType changeType);

	
}
