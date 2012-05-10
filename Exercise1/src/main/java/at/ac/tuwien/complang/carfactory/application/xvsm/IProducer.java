package at.ac.tuwien.complang.carfactory.application.xvsm;

import at.ac.tuwien.complang.carfactory.ui.xvsm.ISpaceListener;

public interface IProducer {
	void produce();
	/**
	 * The time to produce in seconds
	 * @return 
	 */
	int timeInSec();
}
