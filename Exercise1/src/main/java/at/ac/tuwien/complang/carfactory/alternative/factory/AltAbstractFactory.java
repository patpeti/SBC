package at.ac.tuwien.complang.carfactory.alternative.factory;

import at.ac.tuwien.complang.carfactory.alternative.altGui.listener.IQueueListener;

public abstract class AltAbstractFactory{


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
