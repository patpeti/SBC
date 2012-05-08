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
import at.ac.tuwien.complang.carfactory.domain.Body;
import at.ac.tuwien.complang.carfactory.ui.xvsm.ISpaceListener;

public class BodyFactory extends AbstractFactory implements IProducer {

	//Fields
	private long id; //The ID of this producer

	public BodyFactory(long id, Capi capi, ContainerReference cref, ISpaceListener listener) {
		super(capi,cref);
		this.id = id;
		setListener(listener);
	}

	

	public void produce() {
		Body body = new Body(id);
		System.out.println("Produced a body with ID: " + body.getId());
		
		System.out.println("writing Body into space...");
		
		List<CoordinationData> cordinator = new ArrayList<CoordinationData>();
		cordinator.add(LabelCoordinator.newCoordinationData(CarPartType.BODY.toString()));
		cordinator.add(KeyCoordinator.newCoordinationData(""+body.getId()));
		try {
			getCapi().write(getCref(), new Entry(body, cordinator));
			
					
			System.out.println("Body written in space sucessfully");
			//notify listener
			getListener().onObjectWrittenInSpace(body);
		} catch (MzsCoreException e) {
			e.printStackTrace();
		}
	}

	public long getId() {
		return id;
	}
}
