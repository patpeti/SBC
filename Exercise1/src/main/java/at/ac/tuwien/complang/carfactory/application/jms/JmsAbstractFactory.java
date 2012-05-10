package at.ac.tuwien.complang.carfactory.application.jms;

import at.ac.tuwien.complang.carfactory.ui.jms.listener.IQueueListener;

public abstract class JmsAbstractFactory {

	private IQueueListener listener;
	public static long carPartId = 1;
	
	public JmsAbstractFactory() {
		super();
	}

	public IQueueListener getListener() {
		return listener;
	}

	public void setListener(IQueueListener listener) {
		this.listener = listener;
	}
	
	

}
