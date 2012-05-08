package at.ac.tuwien.complang.carfactory.application.jms;

import at.ac.tuwien.complang.carfactory.ui.jms.listener.IQueueListener;

public abstract class AltAbstractFactory {

	private IQueueListener listener;
	public static long carPartId = 1;
	
	public AltAbstractFactory() {
		super();
	}

	public IQueueListener getListener() {
		return listener;
	}

	public void setListener(IQueueListener listener) {
		this.listener = listener;
	}
	
	

}
