package at.ac.tuwien.complang.carfactory.alternative.factory;

import at.ac.tuwien.complang.carfactory.alternative.altGui.listener.IQueueListener;
import at.ac.tuwien.complang.carfactory.application.IProducer;
import at.ac.tuwien.complang.carfactory.domain.Body;

public class AltBodyFactory extends AltAbstractFactory implements IProducer {

	//Fields
	private long id; //The ID of this producer

	public AltBodyFactory(long id, IQueueListener listener) {
		super();
		this.id = id;
		setListener(listener);
	}

	

	public void produce() {
		Body body = new Body(id);
		System.out.println("Produced a body with ID: " + body.getId());
		
		System.out.println("writing Body into jms...");
		
		
	}

	public long getId() {
		return id;
	}
}
