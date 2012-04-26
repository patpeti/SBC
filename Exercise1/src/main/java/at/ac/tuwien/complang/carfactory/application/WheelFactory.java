package at.ac.tuwien.complang.carfactory.application;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;

public class WheelFactory implements IProducer {

	public WheelFactory(Capi capi, ContainerReference cref) {
		// TODO Auto-generated constructor stub
	}

	public void produce() {
		System.out.println("Produced a wheel.");
	}

}
