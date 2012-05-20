package at.ac.tuwien.complang.carfactory.ui.jms.listener;

import at.ac.tuwien.complang.carfactory.domain.ICarPart;
import at.ac.tuwien.complang.carfactory.ui.IFactoryData;

public interface IQueueListener {
	void onObjectWrittenInQueue(ICarPart carPart);
	void setQueueObserver(IFactoryData gui);
	void connectToQueues();
}
