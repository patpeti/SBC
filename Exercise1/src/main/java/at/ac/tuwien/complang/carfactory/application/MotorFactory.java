package at.ac.tuwien.complang.carfactory.application;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;

import at.ac.tuwien.complang.carfactory.domain.Motor;

public class MotorFactory implements IProducer {

	public MotorFactory(Capi capi, ContainerReference cref) {
		// TODO Auto-generated constructor stub
	}

	public void produce() {
		Motor motor = new Motor();
		System.out.println("Produced a motor with id: " + motor.getId());
	}

}
