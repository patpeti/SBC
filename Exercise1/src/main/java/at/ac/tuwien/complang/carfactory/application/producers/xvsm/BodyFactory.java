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
import at.ac.tuwien.complang.carfactory.domain.Body;

public class BodyFactory extends AbstractFactory {

	//Fields
	private long id; //The ID of this producer

	public BodyFactory(long id, Capi capi, ContainerReference cref) {
		super(capi,cref);
		this.id = id;
	}

	public void produce() {
		Body body = new Body(id);
		double random = Math.random();
		if(random < errorRate) {
			body.setDefect(true);
		}
		System.out.println("Produced a body with ID: " + body.getId());		
		List<CoordinationData> cordinator = new ArrayList<CoordinationData>();
		cordinator.add(LabelCoordinator.newCoordinationData(CarPartType.BODY.toString()));
		cordinator.add(KeyCoordinator.newCoordinationData(""+body.getId()));
		try {
			getCapi().write(getCref(), new Entry(body, cordinator));
			System.out.println("Body written in space sucessfully");
		} catch (MzsCoreException e) {
			e.printStackTrace();
		}
	}

	public long getId() {
		return id;
	}
	
	@Override
	public double timeInSec() {
		return TimeConstants.BODY_TIME_IN_SEC;
	}
	
	@Override
	public void finished() {
		setChanged();
		notifyObservers("BODY");
	}
}
