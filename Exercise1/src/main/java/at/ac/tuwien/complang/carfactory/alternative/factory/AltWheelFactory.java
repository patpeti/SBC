package at.ac.tuwien.complang.carfactory.alternative.factory;

import at.ac.tuwien.complang.carfactory.alternative.altGui.listener.IQueueListener;
import at.ac.tuwien.complang.carfactory.application.IProducer;
import at.ac.tuwien.complang.carfactory.domain.Wheel;

public class AltWheelFactory extends AltAbstractFactory implements IProducer  {

	//Fields
	private long id; //The ID of this producer

	public AltWheelFactory(long id, IQueueListener listener) {
		super();
		this.id = id;
		setListener(listener);
	}

	public void produce() {
		Wheel wheel = new Wheel(id);
		System.out.println("Produced a wheel with ID: " + wheel.getId());
		
		System.out.println("writing wheel into jms...");
		
	}

	public long getId() {
		return id;
	}
}
