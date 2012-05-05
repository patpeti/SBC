package at.ac.tuwien.complang.carfactory.alternative.factory;

import at.ac.tuwien.complang.carfactory.alternative.altGui.listener.IQueueListener;
import at.ac.tuwien.complang.carfactory.application.IProducer;
import at.ac.tuwien.complang.carfactory.domain.Motor;

public class AltMotorFactory extends AltAbstractFactory  implements IProducer {
	
	//Fields
	private long id; //The ID of this producer

	public AltMotorFactory(long id,  IQueueListener listener) {
		super();
		this.id = id;
		setListener(listener);
	}

	public void produce() {
		Motor motor = new Motor(id);
		System.out.println("Produced a motor with id: " + motor.getId());
		
		System.out.println("writing Motor into jms...");
		
		
	}

	public long getId() {
		return id;
	}

}
