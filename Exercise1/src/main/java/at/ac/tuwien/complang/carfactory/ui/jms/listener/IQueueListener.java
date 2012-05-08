package at.ac.tuwien.complang.carfactory.ui.jms.listener;

import at.ac.tuwien.complang.carfactory.domain.ICarPart;

public interface IQueueListener {
	public void onObjectWrittenInQueue(ICarPart carPart);
	public void setQueueObserver(IQueueObserver gui);
}
