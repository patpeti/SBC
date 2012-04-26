package at.ac.tuwien.complang.carfactory.application;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;

import at.ac.tuwien.complang.carfactory.domain.Wheel;

public class WheelFactory implements IProducer {

	public WheelFactory(Capi capi, ContainerReference cref) {
		// TODO Auto-generated constructor stub
	}

	public void produce() {
		Wheel wheel = new Wheel();
		System.out.println("Produced a wheel with ID: " + wheel.getId());
	}

}
