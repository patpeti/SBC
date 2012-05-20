package at.ac.tuwien.complang.carfactory.application.xvsm;

import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCoreException;

import at.ac.tuwien.complang.carfactory.application.enums.CarPartType;
import at.ac.tuwien.complang.carfactory.domain.Motor;
import at.ac.tuwien.complang.carfactory.ui.xvsm.ISpaceListener;

public class MotorFactory extends AbstractFactory implements IProducer {
	
	//Static Fields
	private static final int TIME_IN_SEC = 3;
	//Fields
	private long id; //The ID of this producer

	public MotorFactory(long id, Capi capi, ContainerReference cref) {
		super(capi,cref);
		this.id = id;
	}

	public void produce() {
		Motor motor = new Motor(id);
		System.out.println("Produced a motor with id: " + motor.getId());
		
		System.out.println("writing Motor into space...");
		List<CoordinationData> cordinator = new ArrayList<CoordinationData>();
		//cordinator.add(LabelCoordinator.newCoordinationData(CarPartType.MOTOR.toString()));
		cordinator.add(KeyCoordinator.newCoordinationData(""+motor.getId()));
		try {
			getCapi().write(getCref(), new Entry(motor,cordinator));
			System.out.println("Motor written in space sucessfully");
		} catch (MzsCoreException e) {
			e.printStackTrace();
		}
		
	}

	public long getId() {
		return id;
	}

	@Override
	public int timeInSec() {
		return TIME_IN_SEC;
	}

}
