package at.ac.tuwien.complang.carfactory.application.producers;

public interface IProducer {
	void produce();
	/**
	 * The time to produce in seconds
	 * @return The number of seconds it takes to produce the part 
	 */
	double timeInSec();
}
