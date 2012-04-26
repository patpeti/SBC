package at.ac.tuwien.complang.carfactory.application;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;

import at.ac.tuwien.complang.carfactory.domain.Body;

public class BodyFactory implements IProducer {

	public BodyFactory(Capi capi, ContainerReference cref) {
		// TODO Auto-generated constructor stub
	}

	public void produce() {
		Body body = new Body();
		System.out.println("Produced a body with ID: " + body.getId());
	}

}
