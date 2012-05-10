package at.ac.tuwien.complang.carfactory.ui.jms.listener;

import at.ac.tuwien.complang.carfactory.domain.ICarPart;

public interface IQueueListener {
	void onObjectWrittenInQueue(ICarPart carPart);
	void setQueueObserver(IQueueObserver gui);
	void connectToQueues();
}
