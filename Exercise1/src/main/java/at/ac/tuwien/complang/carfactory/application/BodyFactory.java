package at.ac.tuwien.complang.carfactory.application;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCoreException;

import at.ac.tuwien.complang.carfactory.domain.Body;

public class BodyFactory extends AbstractFactory implements IProducer {

	//Fields
	private long id;

	public BodyFactory(long id, Capi capi, ContainerReference cref) {
		super(capi,cref);
		this.id = id;
	}

	public void produce() {
		Body body = new Body();
		System.out.println("Produced a body with ID: " + body.getId());
		
		System.out.println("writing Body into space...");
		try {
			getCapi().write(getCref(), new Entry(body));
			System.out.println("Body written in space sucessfully");
			
		} catch (MzsCoreException e) {
			e.printStackTrace();
		}
	}

	public long getId() {
		return id;
	}
}
