package at.ac.tuwien.complang.carfactory.application.producers.xvsm;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;

import at.ac.tuwien.complang.carfactory.application.enums.ProducerType;
import at.ac.tuwien.complang.carfactory.application.producers.IFacade;
import at.ac.tuwien.complang.carfactory.application.producers.IFactory;

public class FactoryFacade implements IFacade {

	//Static Fields
	private static final String INITIALIZATION_ERROR = "XVSM FactoryFacade has not been initialized " +
			"with the proper space reference.\n" +
			"You must call the initialization methods " +
			"and pass the Capi object and the container reference.";
	private static IFacade facade;
	private List<ContainerReference> crefs;
	private Capi capi;
	private Map<ProducerType, IFactory> factories;
	private long next_id = 0;
	
	
	private FactoryFacade(Capi capi, List<ContainerReference> crefs) {
		this.capi = capi;
		this.crefs = crefs;
		factories = new Hashtable<ProducerType, IFactory>();
	}
	
	public static IFacade getInstance(Capi capi, List<ContainerReference> crefs) {
		if(FactoryFacade.facade == null) {
			synchronized(FactoryFacade.class) {
				if(FactoryFacade.facade == null) {
					facade = new FactoryFacade(capi, crefs);
				}
			}
		}
		return facade;
	}
	
	@Override
	public IFactory getInstance(ProducerType type) {
		if(capi == null ||crefs == null) {
			throw new RuntimeException(INITIALIZATION_ERROR);
		}
		if(factories.get(type) == null) {
			synchronized(factories) {
				if(factories.get(type) == null) {
					next_id++;
					IFactory factory;
					switch(type) {
						case BODY: factory = new BodyFactory(next_id, capi, crefs.get(0));	break;
						case WHEEL: factory = new WheelFactory(next_id, capi, crefs.get(3)); break;
						case MOTOR: factory = new MotorFactory(next_id, capi, crefs.get(2)); break;
						default: throw new IllegalArgumentException("Specificed ProducerType is not implemented");
					}
					factories.put(type, factory);
				}
			}
		}
		return factories.get(type);
	}
}
