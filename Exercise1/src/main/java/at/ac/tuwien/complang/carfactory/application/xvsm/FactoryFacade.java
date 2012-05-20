package at.ac.tuwien.complang.carfactory.application.xvsm;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;

import at.ac.tuwien.complang.carfactory.application.IFacade;
import at.ac.tuwien.complang.carfactory.application.IFactory;
import at.ac.tuwien.complang.carfactory.application.enums.ProducerType;

public class FactoryFacade implements IFacade {

	//Static Fields
	private static final String INITIALIZATION_ERROR = "XVSM FactoryFacade has not been initialized " +
			"with the proper space reference.\n" +
			"You must call the initialization methods " +
			"and pass the Capi object and the container reference.";
	private List<ContainerReference> crefs;
	private Capi capi;
	private static Map<ProducerType, IFactory> factories;
	private static long next_id = 0;
	
	static {
		factories = new Hashtable<ProducerType, IFactory>();
	}
	
	public FactoryFacade(Capi capi, List<ContainerReference> crefs) {
		this.capi = capi;
		this.crefs = crefs;
	}
	
	@Override
	public IFactory getInstance(ProducerType type) {
		if(capi == null ||crefs == null) {
			throw new RuntimeException(INITIALIZATION_ERROR);
		}
		if(factories.get(type) == null) {
			synchronized(FactoryFacade.class) {
				if(factories.get(type) == null) {
					next_id++;
					IFactory producer;
					switch(type) {
						case BODY: producer = new BodyFactory(next_id, capi, crefs.get(0));	break;
						case WHEEL: producer = new WheelFactory(next_id, capi, crefs.get(3)); break;
						case MOTOR: producer = new MotorFactory(next_id, capi, crefs.get(2)); break;
						default: throw new IllegalArgumentException("Specificed ProducerType is not implemented");
					}
					FactoryFacade.factories.put(type, producer);
				}
			}
		}
		return factories.get(type);
	}
}
