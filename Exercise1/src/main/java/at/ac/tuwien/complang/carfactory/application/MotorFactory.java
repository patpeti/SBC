package at.ac.tuwien.complang.carfactory.application;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCoreException;

import at.ac.tuwien.complang.carfactory.domain.Motor;

public class MotorFactory extends AbstractFactory implements IProducer {


	public MotorFactory(Capi capi, ContainerReference cref) {
		super(capi,cref);
	}

	public void produce() {
		Motor motor = new Motor();
		System.out.println("Produced a motor with id: " + motor.getId());
		
		System.out.println("writing Motor into space...");
		try {
			getCapi().write(getCref(), new Entry(motor));
			System.out.println("Motor written in space sucessfully");
			
		} catch (MzsCoreException e) {
			e.printStackTrace();
		}
		
	}

}
