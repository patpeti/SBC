package at.ac.tuwien.complang.carfactory.application;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCoreException;

import at.ac.tuwien.complang.carfactory.domain.Wheel;

public class WheelFactory extends AbstractFactory implements IProducer {

	//Fields
		private long id;

	public WheelFactory(long id, Capi capi, ContainerReference cref) {
		super(capi,cref);
		this.id = id;
	}

	public void produce() {
		Wheel wheel = new Wheel();
		System.out.println("Produced a wheel with ID: " + wheel.getId());
		
		System.out.println("writing wheel into space...");
		try {
			getCapi().write(getCref(), new Entry(wheel));
			System.out.println("wheel written in space sucessfully");
			
		} catch (MzsCoreException e) {
			e.printStackTrace();
		}
	}

	public long getId() {
		return id;
	}
}
