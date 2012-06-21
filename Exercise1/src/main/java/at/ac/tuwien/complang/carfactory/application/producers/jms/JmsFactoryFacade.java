package at.ac.tuwien.complang.carfactory.application.producers.jms;

import java.util.Hashtable;
import java.util.Map;

import at.ac.tuwien.complang.carfactory.application.enums.ProducerType;
import at.ac.tuwien.complang.carfactory.application.producers.IFacade;
import at.ac.tuwien.complang.carfactory.application.producers.IFactory;
import at.ac.tuwien.complang.carfactory.ui.jms.listener.IQueueListener;

public class JmsFactoryFacade implements IFacade {

	//Static Fields
	private static final String INITIALIZATION_ERROR = "JMS FactoryFacade has not been initialized " +
			"with the proper queue listener.\n" +
			"You must call the initialization methods " +
			"and pass queue listener reference.";
	private static IFacade facade;

	//Fields
	private Map<ProducerType, IFactory> factories;
	private long next_id = 0;
	private IQueueListener listener;
	
	private JmsFactoryFacade(IQueueListener listener) {
		this.listener = listener;
		factories = new Hashtable<ProducerType, IFactory>();
	}
	
	public static IFacade getInstance(IQueueListener listener) {
		if(JmsFactoryFacade.facade == null) {
			synchronized (JmsFactoryFacade.class) {
				if(JmsFactoryFacade.facade == null) {
					JmsFactoryFacade.facade = new JmsFactoryFacade(listener);
				}
			}
		}
		return JmsFactoryFacade.facade;
	}
	
	@Override
	public IFactory getInstance(ProducerType type) {
		if(listener == null) {
			throw new RuntimeException(INITIALIZATION_ERROR);
		}
		if(factories.get(type) == null) {
			synchronized(factories) {
				if(factories.get(type) == null) {
					next_id++;
					IFactory factory;
					switch(type) {
						case BODY: factory = new JmsBodyFactory(next_id, listener);	break;
						case WHEEL: factory = new JmsWheelFactory(next_id, listener); break;
						case MOTOR: factory = new JmsMotorFactory(next_id, listener); break;
						default: throw new IllegalArgumentException("Specificed ProducerType is not implemented");
					}
					factories.put(type, factory);
				}
			}
		}
		return factories.get(type);
	}
}
