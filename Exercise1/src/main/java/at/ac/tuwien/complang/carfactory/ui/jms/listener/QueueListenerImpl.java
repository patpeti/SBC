package at.ac.tuwien.complang.carfactory.ui.jms.listener;

import at.ac.tuwien.complang.carfactory.application.jms.enums.QueueChangeType;
import at.ac.tuwien.complang.carfactory.domain.ICarPart;

public class QueueListenerImpl implements IQueueListener{
	private IQueueObserver gui;

	public void onObjectWrittenInQueue(ICarPart carPart) {
		gui.onQueueChange(carPart, QueueChangeType.WRITE);
	}

	public void setQueueObserver(IQueueObserver gui) {
		this.gui = gui;
	}

	
}
