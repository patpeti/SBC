package at.ac.tuwien.complang.carfactory.application.producers.xvsm;

import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCoreException;

import at.ac.tuwien.complang.carfactory.application.TimeConstants;
import at.ac.tuwien.complang.carfactory.application.enums.CarPartType;
import at.ac.tuwien.complang.carfactory.domain.Wheel;

public class WheelFactory extends AbstractFactory {

	//Fields
	private long id; //The ID of this producer

	public WheelFactory(long id, Capi capi, ContainerReference cref) {
		super(capi,cref);
		this.id = id;
	}

	public void produce() {
		Wheel wheel = new Wheel(id);
		double random = Math.random();
		if(random < errorRate) {
			wheel.setDefect(true);
		}
		System.out.println("Produced a wheel with ID: " + wheel.getId());
		
		System.out.println("writing wheel into space...");
		List<CoordinationData> cordinator = new ArrayList<CoordinationData>();
		cordinator.add(LabelCoordinator.newCoordinationData(CarPartType.WHEEL.toString()));
		cordinator.add(KeyCoordinator.newCoordinationData(""+wheel.getId()));
		try {
			getCapi().write(getCref(), new Entry(wheel,cordinator));
			System.out.println("Wheel written in space sucessfully");
		} catch (MzsCoreException e) {
			e.printStackTrace();
		}
	}

	public long getId() {
		return id;
	}
	
	@Override
	public double timeInSec() {
		return TimeConstants.WHEEL_TIME_IN_SEC;
	}
	
	@Override
	public void finished() {
		setChanged();
		notifyObservers("WHEEL");
	}
}
